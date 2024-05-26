package com.fastcampus.sns.producer;

import com.fast.campus.simplesns.model.AlarmEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component // Bean으로 띄워야 하므로 Annotation을 달아줘야 하는데 적당히 매칭되는 @Repository나 @Service 같은 Annotation이 없으니 이럴 경우에 사용하는 @Component으로 Annotation을 달아준다.
@RequiredArgsConstructor
public class AlarmProducer {
    // kafka cli를 만들어주는 library로 key(가령, userId라는 Integer)-value(가령, AlarmEvent라는 Object) 형식으로 데이터를 저장한다.
    // Bean으로 자동으로 띄워진 것을 받아오자.
    private final KafkaTemplate<Integer, AlarmEvent> alarmEventKafkaTemplate;

    @Value("${spring.kafka.topic.alarm}") // 내가 application.yaml에 설정해둔 내가 kafka로 만들 event의 topic은 "alarm"이다.
    private String topic;

    public void send(AlarmEvent event) {
        alarmEventKafkaTemplate.send(topic, event.getReceiverUserId(), event);
        log.info("Send to Kafka finished");
    }
}
