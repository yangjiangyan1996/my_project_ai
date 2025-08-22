package com.example.controller;

import com.example.Facade.FsFacade;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.filter.UserUtil;
import com.example.mapper.ImagesMapper;
import com.example.utils.ImageUtils;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicSessionCredentials;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.Bucket;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import com.tencent.cloud.Response;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/20 14:31
 */
@RestController
@Slf4j
@RequestMapping("/api/auth/common/")
public class UploadController {

    @Resource
    FsFacade fsFacade;
    @Resource
    private ImagesMapper imagesMapper;

    @PostMapping("/upload")
    public RespBean<String> uploadImage(@RequestParam("file") MultipartFile file) {
        File tempFile = null;
        try {
            COSClient cc = fsFacade.getCosClient();
            tempFile = convertMultipartFileToFile(file);

            UserInfo user = UserUtil.getCurrentUser();

            // 生成唯一的文件key，避免文件名冲突
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileKey = "image/"+user.getId() + "/" + System.currentTimeMillis() + "_" + UUID.randomUUID() + fileExtension;

            PutObjectResult putObjectResult = fsFacade.uploadFile(cc, "fy-user-fs-1370764194", fileKey, tempFile);

            // 构建文件的访问URL（需要根据您的实际情况调整）
            String fileUrl = "https://fy-user-fs-1370764194.cos.ap-guangzhou.myqcloud.com/" + fileKey;

            return RespBean.success(fileUrl);

        } catch (CosServiceException e) {
            log.error("COS服务异常: {}", e.getErrorMessage());
            log.error("状态码: {}", e.getStatusCode());
            log.error("错误码: {}", e.getErrorCode());
            log.error("请求ID: {}", e.getRequestId());
            return RespBean.failure(1,"文件上传失败: " + e.getErrorMessage());
        } catch (Exception e) {
            log.error("上传文件异常: {}", e.getMessage(), e);
            return RespBean.failure(1,"文件上传失败: " + e.getMessage());
        } finally {
            // 确保删除临时文件
            if (tempFile != null && tempFile.exists()) {
                if (tempFile.delete()) {
                    log.info("已删除临时文件: {}", tempFile.getAbsolutePath());
                } else {
                    log.warn("无法删除临时文件: {}", tempFile.getAbsolutePath());
                }
            }
        }
    }

    public File convertMultipartFileToFile(MultipartFile multipartFile) {
        // 创建临时文件或指定目标文件
        File file = new File(System.getProperty("java.io.tmpdir") + "/" + multipartFile.getOriginalFilename());

        // 或者创建真正的临时文件（会在JVM退出时自动删除）
        // File file = File.createTempFile("upload-", "-" + multipartFile.getOriginalFilename());

        // 将MultipartFile内容传输到文件
        try {
            multipartFile.transferTo(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

}
