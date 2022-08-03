package com.countingstars.idempotency.service.impl;

import com.countingstars.idempotency.aspect.IdempotentAspect;
import com.countingstars.idempotency.entity.IdempotentToken;
import com.countingstars.idempotency.enums.ExceptionCodeEnum;
import com.countingstars.idempotency.enums.IdempotentBusinessStatus;
import com.countingstars.idempotency.exception.IdempotentException;
import com.countingstars.idempotency.service.IdempotentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.micrometer.core.instrument.util.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class IdempotentServiceImpl implements IdempotentService {

    private static final String LOCK = "LOCK";

    @Value("${idempotency.token.timeout}")
    private Long timeout;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedisTemplate<String, Object> objectRedisTemplate;

    @Override
    public String genToken(HttpServletRequest request) throws JsonProcessingException {
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        String key = genRedisKey(token);
        String clientIpAddr = request.getRemoteAddr();
        IdempotentToken idempotentToken = new IdempotentToken(IdempotentBusinessStatus.NEW, clientIpAddr);
        Boolean b = objectRedisTemplate.opsForValue().setIfAbsent(key, idempotentToken, timeout, TimeUnit.MILLISECONDS);
        if (b == null || !b) throw new IdempotentException(ExceptionCodeEnum.UNKNOWN_EXCEPTION);
        return token;
    }

    /**
     * todo: to be implemented
     * @param token
     * @return
     */
    private String genRedisKey(String token) {
        return token;
    }

    private String getRequestHeader(String header) {
        if (StringUtils.isBlank(header)) return null;
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) return null;
        HttpServletRequest request = requestAttributes.getRequest();
        return request.getHeader(header);
    }

    @Override
    public IdempotentToken getToken() throws Exception {
        String idempotentTokenHeader = getRequestHeader(IdempotentAspect.HEADER_IDEMPOTENT_TOKEN);
        if (StringUtils.isBlank(idempotentTokenHeader)) throw new IdempotentException(ExceptionCodeEnum.REQUEST_HEADER_BLANK);
        String key = genRedisKey(idempotentTokenHeader);
        IdempotentToken idempotentToken = (IdempotentToken) objectRedisTemplate.boundValueOps(key).get();
        if (idempotentToken == null) throw new IdempotentException(ExceptionCodeEnum.TOKEN_INVALID_EXCEPTION);
        String clientIpAddr = idempotentToken.getClientIpAddr();
        validateClientIpAddr(clientIpAddr, getClientIpAddr());
        return idempotentToken;
    }

    private void validateClientIpAddr(String clientIpAddrOfToken, String clientIpAddrOfReq) {
        if (clientIpAddrOfToken != null && clientIpAddrOfToken.equals(clientIpAddrOfReq)) return;
        throw new IdempotentException(ExceptionCodeEnum.TOKEN_ILLEGAL_EXCEPTION);
    }

    private String getClientIpAddr() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) return null;
        HttpServletRequest request = requestAttributes.getRequest();
        return request.getRemoteAddr();
    }

    /**
     * todo: distributed lock to be implemented.
     * @param token
     * @return
     * @throws Exception
     */
    @Override
    public void updateToken(IdempotentToken token) throws Exception {
        if (token == null) throw new IdempotentException(ExceptionCodeEnum.TOKEN_INVALID_EXCEPTION);
        String idempotentHeader = getRequestHeader(IdempotentAspect.HEADER_IDEMPOTENT_TOKEN);
        String key = genRedisKey(idempotentHeader);
        objectRedisTemplate.opsForValue().setIfPresent(key, token, timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public void validateReq(Object req, Object reqOfToken) {
        boolean b = req.equals(reqOfToken);
        if (!b) throw new IdempotentException(ExceptionCodeEnum.TOKEN_ILLEGAL_EXCEPTION);
    }

    @Override
    public String genLock() {
        String idempotentToken = getRequestHeader(IdempotentAspect.HEADER_IDEMPOTENT_TOKEN);
        return LOCK + idempotentToken;
    }
}
