package com.fastcampus.sns.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SnsApplicationException extends RuntimeException{
    // 실패하는 경우의 reason code를 반환해주기 위해서 이 application service 내에서 사용할 exception을 정의하기 위해서 exception package를 만든 후 그 안에 SnsApplicationException class를 생성해주자
    private ErrorCode errorCode;
    private String message;

    public SnsApplicationException(ErrorCode errorCode) { // message가 null이고, errorCode만 존재할 경우의 SnsApplicaitonException
        this.errorCode = errorCode;
        this.message = null;
    }

    @Override // Message Getter Override 해준 것 => 이유 : message가 null일 수도 있으므로 이 경우를 미리 대비함.
    public String getMessage() {
        if (message == null) {
            return errorCode.getMessage();
        }
        return String.format("%s, %s", errorCode.getMessage(), message);
    }
}
