package com.fastcampus.sns.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SnsApplicationException extends RuntimeException{
    private ErrorCode errorCode;
    private String message;

    public SnsApplicationException(ErrorCode errorCode) { // message가 null이고, errorCode만 존재할 경우의 SnsApplicaitonException
        this.errorCode = errorCode;
        this.message = null;
    }

    @Override // RuntimeException class에 존재하는 getMessage() method override
    public String getMessage() {
        if (message == null) {
            return errorCode.getMessage();
        }
        return String.format("%s, %s", errorCode.getMessage(), message);
    }
}
