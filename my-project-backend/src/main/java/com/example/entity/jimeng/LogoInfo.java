package com.example.entity.jimeng;

import lombok.Getter;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/25 22:24
 */
@Getter
public class LogoInfo {
    private Boolean add_logo;
    private Integer position;
    private Integer language;
    private Double opacity;
    private String logo_text_content;

    public LogoInfo() {
        this.add_logo = true;
        this.position = 0;
        this.language = 0;
        this.opacity = 0.5;
        this.logo_text_content = "平耀聚业通";
    }
}
