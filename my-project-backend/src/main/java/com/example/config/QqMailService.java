package com.example.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class QqMailService {

    @Autowired
    private JavaMailSender mailSender;

    // 发送简单文本邮件
    public void sendSimpleMail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("1776080295@qq.com"); // 发件人邮箱(必须与配置的username一致)
        message.setTo(to);                   // 收件人邮箱
        message.setSubject(subject);         // 邮件主题
        message.setText(content);            // 邮件内容
        mailSender.send(message);
    }
//
//    // 发送HTML邮件
//    public void sendHtmlMail(String to, String subject, String content) throws MessagingException {
//        MimeMessage message = mailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(message, true);
//        helper.setFrom("123456789@qq.com");
//        helper.setTo(to);
//        helper.setSubject(subject);
//        helper.setText(content, true); // true表示发送HTML格式
//
//        mailSender.send(message);
//    }
//
//    // 发送带附件的邮件
//    public void sendAttachmentMail(String to, String subject, String content, String filePath) throws MessagingException {
//        MimeMessage message = mailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(message, true);
//        helper.setFrom("123456789@qq.com");
//        helper.setTo(to);
//        helper.setSubject(subject);
//        helper.setText(content, true);
//
//        // 添加附件
//        File file = new File(filePath);
//        String fileName = file.getName();
//        helper.addAttachment(fileName, file);
//
//        mailSender.send(message);
//    }
}