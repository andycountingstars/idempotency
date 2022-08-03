package com.countingstars.idempotency.service;

import com.countingstars.idempotency.entity.IdempotentToken;

import javax.servlet.http.HttpServletRequest;

public interface IdempotentService {
    String genToken(HttpServletRequest request) throws Exception;

    void updateToken(IdempotentToken token) throws Exception;

    IdempotentToken getToken() throws Exception;

    void validateReq(Object req, Object reqOfToken);

    String genLock();
}
