package com.example.config;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/16 20:36
 */
public class AsyncTest {
    public static void main(String[] args) {
//        // 无返回值任务
//        AsyncTaskUtil.execute(() -> {
//            userOperationLogService.saveLog(userId, "login", ip);
//        });
//
//        // 带返回值的Future模式
//        Future<BigDecimal> future = AsyncTaskUtil.submit(() -> {
//            return calculateCommission(orderId);
//        });
//
//
//        // 获取结果（可设置超时）
//        try {
//            BigDecimal result = future.get(2, TimeUnit.SECONDS);
//        } catch (TimeoutException e) {
//            // 超时处理
//        }
//
//        // CompletableFuture链式调用
//        AsyncTaskUtil.supplyAsync(() -> userApi.getUserInfo(userId))
//                .thenApplyAsync(user -> orderService.getOrders(user), AsyncThreadPool.getThreadPool())
//                .thenAccept(orders -> sendNotification(orders))
//                .exceptionally(e -> {
//                    System.err.println("Chain error: " + e.getMessage());
//                    return null;
//                });
    }
}
