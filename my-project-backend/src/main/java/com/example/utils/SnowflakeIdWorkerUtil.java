package com.example.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 雪花算法ID生成器
 * 64位ID = 1位符号位 + 41位时间戳 + 5位数据中心ID + 5位机器ID + 12位序列号
 * 
 * 特性：
 * 1. 分布式唯一ID
 * 2. 趋势递增（时间戳部分）
 * 3. 高性能（本地生成，无网络开销）
 * 4. 可反解析（可以解析出生成时间、数据中心、机器等信息）
 */
@Slf4j
@Component
public class SnowflakeIdWorkerUtil {
    
    // ============================== 基础配置 ==============================
    
    /** 开始时间戳 (2025-01-01 00:00:00) */
    private final long twepoch = 1735660800000L;
    
    /** 机器ID所占的位数 */
    private final long workerIdBits = 5L;
    
    /** 数据中心ID所占的位数 */
    private final long dataCenterIdBits = 5L;
    
    /** 支持的最大机器ID，结果是31 (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数) */
    private final long maxWorkerId = ~(-1L << workerIdBits);
    
    /** 支持的最大数据中心ID，结果是31 */
    private final long maxDataCenterId = ~(-1L << dataCenterIdBits);
    
    /** 序列在ID中占的位数 */
    private final long sequenceBits = 12L;
    
    /** 机器ID向左移12位 */
    private final long workerIdShift = sequenceBits;
    
    /** 数据中心ID向左移17位(12+5) */
    private final long dataCenterIdShift = sequenceBits + workerIdBits;
    
    /** 时间戳向左移22位(5+5+12) */
    private final long timestampLeftShift = sequenceBits + workerIdBits + dataCenterIdBits;
    
    /** 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095) */
    private final long sequenceMask = ~(-1L << sequenceBits);
    
    // ============================== 实例变量 ==============================
    
    /** 数据中心ID(0~31) */
    private long dataCenterId;
    
    /** 工作机器ID(0~31) */
    private long workerId;
    
    /** 毫秒内序列(0~4095) */
    private long sequence = 0L;
    
    /** 上次生成ID的时间戳 */
    private long lastTimestamp = -1L;
    
    /** 时钟回拨容忍毫秒数 */
    private static final long MAX_BACKWARD_MS = 10L;
    
    /** 时钟回拨警告阈值 */
    private static final long CLOCK_BACKWARD_WARNING_MS = 1000L;
    
    /** 是否启用机器ID自动生成 */
    @Value("${snowflake.worker.auto-generate:true}")
    private boolean autoGenerateWorkerId;
    
    /** 机器ID（手动配置时使用） */
    @Value("${snowflake.worker.id:0}")
    private long manualWorkerId;
    
    /** 数据中心ID（手动配置时使用） */
    @Value("${snowflake.data-center.id:0}")
    private long manualDataCenterId;
    
    // ============================== 构造方法 ==============================
    
    /**
     * 构造函数（自动生成workerId和dataCenterId）
     */
    public SnowflakeIdWorkerUtil() {
        if (autoGenerateWorkerId) {
            try {
                // 自动生成workerId和dataCenterId
                this.dataCenterId = getDataCenterId();
                this.workerId = getWorkerId(dataCenterId);
            } catch (Exception e) {
                log.warn("自动生成workerId失败，使用默认值", e);
                this.dataCenterId = 0L;
                this.workerId = 0L;
            }
        } else {
            // 使用手动配置的ID
            if (manualWorkerId > maxWorkerId || manualWorkerId < 0) {
                throw new IllegalArgumentException(
                    String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
            }
            if (manualDataCenterId > maxDataCenterId || manualDataCenterId < 0) {
                throw new IllegalArgumentException(
                    String.format("datacenter Id can't be greater than %d or less than 0", maxDataCenterId));
            }
            this.workerId = manualWorkerId;
            this.dataCenterId = manualDataCenterId;
        }
        
        log.info("Snowflake ID Generator initialized. DataCenterId: {}, WorkerId: {}, AutoGenerate: {}", 
                this.dataCenterId, this.workerId, autoGenerateWorkerId);
    }
    
    /**
     * 构造函数（手动指定workerId和dataCenterId）
     */
    public SnowflakeIdWorkerUtil(long workerId, long dataCenterId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(
                String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (dataCenterId > maxDataCenterId || dataCenterId < 0) {
            throw new IllegalArgumentException(
                String.format("datacenter Id can't be greater than %d or less than 0", maxDataCenterId));
        }
        this.workerId = workerId;
        this.dataCenterId = dataCenterId;
    }
    
    // ============================== 核心方法 ==============================
    
    /**
     * 生成下一个ID（线程安全）
     * @return SnowflakeId
     */
    public synchronized long nextId() {
        long timestamp = timeGen();
        
        // 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过，抛出异常或处理
        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;
            if (offset <= MAX_BACKWARD_MS) {
                // 容忍小的时钟回拨，等待时钟追上
                try {
                    wait(offset << 1);
                    timestamp = timeGen();
                    if (timestamp < lastTimestamp) {
                        throw new RuntimeException(
                            String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", offset));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Clock moved backwards and wait interrupted", e);
                }
            } else if (offset <= CLOCK_BACKWARD_WARNING_MS) {
                log.warn("Clock moved backwards. Waiting for {} milliseconds", offset);
                // 等待时钟恢复
                try {
                    wait(offset << 1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                timestamp = timeGen();
            } else {
                // 时钟回拨过大，抛出异常
                throw new RuntimeException(
                    String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", offset));
            }
        }
        
        // 如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            // 毫秒内序列溢出
            if (sequence == 0) {
                // 阻塞到下一个毫秒，获得新的时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 时间戳改变，毫秒内序列重置
            // 为了提高并发性能，可以在不同毫秒开始时随机初始化序列
            sequence = ThreadLocalRandom.current().nextLong(0, 10);
        }
        
        // 上次生成ID的时间截
        lastTimestamp = timestamp;
        
        // 移位并通过或运算拼到一起组成64位的ID
        return ((timestamp - twepoch) << timestampLeftShift)
                | (dataCenterId << dataCenterIdShift)
                | (workerId << workerIdShift)
                | sequence;
    }
    
    /**
     * 批量生成ID
     * @param count 生成数量
     * @return ID数组
     */
    public synchronized long[] nextIds(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be positive");
        }
        if (count > 10000) {
            throw new IllegalArgumentException("Count cannot exceed 10000");
        }
        
        long[] ids = new long[count];
        for (int i = 0; i < count; i++) {
            ids[i] = nextId();
        }
        return ids;
    }
    
    /**
     * 解析Snowflake ID
     * @param id Snowflake ID
     * @return 解析结果
     */
    public SnowflakeId parseId(long id) {
        long timestamp = (id >> timestampLeftShift) + twepoch;
        long dataCenterId = (id >> dataCenterIdShift) & ~(-1L << dataCenterIdBits);
        long workerId = (id >> workerIdShift) & ~(-1L << workerIdBits);
        long sequence = id & sequenceMask;
        
        return new SnowflakeId(id, timestamp, dataCenterId, workerId, sequence);
    }
    
    // ============================== 私有方法 ==============================
    
    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     * @param lastTimestamp 上次生成ID的时间截
     * @return 当前时间戳
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }
    
    /**
     * 返回当前时间（毫秒）
     * @return 当前时间(毫秒)
     */
    private long timeGen() {
        return System.currentTimeMillis();
    }
    
    /**
     * 获取机器ID（基于MAC地址和进程ID）
     */
    private long getWorkerId(long dataCenterId) {
        StringBuilder sb = new StringBuilder();
        sb.append(dataCenterId);
        
        try {
            // 获取本机MAC地址
            String macAddress = getMacAddress();
            if (macAddress != null) {
                // 取MAC地址最后4位字符的hashCode
                int macHash = Math.abs(macAddress.substring(macAddress.length() - 4).hashCode());
                sb.append(macHash);
            }
            
            // 获取JVM进程ID
            String processId = getProcessId();
            if (processId != null) {
                sb.append(processId);
            }
            
            // 生成hash并取模得到workerId
            int hash = Math.abs(sb.toString().hashCode());
            return hash % (maxWorkerId + 1);
            
        } catch (Exception e) {
            // 如果获取失败，使用随机数
            log.warn("获取机器信息失败，使用随机workerId", e);
            return ThreadLocalRandom.current().nextLong(maxWorkerId + 1);
        }
    }
    
    /**
     * 获取数据中心ID（基于IP地址）
     */
    private long getDataCenterId() {
        try {
            // 获取本机IP地址
            InetAddress ip = InetAddress.getLocalHost();
            byte[] ipBytes = ip.getAddress();
            
            // 使用IP地址的最后两位生成dataCenterId
            long id = ((ipBytes[ipBytes.length - 2] & 0xFF) << 8) 
                     | (ipBytes[ipBytes.length - 1] & 0xFF);
            
            return id % (maxDataCenterId + 1);
            
        } catch (Exception e) {
            log.warn("获取IP地址失败，使用默认dataCenterId", e);
            return 0L;
        }
    }
    
    /**
     * 获取MAC地址
     */
    private String getMacAddress() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            byte[] mac = network.getHardwareAddress();
            
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 获取JVM进程ID
     */
    private String getProcessId() {
        try {
            // 格式: pid@hostname
            String jvmName = ManagementFactory.getRuntimeMXBean().getName();
            return jvmName.split("@")[0];
        } catch (Exception e) {
            return null;
        }
    }
    
    // ============================== 静态工具方法 ==============================
    
    /**
     * 静态方法：生成ID（使用默认实例）
     */
    public static long generateId() {
        return getInstance().nextId();
    }
    
    /**
     * 静态方法：批量生成ID
     */
    public static long[] generateIds(int count) {
        return getInstance().nextIds(count);
    }
    
    /**
     * 静态方法：解析ID
     */
    public static SnowflakeId parse(long id) {
        return getInstance().parseId(id);
    }
    
    /**
     * 单例实例（延迟加载）
     */
    private static class InstanceHolder {
        private static final SnowflakeIdWorkerUtil instance = new SnowflakeIdWorkerUtil();
    }
    
    /**
     * 获取单例实例
     */
    public static SnowflakeIdWorkerUtil getInstance() {
        return InstanceHolder.instance;
    }
    
    // ============================== 内部类 ==============================
    
    /**
     * Snowflake ID 解析结果
     */
    public static class SnowflakeId {
        private final long id;
        private final long timestamp;
        private final long dataCenterId;
        private final long workerId;
        private final long sequence;
        private final String createTime;
        
        public SnowflakeId(long id, long timestamp, long dataCenterId, long workerId, long sequence) {
            this.id = id;
            this.timestamp = timestamp;
            this.dataCenterId = dataCenterId;
            this.workerId = workerId;
            this.sequence = sequence;
            this.createTime = formatTimestamp(timestamp);
        }
        
        private String formatTimestamp(long timestamp) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            return sdf.format(new java.util.Date(timestamp));
        }
        
        @Override
        public String toString() {
            return String.format(
                "SnowflakeId{id=%d, timestamp=%d, createTime='%s', dataCenterId=%d, workerId=%d, sequence=%d}",
                id, timestamp, createTime, dataCenterId, workerId, sequence);
        }
        
        // Getter方法
        public long getId() { return id; }
        public long getTimestamp() { return timestamp; }
        public long getDataCenterId() { return dataCenterId; }
        public long getWorkerId() { return workerId; }
        public long getSequence() { return sequence; }
        public String getCreateTime() { return createTime; }
    }
    
    // ============================== 测试方法 ==============================
    
    /**
     * 测试方法
     */
    public static void main(String[] args) {
        SnowflakeIdWorkerUtil idWorker = new SnowflakeIdWorkerUtil(1, 1);
        
        // 生成单个ID
        long id1 = idWorker.nextId();
        System.out.println("生成的ID: " + id1);
        
        // 解析ID
        SnowflakeId parsed = idWorker.parseId(id1);
        System.out.println("解析结果: " + parsed);
        
        // 批量生成
        long[] ids = idWorker.nextIds(5);
        System.out.println("批量生成的ID:");
        for (long id : ids) {
            System.out.println(id + " -> " + idWorker.parseId(id).getCreateTime());
        }
        
        // 性能测试
        long start = System.currentTimeMillis();
        int count = 100000;
        for (int i = 0; i < count; i++) {
            idWorker.nextId();
        }
        long end = System.currentTimeMillis();
        System.out.println(String.format("生成 %d 个ID耗时: %d ms, QPS: %.2f万/秒", 
            count, (end - start), (count / (end - start) / 10.0)));
    }
}