package com.fastcampus.sns.exception;

import com.fastcampus.sns.controller.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j // Console창에 log를 찍는 Annotation
@RestControllerAdvice // exception handling할때 사용하는 class를 만드는 Annotation
public class GlobalControllerAdvice { // exception을 던졌을때 response에 이 exception을 잡아서 반영한 후 내려줌

    @ExceptionHandler(SnsApplicationException.class) // SnsApplicationException이 뜨게되면 바로 이 Handler가 붙잡아서 실행한다.
    public ResponseEntity<?> applicationHandler(SnsApplicationException e) { // springframework libray에 존재하는 ResponseEntity로 Return한다.
        log.error("Error occurs {}", e.toString());
        return ResponseEntity.status(e.getErrorCode().getStatus())
                .body(Response.error(e.getErrorCode().name())); // enum class의 name을 그대로 전달한다.
    }


    @ExceptionHandler(RuntimeException.class) // 내가 코드단에서 관리하는 exception들인 SnsApplicationException.class 형태의 Exception이 발생했을때만 잡아서 던지는게 아니라 실제로 앱 Runtime시 발생하는 에러들도 모두 다 발생했을때도 잡아서 던질 수 있게 RuntimeException을 잡아서 던지자
    public ResponseEntity<?> applicationHandler(RuntimeException e) {
        log.error("Error occurs {}", e.toString());
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(Response.error(ErrorCode.INTERNAL_SERVER_ERROR.name()));
    }
}
