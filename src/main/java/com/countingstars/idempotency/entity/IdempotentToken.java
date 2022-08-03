package com.countingstars.idempotency.entity;

import com.countingstars.idempotency.enums.IdempotentBusinessStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class IdempotentToken {

    private IdempotentBusinessStatus businessStatus;
    private String clientIpAddr;
    private Object req;
    private Object resp;

    public IdempotentToken(IdempotentBusinessStatus businessStatus) {
        this(businessStatus, null, null, null);
    }

    public IdempotentToken(IdempotentBusinessStatus businessStatus, String clientIpAddr) {
        this(businessStatus, clientIpAddr,null,null);
    }

}
