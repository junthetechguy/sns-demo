package com.fastcampus.sns.service;

import com.fastcampus.sns.exception.ErrorCode;
import com.fastcampus.sns.exception.SnsApplicationException;
import com.fastcampus.sns.repository.EmitterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor // 이 Annotation이 있어야 private final로 Bean을 가져와서 만들어줄 수 있는 것이다.
public class AlarmService {

    private final static Long DEFAULT_TIMEOUT = 60L * 1000 * 60;
    private final static String ALARM_NAME = "alarm"; // frontend에서 subscribe를 걸어둔게 alarm이라는 이름의 Event이므로 그 이름을 맞춰줘야한다.
    private final EmitterRepository emitterRepository;

    public void send(Integer alarmId, Integer userId) {
        // Browser connect당 하나의 인스턴스로 생기게 되는 SseEmitter들 중에 해당 브라우저에 해당되는 SseEmitter를 직접 찾아서 event 정보를 보내줘야 한다.
        // 따라서 이 SseEmitter(=결국 이게 Sse)를 저장할 class인 emitterRepository를 만들어주자.
        emitterRepository.get(userId).ifPresentOrElse(sseEmitter -> { // userId로 저장을 했으므로 userId로 가져와주자.
            try {
                sseEmitter.send(SseEmitter.event().id(alarmId.toString()).name(ALARM_NAME).data("new alarm")); // 프론트엔드로 alarm event를 보내주자.
            } catch (IOException e) {
                emitterRepository.delete(userId); // error가 발생하면 지워주자.
                throw new SnsApplicationException(ErrorCode.ALARM_CONNECT_ERROR);
            }
        }, () -> log.info("No emitter found.")); // SseEmitter가 null일 경우에는 유저가 아직 Web Browser로 접속해있지 않았을 수도 있으므로 그냥 error로 처리하지말고 log로 한번 찍어주자.
    }

    // 이 SseEmitter의 정체는 결국에는 Browser connect당 하나의 인스턴스로 생기게 된다.
    public SseEmitter connectAlarm(Integer userId) { // 맨 처음에 connection이 맺어지면 connect completed라는 event를 보내주자.
        SseEmitter sseEmitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitterRepository.save(userId, sseEmitter);

        // complete나 timeout 일 경우에는 다시 재접속하면 되므로 일단 local cache에서 지워주자.
        sseEmitter.onCompletion(() -> emitterRepository.delete(userId));
        sseEmitter.onTimeout(() -> emitterRepository.delete(userId));

        try { // 지금은 데이터 자체를 분석해서 하는게 아니라 그냥 이벤트가 오면 api를 한번 refresh하는 형태이므로 별다른 로직 없이 이렇게만 해주자.
            sseEmitter.send(SseEmitter.event()  // send를 통해서 event를 프론트엔드로 전송하자.
                    .id("") // id는 이게 몇번째 event인지를 id를 발급해서 알 수가 있는데 만약에 가장 최근에 받은 id를 가지고 있다가 그 다음에 온 id까지 데이터를 쭉 가져온다.
                    // 가령 Web Browser로 데이터가 가다가 어떤 이유로 전송이 안될 수도 있는데 서버에서는 내가 마지막으로 전송했던 것 까지 딱 저장을 하고 있다가 이후에 발생한 이벤트를 모두 다 옮겨줄 수 잇는 형태로
                    // id를 관리해줄 수 있으므로 그냥 지금은 connection이 맺어진거에 대해서 connect completed라는 event만 보내주면 되므로 그냥 empty string으로 한다.
                    .name(ALARM_NAME) // frontend에서 subscribe를 걸어둔게 alarm이라는 이름의 Event이므로 그 이름을 맞춰줘야한다.
                    .data("connect completed"));
        }catch (IOException exception) {
            throw new SnsApplicationException(ErrorCode.ALARM_CONNECT_ERROR);
        }

        return sseEmitter;
    }
}
