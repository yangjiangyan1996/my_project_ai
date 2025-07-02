package com.example.controller;

import com.example.entity.base.RespBean;
import com.example.mapper.ImagesMapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
//        if (!Arrays.asList("image/jpeg", "image/png").contains(file.getContentType())) {
//            return RespBean.failure(2001, "文件格式必须为JPG/PNG");
//        }
//        if (file.getSize() > 2 * 1024 * 1024) {
//            return RespBean.failure(2002, "文件大小不能超过2MB");
//        }
//
//        try {
//            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
//            Path path = Paths.get("uploads", fileName);
//            Files.createDirectories(path.getParent());
//            Files.write(path, file.getBytes());
//
//            Images image = new Images();
//            image.setName(fileName);
//            image.setUrl(path.toString());
//            image.setImg(file.getContentType());
//            imagesMapper.insert(image);
//
//            return RespBean.success("http://localhost:8080/upload/" + fileName);
//        } catch (IOException e) {
//            return RespBean.failure(2003, "文件上传失败");
//        }

        List<String> list = new ArrayList<>();
        list.add("https://img1.baidu.com/it/u=3715498923,2939634736&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=1428");
        list.add("https://img0.baidu.com/it/u=131893562,86665228&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=898");
        list.add("https://img2.baidu.com/it/u=851514045,2013562092&fm=253&fmt=auto&app=120&f=JPEG?w=800&h=1067");
        list.add("https://img1.baidu.com/it/u=2182146859,1446841222&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=1421");
        list.add("https://img1.baidu.com/it/u=628860371,3856933897&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=1422");
        list.add("https://img0.baidu.com/it/u=3517081069,3188644709&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=1422");
        list.add("https://img1.baidu.com/it/u=1444067090,4162561865&fm=253&fmt=auto&app=120&f=JPEG?w=500&h=667");
        list.add("https://img1.baidu.com/it/u=2309258749,2601028906&fm=253&fmt=auto&app=120&f=JPEG?w=500&h=628");
        list.add("https://img2.baidu.com/it/u=2179749649,398913166&fm=253&fmt=auto&app=120&f=JPEG?w=500&h=1255");
        list.add("https://img0.baidu.com/it/u=897907117,495356401&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=889");

        list.add("https://img1.baidu.com/it/u=488307329,791192203&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=624");
        Random random = new Random();
        int num = random.nextInt(11); // 生成 0 到 10 之间的整数
        String s = list.get(num);
        return RespBean.success(s);
    }

}
