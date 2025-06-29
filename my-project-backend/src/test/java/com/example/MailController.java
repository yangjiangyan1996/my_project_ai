//package com.example;
//
//import com.example.config.QqMailService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/mail")
//public class MailController {
//
//    @Autowired
//    private QqMailService qqMailService;
//
//    @GetMapping("/simple")
//    public String sendSimpleMail() {
//        qqMailService.sendSimpleMail(
//                "aqawaearadf@gmail.com",
//                "测试简单邮件",
//                "这是一封来自Spring Boot的测试邮件"
//        );
//        return "简单邮件发送成功";
//    }
//
////    @GetMapping("/html")
////    public String sendHtmlMail() throws MessagingException {
////        String htmlContent = "<h1 style='color:red'>这是一封HTML邮件</h1>" +
////                           "<p>这是通过Spring Boot发送的HTML格式邮件</p>";
////
////        qqMailService.sendHtmlMail(
////            "recipient@example.com",
////            "测试HTML邮件",
////            htmlContent
////        );
////        return "HTML邮件发送成功";
////    }
////
////    @GetMapping("/attachment")
////    public String sendAttachmentMail() throws MessagingException {
////        String filePath = "/path/to/your/file.pdf"; // 替换为实际文件路径
////
////        qqMailService.sendAttachmentMail(
////            "recipient@example.com",
////            "测试带附件邮件",
////            "请查看附件",
////            filePath
////        );
////        return "带附件邮件发送成功";
////    }
//}