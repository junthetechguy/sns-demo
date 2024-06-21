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

    private final UserService userService; // private final로 bean(controller에서 받아올 bean은 Service밖에 없음)을 받아온다.
    private final AlarmService alarmService;

    // request call 자체는 controller package에서 받아오고,
    // 그 내부 동작은 service package에서 이루어진다.
    @PostMapping("/join")
    public Response<UserJoinResponse> join(@RequestBody UserJoinRequest request) {
        User user = userService.join(request.getName(), request.getPassword());
        return Response.success(UserJoinResponse.fromUser(user));
        /*
        그냥 바로 그대로 UserJoinResponse로 반환해줄수도 있지만 response를 내려줄때 성공하는 경우가 있고, 실패하는 경우가 있는데
        이때마다 각각 response 값이 제각각이면 이 API를 가져다 쓰는 프론트엔드쪽에서 이 response를 parsing하기가 굉장히 어려우므로
        획일화된 template 형태의 response template을 만들어주고 내가 내려줄 response의 형태에 따라서
        해당되는 response를 넣어주기 위해서 response package에 Response class를 만들어주자.
        */
    }

    @PostMapping("/login") // health check 경로는 이 /login PATH로 한다 => 언제나 살아있고 항상 모든 Source로 부터 이 /login API PATH는 열려있기 때문이다.
    public Response<UserLoginResponse> login(@RequestBody UserLoginRequest request) {
        String token = userService.login(request.getName(), request.getPassword());
        return Response.success(new UserLoginResponse(token));
    }

    @GetMapping("/alarm")
    public Response<Page<AlarmResponse>> alarm(Pageable pageable, Authentication authentication) {

        // 아래와 같이 userName으로 주게 되면 이 userName으로 service 단에서 해당 userEntity가 있는지 없는지 검사 후 userEntity를 가져온 다음
        // 해당 userEntity로 alarm을 찾을때 실제로는 alarm table에서
        // 즉, AlarmEntity에서 user column이 실제로는 ManyToOne으로 JoinColumn이 user_id로 되어 잇으므로 이 말인 즉슨 실제 이 alarm table에는 user_id로 들어가 있어서
        // 사실 이 userEntity로 alarm을 찾을때는 그냥 user_id로 한번에 줘서 찾도록 하면 service 단에서 해당 userEntity가 있는지 없는지 검사하는 코드가 사라지게 된다.
        // 차라리 굳이 그럴 필요없이 JwtTokenFilter 부분에서 이미 SetAuthentication으로 authentication으로 넣어줄때부터 이미 pricipal로 user를 넣어주게 되므로
        // 그냥 User user = (User).authentication.getPricipal()의 느낌으로 여기서 User를 뽑아낸 다음에 이 User의 user_id를 service 단으로 내려보내자
        //
        // return Response.success(userService.alarmList(user.getName(), pageable).map(AlarmResponse::fromAlarm));

        // User user = (User).authentication.getPricipal()는 에러를 좀더 세이프하게 캐스팅을 하도록 아래와 같이 해주자.
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
