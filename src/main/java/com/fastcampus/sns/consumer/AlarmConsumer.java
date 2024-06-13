package com.fastcampus.sns.consumer;

import com.fastcampus.sns.model.event.AlarmEvent;
import com.fastcampus.sns.service.AlarmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component // Bean으로 띄워야 하므로 Annotation을 달아줘야 하는데 적당히 매칭되는 @Repository나 @Service 같은 Annotation이 없으니 이럴 경우에 사용하는 @Component로 Annotation을 달아준다.
@RequiredArgsConstructor
public class AlarmConsumer {

    private final AlarmService alarmService;

    @KafkaListener(topics = "${spring.kafka.topic.alarm}") // 얘가 Consumer인 것을 표기하기 위해서 @KafkaListner(consuming하는 topic)를 달아주자.
    public void consumeNotification(AlarmEvent event, Acknowledgment ack) {
        log.info("Consume the event {}", event);
        alarmService.send(event.getAlarmType(), event.getArgs(), event.getReceiveUserId()); // PostService에서 alarm DB에 저장하고 sse send 하던 부분을 여기 consumer 단에서 처리해주자.
        ack.acknowledge(); // consuming 한 후 DB에 저장하고 sse send 해준 후에는 ack를 날려주자.
    }
}