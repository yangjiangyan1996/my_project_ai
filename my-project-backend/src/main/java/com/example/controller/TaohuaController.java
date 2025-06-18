package com.example.controller;

import com.example.entity.req.TaohuaRequest;
import com.example.entity.req.TargetPerson;
import com.example.entity.req.UserInput;
import com.example.entity.resp.NameResponse;
import com.example.entity.resp.SuanTaoHuaVO;
import com.example.entity.resp.TargetVO;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

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
        if (!CollectionUtils.isEmpty(request.getTargets())) {
            List<TargetVO> targetVo = new ArrayList<>();
            for (TargetPerson targetPerson : request.getTargets()) {
                TargetVO targetVO = new TargetVO();
                targetVO.setAnalysis("🌸 桃花运势分析报告：\n近期将有重要邂逅机会");
                targetVO.setIndex(4);
                targetVO.setLuckyDirection("东南方");
                targetVO.setAdvice(List.of("佩戴粉水晶", "参加社交活动"));
                targetVO.setTargetName(targetPerson.getTargetName());
                targetVo.add(targetVO);
            }
            vo.setTargetVo(targetVo);
        }

        return ResponseEntity.ok(vo);
    }
}