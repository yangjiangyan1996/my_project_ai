package com.example.service.impl;

import com.example.enums.CommonEnum;
import com.example.utils.DeepSeekUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeepSeekContentService {

    @Resource
    private DeepSeekUtils deepSeekUtils;

    /**
     * 生成圈子帖子标题
     */
    public String generateQuanTieTitle(String barName) {
        String prompt = "请为'" + barName + "'圈子生成一个创业教学帖子的标题。" +
                "要求：1、突出创业教学主题，吸引人点击，长度在15-25字之间。" +
                " 2、只提供一个标题\n" +
                "示例：'摆摊创业全攻略：从零开始月入过万的烧烤技巧'";

        return deepSeekUtils.callDeepSeek(prompt);
    }

    /**
     * 生成圈子帖子内容
     */
    public String generateQuanTieContent(String barName, String title) {
        String prompt = "请为'" + barName + "'领域生成一篇关于" + title + "的完整的创业教学帖子内容。" +
                "要求包含：具体操作步骤、所需工具材料、盈利模式分析。" +
                "不要带#，*这样的符号。" +
                "内容要实用具体，适合创业者学习，使用纯文本格式分段，字数在800-1200字。";

        return deepSeekUtils.callDeepSeek(prompt);
    }

    public String generateProjectContent(String prompt) {
        return deepSeekUtils.callDeepSeek(prompt);
    }

    // 其他方法保持不变...
    public String generateProjectName(CommonEnum.IndustryCategory parent, CommonEnum.IndustryCategory.IndustrySubCategory sub) {
        String prompt = "生成一个副业项目名称，要求：\n"
                + "- 简洁明了，不超过20个字\n"
                + "- 包含行业关键词如 " + parent.getName() + "\n"
                + "- 包含行业关键词如 " + sub.getName() + "等\n"
                + "- 体现项目特点如'零门槛'、'高收益'等\n"
                + "- 只返回名称，不要其他内容";
        return generateProjectContent(prompt);
    }

    public String generateProjectDescription(String projectName) {
        String prompt = "生成一个副业项目,关于" + projectName + "的描述，要求：\n"
                + "- 100-200字\n"
                + "- 包含项目优势、适合人群、收益潜力\n"
                + "- 语言生动有吸引力\n"
                + "- 使用段落格式";
        return generateProjectContent(prompt);
    }

    public String generateProjectSteps(String projectName) {
        String lizi = "<h1>1、这是一个h1的行</h1><p><span style=\"color: rgb(255, 163, 158);\">这是有颜色的内容</span></p><h2>2、这是一个h2的行</h2><p>\uD83D\uDE0C\uD83D\uDE11\uD83E\uDD1E</p><h3>3、这是一个h3的行</h3><h4>4、这是一个h4的行</h4><h5>5、这是一个h5的行</h5><p>6、这是正文行</p><p><br></p><blockquote>这里是引用的内容<br>哈哈哈哈哈哈哈哈哈</blockquote>";
        String prompt = "生成一个副业项目" + projectName + "的操作步骤，要求：\n"
                + "- 分5-7个步骤\n"
                + "- 每个步骤包含具体操作方法\n"
                + "- 输出内容格式使用：" + lizi + "\n"
                + "- 包含必要的细节但不过于复杂";
        return generateProjectContent(prompt);
    }

    public String generateProjectTools(String projectName) {
        String lizi = "1. 抖音 (Douyin) / 抖音极速版 \n" +
                "        用途： 核心用途是通过创作短视频内容（如知识分享、好物推荐、生活记录、技能展示）吸引粉丝，然后通过直播带货、短视频带货、星图广告、创作者基金等多种方式变现。 \n" +
                "        使用频率： 极高 (每天)。作为内容发布和变现的主阵地，需要高频地发布内容、与粉丝互动、查看数据分析和参与平台活动。 \n" +
                "        使用熟练度： 初级即可上手。应用内剪辑功能强大，“剪同款”功能让零基础用户也能快速产出优质视频。但要精通流量规则和变现技巧，需要达到中级以上熟练度。\n" +
                "2. 剪映 (Jianying/CapCut) \n" +
                "        用途： 快速、高效地剪辑视频，添加字幕（识别功能强大）、美颜、滤镜、特效和热门背景音乐，极大降低视频制作门槛。 \n" +
                "        使用频率： 高 (每次创作必用)。每制作一条短视频都需要使用它进行后期处理，使用频率与你的内容更新频率一致。 \n" +
                "        使用熟练度： 初级到中级。基本功能（剪辑、加字、加音乐）半小时内就能学会。熟练使用模板和高级功能（关键帧、蒙版）能显著提升视频质量，达到中级水平即可满足大部分需求。 \n";
        String prompt = "列出3-5个适合副业项目" + projectName + "的工具或平台，要求：\n"
                + "- 直接返回与" + lizi + "格式相同的数据即可\n"
                + "- 不要产生#,*这两种符号，只提供标点符号\n"
                + "- 每个工具的使用频率\n"
                + "- 每个工具的使用熟练度\n"
                + "- 工具与工具之间的内容中间隔开两行\n"
                + "- 包含工具名称和用途";
        return generateProjectContent(prompt);
    }

    public String generateRiskWarning(String projectName) {
        String prompt = "生成一个副业项目" + projectName + "的风险提示，要求：\n"
                + "- 列出3-4个主要风险点\n"
                + "- 每个风险点通过1，2，3标出，附带简要说明\n"
                + "- 不要产生#,*这两种符号，只提供标点符号\n"
                + "- 风险点与风险点之间的内容中间隔开两行\n"
                + "- 语气专业但易懂";
        return generateProjectContent(prompt);
    }

    public String generateProjectTags(String projectName) {
        String prompt = "生成3-5个适合副业项目" + projectName + "的标签，要求：\n"
                + "- 每个标签2-4个字\n"
                + "- 用逗号分隔\n"
                + "- 包含项目特点和适合人群\n"
                + "- 只返回标签，不要其他内容";
        return generateProjectContent(prompt);
    }

    public String generateSecondCategory(String projectName) {
        String code = "100020=\"互联网\"；100001=\"电子商务\"；100021=\"计算机软件\"；100007=\"生活服务(O2O,\"；100015=\"企业服务\"；100006=\"医疗健康\"；100002=\"游戏\"；100003=\"社交网络与媒体\"；100028=\"人工智能\"；100029=\"云计算\"；100012=\"在线教育\"；100023=\"计算机服务\"；100005=\"大数据\"；100004=\"广告营销\"；100030=\"物联网\"；100017=\"新零售\"；100016=\"信息安全\"；101405=\"半导体/芯片\"；101406=\"电子/硬件开发\"；101402=\"通信/网络设备\"；101401=\"智能硬件/消费电子\"；101403=\"运营商/增值服务\"；101404=\"计算机硬件\"；101101=\"餐饮\"；101111=\"美容\"；101112=\"美发\"；101102=\"酒店/民宿\"；101107=\"休闲/娱乐\"；101113=\"运动/健身\"；101114=\"保健/养生\"；101109=\"家政服务\"；101103=\"旅游/景区\"；101105=\"婚庆/摄影\"；101110=\"宠物服务\"；101108=\"回收/维修\"；101106=\"其他生活服务\"；101011=\"批发/零售\"；101012=\"进出口贸易\"；101001=\"食品/饮料/烟酒\"；101003=\"服装/纺织\"；101009=\"家具/家居\"；101010=\"家用电器\"；101002=\"日化\"；101006=\"珠宝/首饰\"；101013=\"其他消费品\"；100704=\"装修装饰\"；100708=\"房屋建筑工程\"；100709=\"土木工程\"；100710=\"机电工程\"；100707=\"物业管理\"；100706=\"房地产中介/租赁\"；100705=\"建筑材料\"；100701=\"房地产开发经营\"；100703=\"建筑设计\"；100711=\"建筑工程咨询服务\"；100712=\"土地与公共设施管理\"；100303=\"培训/辅导机构\"；100305=\"职业培训\"；100301=\"学前教育\"；100302=\"学校/学历教育\"；100304=\"学术/科研\"；100104=\"文化艺术/娱乐\"；100105=\"体育\"；100101=\"广告/公关/会展\"；100103=\"广播/影视\"；100102=\"新闻/出版\"；100906=\"通用设备\"；100907=\"专用设备\"；100908=\"电气机械/器材\"；100909=\"金属制品\"；100910=\"非金属矿物制品\"；100911=\"橡胶/塑料制品\"；100912=\"化学原料/化学制品\"；100913=\"仪器仪表\"；100914=\"自动化设备\"；100904=\"印刷/包装/造纸\"；100905=\"铁路/船舶/航空/航天制造\"；100915=\"计算机/通信/其他电子设备\"；100916=\"新材料\"；100917=\"其他制造业\"；100601=\"咨询\"；100605=\"财务/审计/税务\"；100604=\"人力资源服务\"；100602=\"法律\"；100609=\"检测/认证/知识产权\"；100603=\"翻译\"；100608=\"其他专业服务\"；100402=\"医疗服务\"；100404=\"医美服务\"；100403=\"医疗器械\"；100405=\"IVD\"；100401=\"生物/制药\"；100406=\"医药批发零售\"；100407=\"医疗研发外包\"；100804=\"新能源汽车\"；100805=\"汽车智能网联\"；100806=\"汽车经销商\"；100807=\"汽车后市场\"；100801=\"汽车研发/制造\"；100802=\"汽车零部件\"；100808=\"摩托车/自行车制造\"；100505=\"即时配送\"；100506=\"快递\"；100507=\"公路物流\"；100508=\"同城货运\"；100509=\"跨境物流\"；100510=\"装卸搬运和仓储业\"；100511=\"客运服务\"；100512=\"港口/铁路/公路/机场\"；101208=\"光伏\"；101209=\"储能\"；101210=\"动力电池\"；101211=\"风电\"；101212=\"其他新能源\"；101207=\"环保\"；101202=\"化工\"；101205=\"电力/热力/燃气/水利\"；101201=\"石油/石化\"；101203=\"矿产/地质\"；101204=\"采掘/冶炼\"；100206=\"互联网金融\"；100201=\"银行\"；100207=\"投资/融资\"；100203=\"证券/期货\"；100204=\"基金\"；100202=\"保险\"；100208=\"租赁/拍卖/典当/担保\"；100205=\"信托\"；100209=\"财富管理\"；100210=\"其他金融业\"；101303=\"农/林/牧/渔\"；101302=\"非盈利机构\"；101301=\"政府/公共事业\"；101304=\"其他行业；";
        String prompt = "根据副业项目" + projectName + "获取合适的标签ID，要求：\n"
                + "- 从" + code + "中匹配合适的数字"
                + "- 只提供一个最核心的标签\n"
                + "- 直接返回标签的数字编号即可\n";
        return generateProjectContent(prompt);
    }

    // 内部请求/响应类
    @Data
    private static class ChatCompletionRequest {
        private String model;
        private List<ChatMessage> messages;
        private Double temperature;
        @JsonProperty("max_tokens")
        private Integer maxTokens;
    }

    @Data
    private static class ChatMessage {
        private String role;
        private String content;

        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    @Data
    private static class ChatCompletionResponse {
        private List<Choice> choices;
    }

    @Data
    private static class Choice {
        private ChatMessage message;
    }
}