package com.fastcampus.sns.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AlarmArgs {

    // 알람을 발생시킨 사람
    private Integer fromUserId;

    // 알람이 발생된 주체(post, 댓글, 스토리 등)에 대한 id
    private Integer targetId;

}
