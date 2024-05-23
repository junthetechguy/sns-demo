package com.fastcampus.sns.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Response<T> { // 또한 이 class에서 회원가입 시에는 UserJoinResponse가 내려가겠지만 다른 동작을 할때는 다른 형태의 response가 내려가므로 Generic type으로 선언해주자.
    private String resultCode;
    private T result;

    public static Response<Void> error(String errorCode) {
        return new Response<>(errorCode, null);
    }

    public static Response<Void> success() { // result가 없는 Response
        return new Response<Void>("SUCCESS", null);
    }

    public static <T> Response<T> success(T result) {
        return new Response<>("SUCCESS", result);
    }

    public String toStream() { // Response를 이쁘게 만들어주는 코드
        if(result == null) {
            return "{" +
                    "\"resultCode\":" + "\"" + resultCode + "\"," +
                    "\"result\":" + null + "}";
        }
        return "{" +
                "\"resultCode\":" + "\"" + resultCode + "\"," +
                "\"result\":" + "\"" + result + "\"" + "}";

    }
}
