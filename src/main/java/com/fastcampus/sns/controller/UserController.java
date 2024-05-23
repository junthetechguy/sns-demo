package com.fastcampus.sns.controller;

import com.fastcampus.sns.controller.request.UserJoinRequest;
import com.fastcampus.sns.controller.request.UserLoginRequest;
import com.fastcampus.sns.controller.response.AlarmResponse;
import com.fastcampus.sns.controller.response.Response;
import com.fastcampus.sns.controller.response.UserJoinResponse;
import com.fastcampus.sns.controller.response.UserLoginResponse;
import com.fastcampus.sns.exception.ErrorCode;
import com.fastcampus.sns.exception.SnsApplicationException;
import com.fastcampus.sns.model.User;
import com.fastcampus.sns.service.AlarmService;
import com.fastcampus.sns.service.UserService;
import com.fastcampus.sns.util.ClassUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController // controller이므로 @RestController 넣어준다
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor // @AllArgsConstructor는 Class의 모든 field를 매개변수로 받는 생성자를 생성하지만
// @RequiredArgsConstructor는 반드시 초기화되어야 하는 필드만(final로 선언된 필드나 @NonNull) 만을 매개변수로 사용하는 생성자를 생성
public class UserController {

    private final UserService userService; // 항상 Service 부분은 final로 설정해두는게 관례이다.
    private final AlarmService alarmService;

    @PostMapping("/join")
    public Response<UserJoinResponse> join(@RequestBody UserJoinRequest request) {
        User user = userService.join(request.getName(), request.getPassword());
        return Response.success(UserJoinResponse.fromUser(user));
    }

    @PostMapping("/login")
    public Response<UserLoginResponse> login(@RequestBody UserLoginRequest request) {
        String token = userService.login(request.getName(), request.getPassword());
        return Response.success(new UserLoginResponse(token));
    }

    @GetMapping("/alarm")
    public Response<Page<AlarmResponse>> alarm(Pageable pageable, Authentication authentication) {
        User user = ClassUtils.getSafeCastInstance(authentication.getPrincipal(), User.class).orElseThrow(
                () -> new SnsApplicationException(ErrorCode.INTERNAL_SERVER_ERROR,
                        "Casting to User class failed"));
        return Response.success(userService.alarmList(user.getId(), pageable).map(AlarmResponse::fromAlarm));
    }

    @GetMapping("/alarm/subscribe")
    public SseEmitter subscribe(Authentication authentication) {
        User user = ClassUtils.getSafeCastInstance(authentication.getPrincipal(), User.class).orElseThrow(
                () -> new SnsApplicationException(ErrorCode.INTERNAL_SERVER_ERROR,
                        "Casting to User class failed"));
        return alarmService.connectAlarm(user.getId());
    }
}