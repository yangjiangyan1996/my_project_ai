package com.example.controller;

import com.example.entity.req.TaohuaRequest;
import com.example.entity.req.UserInput;
import com.example.entity.resp.NameResponse;
import com.example.entity.resp.SuanTaoHuaVO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/taohua")
public class TaohuaController {
    @PostMapping("/kanTaohua")
    public ResponseEntity<SuanTaoHuaVO> kanTaohua(@RequestBody TaohuaRequest request) {
        SuanTaoHuaVO vo = new SuanTaoHuaVO();
        vo.setAnalysis("🌸 桃花运势分析报告：\n近期将有重要邂逅机会");
        vo.setIndex(4);
        vo.setLuckyDirection("东南方");
        vo.setAdvice(List.of("佩戴粉水晶", "参加社交活动"));

        return ResponseEntity.ok(vo);
    }
}