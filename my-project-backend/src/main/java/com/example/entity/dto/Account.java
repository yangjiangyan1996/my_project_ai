package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.BaseData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 数据库中的用户信息
 */
@Data
@TableName("account")
@NoArgsConstructor
@AllArgsConstructor
public class Account implements BaseData {
    @TableId(type = IdType.AUTO)
    Long id;
    String username;
    String password;
    String nickname;
    Integer sex;
    String avatarUrl;
    String email;
    String phone;
    String role;
    Date registerTime;
    String province;
    String city;
    String county;
}
