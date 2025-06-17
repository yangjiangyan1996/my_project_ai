package com.example.controller;

import com.example.entity.req.UserInput;
import com.example.entity.resp.NameResponse;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth/name")
public class NameController {
    @PostMapping("/generateName")
    public NameResponse generateName(@RequestBody UserInput input) {
        // 构造示例prompt
        String prompt = String.format("请根据以下信息帮我取中文名：姓氏%s，性别%s，出生时间%d年%d月%d日%d时。希望名字%s",
                input.getLastName(), input.getGender(), input.getBirthYear(),
                input.getBirthMonth(), input.getBirthDay(), input.getBirthHour(),
                input.getAdditionalInfo() != null ? input.getAdditionalInfo() : "");
        
        // 模拟API调用
        return new NameResponse("王宇辰", "宇，意为广阔；辰，寓意时间和希望，整体寓意胸怀广阔，前程光明");
    }
}