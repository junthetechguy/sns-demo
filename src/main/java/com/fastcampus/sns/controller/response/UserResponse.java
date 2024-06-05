package com.fastcampus.sns.controller.response;

import com.fastcampus.sns.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    // 정상적인 수행이 완료되었을때 서비스 단에서 실제로 응답이 내려가는 클래스를 만들기 위해서 controller에 response package를 만들어 준 후 각각의 동작에 해당하는 Response들을 만들어준다. 가령, 회원가입이 수행되었을때는 Response를 내려주기 위해서 UserJoinResponse를 만들어주자.

    private Integer id;
    private String userName;

    public static UserResponse fromUser(User user) {
        return new UserResponse(
                user.getId(),
                user.getUserName()
        );
    }
}
