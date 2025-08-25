package com.example.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.Facade.FsFacade;
import com.example.Facade.JimengFacade;
import com.example.config.AsyncTaskUtil;
import com.example.config.JMConfig;
import com.example.config.TenXunConfig;
import com.example.entity.RestBean;
import com.example.entity.jimeng.JimengResp;
import com.example.entity.jimeng.LogoInfo;
import com.example.service.AutoProjectService;
import com.example.utils.FileUtils;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectResult;
import com.volcengine.service.visual.IVisualService;
import com.volcengine.service.visual.impl.VisualServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/3 14:50
 */

@RestController
@Slf4j
@RequestMapping("/api/unauth/project/")
public class TestController {
    @Resource
    JMConfig jmConfig;
    @Resource
    TenXunConfig tenXunConfig;
    @Resource
    JimengFacade jimengFacade;
    @Resource
    FsFacade fsFacade;


    @Resource
    AutoProjectService autoProjectService;

    @Operation(summary = "手动触发自动发布项目")
    @GetMapping("/publish")
    public RestBean<Long> autoPublishProject() {
        AsyncTaskUtil.execute(() -> autoProjectService.autoPublishProject());
        return RestBean.success();
    }

    //
    @GetMapping("/jimengPicture")
    public String sendSimpleMail() {
        IVisualService visualService = VisualServiceImpl.getInstance();

        visualService.setAccessKey(jmConfig.getAccessKey());

        visualService.setSecretKey(jmConfig.getAccessKeySecret());

        JSONObject req = new JSONObject();
        req.put("req_key", "jimeng_high_aes_general_v21_L");
        req.put("prompt", "无需经验，用AI开启你的广告创富之旅！我们提供智能平台，一键生成高质量广告文案和视觉内容，零技术门槛也能快速上手。  \n" +
                "**项目优势**：AI自动优化投放效果，成本低、效率高，24小时持续赚取收益；  \n" +
                "**适合人群**：学生、宝妈、上班族及任何渴望轻资产副业者，每天30分钟碎片时间即可操作；  \n" +
                "**收益潜力**：单账号月收益可达3000-10000+元，多平台布局收入倍增，轻松实现“睡后收入”！  \n" +
                "\n" +
                "马上拥抱AI红利，开启你的智能广告副业！");
        req.put("logo_info", JSON.toJSONString(new LogoInfo()));
        req.put("ddim_steps", 30);
        req.put("scale", 7.5);
        try {
            log.info("输入参数：" + JSON.toJSONString(req));
            Object response = visualService.cvProcess(req);
            log.info("输出参数：" + JSON.toJSONString(response));

            JimengResp jimengResp = JSON.parseObject(response.toString(), JimengResp.class);

            MultipartFile file = jimengFacade.convert(jimengResp.getData().getBinaryDataBase64().get(0), "测试上传ai图片");

            File tempFile = FileUtils.convertMultipartFileToFile(file);

            // 生成唯一的文件key，避免文件名冲突
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileKey = tenXunConfig.getDir() + System.currentTimeMillis() + "_" + UUID.randomUUID() + fileExtension;

            COSClient cc = fsFacade.getCosClient();

            PutObjectResult putObjectResult = fsFacade.uploadFile(cc, tenXunConfig.bucketName, fileKey, tempFile);

            // 构建文件的访问URL（需要根据您的实际情况调整）
            String fileUrl = tenXunConfig.bucketUrl + fileKey;

            return fileUrl;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "失败";
    }
}
