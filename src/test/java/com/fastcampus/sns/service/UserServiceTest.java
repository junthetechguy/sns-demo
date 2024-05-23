package com.fastcampus.sns.service;

import com.fastcampus.sns.exception.ErrorCode;
import com.fastcampus.sns.exception.SnsApplicationException;
import com.fastcampus.sns.fixture.UserEntityFixture;
import com.fastcampus.sns.model.entity.UserEntity;
import com.fastcampus.sns.repository.UserEntityRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
public class UserServiceTest {
    @Autowired
    private UserService userService; // 이 Test 코드에서 Test할 것만(가령, 여기서는 해당되는 Service인 UserService만) Autowired로 받아오고, 나머지는 전부 다 MockBean으로 만들어줘서 이미 Bean으로 올라와있는 것을 Mocking을 해주자.
    @MockBean
    private UserEntityRepository userEntityRepository;
    @MockBean
    private BCryptPasswordEncoder encoder;

    @Test
    void 회원가입이_정상적으로_동작하는_경우() {
        String userName = "userName"
        String password = "password";

        // join이 정상적으로 진행될때 순차적으로 수행되는 모든 동작들을 순서대로 mocking
        when(userEntityRepository.findByUserName(userName)).thenReturn(Optional.empty()); // 유저가 존재하지 않아야 하므로 empty로 반환이 되어야 함
        when(encoder.encode(password)).thenReturn("encrpt_password"); // String type으로 반환된다.
        when(userEntityRepository.save(any())).thenReturn(UserEntityFixture.get(userName, password, 1)); // any()는 그냥 바로 무조건 실행이 되는 것으로 일단 save가 되면 반드시 이 UserEntity class type이 나와야 한다.

        Assertions.assertDoesNotThrow(() -> userService.join(userName, password)); // 정상적으로 join을 했을때 아무런 error도 throw되면 안되도록 설정한다.
    }

    @Test
    void 회원가입시_userName으로_회원가입한_유저가_이미_있는경우() {
        String userName = "userName";
        String password = "password";
        UserEntity fixture = UserEntityFixture.get(userName, password, 1);

        when(userEntityRepository.findByUserName(userName)).thenReturn(Optional.of(fixture)); // 유저가 존재하므로 이미 만들어진 UserEntity type의 Object가 반환된다.
        when(encoder.encode(password)).thenReturn("encrpt_password");
        when(userEntityRepository.save(any())).thenReturn(Optional.of(fixture));

        SnsApplicationException e = Assertions.assertThrows(SnsApplicationException.class, () -> userService.join(userName, password)); // 반드시 이 경우의 join은 SnsApplicationClass라는 Object type을 반환해야함.
        Assertions.assertEquals(ErrorCode.DUPLICATED_USER_NAME, e.getErrorCode());
    }


    @Test
    void 로그인이_정상적으로_동작하는_경우() {
        String userName = "userName";
        String password = "password";

        UserEntity fixture = UserEntityFixture.get(userName, password, 1);

        // 로그인을 하는 부분에서는 password를 비교하는 부분이 있는데 그냥 이것도 비교하는 logic을 만들면 귀찮으므로 test용 UserEntity인  fixture를 하나 만들어두자.
        when(userEntityRepository.findByUserName(userName)).thenReturn(Optional.of(fixture)); // 로그인 했을때는 반드시 UserEntity Type의 Object가 찾아져야함.
        when(encoder.matches(password, fixture.getPassword())).thenReturn(true);

        Assertions.assertDoesNotThrow(() -> userService.login(userName, password));
    }

    @Test
    void 로그인시_userName으로_회원가입한_유저가_없는_경우() {
        String userName = "userName";
        String password = "password";

        when(userEntityRepository.findByUserName(userName)).thenReturn(Optional.empty());

        SnsApplicationException e = Assertions.assertThrows(SnsApplicationException.class, () -> userService.login(userName, password));
        Assertions.assertEquals(ErrorCode.USER_NOT_FOUND, e.getErrorCode());

    }

    @Test
    void 로그인시_패스워드가_틀린_경우() {
        String userName = "userName";
        String password = "password";
        String wrongPassword = "wrongPassword"; // 즉석에서 테스트 용으로 만든 새로운 fixture
        UserEntity fixture = UserEntityFixture.get(userName, password, 1);

        when(userEntityRepository.findByUserName(userName)).thenReturn(Optional.of(fixture));

        SnsApplicationException e = Assertions.assertThrows(SnsApplicationException.class, () -> userService.login(userName, wrongPassword));
        Assertions.assertEquals(ErrorCode.INVALID_PASSWORD, e.getErrorCode());
    }



}
