package com.countingstars.idempotency.exception;

import com.countingstars.idempotency.dto.BaseRespDTO;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ControllerAdvice
public class GlobalControllerExceptionHandler {

    @ExceptionHandler(IdempotentException.class)
    public BaseRespDTO handleIdempotentException(IdempotentException e) {
        return new BaseRespDTO(e);
    }

}
