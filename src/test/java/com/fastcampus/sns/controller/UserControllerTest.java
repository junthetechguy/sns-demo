package com.fastcampus.sns.controller;

import com.fastcampus.sns.controller.request.UserJoinRequest;
import com.fastcampus.sns.controller.request.UserLoginRequest;
import com.fastcampus.sns.exception.ErrorCode;
import com.fastcampus.sns.exception.SnsApplicationException;
import com.fastcampus.sns.model.User;
import com.fastcampus.sns.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest // Test Code는 언제나 @SpringBootTest를 달아준다.
@AutoConfigureMockMvc // API 형태의 테스트(=Controller단 테스트)를 작성하므로 @AutoConfigureMockMvc를 달아준다.
// Service단 테스트는 API 형태의 테스트가 아니고 그냥 비즈니스 로직만 돌아가는 부분이므로 @AutoConfigureMockMvc를 달지 않는다
public class UserControllerTest {

    @Autowired // 미리 Framework단에서 만들어진 MockMvc Bean을 가져다 꽂는 것.
    private MockMvc mockMvc; // 이 Test 코드에서 Test할 것만(가령, Controller Test에서는 MockMvc, ObjectMapper 만) Autowired로 받아오고, 나머지는 전부 다 MockBean으로 받아오자.

    @Autowired // 미리 Framework단에서 만들어진 ObjectMapper Bean을 가져다 꽂는 것.
    private ObjectMapper objectMapper;

    @MockBean // when->thenReturn 부분에서 mocking을 위해서 가져옴
    private UserService userService;

    @Test
    void 회원가입() throws Exception { // 항상 controller test code는 throws Exception을 해야하고, service test code는 안해도 되지만 항상 둘 다 void의 형태이다.
        // 요구사항 분석에서 분석해놓은 회원가입시 필요한 정보 2가지
        String userName = "userName";
        String password = "password";

        // join이 정상적으로 진행될때 순차적으로 수행되는 모든 동작을(여기선 유일하게 join service logic이 들어가는 것 뿐) 순서대로 mocking
        // mocking하는 부분 : Service logic이 돌아가는 부분으로 when->thenReturn의 구조로 진행.
        when(userService.join(userName, password)).thenReturn(mock(User.class));
        // 정상적으로 동작이 되어야하므로 User.class가 반환이 되어야 함. 또한 when->thenReturn의 경우 Object Type만 본다. 가령, 같은 클래스인지 등을 파악한다.

        // 언제나 Controller 단 test code는 먼저 service 동작을 when->then으로 mocking scenario를 설정 후 mockMvc.perform으로 API test를 해주자.

        mockMvc.perform(post("/api/v1/users/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(new UserJoinRequest(userName, password)))
                // userName과 password를 가지고 Reqeust를 만든 후 Json의 body 부분에 objectMapper로 넣어준다.
                ).andDo(print()) // 결과 한번 프린트.
                .andExpect(status().isOk()); // 정상적으로 동작해야하는 Test이므로 Ok로 status가 떨어져야함.
    }

    @Test
    void 회원가입시_이미_회원가입된_userName으로_회원가입을_하는경우_에러반환() throws Exception{
        String userName = "userName";
        String password = "password";


        when(userService.join(userName, password)).thenThrow(new SnsApplicationException(ErrorCode.DUPLICATED_USER_NAME));

        mockMvc.perform(post("/api/v1/users/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new UserJoinRequest(userName, password)))
                ).andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    void 로그인() throws Exception {
        String userName = "userName";
        String password = "password";

        // login의 경우 성공하면 그냥 String을 반환하므로 아무런 String을 Return하도록 한다.
        when(userService.login(userName, password)).thenReturn("test_token");

        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new UserLoginRequest(userName, password)))
                ).andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void 로그인시_회원가입이_안된_userName을_입력할경우_에러반환() throws Exception {
        String userName = "userName";
        String password = "password";

        when(userService.login(userName, password)).thenThrow(new SnsApplicationException(ErrorCode.USER_NOT_FOUND));

        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new UserLoginRequest(userName, password)))
                ).andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void 로그인시_틀린_password를_입력할경우_에러반환() throws Exception {
        String userName = "userName";
        String password = "password";

        when(userService.login(userName, password)).thenThrow(new SnsApplicationException(ErrorCode.INVALID_PASSWORD));

        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new UserLoginRequest(userName, password)))
                ).andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void 알람기능() throws Exception { // 알람 기능 같은 경우에는 User 단위로 받으므로 User 단위의 API여야만 하므로 UserController와 UserService에 기능을 만들어줘야 한다.
        when(userService.alarmList(any(), any())).thenReturn(Page.empty()); // alarm도 양이 많을 수 있으므로 반드시 성능 문제로 인해 paging 처리가 필요하므로 Page로 받되 일단 무조건 동작시에는 empty page가 뜨게 된다.
        mockMvc.perform(get("/api/v1/users/alarm")
                    .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    void 알람리스트요청시_로그인하지_않은경우() throws Exception {
        when(userService.alarmList(any(), any())).thenReturn(Page.empty());
        mockMvc.perform(get("/api/v1/users/alarm")
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isUnauthorized());
    }


}
