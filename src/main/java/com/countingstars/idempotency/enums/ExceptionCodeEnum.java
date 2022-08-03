package com.countingstars.idempotency.enums;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public enum ExceptionCodeEnum {
    TOKEN_INVALID_EXCEPTION("E00001", "Token invalid"),
    TOKEN_ILLEGAL_EXCEPTION("E00002", "Token illegal"),

    REQUEST_HEADER_BLANK("E10001", "Request header is blank"),
    REPEATED_REQUEST("E10002", "Repeated request"),

    UNKNOWN_EXCEPTION("E99999", "Unknown exception");

    private String code;
    private String message;

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
