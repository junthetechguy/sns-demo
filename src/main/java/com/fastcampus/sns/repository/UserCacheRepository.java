package com.fastcampus.sns.repository;

import com.fastcampus.sns.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserCacheRepository { // Redis에다가 user를 caching하고 이 Redis에서 user를 다시 가져오기 위한 클래스이다.

    private final RedisTemplate<String, User> userRedisTemplate;
    private final static Duration USER_CACHE_TTL = Duration.ofDays(3); // TTL = 3일

    public void setUser(User user) {
        String key = getKey(user.getUserName()); // 결국에 JwtTokenFilter를 탈때 username이 있는지 찾기 때문에(loaduserbyusername) 아싸리 username을 key로 설정해주자.
        log.info("Set User to Redis {} , {}", key, user);
        // Redis는 언제나 TTL을 걸어줘서 더이상 사용하지 않는 유저의 경우에는 영원히 캐시에 남아있는 데이터가 없도록 해서 최대한 유효한 데이터만 저장해서 좀 더 공간을 효율적으로 사용하도록 하자.
        userRedisTemplate.opsForValue().set(key, user, USER_CACHE_TTL); // TTL이 걸려있으므로 굳이 setIfAbsent()(Redis에 해당 데이터가 Absent일 경우에만 Set하는것)를 쓰지 않고 그냥 set()으로 사용해도 된다.
    }

    // Redis에 존재하지 않을 경우에 Null로 반환되므로 nullable 처리를 쉽게 하기 위해서 Optional로 감싸준 다음 서비스 단에서 getUser(이 Redis).orElseGet(DB에서 확인하는 람다식)으로 nullable을 처리해주자.
    // nullable 처리는 이런식으로 간단하게 처리해준다 => 애초에 데이터를 Optional.ofNullable로 가지고 온 후 이걸 받는 쪽에서 ifPresentorElse (람다식)으로 처리해준다.
    public Optional<User> getUser(String userName) {
        String key = getKey(userName); // 결국에 API call이 일어날때 username이 있는지 찾기 때문에(loaduserbyusername) 아싸리 username을 key로 설정해주자.
        User user = userRedisTemplate.opsForValue().get(key);
        log.info("Get data from Redis {} , {}", key, user);
        return Optional.ofNullable(user);
    }

    // Redis는 보통 하나의 Cluster로 만들어두고 거기에다가 서비스에 쓰는 모든 Caching을 다 넣게 된다.
    // 지금 나는 user만 caching하게 되지만 나중에 서비스가 확장된다면 존나게 다른 여러가지 데이터들도 caching을 할 수도 있으므로
    // key 값을 그저 userName으로 하게 되면 이게 어떤 데이터의 key 값인지 알아보기 어려우므로 Redis에 저장할때는 언제나 아래와 같이 prefix(어떤 정보를 저장하는지에 대한 정보)를 붙여준다.
    private String getKey(String userName) {
        return "USER:" + userName;
    }
}
