// RespBean.java
package com.example.entity.base;

import lombok.Data;

@Data
public class RespBean<T> {
    private int code;
    private String message;
    private T data;

    public static <T> RespBean<T> success(T data) {
        RespBean<T> result = new RespBean<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    public static <T> RespBean<T> failure(int code, String message) {
        RespBean<T> result = new RespBean<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
