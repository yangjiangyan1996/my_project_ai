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
    public enum LabelEnums {
        Long_Term_Cooperation(1, "长期合作"),
        Suitable_For_New_Users(2, "适合新手"),
        Threshold_Low(3, "零门槛"),
        Can_Train(4, "可培训"),
        Easy_Money(5, "轻松赚钱"),
        Remote(6, "可远程"),

        ;
        private Integer code;
        private String name;

        LabelEnums(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        public static String getByCode(Integer v) {
            for (LabelEnums value : LabelEnums.values()) {
                if (value.code.equals(v)) {
                    return value.name;
                }
            }
            return null;
        }
    }

    @Getter
    public enum IndustryCategory {
        INTERNET_AI(100000, "互联网/AI", new IndustrySubCategory[] {
                new IndustrySubCategory(100020, "互联网"),
                new IndustrySubCategory(100001, "电子商务"),
                new IndustrySubCategory(100021, "计算机软件"),
                new IndustrySubCategory(100007, "生活服务(O2O)"),
                new IndustrySubCategory(100015, "企业服务"),
                new IndustrySubCategory(100006, "医疗健康"),
                new IndustrySubCategory(100002, "游戏"),
                new IndustrySubCategory(100003, "社交网络与媒体"),
                new IndustrySubCategory(100028, "人工智能"),
                new IndustrySubCategory(100029, "云计算"),
                new IndustrySubCategory(100012, "在线教育"),
                new IndustrySubCategory(100023, "计算机服务"),
                new IndustrySubCategory(100005, "大数据"),
                new IndustrySubCategory(100004, "广告营销"),
                new IndustrySubCategory(100030, "物联网"),
                new IndustrySubCategory(100017, "新零售"),
                new IndustrySubCategory(100016, "信息安全")
        }),

        ELECTRONICS_COMMUNICATION_SEMICONDUCTOR(101400, "电子/通信/半导体", new IndustrySubCategory[] {
                new IndustrySubCategory(101405, "半导体/芯片"),
                new IndustrySubCategory(101406, "电子/硬件开发"),
                new IndustrySubCategory(101402, "通信/网络设备"),
                new IndustrySubCategory(101401, "智能硬件/消费电子"),
                new IndustrySubCategory(101403, "运营商/增值服务"),
                new IndustrySubCategory(101404, "计算机硬件")
        }),

        SERVICE_INDUSTRY(101100, "服务业", new IndustrySubCategory[] {
                new IndustrySubCategory(101101, "餐饮"),
                new IndustrySubCategory(101111, "美容"),
                new IndustrySubCategory(101112, "美发"),
                new IndustrySubCategory(101102, "酒店/民宿"),
                new IndustrySubCategory(101107, "休闲/娱乐"),
                new IndustrySubCategory(101113, "运动/健身"),
                new IndustrySubCategory(101114, "保健/养生"),
                new IndustrySubCategory(101109, "家政服务"),
                new IndustrySubCategory(101103, "旅游/景区"),
                new IndustrySubCategory(101105, "婚庆/摄影"),
                new IndustrySubCategory(101110, "宠物服务"),
                new IndustrySubCategory(101108, "回收/维修"),
                new IndustrySubCategory(101106, "其他生活服务")
        }),

        CONSUMER_GOODS(101000, "消费品/批发/零售", new IndustrySubCategory[] {
                new IndustrySubCategory(101011, "批发/零售"),
                new IndustrySubCategory(101012, "进出口贸易"),
                new IndustrySubCategory(101001, "食品/饮料/烟酒"),
                new IndustrySubCategory(101003, "服装/纺织"),
                new IndustrySubCategory(101009, "家具/家居"),
                new IndustrySubCategory(101010, "家用电器"),
                new IndustrySubCategory(101002, "日化"),
                new IndustrySubCategory(101006, "珠宝/首饰"),
                new IndustrySubCategory(101013, "其他消费品")
        }),

        REAL_ESTATE_CONSTRUCTION(100700, "房地产/建筑", new IndustrySubCategory[] {
                new IndustrySubCategory(100704, "装修装饰"),
                new IndustrySubCategory(100708, "房屋建筑工程"),
                new IndustrySubCategory(100709, "土木工程"),
                new IndustrySubCategory(100710, "机电工程"),
                new IndustrySubCategory(100707, "物业管理"),
                new IndustrySubCategory(100706, "房地产中介/租赁"),
                new IndustrySubCategory(100705, "建筑材料"),
                new IndustrySubCategory(100701, "房地产开发经营"),
                new IndustrySubCategory(100703, "建筑设计"),
                new IndustrySubCategory(100711, "建筑工程咨询服务"),
                new IndustrySubCategory(100712, "土地与公共设施管理")
        }),

        EDUCATION_TRAINING(100300, "教育培训", new IndustrySubCategory[] {
                new IndustrySubCategory(100303, "培训/辅导机构"),
                new IndustrySubCategory(100305, "职业培训"),
                new IndustrySubCategory(100301, "学前教育"),
                new IndustrySubCategory(100302, "学校/学历教育"),
                new IndustrySubCategory(100304, "学术/科研")
        }),

        ADVERTISING_MEDIA_CULTURE_SPORTS(100100, "广告/传媒/文化/体育", new IndustrySubCategory[] {
                new IndustrySubCategory(100104, "文化艺术/娱乐"),
                new IndustrySubCategory(100105, "体育"),
                new IndustrySubCategory(100101, "广告/公关/会展"),
                new IndustrySubCategory(100103, "广播/影视"),
                new IndustrySubCategory(100102, "新闻/出版")
        }),

        MANUFACTURING(100900, "制造业", new IndustrySubCategory[] {
                new IndustrySubCategory(100906, "通用设备"),
                new IndustrySubCategory(100907, "专用设备"),
                new IndustrySubCategory(100908, "电气机械/器材"),
                new IndustrySubCategory(100909, "金属制品"),
                new IndustrySubCategory(100910, "非金属矿物制品"),
                new IndustrySubCategory(100911, "橡胶/塑料制品"),
                new IndustrySubCategory(100912, "化学原料/化学制品"),
                new IndustrySubCategory(100913, "仪器仪表"),
                new IndustrySubCategory(100914, "自动化设备"),
                new IndustrySubCategory(100904, "印刷/包装/造纸"),
                new IndustrySubCategory(100905, "铁路/船舶/航空/航天制造"),
                new IndustrySubCategory(100915, "计算机/通信/其他电子设备"),
                new IndustrySubCategory(100916, "新材料"),
                new IndustrySubCategory(100917, "其他制造业")
        }),

        PROFESSIONAL_SERVICES(100600, "专业服务", new IndustrySubCategory[] {
                new IndustrySubCategory(100601, "咨询"),
                new IndustrySubCategory(100605, "财务/审计/税务"),
                new IndustrySubCategory(100604, "人力资源服务"),
                new IndustrySubCategory(100602, "法律"),
                new IndustrySubCategory(100609, "检测/认证/知识产权"),
                new IndustrySubCategory(100603, "翻译"),
                new IndustrySubCategory(100608, "其他专业服务")
        }),

        PHARMACEUTICAL_MEDICAL(100400, "制药/医疗", new IndustrySubCategory[] {
                new IndustrySubCategory(100402, "医疗服务"),
                new IndustrySubCategory(100404, "医美服务"),
                new IndustrySubCategory(100403, "医疗器械"),
                new IndustrySubCategory(100405, "IVD"),
                new IndustrySubCategory(100401, "生物/制药"),
                new IndustrySubCategory(100406, "医药批发零售"),
                new IndustrySubCategory(100407, "医疗研发外包")
        }),

        AUTOMOTIVE(100800, "汽车", new IndustrySubCategory[] {
                new IndustrySubCategory(100804, "新能源汽车"),
                new IndustrySubCategory(100805, "汽车智能网联"),
                new IndustrySubCategory(100806, "汽车经销商"),
                new IndustrySubCategory(100807, "汽车后市场"),
                new IndustrySubCategory(100801, "汽车研发/制造"),
                new IndustrySubCategory(100802, "汽车零部件"),
                new IndustrySubCategory(100808, "摩托车/自行车制造")
        }),

        TRANSPORTATION_LOGISTICS(100500, "交通运输/物流", new IndustrySubCategory[] {
                new IndustrySubCategory(100505, "即时配送"),
                new IndustrySubCategory(100506, "快递"),
                new IndustrySubCategory(100507, "公路物流"),
                new IndustrySubCategory(100508, "同城货运"),
                new IndustrySubCategory(100509, "跨境物流"),
                new IndustrySubCategory(100510, "装卸搬运和仓储业"),
                new IndustrySubCategory(100511, "客运服务"),
                new IndustrySubCategory(100512, "港口/铁路/公路/机场")
        }),

        ENERGY_CHEMICAL_ENVIRONMENTAL(101200, "能源/化工/环保", new IndustrySubCategory[] {
                new IndustrySubCategory(101208, "光伏"),
                new IndustrySubCategory(101209, "储能"),
                new IndustrySubCategory(101210, "动力电池"),
                new IndustrySubCategory(101211, "风电"),
                new IndustrySubCategory(101212, "其他新能源"),
                new IndustrySubCategory(101207, "环保"),
                new IndustrySubCategory(101202, "化工"),
                new IndustrySubCategory(101205, "电力/热力/燃气/水利"),
                new IndustrySubCategory(101201, "石油/石化"),
                new IndustrySubCategory(101203, "矿产/地质"),
                new IndustrySubCategory(101204, "采掘/冶炼")
        }),

        FINANCE(100200, "金融", new IndustrySubCategory[] {
                new IndustrySubCategory(100206, "互联网金融"),
                new IndustrySubCategory(100201, "银行"),
                new IndustrySubCategory(100207, "投资/融资"),
                new IndustrySubCategory(100203, "证券/期货"),
                new IndustrySubCategory(100204, "基金"),
                new IndustrySubCategory(100202, "保险"),
                new IndustrySubCategory(100208, "租赁/拍卖/典当/担保"),
                new IndustrySubCategory(100205, "信托"),
                new IndustrySubCategory(100209, "财富管理"),
                new IndustrySubCategory(100210, "其他金融业")
        }),

        GOVERNMENT_NONPROFIT_OTHER(101300, "政府/非盈利机构/其他", new IndustrySubCategory[] {
                new IndustrySubCategory(101303, "农/林/牧/渔"),
                new IndustrySubCategory(101302, "非盈利机构"),
                new IndustrySubCategory(101301, "政府/公共事业"),
                new IndustrySubCategory(101304, "其他行业")
        });

        private final int code;
        private final String name;
        private final IndustrySubCategory[] subCategories;

        IndustryCategory(int code, String name, IndustrySubCategory[] subCategories) {
            this.code = code;
            this.name = name;
            this.subCategories = subCategories;
        }

        public static int checkCodeLevel(int code) {
            // First check if it's a top-level category code
            for (IndustryCategory category : values()) {
                if (category.code == code) {
                    return 1; // Belongs to first level
                }
            }

            // Then check all subcategories
            for (IndustryCategory category : values()) {
                for (IndustrySubCategory subCategory : category.subCategories) {
                    if (subCategory.code == code) {
                        return 2; // Belongs to second level
                    }
                }
            }

            // If not found in either level
            return 0; // Doesn't exist
        }

        public static String getNameByCode(int code) {
            // First check top-level categories
            for (IndustryCategory category : values()) {
                if (category.code == code) {
                    return category.name;
                }
            }

            // Then check all subcategories
            for (IndustryCategory category : values()) {
                for (IndustrySubCategory subCategory : category.subCategories) {
                    if (subCategory.code == code) {
                        return subCategory.name;
                    }
                }
            }

            // If not found
            return null;
        }

        // Getters
        public int getCode() { return code; }
        public String getName() { return name; }
        public IndustrySubCategory[] getSubCategories() { return subCategories; }

        public static class IndustrySubCategory {
            private final int code;
            private final String name;

            public IndustrySubCategory(int code, String name) {
                this.code = code;
                this.name = name;
            }

            // Getters
            public int getCode() { return code; }
            public String getName() { return name; }
        }
    }

    @Getter
    public enum UserTypeEnum {
        //学生
        STUDENT(0, "学生"),
        //上班族
        WORKER(1, "上班族/职场人士"),
        //自由职业者
        FREELANCER(2, "自由职业者"),
        //家庭主妇/主夫
        HOMEMAKER(3, "家庭主妇/主夫"),
        //退休人员
        RETIREE(4, "退休人员"),
        //专业人士(教师、医生、设计师等)
        PROFESSIONAL(5, "专业人士(教师、医生、设计师等)"),
        //创业者/小企业主
        ENTERPRISE(6, "创业者/小企业主"),
        //兼职工作者
        PART_TIME_WORKER(7, "兼职工作者"),
        //待业人员
        UNEMPLOYED(8, "待业人员"),
        //艺术创作者
        ARTIST(9, "艺术创作者"),
        //科技爱好者
        TECH_ENTHUSIAST(10, "科技爱好者"),
        //适合所有人
        ALL(11, "适合所有人"),
        ;

        private Integer code;
        private String name;
        UserTypeEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        public static String getByCode(Long industryCode) {
            for (UserTypeEnum industryEnum : UserTypeEnum.values()) {
                if (Long.valueOf(industryEnum.code).equals(industryCode)) {
                    return industryEnum.name;
                }
            }
            return null;
        }
    }


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
