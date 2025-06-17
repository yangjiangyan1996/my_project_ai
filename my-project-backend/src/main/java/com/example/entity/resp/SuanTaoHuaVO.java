package com.example.entity.resp;

import lombok.Data;
import java.util.List;

@Data
public class SuanTaoHuaVO {
    // 运势分析文本（支持换行符）
    private String analysis;
    
    // 桃花指数（1-5星）
    private Integer index;
    
    // 幸运方位
    private String luckyDirection;
    
    // 建议列表
    private List<String> advice;
}