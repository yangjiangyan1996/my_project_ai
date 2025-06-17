package com.example.entity.resp;

import lombok.Data;

@Data
public class NameResponse {
    private String name;
    private String explanation;

    public NameResponse(String name, String explanation) {
        this.name = name;
        this.explanation = explanation;
    }
}