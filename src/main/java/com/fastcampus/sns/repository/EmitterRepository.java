package com.fastcampus.sns.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
public class EmitterRepository {
    // Sse instance 자체를 저장을 해야 해당 instance에다 connect를 해주므로 redis나 DB를 별도로 쓰지말고 local cache를 만들어서 사용하자.

    private Map<String, SseEmitter> emitterMap = new HashMap<>();

    public SseEmitter save(Integer userId, SseEmitter sseEmitter) { // alarm이 발생했을 때 이 alarm을 받는 user의 Id로 그 user가 접속한 web browser를 찾아야 하기 때문에 userId도 함께 받아주자.
        final String key = getKey(userId); // 기본적인 구조는 in-memory cache인 redis와 유사하게 가자.
        emitterMap.put(key, sseEmitter);
        log.info("Set sseEmitter {}", userId);
        return sseEmitter;
    }

    public Optional<SseEmitter> get(Integer userId) {
        final String key = getKey(userId);
        log.info("Get sseEmitter {}", userId);
        return Optional.ofNullable(emitterMap.get(key));
    }

    public void delete(Integer userId) {
        emitterMap.remove(getKey(userId));
    }

    private String getKey(Integer userId) {
        return "Emitter:UID:" + userId;
    }

    // 이 정도로 해서 local cache로 instance를 저장을 했는데 redis랑 local cache를 비교할때 처럼 redis는 여러개의 서버 instance가 있을때도 그 데이터를 공유할 수 있는데
    // local cache는 그게 안되므로 가령 서버가 2대인데 user1이 접속했을때 사용한 서버가 서버1이고, user2가 접속했을때 사용한 서버가 서버2라고 가정해보면
    // user1에게 알람이 가야 하는데 만약 알람이 생성된 곳이 서버2였다면 서버2에 connect된 user1의 web browser가 없으므로 보내줄 수 없게 되므로
    // 이처럼 다중 서버 instance인 경우에는 이 local cache 위에다가 추가로 messaging을 더 둬서 "user1에게 알람을 보내야해"를 전체 서버 instance에게 알려준 후
    // 이 instance들 중에서 user1에 커넥트되어 있는 서버 instance가 보내주는 형태로 구현이 되어야 한다. 헌데 지금 나는 인스턴스가 하나이므로 그냥 간단하게 그런거 없이 구현을 해주자.

}
