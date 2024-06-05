package com.fastcampus.sns.model.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter // enum은 언제나 Getter를 넣어주는게 필수다.
@RequiredArgsConstructor // enum에서 필드를 따로 만드는 경우에는 다 private final이니까 무조건 RequiredArgsConstructor로 생성자를 만든다.
public enum AlarmType {
    NEW_COMMENT_ON_POST("new comment!"),
    NEW_LIKE_ON_POST("new like!"),
    ;

    private final String alarmText;
}
// 그리고 이 alarm Type에 따라서 Text는 계속 변할 수 있으므로 이 Text 자체를 DB에 저장하게 되면
// Text가 변화할 경우(어느 post에 text가 달렸습니다 식으로 혹은 어느 누가 코멘트를 달았습니다 식으로)
// DB에 text 자체를 저장하는 것은 계속 DB에 저장된 데이터를 바꿔줘야 하므로 비효율적이므로 이걸 이런 식으로 서버에서 저장 및 관리할 수 있게 해주자.
// 아니면 클라이언트에서도 관리할수도 있다. 가령 AlarmType이 A인 경우 이런식으로 띄워주세요, B인경우 저렇게 띄워주세요라고 할 수도 잇는데 이게 웹 앱인 경우에는 상관이 없지만
// 모바일 앱인 경우 버전이 업데이트 되어야(ex. yes24 업데이트) 그게 변할 수 있어서 이런식으로 변화가능성이 있는 것들은 서버 자체에서(DB에서말고 서버 그 자체에서) 관리해주자.