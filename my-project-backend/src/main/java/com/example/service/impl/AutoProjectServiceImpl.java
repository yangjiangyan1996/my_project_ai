package com.example.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.Facade.JimengFacade;
import com.example.entity.dto.*;
import com.example.enums.CommonEnum;
import com.example.enums.ProjectEnum;
import com.example.enums.QuanEnum;
import com.example.mapper.ProjectsDetailMapper;
import com.example.mapper.ProjectsMapper;
import com.example.service.AccountService;
import com.example.service.AutoProjectService;
import com.example.service.QuanBarTieService;
import com.example.service.QuanBarsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class AutoProjectServiceImpl extends ServiceImpl<ProjectsMapper, Projects> implements AutoProjectService {

    @Resource
    JimengFacade jimengFacade;
    @Resource
    ProjectsMapper projectMapper;
    @Resource
    ProjectsDetailMapper projectDetailMapper;
    @Resource
    AccountService accountService;
    @Resource
    DeepSeekContentService deepSeekContentService;

    @Resource
    QuanBarsService quanBarsService;
    @Resource
    QuanBarTieService quanBarTieService;


    @Override
    public void autoQuanTieProject() {
        List<QuanBars> quanBars = quanBarsService.selectAll();
        //随机挑选一个
        QuanBars quanBar = quanBars.get(RandomUtils.nextInt(0, quanBars.size()));
        try {
            // 生成与圈子相关的帖子内容
            QuanBarTie tie = generateQuanBarTie(quanBar);
            if (tie != null) {
                // 设置圈子ID
                tie.setBarId(quanBar.getId());
                // 保存帖子
                quanBarTieService.save(tie);
//                log.info("创建帖子成功 https://jimeng.jianying.com/ai-tool/generate/?type=image");
//                log.info("创建帖子成功 圈子： {} ", quanBar.getName());
//                log.info("创建帖子成功 创建帖子标题: {}", tie.getTitle());
//                log.info("创建帖子成功 内容是：{}",tie.getContent());
            }
        } catch (Exception e) {
            log.error("为圈子 {} 创建帖子失败", quanBar.getName(), e);
        }
    }

    /**
     * 为圈子生成帖子
     *
     * @param quanBar
     * @return
     */
    private QuanBarTie generateQuanBarTie(QuanBars quanBar) {
        Long userId = getRandomActiveUserId();
        if (userId == null) {
            log.warn("没有找到活跃用户，无法自动发布项目");
            return null;
        }
        // 使用DeepSeek生成与圈子相关的帖子标题和内容
        String barName = quanBar.getName();

        // 生成创业教学相关的帖子标题
        String title = deepSeekContentService.generateQuanTieTitle(barName);
        if (title == null) {
            title = "如何在" + barName + "领域成功创业 - 实用指南";
        }

        // 生成详细的创业教学内容
        String content = deepSeekContentService.generateQuanTieContent(barName, title);
        if (content == null) {
            content = generateDefaultContent(barName);
        }

        // 处理内容中的图片
        //content = processContentWithImages(content);

        // 生成相关的头像图片
        //String avatar = jimengFacade.getAiPictureUrl(title);

        QuanBarTie tie = new QuanBarTie();
        tie.setTitle(title);
        tie.setContent(content);
        //tie.setAvatar(avatar != null ? JSON.toJSONString(Lists.newArrayList(avatar)) : new String());
        tie.setAvatar("");
        tie.setStatus(QuanEnum.TieStatusEnums.NORMAL.getCode());
        tie.setCreatedBy(userId);
        tie.setCreatedAt(new Date());
        tie.setModifiedBy(userId);
        tie.setModifiedAt(new Date());
        return tie;
    }

    private String generateDefaultContent(String barName) {
        return "<h1>" + barName + "创业完整指南</h1>" +
                "<p><strong>一、市场分析</strong></p>" +
                "<p>在进入" + barName + "领域前，需要充分了解市场需求和竞争情况。</p>" +
                "<p><strong>二、启动步骤</strong></p>" +
                "<p>1. 市场调研</p>" +
                "<p>2. 资金准备</p>" +
                "<p>3. 技能学习</p>" +
                "<p>4. 实际操作</p>" +
                "<p><strong>三、盈利模式</strong></p>" +
                "<p>详细分析各种盈利途径和收入预期。</p>" +
                "<p><strong>四、风险提示</strong></p>" +
                "<p>需要注意的市场风险和应对策略。</p>";
    }

    private String processContentWithImages(String content) {
        // 分析内容结构，为关键段落添加配图
        String[] sections = content.split("(?=<p><strong>|<h[1-6]>)");
        StringBuilder result = new StringBuilder();

        for (String section : sections) {
            result.append(section);

            // 为每个主要段落添加相关图片
            if (section.contains("<strong>") || section.startsWith("<h")) {
                // 提取段落关键词用于生成图片
                String keywords = extractKeywordsFromSection(section);
                if (keywords != null && !keywords.trim().isEmpty()) {
                    String imageUrl = generateImageForContent(keywords);
                    if (imageUrl != null) {
                        result.append("<p><img src=\"")
                                .append(imageUrl)
                                .append("\" alt=\"")
                                .append(keywords)
                                .append("\" style=\"width: 100%; margin: 10px 0;\"/></p>");
                    }
                }
            }
        }

        return result.toString();
    }

    private String extractKeywordsFromSection(String section) {
        // 移除HTML标签
        String text = section.replaceAll("<[^>]*>", "");
        // 提取关键名词和动词
        return text.length() > 50 ? text.substring(0, Math.min(50, text.length())) : text;
    }

    private String generateImageForContent(String keywords) {
        try {
            return jimengFacade.getAiPictureUrl(keywords + " 创业教学,图片不要带文字");
        } catch (Exception e) {
            log.error("生成内容图片失败: {}", keywords, e);
            return null;
        }
    }

    private String getDefaultAvatar() {
        // 返回默认头像URL
        return "https://example.com/default-avatar.png";
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    private Projects generateRandomProject(CommonEnum.IndustryCategory parent, CommonEnum.IndustryCategory.IndustrySubCategory sub) {
        Random random = new Random();
        Projects project = new Projects();

        // 使用DeepSeek生成内容
        String name = deepSeekContentService.generateProjectName(parent, sub);
        if (name == null) {
            return null;
        }
        String description = deepSeekContentService.generateProjectDescription(name);

        project.setName(name);
        project.setDescription(description != null ? description : "这是一个由AI生成的副业项目，适合各类人群参与");
        project.setDifficulty(random.nextInt(ProjectEnum.ProjectDifficultyEnum.values().length));

        String secondCategory = deepSeekContentService.generateSecondCategory(name);
        int firstCategory = CommonEnum.IndustryCategory.getParentCode(Integer.valueOf(secondCategory));
        project.setFirstCategory(firstCategory);
        project.setSecondCategory(Integer.valueOf(secondCategory));

//        String aiPictureUrl = jimengFacade.getAiPictureUrl(description);
//        project.setImageUrl(aiPictureUrl);

        return project;
    }


    @Override
    public Long autoPublishProject() {
        CommonEnum.IndustryCategory parent = CommonEnum.IndustryCategory.values()[RandomUtils.nextInt(0, CommonEnum.IndustryCategory.values().length)];
        CommonEnum.IndustryCategory.IndustrySubCategory sub = parent.getSubCategories()[RandomUtils.nextInt(0, parent.getSubCategories().length)];
        log.info("开始自动发布项目，项目类别: {}, 子类别: {}", parent.getName(), sub.getName());
        createAutoProject(parent, sub);
        return 1L;
    }

    public Long createAutoProject(CommonEnum.IndustryCategory parent, CommonEnum.IndustryCategory.IndustrySubCategory sub) {
        // 获取一个随机用户作为创建者
        Long userId = getRandomActiveUserId();
        if (userId == null) {
            log.warn("没有找到活跃用户，无法自动发布项目");
            return null;
        }

        // 生成随机项目数据
        Projects project = generateRandomProject(parent, sub);
        project.setCreatedBy(userId);
//        project.setStatus(ProjectEnum.ProjectStatusEnum.PUBLISHING.getCode());

        // 保存项目
        //projectMapper.insert(project);

        // 生成并保存项目详情
        ProjectsDetail detail = generateRandomProjectDetail(project.getId(), project.getName());
        detail.setCreatedBy(userId);
        //projectDetailMapper.insert(detail);

        log.info("自动发布项目成功");
        log.info("自动发布项目成功，: 标题：{}", project.getName());
        log.info("自动发布项目成功，: 一级分类：{}", project.getFirstCategory());
        log.info("自动发布项目成功，: 二级分类：{}", project.getSecondCategory());
        log.info("自动发布项目成功，: 操作步骤：{}", detail.getSteps());
        log.info("自动发布项目成功，: 推荐工具：{}", detail.getTools());
        log.info("自动发布项目成功，: 风险提示：{}", detail.getRiskWarning());

        return project.getId();
    }


    private ProjectsDetail generateRandomProjectDetail(Long projectId, String projectName) {
        Random random = new Random();
        ProjectsDetail detail = new ProjectsDetail();

        // 使用DeepSeek生成内容
        String steps = deepSeekContentService.generateProjectSteps(projectName);
//        if (steps != null) {
//            steps = processStepsWithImages(steps);
//        } else {
//            steps = "1. 注册账号\n2. 完成培训\n3. 开始工作\n4. 获取收益";
//        }


        String tools = deepSeekContentService.generateProjectTools(projectName);
        String riskWarning = deepSeekContentService.generateRiskWarning(projectName);
//        String tags = deepSeekContentService.generateProjectTags(projectName);

        detail.setProjectsId(projectId);
        detail.setSteps(steps != null ? steps : "1. 注册账号\n2. 完成培训\n3. 开始工作\n4. 获取收益");
        detail.setTools(tools != null ? tools : "- 剪映: 视频编辑工具\n- Canva: 设计工具");
        detail.setTimePerDay((long) (random.nextInt(4) + 1));
        detail.setIncomeEstimateMin((long) (random.nextInt(3000) + 1000));
        detail.setIncomeEstimateMax(detail.getIncomeEstimateMin() + (long) (random.nextInt(5000) + 1000));
        detail.setTargetAudience((long) (random.nextInt(3) + 1));
        detail.setRiskWarning(riskWarning != null ? riskWarning : "- 市场竞争风险\n- 平台政策风险");
//        detail.setTags(tags != null ? tags : "新手友好,高收益");
        detail.setNeedMember(random.nextBoolean() ? 1 : 0);
        detail.setMemberNum(detail.getNeedMember() == 1 ? random.nextInt(5) + 1 : 0);

        return detail;
    }

    private String processStepsWithImages(String steps) {
        // 使用正则表达式匹配每个步骤
        String[] stepParts = steps.split("(?=<h[1-6]>|<p>6、)");
        StringBuilder result = new StringBuilder();

        for (String part : stepParts) {
            result.append(part);

            // 为每个步骤生成图片（除了最后的提示部分）
            if (part.startsWith("<h") || part.startsWith("<p>6、")) {
                // 提取步骤文本用于生成图片提示
                String textContent = extractTextFromStep(part);
                // 调用文成图接口生成图片URL
                String imageUrl = generateImageForStep(textContent);

                // 添加图片到步骤后面
                if (imageUrl != null) {
                    result.append("<p><img src=\"")
                            .append(imageUrl)
                            .append("\" alt=\"\" data-href=\"\" style=\"width: 100%;\"/></p>");
                }
            }
        }

        return result.toString();
    }

    private String extractTextFromStep(String stepHtml) {
        // 移除HTML标签，提取纯文本内容
        return stepHtml.replaceAll("<[^>]*>", "").trim();
    }

    private String generateImageForStep(String stepText) {
        try {
            // 调用文成图接口生成图片
            // 这里需要根据你的实际文成图API进行调整
            return jimengFacade.getAiPictureUrl(stepText);
        } catch (Exception e) {
            log.error("生成步骤图片失败: {}", stepText, e);
            return null;
        }
    }


    private Long getRandomActiveUserId() {
        // 获取默认用户
        List<Account> activeUsers = accountService.selectDefaultUser();
        if (activeUsers.isEmpty()) {
            log.warn("没有找到默认用户");
            return null;
        }
        return activeUsers.get(RandomUtils.nextInt(0, activeUsers.size())).getId();
    }
}