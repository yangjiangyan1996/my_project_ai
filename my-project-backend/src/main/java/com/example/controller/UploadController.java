package com.example.controller;

import com.example.entity.base.RespBean;
import com.example.entity.dto.Images;
import com.example.entity.resp.EnumResp;
import com.example.enums.ProjectEnum;
import com.example.mapper.ImagesMapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/20 14:31
 */
@RestController
@RequestMapping("/api/unauth/common/")
public class UploadController {

    @Resource
    private ImagesMapper imagesMapper;

    @PostMapping("/upload")
    public RespBean<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (!Arrays.asList("image/jpeg", "image/png").contains(file.getContentType())) {
            return RespBean.failure(2001, "文件格式必须为JPG/PNG");
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            return RespBean.failure(2002, "文件大小不能超过2MB");
        }

        try {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get("uploads", fileName);
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());

            Images image = new Images();
            image.setName(fileName);
            image.setUrl(path.toString());
            image.setImg(file.getContentType());
            imagesMapper.insert(image);

            return RespBean.success("http://localhost:8080/upload/" + fileName);
        } catch (IOException e) {
            return RespBean.failure(2003, "文件上传失败");
        }
    }

}
