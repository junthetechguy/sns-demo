package com.fastcampus.sns.controller.response;

import com.fastcampus.sns.model.User;
import com.fastcampus.sns.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter // 항상 request와 response는 @Getter, @AllArgsConstructor, @NoArgsConstructor를 만들어주자.
@AllArgsConstructor
@NoArgsConstructor
public class UserJoinResponse {
    private Integer id;
    private String userName;
    private UserRole role;

    public static UserJoinResponse fromUser(User user) { // static으로 선언해줘서 다른 곳에서 이 method를 바로 가져다 사용하도록 해주자.
        return new UserJoinResponse(
                user.getId(), // 회원가입이 성공했을때 실제 그 User가 몇 번째 User인지 내부에서 관리하는 UserId를 같이 반환해주면 이 사람이 정상적으로 회원가입했는지 관리하기 더 쉬워지게 된다.
                user.getUserName(),
                user.getUserRole()
        );
    }

}
