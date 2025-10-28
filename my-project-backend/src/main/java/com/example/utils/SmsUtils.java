package com.example.utils;

import com.example.config.TenXunConfig;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * 腾讯云短信工具类
 * @Author YangJian
 * @Email 1776080295@qq.com
 */
@Component
public class SmsUtils {

    private static TenXunConfig tenXunConfig;

    @Autowired
    private TenXunConfig config;

    private static SmsClient smsClient;

    // 短信应用ID
    private static final String SDK_APP_ID = "1401031770";
    // 短信签名
    private static final String SIGN_NAME = "杭州平耀商贸有限公司";
    // 模板ID
    private static final String TEMPLATE_ID = "2512748";

    @PostConstruct
    public void init() {
        SmsUtils.tenXunConfig = config;
        initSmsClient();
    }

    /**
     * 初始化短信客户端
     */
    private static void initSmsClient() {
        try {
            Credential cred = new Credential(tenXunConfig.getSecretId(), tenXunConfig.getSecretKey());
            smsClient = new SmsClient(cred, "ap-guangzhou", getClientProfile());
        } catch (Exception e) {
            throw new RuntimeException("初始化腾讯云短信客户端失败", e);
        }
    }

    /**
     * 发送短信
     * @param phoneNumber 手机号码（支持多个，用逗号分隔）
     * @param templateParams 模板参数
     * @return 发送结果
     */
    public static SendSmsResponse sendSms(String phoneNumber, String[] templateParams) throws TencentCloudSDKException {
        return sendSms(new String[]{phoneNumber}, templateParams);
    }

    /**
     * 发送短信
     * @param phoneNumbers 手机号码数组
     * @param templateParams 模板参数
     * @return 发送结果
     */
    public static SendSmsResponse sendSms(String[] phoneNumbers, String[] templateParams) throws TencentCloudSDKException {
        if (smsClient == null) {
            initSmsClient();
        }

        SendSmsRequest req = new SendSmsRequest();
        req.setSmsSdkAppId(SDK_APP_ID);
        req.setSignName(SIGN_NAME);
        req.setTemplateId(TEMPLATE_ID);
        req.setTemplateParamSet(templateParams);

        // 格式化手机号码（添加+86前缀）
        String[] formattedNumbers = formatPhoneNumbers(phoneNumbers);
        req.setPhoneNumberSet(formattedNumbers);

        return smsClient.SendSms(req);
    }

    /**
     * 格式化手机号码
     * @param phoneNumbers 原始手机号码数组
     * @return 格式化后的手机号码数组
     */
    private static String[] formatPhoneNumbers(String[] phoneNumbers) {
        String[] formatted = new String[phoneNumbers.length];
        for (int i = 0; i < phoneNumbers.length; i++) {
            String number = phoneNumbers[i];
            if (!number.startsWith("+")) {
                // 默认添加+86前缀
                formatted[i] = "+86" + number;
            } else {
                formatted[i] = number;
            }
        }
        return formatted;
    }

    private static ClientProfile getClientProfile() {
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setSignMethod("HmacSHA256");
        clientProfile.setHttpProfile(getHttpProfile());
        return clientProfile;
    }

    private static HttpProfile getHttpProfile() {
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setReqMethod("GET");
        httpProfile.setConnTimeout(10);
        httpProfile.setWriteTimeout(10);
        httpProfile.setReadTimeout(10);
        httpProfile.setEndpoint("sms.tencentcloudapi.com");
        return httpProfile;
    }
}