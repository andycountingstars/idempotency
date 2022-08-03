package com.countingstars.idempotency.exception;

import com.countingstars.idempotency.enums.ExceptionCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class IdempotentException extends RuntimeException {
    private String code;
    private String message;

    public IdempotentException(ExceptionCodeEnum codeEnum) {
        this(codeEnum.getCode(), codeEnum.getMessage());
    }
}
