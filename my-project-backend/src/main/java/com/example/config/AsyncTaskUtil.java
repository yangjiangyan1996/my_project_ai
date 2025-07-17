package com.example.config;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Supplier;

public class AsyncTaskUtil {

    /**
     * 提交无返回值的任务
     */
    public static void execute(Runnable task) {
        AsyncThreadPool.getThreadPool().execute(() -> {
            long start = System.currentTimeMillis();
            try {
                task.run();
            } catch (Exception e) {
                handleException(e);
            } finally {
                logCostTime(start);
            }
        });
    }

    /**
     * 提交有返回值的任务（Future模式）
     */
    public static <T> Future<T> submit(Callable<T> task) {
        return AsyncThreadPool.getThreadPool().submit(() -> {
            long start = System.currentTimeMillis();
            try {
                return task.call();
            } catch (Exception e) {
                handleException(e);
                throw e;
            } finally {
                logCostTime(start);
            }
        });
    }

    /**
     * 提交任务（CompletableFuture增强）
     */
    public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(() -> {
            long start = System.currentTimeMillis();
            try {
                return supplier.get();
            } catch (Exception e) {
                handleException(e);
                throw new RuntimeException(e);
            } finally {
                logCostTime(start);
            }
        }, AsyncThreadPool.getThreadPool());
    }

    /**
     * 异常统一处理
     */
    private static void handleException(Exception e) {
        // 实际项目中接入日志框架如Log4j
        System.err.println("Async task error: " + e.getMessage());
        // 可扩展：告警通知、事务回滚等
    }

    /**
     * 耗时记录
     */
    private static void logCostTime(long startTime) {
        long cost = System.currentTimeMillis() - startTime;
        if (cost > 1000) { // 记录慢任务
            System.out.println("Task cost: " + cost + "ms");
        }
    }

    /**
     * 获取线程池状态
     */
    public static String getPoolStatus() {
        ThreadPoolExecutor executor = AsyncThreadPool.getThreadPool();
        return String.format(
            "Pool Status: [Active=%d, Queue=%d/%d, Completed=%d]",
            executor.getActiveCount(),
            executor.getQueue().size(),
            executor.getQueue().remainingCapacity(),
            executor.getCompletedTaskCount()
        );
    }
}