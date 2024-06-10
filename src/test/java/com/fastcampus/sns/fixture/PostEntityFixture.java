package com.fastcampus.sns.fixture;

import com.fastcampus.sns.model.entity.PostEntity;
import com.fastcampus.sns.model.entity.UserEntity;

public class PostEntityFixture {
    // 이런식으로 Fixture를 따로 만들어둔 이유는 mock(PostEntity.class) 등으로 특정 객체를 mocking할때 그 안에 초기화시킬 값들을 넣는 것이나 그런 동작을 하게 되면 test code가 길어지기 때문에
    // 이런식으로 Fixture를 따로 만들어둬서 진행한다.

    public static PostEntity get(String userName, Integer postId, Integer userId) {
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setUserName(userName);

        PostEntity result = new PostEntity();
        result.setUser(user);
        result.setId(postId);
        return result;

    }
}
