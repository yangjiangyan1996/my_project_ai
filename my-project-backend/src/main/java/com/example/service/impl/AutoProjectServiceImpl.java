package com.example.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.Facade.JimengFacade;
import com.example.entity.dto.Account;
import com.example.entity.dto.Projects;
import com.example.entity.dto.ProjectsDetail;
import com.example.enums.CommonEnum;
import com.example.enums.ProjectEnum;
import com.example.mapper.ProjectsDetailMapper;
import com.example.mapper.ProjectsMapper;
import com.example.service.AccountService;
import com.example.service.AutoProjectService;
import io.lettuce.core.internal.LettuceLists;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

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
    WebClient webClient;

    @Value("${auto-publish.max-daily:3}")
    private int maxDailyPublish;

    @Override
    public Projects generateRandomProject(CommonEnum.IndustryCategory parent, CommonEnum.IndustryCategory.IndustrySubCategory sub) {
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

        String aiPictureUrl = jimengFacade.getAiPictureUrl(description);
        project.setImageUrl(aiPictureUrl);

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
        project.setStatus(ProjectEnum.ProjectStatusEnum.PUBLISHING.getCode());

        // 保存项目
        projectMapper.insert(project);

        // 生成并保存项目详情
        ProjectsDetail detail = generateRandomProjectDetail(project.getId(), project.getName());
        detail.setCreatedBy(userId);
        projectDetailMapper.insert(detail);

        log.info("自动发布项目成功，项目ID: {}", project.getId());
        return project.getId();
    }


    @Override
    public ProjectsDetail generateRandomProjectDetail(Long projectId, String projectName) {
        Random random = new Random();
        ProjectsDetail detail = new ProjectsDetail();

        // 使用DeepSeek生成内容
        String steps = deepSeekContentService.generateProjectSteps(projectName);
        if (steps != null) {
            steps = processStepsWithImages(steps);
        } else {
            steps = "1. 注册账号\n2. 完成培训\n3. 开始工作\n4. 获取收益";
        }


        String tools = deepSeekContentService.generateProjectTools(projectName);
        String riskWarning = deepSeekContentService.generateRiskWarning(projectName);
        String tags = deepSeekContentService.generateProjectTags(projectName);

        detail.setProjectsId(projectId);
        detail.setSteps(steps != null ? steps : "1. 注册账号\n2. 完成培训\n3. 开始工作\n4. 获取收益");
        detail.setTools(tools != null ? tools : "- 剪映: 视频编辑工具\n- Canva: 设计工具");
        detail.setTimePerDay((long) (random.nextInt(4) + 1));
        detail.setIncomeEstimateMin((long) (random.nextInt(3000) + 1000));
        detail.setIncomeEstimateMax(detail.getIncomeEstimateMin() + (long) (random.nextInt(5000) + 1000));
        detail.setTargetAudience((long) (random.nextInt(3) + 1));
        detail.setRiskWarning(riskWarning != null ? riskWarning : "- 市场竞争风险\n- 平台政策风险");
        detail.setTags(tags != null ? tags : "新手友好,高收益");
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
        // 获取最近30天活跃的用户
        List<Long> ods = LettuceLists.newList(1L, 2L, 8L, 12L);
        List<Account> activeUsers = accountService.selectByIds(ods);
        if (activeUsers.isEmpty()) {
            return null;
        }
        return activeUsers.get(RandomUtils.nextInt(0, activeUsers.size())).getId();
    }
}