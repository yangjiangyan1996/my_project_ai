package com.example.entity.req;

import lombok.Data;

@Data
public class UserInput {
    private String lastName;
    private String gender;
    private int birthYear;
    private int birthMonth;
    private int birthDay;
    private int birthHour;
    private String additionalInfo;
}