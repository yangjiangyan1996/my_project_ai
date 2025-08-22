package com.example.Facade;

import com.alibaba.fastjson2.JSON;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicSessionCredentials;
import com.qcloud.cos.model.*;
import com.qcloud.cos.region.Region;
import com.tencent.cloud.CosStsClient;
import com.tencent.cloud.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.TreeMap;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/22 16:03
 */
@Service
@Slf4j
public class FsFacade {


    private Response getCredential() throws IOException {
        TreeMap<String, Object> config = new TreeMap<String, Object>();
        // 云 api 密钥 SecretId
        config.put("secretId", "");
        // 云 api 密钥 SecretKey
        config.put("secretKey", "");

        // 设置域名,可通过此方式设置内网域名
        //config.put("host", "sts.internal.tencentcloudapi.com");

        // 临时密钥有效时长，单位是秒
        config.put("durationSeconds", 1800);

        // 换成你的 bucket
        config.put("bucket", "fy-user-fs-1370764194");
        // 换成 bucket 所在地区
        config.put("region", "ap-guangzhou");

        // 可以通过 allowPrefixes 指定前缀数组, 例子： a.jpg 或者 a/* 或者 * (使用通配符*存在重大安全风险, 请谨慎评估使用)
        config.put("allowPrefixes", new String[] {
                "image/*",           // 允许所有以 image/ 开头的文件
                "video/*",           // 如果需要，添加视频路径
                "document/*",        // 如果需要，添加文档路径
//                    "*"                  // 或者直接允许所有路径（测试用，生产环境请谨慎）
        });

        // 密钥的权限列表。简单上传和分片需要以下的权限，其他权限列表请看 https://cloud.tencent.com/document/product/436/31923
        // 扩展允许的操作权限
        String[] allowActions = new String[] {
                // 简单上传
                "name/cos:PutObject",
                "name/cos:PostObject",
                // 分片上传
                "name/cos:InitiateMultipartUpload",
                "name/cos:ListMultipartUploads",
                "name/cos:ListParts",
                "name/cos:UploadPart",
                "name/cos:CompleteMultipartUpload",
                // 下载和查看
                "name/cos:GetObject",
                "name/cos:HeadObject",
                "name/cos:ListObjects",
                // 删除
                "name/cos:DeleteObject"
        };
        config.put("allowActions", allowActions);

        log.info("临时密钥: {}", config);
        Response response = CosStsClient.getCredential(config);
        System.out.println(response.credentials.tmpSecretId);
        System.out.println(response.credentials.tmpSecretKey);
        System.out.println(response.credentials.sessionToken);
        return response;
    }
    public COSClient getCosClient() throws IOException {
        Response response = getCredential();
        // 1 传入获取到的临时密钥 (tmpSecretId, tmpSecretKey, sessionToken)
        String tmpSecretId = response.credentials.tmpSecretId;
        String tmpSecretKey = response.credentials.tmpSecretKey;
        String sessionToken = response.credentials.sessionToken;
        BasicSessionCredentials cred = new BasicSessionCredentials(tmpSecretId, tmpSecretKey, sessionToken);
        // 2 设置 bucket 的地域
        // clientConfig 中包含了设置 region, https(默认 http), 超时, 代理等 set 方法, 使用可参见源码或者常见问题 Java SDK 部分
        Region region = new Region("ap-guangzhou"); //COS_REGION 参数：配置成存储桶 bucket 的实际地域，例如 ap-beijing，更多 COS 地域的简称请参见 https://cloud.tencent.com/document/product/436/6224
        ClientConfig clientConfig = new ClientConfig(region);
        // 3 生成 cos 客户端
        COSClient cosClient = new COSClient(cred, clientConfig);
        return cosClient;
    }


    public Bucket getBucket(COSClient cosClient, String bucketName) {
        List<Bucket> buckets = cosClient.listBuckets();
        for (Bucket bucketElement : buckets) {
            String bn = bucketElement.getName();
            if (bn.equals(bucketName)) {
                return bucketElement;
            }
        }
        return null;
    }

    public PutObjectResult  uploadFile(COSClient cosClient, String bucketName, String key, File localFile) {
        // 指定要上传的文件
        // 指定文件上传到 COS 上的路径，即对象键。例如对象键为 folder/picture.jpg，则表示将文件 picture.jpg 上传到 folder 路径下
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, localFile);
        log.info("上传文件: {}", JSON.toJSONString(putObjectRequest));
        PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
        log.info("上传结果: {}", putObjectResult);
        return putObjectResult;
    }

    public String downLoadFile(COSClient cosClient,String bucketName) {
        // Bucket 的命名格式为 BucketName-APPID ，此处填写的存储桶名称必须为此格式
        // 指定文件在 COS 上的路径，即对象键。例如对象键为 folder/picture.jpg，则表示下载的文件 picture.jpg 在 folder 路径下
        String key = "folder/picture.jpg";
        // 方法1 获取下载输入流
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, key);
        COSObject cosObject = cosClient.getObject(getObjectRequest);
        COSObjectInputStream cosObjectInput = cosObject.getObjectContent();
        // 下载对象的 CRC64
        String crc64Ecma = cosObject.getObjectMetadata().getCrc64Ecma();
        // 关闭输入流
        try {
            cosObjectInput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return crc64Ecma;
    }

}
