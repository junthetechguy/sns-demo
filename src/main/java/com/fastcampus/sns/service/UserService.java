package com.fastcampus.sns.service;

import com.fastcampus.sns.exception.ErrorCode;
import com.fastcampus.sns.exception.SnsApplicationException;
import com.fastcampus.sns.model.Alarm;
import com.fastcampus.sns.model.User;
import com.fastcampus.sns.model.entity.UserEntity;
import com.fastcampus.sns.repository.AlarmEntityRepository;
import com.fastcampus.sns.repository.UserCacheRepository;
import com.fastcampus.sns.repository.UserEntityRepository;
import com.fastcampus.sns.util.JwtTokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service // Service임을 명시하는 Annotation
@RequiredArgsConstructor
public class UserService {
    // Bean으로 띄워져 있는 것을 @Autowired Annotation으로 받아오지 않아도
    // 그냥 바로 이렇게 private final로 받아올 수 있으므로 Service 단에서 사용하는 모든 bean들은 그냥 private final로 받아오자.
    private final UserEntityRepository userEntityRepository;
    private final AlarmEntityRepository alarmEntityRepository;
    private final BCryptPasswordEncoder encoder;
    private final UserCacheRepository userCacheRepository;

    // key와 expired time은 hard coding하지 않고, soft coding으로 application.yaml에 configuration을 설정 후 받아오는 형태로 넣어주자.
    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.token.expired-time-ms}")
    private Long expiredTimeMs;

    @Transactional // DB Transaction 연산을 할때(가령, 회원가입시) 오류가 나게 되면
    // 애초에 저장이 되면 안되는데(가령, 유저가 저장이 되면 안됨) @Transactional을 달아줌으로서 이런식으로 exception 발생시 자동으로 save한 것을 rollback이 되도록한다.
    // 이 @Transactional이 없으면 회원가입시 오류가 났을때 회원가입이 제대로 처리가 되면(save가 되면 안됨) 안되는데 회원 가입이 처리가 되어 버림(save가 되어버림)
    public User join(String userName, String password) {
        // 회원가입하려는 userName으로 회원가입된 user가 있는지
        userEntityRepository.findByUserName(userName).ifPresent(it -> {
            throw new SnsApplicationException(ErrorCode.DUPLICATED_USER_NAME, String.format("%s is duplicated", userName));
        });

        // join 진행 = user를 등록
        UserEntity userEntity = userEntityRepository.save(UserEntity.of(userName, encoder.encode(password)));
        return User.fromEntity(userEntity);
    }

    public String login(String userName, String password) {
        // 회원가입 여부 체크
        User user = loadUserByUserName(userName);
        userCacheRepository.setUser(user); // Redis에 Immutable Data인 user를 담아주기


        // 비밀번호 체크
        if(!encoder.matches(password, user.getPassword())) {
            throw new SnsApplicationException(ErrorCode.INVALID_PASSWORD);
        }

        // 토큰 생성
        String token = JwtTokenUtils.generateToken(userName, secretKey, expiredTimeMs);

        return token;
    }

    public User loadUserByUserName(String userName) {
        // 일단 Redis에서 찾아보고, Redis에 없으면 UserEntityRepository에서 가져오고 이때 User class의 형태로 가져오고, 없으면 Exception을 낸다.
        return userCacheRepository.getUser(userName).orElseGet(() ->
                userEntityRepository.findByUserName(userName).map(User::fromEntity).orElseThrow(() ->
                        new SnsApplicationException(ErrorCode.USER_NOT_FOUND, String.format("%s not found", userName)))
        );
    }


    public Page<Alarm> alarmList(Integer userId, Pageable pageable) {
        // 이 method에서 param으로 userName을 받을 경우
        // UserEntity userEntity = userEntityRepository.findByUserName(userName).orElseThrow(()-> new SnsApplicationException(ErrorCode.USER_NOT_FOUND));
        // return alarmEntityRepository.findAllByUser(userEntity, pageable).map(Alarm::fromEntity);

        // 위와 같이 userName으로 받아서 이 userName으로 service 단에서 해당 userEntity가 있는지 없는지 검사 후 userEntity를 가져온 다음 해당 userEntity로 alarm을 찾을때 실제로는 alarm table에서
        // 즉, AlarmEntity에서 user column이 실제로는 ManyToOne으로 JoinColumn이 user_id로 되어 잇으므로 이 말인 즉슨 실제 이 alarm table에는 user_id로 들어가 있어서
        // 사실 이 userEntity로 alarm을 찾을때는 그냥 user_id로 한번에 줘서 찾도록 하면 service 단에서 해당 userEntity가 있는지 없는지 검사하는 코드가 사라지게 된다.


        return alarmEntityRepository.findAllByUserId(userId, pageable).map(Alarm::fromEntity);
    }
}
