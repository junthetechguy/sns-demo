package com.fastcampus.sns.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 개념 설명
// -Serialize = JSON 데이터 <- Java 객체로 변환
// -Deserialize = Json 데이터 -> Java 객체
// 이 두 작업을 Jackson library가 진행한다.

@Getter
@AllArgsConstructor
// 결국에 이 AllArgsConstructor가 Reqeust Body에 들어오는 Json 정보를 가지고 Serialize 해서 Java Object로 만들어준다.
@NoArgsConstructor // Cannot construct instance of error가 뜨므로 그냥 모든 request와 response는 특별히 NoArgsConstructor까지 만들어주자.
public class PostCommentRequest {
    private String comment;
}
