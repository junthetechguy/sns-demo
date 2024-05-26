package com.fastcampus.sns.model.event;

import com.fastcampus.sns.model.AlarmArgs;
import com.fastcampus.sns.model.entity.AlarmType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor // Cannot construct instance of error가 뜨므로 얘는 특별히 NoArgsConstructor까지 만들어주자.
public class AlarmEvent { // alarm을 save할때 필요했던 모든 정보들(AlarmEntity의 field)을 다 넣어준다.
    private Integer receiveUserId; // Key로 사용할건데 AlarmEntity의 field에는 UserEntity로 설정이 되어 있지만 UserEntity를 그대로 넣을 수는 없으니 Integer로 알람을 받는(receiver)의 UserId를 넣어준다.
    private AlarmType alarmType;
    private AlarmArgs args;
}
