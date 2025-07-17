package com.example.config;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class AsyncThreadPool {

    // 线程池实例 (双重校验锁单例)
    private static volatile ThreadPoolExecutor threadPool;

    // 任务计数器
    private static final AtomicLong taskCount = new AtomicLong(0);

    // 私有化构造器
    private AsyncThreadPool() {}

    /**
     * 获取线程池实例
     */
    public static ThreadPoolExecutor getThreadPool() {
        if (threadPool == null) {
            synchronized (AsyncThreadPool.class) {
                if (threadPool == null) {
                    // 核心参数
                    int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;
                    int maxPoolSize = corePoolSize * 4;
                    long keepAliveTime = 60L;
                    TimeUnit unit = TimeUnit.SECONDS;
                    
                    // 创建有界队列（防止OOM）
                    BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(1000);
                    
                    // 自定义线程工厂（命名、守护线程控制）
                    ThreadFactory threadFactory = new CustomThreadFactory("async-pool-");
                    
                    // 自定义拒绝策略（记录日志+降级处理）
                    RejectedExecutionHandler handler = new CustomRejectionPolicy();

                    threadPool = new ThreadPoolExecutor(
                            corePoolSize,
                            maxPoolSize,
                            keepAliveTime,
                            unit,
                            workQueue,
                            threadFactory,
                            handler
                    );
                }
            }
        }
        return threadPool;
    }

    /**
     * 自定义线程工厂（给线程命名便于监控）
     */
    static class CustomThreadFactory implements ThreadFactory {
        private final String namePrefix;
        private final AtomicLong threadCounter = new AtomicLong(1);

        CustomThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, namePrefix + threadCounter.getAndIncrement());
            thread.setDaemon(false); // 非守护线程
            return thread;
        }
    }

    /**
     * 自定义拒绝策略
     */
    static class CustomRejectionPolicy implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            // 记录被拒绝的任务
            System.err.println("Task rejected: " + r.toString());
            
            // 降级策略：尝试重新放入队列
            try {
                if (!executor.isShutdown()) {
                    executor.getQueue().put(r);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Retry put task failed: " + e.getMessage());
            }
        }
    }
}