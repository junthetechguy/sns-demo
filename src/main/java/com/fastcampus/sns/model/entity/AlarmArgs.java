package com.fastcampus.sns.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor // Cannot construct instance of error가 뜨므로 얘는 특별히 NoArgsConstructor까지 만들어주자.
public class AlarmArgs {

    // 알람을 발생시킨 사람
    private Integer fromUserId;

    // 알람이 발생된 주체(post, 댓글, 스토리 등)에 대한 id
    private Integer targetId;
}
