package com.example.enums;

import ch.qos.logback.classic.model.LoggerModel;
import lombok.Getter;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/7 14:52
 */
public class CommonEnum {
    @Getter
    public enum IndustryEnum {
        // 一、互联网 💻
        INTERNET(101, "互联网"),
        SOFTWARE(102, "计算机软件"),
        HARDWARE(103, "计算机硬件"),
        ECOMMERCE(104, "电子商务"),
        GAME_DEV(105, "游戏开发"),
        AI(106, "人工智能"),
        CLOUD_BIGDATA(107, "云计算/大数据"),

        // 二、金融 💰
        BANK(201, "银行"),
        SECURITIES(202, "证券/基金"),
        INSURANCE(203, "保险"),
        INVESTMENT(204, "投资管理"),
        ACCOUNTING(205, "会计/审计"),
        FINTECH(206, "互联网金融"),

        // 三、教育/科研 📚
        HIGH_EDU(301, "高等教育"),
        K12(302, "K12教育"),
        VOCATIONAL(303, "职业培训"),
        EDTECH(304, "教育科技"),
        RESEARCH(305, "科研机构"),
        ONLINE_EDU(306, "在线教育"),

        // 四、医疗/健康 🏥
        HOSPITAL(401, "医院/医疗"),
        PHARMA(402, "制药/生物工程"),
        MED_DEVICE(403, "医疗器械"),
        HEALTH_MGMT(404, "健康管理"),
        ELDERLY_CARE(405, "养老产业"),

        // 五、娱乐 🎭
        FILM_MEDIA(501, "影视/媒体"),
        PUBLISHING(502, "出版/新闻"),
        AD_PR(503, "广告/公关"),
        ART_DESIGN(504, "艺术/设计"),
        SPORTS(505, "体育/健身"),
        ESPORTS(506, "游戏/电竞"),

        // 六、制造业 🏭
        AUTO(601, "汽车制造"),
        ELECTRONICS(602, "电子设备"),
        MACHINERY(603, "机械制造"),
        FOOD_BEVERAGE(604, "食品饮料"),
        TEXTILE(605, "纺织服装"),
        CHEMICALS(606, "化工/材料"),

        // 七、房地产/建筑 🏗️
        REAL_ESTATE(701, "房地产开发"),
        ARCHITECTURE(702, "建筑设计"),
        CONSTRUCTION(703, "建筑施工"),
        PROPERTY_MGMT(704, "物业管理"),
        INTERIOR(705, "室内设计"),

        // 八、零售/消费 🛍️
        RETAIL(801, "零售业"),
        FMCG(802, "快消品"),
        FOOD_HOTEL(803, "餐饮/酒店"),
        TRAVEL(804, "旅游/休闲"),

        // 九、专业服务 👔
        LAW(901, "法律"),
        CONSULTING(902, "咨询"),
        HR(903, "人力资源"),
        TRANSLATION(904, "翻译服务"),
        TEST_CERT(905, "检测/认证"),

        // 十、能源/环保 🌱
        NEW_ENERGY(1001, "新能源"),
        TRADITIONAL_ENERGY(1002, "传统能源"),
        ENV_TECH(1003, "环保技术"),
        ENERGY_SAVING(1004, "节能服务"),

        // 十一、交通运输 🚚
        LOGISTICS(1101, "物流/快递"),
        AIR_SHIPPING(1102, "航空/海运"),
        PUBLIC_TRANSPORT(1103, "公共交通"),

        // 十二、政府/非盈利 🏛️
        GOVERNMENT(1201, "政府机构"),
        PUBLIC_SECTOR(1202, "事业单位"),
        NGO(1203, "NGO组织"),

        // 十三、农业/林业/渔业 🌾
        AGRICULTURE(1301, "现代农业"),
        FORESTRY(1302, "林业"),
        FISHERY(1303, "渔业/水产"),

        // 十四、自由职业/个体 🧑‍🎨
        FREELANCER(1401, "自由职业者"),
        SELF_EMPLOYED(1402, "个体经营者"),

        // 十五、其他行业 🔄
        STUDENT(1501, "学生"),
        UNEMPLOYED(1502, "待业"),
        HOUSEHOLDER(1503, "家庭主妇/主夫");

        private final Integer code;
        private final String name;

        IndustryEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        public static String getByCode(Long industryCode) {
            for (IndustryEnum industryEnum : IndustryEnum.values()) {
                if (Long.valueOf(industryEnum.code).equals(industryCode)) {
                    return industryEnum.name;
                }
            }
            return null;
        }
    }
}
