package com.example.entity.resp;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class MemberListResp {
    private List<MemberInfo> members;
    private String currentUserRole;

    @Data
    public static class MemberInfo {
        private Long id;
        private String avatarUrl;
        private String nickname;
        private String username;
        private Integer roleOfMemberGroupCode;
        private String roleOfMemberGroup;
        private String email;
        private String province;
        private String city;
        private Date createdAt;
    }
}
