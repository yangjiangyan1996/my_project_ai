package com.example.entity.resp;

import lombok.Data;

import java.util.Map;

@Data
public class NameResponse {
    private String name;
    private String meaning;
    private Double score;
    Map<String, String> wuxing;

    public NameResponse(String name, String meaning, Double score,Map<String, String> wuxing) {
        this.name = name;
        this.score = score;
        this.meaning = meaning;
        this.wuxing = wuxing;
    }
}