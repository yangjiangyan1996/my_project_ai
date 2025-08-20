package com.example.entity.base;

import lombok.Data;

@Data
public class ProjectPreviewDTO {
    private String name;
    private String description;
    private String steps;
    private String tools;
    private String riskWarning;
    private String tags;
    private Integer difficulty;
    private String imageUrl;
    private Long estimatedIncomeMin;
    private Long estimatedIncomeMax;
}