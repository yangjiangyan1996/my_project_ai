package com.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/23 09:33
 */
@Configuration
public class TenXunConfig {

    @Value("${tengxun.secretId}")
    public String secretId;
    @Value("${tengxun.secretKey}")
    public String secretKey;
    @Value("${tengxun.prodEvn}")
    public Boolean prodEvn;


    public  String bucketName = "xscode-fyfs-1370764194";

    public  String bucketUrl = "https://"+bucketName+".cos.ap-guangzhou.myqcloud.com/";

    public  String bucketRegion = "ap-guangzhou";

    public String dir;



    public String getDir() {
        if (Boolean.TRUE.equals(prodEvn)) {
            return "image/";
        } else {
            return "image/dev/";
        }
    }

    public String getSecretId() {
        return secretId;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public Boolean getProdEvn() {
        return prodEvn;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getBucketUrl() {
        return bucketUrl;
    }

    public String getBucketRegion() {
        return bucketRegion;
    }
}
