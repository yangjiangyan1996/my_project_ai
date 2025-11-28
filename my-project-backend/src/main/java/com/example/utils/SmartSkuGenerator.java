package com.example.utils;

import com.example.constants.CkCommonConstant;
import jakarta.validation.ValidationException;
import org.apache.commons.lang3.StringUtils;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 智能SKU生成器 - 无需初始化数据
 */
public class SmartSkuGenerator {

    // 日期格式
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");

    // 序列号计数器（实际项目中应该从数据库获取当前最大值）
    private static final AtomicInteger sequence = new AtomicInteger(1);

    /**
     * 生成智能SKU
     * @param productName 产品名称
     * @param color 颜色
     * @param spec 规格
     * @return 生成的SKU
     */
    public static String generateSmartSku(String productName, String color, String spec) {
        if (StringUtils.isBlank(productName)) {
            throw new ValidationException("生产sku时产品名称不能为空");
        }

        // 1. 提取产品关键词
        String productCode = extractProductCode(productName);

        // 2. 处理颜色代码
        String colorCode = extractColorCode(color);

        // 3. 处理规格代码
        String specCode = extractSpecCode(spec);

        // 4. 获取当前日期
        String dateCode = LocalDate.now().format(DATE_FORMATTER);

        // 5. 生成序列号
        String serialNumber = generateSerialNumber();

        // 6. 组合SKU
        return CkCommonConstant.PREFIX_SKU + buildFinalSku(productCode, colorCode, specCode, dateCode, serialNumber);
    }

    /**
     * 从产品名称中提取产品代码
     */
    private static String extractProductCode(String productName) {
        if (StringUtils.isBlank(productName)) {
            return "PROD";
        }

        // 清理产品名称
        String cleanName = productName.replaceAll("[()（）需要放进]", "")
                .replaceAll("\\s+", " ")
                .trim();

        // 提取关键词生成代码
        String code = generateCodeFromName(cleanName);

        // 确保代码长度在2-6个字符之间
        if (code.length() < 2) {
            code = code + "X";
        }
        if (code.length() > 6) {
            code = code.substring(0, 6);
        }

        return code.toUpperCase();
    }

    /**
     * 从名称生成代码
     */
    private static String generateCodeFromName(String name) {
        // 策略1: 提取英文单词首字母
        if (name.matches(".*[a-zA-Z].*")) {
            String englishPart = extractEnglishInitials(name);
            if (!englishPart.isEmpty()) {
                return englishPart;
            }
        }

        // 策略2: 提取中文关键词首字母（简化版）
        String chineseCode = extractChineseKeywords(name);
        if (!chineseCode.isEmpty()) {
            return chineseCode;
        }

        // 策略3: 使用名称前几个字符的简写
        return abbreviateName(name);
    }

    /**
     * 提取英文单词首字母
     */
    private static String extractEnglishInitials(String name) {
        StringBuilder initials = new StringBuilder();
        String[] words = name.split("[^a-zA-Z]");

        for (String word : words) {
            if (word.length() > 0 && Character.isLetter(word.charAt(0))) {
                initials.append(Character.toUpperCase(word.charAt(0)));
            }
        }

        return initials.length() > 0 ? initials.toString() : "";
    }

    /**
     * 提取中文关键词（简化实现）
     */
    private static String extractChineseKeywords(String name) {
        // 常见产品类型关键词映射
        String[] keywords = {"架子", "外袋", "顶", "围布", "地钉", "拉绳", "沙包", "帐篷", "窗", "门"};
        String[] codes = {"JG", "WD", "TOP", "WB", "DD", "LS", "SB", "TENT", "WIN", "DOOR"};

        for (int i = 0; i < keywords.length; i++) {
            if (name.contains(keywords[i])) {
                return codes[i];
            }
        }

        return "";
    }

    /**
     * 名称简写
     */
    private static String abbreviateName(String name) {
        // 取前3个非空格字符
        String clean = name.replaceAll("\\s", "");
        if (clean.length() >= 3) {
            return clean.substring(0, 3).toUpperCase();
        } else {
            return (clean + "XXX").substring(0, 3).toUpperCase();
        }
    }

    /**
     * 提取颜色代码
     */
    private static String extractColorCode(String color) {
        if (StringUtils.isBlank(color)) {
            return "DEF";
        }

        String cleanColor = color.replaceAll("[（）()]", "").trim();

        // 常见颜色映射
        String colorCode = mapCommonColor(cleanColor);
        if (colorCode != null) {
            return colorCode;
        }

        // 未知颜色：取前3个字符
        if (cleanColor.length() >= 3) {
            return cleanColor.substring(0, 3).toUpperCase();
        } else {
            return (cleanColor + "XXX").substring(0, 3).toUpperCase();
        }
    }

    /**
     * 常见颜色映射
     */
    private static String mapCommonColor(String color) {
        switch (color) {
            case "黑色": return "BLK";
            case "白色": return "WHT";
            case "红色": return "RED";
            case "蓝色": return "BLU";
            case "绿色": return "GRN";
            case "灰色": return "GRY";
            case "黄色": return "YLW";
            case "紫色": return "PUR";
            case "粉色": return "PNK";
            case "橙色": return "ORG";
            case "棕色": return "BRN";
            case "银色": return "SLV";
            case "金色": return "GLD";
            case "迷彩色": return "CAM";
            case "藏青色": return "NAV";
            case "铁（黑金刚）": return "MTL";
            default: return null;
        }
    }

    /**
     * 提取规格代码
     */
    private static String extractSpecCode(String spec) {
        if (StringUtils.isBlank(spec)) {
            return "STD";
        }

        // 清理规格
        String cleanSpec = spec.replaceAll("[×*xX\\s]", "X")
                .replaceAll("[^a-zA-Z0-9X.-]", "")
                .toUpperCase();

        // 处理常见规格格式
        if (cleanSpec.contains("X")) {
            // 尺寸规格如 2X2M, 3X4.5M
            cleanSpec = cleanSpec.replace("M", "")
                    .replace("CM", "")
                    .replace("MM", "");
        }

        // 限制长度
        if (cleanSpec.length() > 8) {
            cleanSpec = cleanSpec.substring(0, 8);
        }

        if (cleanSpec.isEmpty()) {
            return "STD";
        }

        return cleanSpec;
    }

    /**
     * 生成序列号（实际项目中应该从数据库获取）
     */
    private static String generateSerialNumber() {
        // 这里使用简单的自增序列，实际项目中应该从数据库获取当前最大值
        int seq = sequence.getAndIncrement();
        if (seq > 999) {
            sequence.set(1); // 重置，实际项目中不应该这样处理
            seq = 1;
        }
        return String.format("%03d", seq);
    }

    /**
     * 构建最终SKU
     */
    private static String buildFinalSku(String productCode, String colorCode,
                                        String specCode, String dateCode, String serialNumber) {
        return String.format("%s-%s-%s-%s-%s",
                productCode, colorCode, specCode, dateCode, serialNumber);
    }

    /**
     * 验证SKU格式
     */
    public static boolean validateSku(String sku) {
        if (StringUtils.isBlank(sku)) {
            return false;
        }

        // SKU格式: XXX-XXX-XXX-YYMMDD-XXX
        String skuPattern = "^[A-Z0-9]{2,6}-[A-Z0-9]{2,3}-[A-Z0-9]{2,8}-\\d{6}-[A-Z0-9]{3}$";
        return sku.matches(skuPattern);
    }

    /**
     * 从数据库获取下一个序列号（实际项目中使用）
     */
    public static void setSequenceFromDatabase(int currentMax) {
        sequence.set(currentMax + 1);
    }

    public static void main(String[] args) {
        System.out.println(generateSmartSku("无轮架子外袋", "黑色", "3x3m/2.5m"));
    }
}