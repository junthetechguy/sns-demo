package com.fastcampus.sns.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserJoinRequest { // 회원가입시 보낼 Reqeust의 Body 부분에 들어갈 정보
    private String name;
    private String password;
}
