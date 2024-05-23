package com.fastcampus.sns.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserJoinRequest { // 회원가입시 보낼 Reqeust의 Body 부분에 들어갈 정보
    private String name; // 원래는 userName이었지만 Frontend code에서 name이라고 세팅이 되어 있어서 name으로 바꾼거임
    private String password;
}
