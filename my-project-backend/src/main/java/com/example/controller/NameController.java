package com.example.controller;

import com.example.entity.req.UserInput;
import com.example.entity.resp.NameResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/name")
public class NameController {
    @PostMapping("/generateName")
    public List<NameResponse> generateName(@RequestBody UserInput input) {
        //TODO yang 接入ai模型生成中文名字
        // 构造示例prompt
//        String prompt = String.format("请根据以下信息帮我取中文名：姓氏%s，性别%s，出生时间%d年%d月%d日%d时。希望名字%s",
//                input.getLastName(), input.getGender(), input.getBirthYear(),
//                input.getBirthMonth(), input.getBirthDay(), input.getBirthHour(),
//                input.getAdditionalInfo() != null ? input.getAdditionalInfo() : "");

        // 模拟API调用
        List<NameResponse> result = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Map<String, String> wuxing = new HashMap<>();
            wuxing.put("金", "20%");
            wuxing.put("木", "35%");
            wuxing.put("水", "15%");
            wuxing.put("火", "20%");
            wuxing.put("土", "10%");

            NameResponse n1 = new NameResponse(
                    "王宇辰" + i,
                    "宇，意为广阔；辰，寓意时间和希望，整体寓意胸怀广阔，前程光明",
                    9d,  // 修改为整数评分
                    wuxing
            );
            result.add(n1);
        }
        return result;
    }
}