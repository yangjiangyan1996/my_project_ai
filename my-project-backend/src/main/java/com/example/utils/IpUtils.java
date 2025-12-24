package com.example.utils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/24 22:37
 */
public  class IpUtils {
    /**
     * 获取客户端真实IP地址
     */
    public static String getIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "";
        }

        String ip = null;

        // 1. 优先从 X-Forwarded-For 获取（代理服务器转发）
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && xff.length() != 0 && !"unknown".equalsIgnoreCase(xff)) {
            // 多次反向代理后会有多个IP值，第一个为真实IP
            String[] ips = xff.split(",");
            for (String ipStr : ips) {
                if (ipStr != null && !"unknown".equalsIgnoreCase(ipStr.trim())) {
                    ip = ipStr.trim();
                    break;
                }
            }
        }

        // 2. 如果 X-Forwarded-For 获取不到，尝试 Proxy-Client-IP
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }

        // 3. 如果 Proxy-Client-IP 获取不到，尝试 WL-Proxy-Client-IP
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }

        // 4. 如果 WL-Proxy-Client-IP 获取不到，尝试 X-Real-IP
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }

        // 5. 如果以上都获取不到，使用 request.getRemoteAddr()
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();

            // 处理 IPv6 的本地地址
            if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
                ip = "127.0.0.1"; // 转换为 IPv4 的本地地址
            }
        }

        // 6. 处理可能存在的端口号（如 192.168.1.1:8080）
        if (ip != null && ip.contains(":")) {
            int colonIndex = ip.indexOf(":");
            ip = ip.substring(0, colonIndex);
        }

        // 7. 最终验证和清理
        ip = ip == null ? "" : ip.trim();

        // 8. 验证是否是有效的 IP 地址
        if (!isValidIpAddress(ip)) {
            return "";
        }

        return ip;
    }

    /**
     * 验证是否是有效的 IP 地址（IPv4 或 IPv6）
     */
    private static boolean isValidIpAddress(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }

        // IPv4 正则表达式
        String ipv4Pattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

        // IPv6 正则表达式（简化版）
        String ipv6Pattern = "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$";

        // 支持 IPv6 压缩形式（如 ::1）
        String ipv6CompressedPattern = "^(([0-9a-fA-F]{1,4}:){1,7}:|:(:[0-9a-fA-F]{1,4}){1,7}|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4})$";

        // 支持 IPv4 映射的 IPv6 地址（如 ::ffff:192.168.1.1）
        String ipv6MappedIpv4Pattern = "^::ffff:((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

        return ip.matches(ipv4Pattern) ||
                ip.matches(ipv6Pattern) ||
                ip.matches(ipv6CompressedPattern) ||
                ip.matches(ipv6MappedIpv4Pattern) ||
                "127.0.0.1".equals(ip) ||
                "localhost".equals(ip);
    }
}
