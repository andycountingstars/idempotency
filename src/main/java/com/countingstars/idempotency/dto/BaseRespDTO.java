package com.countingstars.idempotency.dto;

import com.countingstars.idempotency.exception.IdempotentException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseRespDTO {
    private String code;
    private String message;
    private Object content;

    public BaseRespDTO(IdempotentException e) {
        this.code = e.getCode();
        this.message = e.getMessage();
    }
}
