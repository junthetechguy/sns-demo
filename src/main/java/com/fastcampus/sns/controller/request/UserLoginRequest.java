package com.fastcampus.sns.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserLoginRequest {
    private String name; // 원래는 userName이었지만 Frontend code에서 name이라고 세팅이 되어 있어서 name으로 바꾼거임
    private String password;

}
