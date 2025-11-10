package com.example.enums;

public enum CkOperationType {
    
    CREATE("创建"),
    UPDATE("更新"),
    DELETE("删除"),
    QUERY("查询"),
    IMPORT("导入"),
    EXPORT("导出"),
    LOGIN("登录"),
    LOGOUT("登出"),
    APPROVE("审批"),
    REJECT("驳回"),
    CANCEL("取消"),
    OTHER("其他");
    
    private final String description;
    
    CkOperationType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}