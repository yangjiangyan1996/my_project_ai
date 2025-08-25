package com.example.Facade;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.config.JMConfig;
import com.example.config.TenXunConfig;
import com.example.entity.jimeng.CustomMultipartFile;
import com.example.entity.jimeng.JimengResp;
import com.example.entity.jimeng.LogoInfo;
import com.example.utils.FileUtils;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectResult;
import com.volcengine.service.visual.IVisualService;
import com.volcengine.service.visual.impl.VisualServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/25 16:38
 */
@Service
@Slf4j
public class JimengFacade {

    @Resource
    JMConfig jmConfig;
    @Resource
    TenXunConfig tenXunConfig;
    @Resource
    private FsFacade fsFacade;

    public String getAiPictureUrl(String prompt) {
        try {
            if (StringUtils.isEmpty(prompt)) {
                log.error("生成图片失败: prompt为空");
                return null;
            }
            String img2Jinzhi = generateImageSync(prompt);

            //从prompt中随机截取3个字符串作为文件名
            // 截取的位数可以根据您的需求调整
            // 如果prompt长度小于3，则使用prompt
            String fileName = null;
            if (prompt.length() < 3) {
                fileName = prompt;
            } else {
                fileName = prompt.substring(0, 3);
            }
            MultipartFile file = convert(img2Jinzhi, fileName);

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
            log.error("生成图片失败: {}", prompt, e);
        }
        return null;
    }

    /**
     * 同步生成图片（直接返回结果）
     *
     * @param prompt 文字描述
     * @return 图片生成结果地址
     */
    private String generateImageSync(String prompt) throws Exception {
        IVisualService visualService = VisualServiceImpl.getInstance();

        visualService.setAccessKey(jmConfig.getAccessKey());

        visualService.setSecretKey(jmConfig.getAccessKeySecret());

        JSONObject req = new JSONObject();
        req.put("req_key", "jimeng_high_aes_general_v21_L");
        req.put("prompt", prompt);
        req.put("logo_info", JSON.toJSONString(new LogoInfo()));
        req.put("ddim_steps", 30);
        req.put("scale", 7.5);

        Object response = visualService.cvProcess(req);
        //把response 转换成 JimengResp对象
        JimengResp jimengResp = JSON.parseObject(response.toString(), JimengResp.class);
        return jimengResp.getData().getBinaryDataBase64().get(0);
    }


    /**
     * 将Base64字符串转换为MultipartFile
     *
     * @param base64String Base64编码的图片字符串（可包含data:image/png;base64,前缀）
     * @param fileName     文件名（可选，如为空则自动生成）
     * @return MultipartFile对象
     */
    public MultipartFile convert(String base64String, String fileName) throws IOException {
        try {
            // 清理Base64字符串（移除可能的数据URI前缀）
            String cleanedBase64 = cleanBase64String(base64String);

            // 解码Base64字符串
            byte[] imageBytes = Base64.decodeBase64(cleanedBase64);

            // 确定文件类型
            String contentType = determineContentType(cleanedBase64, imageBytes);

            // 生成文件名（如果未提供）
            String finalFileName = ensureFileExtension(fileName, contentType);


            // 创建MultipartFile对象
            return new CustomMultipartFile(
                    "file",
                    finalFileName,
                    contentType,
                    imageBytes
            );
        } catch (Exception e) {
            throw new IOException("Base64转换为MultipartFile失败: " + e.getMessage(), e);
        }
    }

    /**
     * 确保文件名有正确的扩展名
     */
    private String ensureFileExtension(String fileName, String contentType) {
        String extension = getExtensionFromContentType(contentType);

        if (fileName == null) {
            return generateFileName(contentType);
        }

        // 检查文件名是否已有扩展名
        if (fileName.contains(".")) {
            return fileName;
        }

        // 如果没有扩展名，添加扩展名
        return fileName + "." + extension;
    }

    /**
     * 根据ContentType获取文件扩展名
     */
    private String getExtensionFromContentType(String contentType) {
        if (contentType == null) {
            return "png";
        }

        switch (contentType) {
            case "image/jpeg":
                return "jpg";
            case "image/png":
                return "png";
            case "image/gif":
                return "gif";
            case "image/webp":
                return "webp";
            case "image/bmp":
                return "bmp";
            default:
                return "png";
        }
    }


    /**
     * 清理Base64字符串，移除数据URI前缀
     */
    private String cleanBase64String(String base64String) {
        if (base64String.contains("base64,")) {
            return base64String.substring(base64String.indexOf("base64,") + 7);
        }
        return base64String;
    }

    /**
     * 根据Base64前缀或文件内容确定Content-Type
     */
    private String determineContentType(String base64String, byte[] imageBytes) {
        // 首先检查Base64字符串是否包含MIME类型信息
        if (base64String.startsWith("/9j/") ||
                (imageBytes.length > 2 && imageBytes[0] == (byte) 0xFF && imageBytes[1] == (byte) 0xD8)) {
            return "image/jpeg";
        } else if (base64String.startsWith("iVBORw0KGgo")) {
            return "image/png";
        } else if (base64String.startsWith("R0lGODlh") || base64String.startsWith("/+")) {
            return "image/gif";
        } else if (base64String.startsWith("UklGR")) {
            return "image/webp";
        }

        // 默认使用PNG
        return "image/png";
    }

    /**
     * 生成随机文件名
     */
    private String generateFileName(String contentType) {
        String extension = "png"; // 默认扩展名

        if (contentType != null) {
            switch (contentType) {
                case "image/jpeg":
                    extension = "jpg";
                    break;
                case "image/png":
                    extension = "png";
                    break;
                case "image/gif":
                    extension = "gif";
                    break;
                case "image/webp":
                    extension = "webp";
                    break;
            }
        }

        return "ai_generated_" + System.currentTimeMillis() + "_" +
                UUID.randomUUID().toString().substring(0, 8) + "." + extension;
    }


}
