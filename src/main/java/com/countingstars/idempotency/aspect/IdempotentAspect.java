package com.countingstars.idempotency.aspect;

import com.countingstars.idempotency.annotation.Idempotent;
import com.countingstars.idempotency.dto.BaseRespDTO;
import com.countingstars.idempotency.dto.DefaultRespDTO;
import com.countingstars.idempotency.entity.IdempotentToken;
import com.countingstars.idempotency.enums.ExceptionCodeEnum;
import com.countingstars.idempotency.enums.IdempotentBusinessStatus;
import com.countingstars.idempotency.exception.IdempotentException;
import com.countingstars.idempotency.service.IdempotentService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class IdempotentAspect extends BaseAspect {

    public static final String HEADER_IDEMPOTENT_TOKEN = "IdempotentToken";

    @Autowired
    private IdempotentService idempotentService;

    @Autowired
    private RedissonClient redissonClient;

    @Around("onClass() && onControllerMethod() && args(req)")
    public Object around(ProceedingJoinPoint pjp, Object req) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        Idempotent annotation = AnnotationUtils.findAnnotation(methodSignature.getMethod(), Idempotent.class);
        if (annotation == null) return pjp.proceed();
        IdempotentToken token = idempotentService.getToken();
        IdempotentBusinessStatus businessStatus = token.getBusinessStatus();
        if (IdempotentBusinessStatus.NEW == businessStatus) {
            String lockName = idempotentService.genLock();
            RLock lock = redissonClient.getLock(lockName);
            try {
                boolean b = lock.tryLock();
                if (!b) throw new IdempotentException(ExceptionCodeEnum.UNKNOWN_EXCEPTION);
                token.setBusinessStatus(IdempotentBusinessStatus.PROCEEDING).setReq(req);
                idempotentService.updateToken(token);
                Object resp;
                resp = pjp.proceed();
                token.setBusinessStatus(IdempotentBusinessStatus.DONE).setResp(resp);
                idempotentService.updateToken(token);
                return resp;
            } catch (Exception e) {
                // todo: wrap the exception and return
                log.error("Idempotent business proceeding exception: ", e);
                if (e instanceof IdempotentException) {
                    BaseRespDTO respDTO = new BaseRespDTO((IdempotentException) e);
                    token.setBusinessStatus(IdempotentBusinessStatus.DONE).setResp(respDTO);
                    idempotentService.updateToken(token);
                    throw e;
                } else {
                    BaseRespDTO respDTO = new BaseRespDTO(new IdempotentException(ExceptionCodeEnum.UNKNOWN_EXCEPTION));
                    token.setBusinessStatus(IdempotentBusinessStatus.DONE).setResp(respDTO);
                    idempotentService.updateToken(token);
                    throw new IdempotentException(ExceptionCodeEnum.UNKNOWN_EXCEPTION);
//                    throw e;
                }
            } finally {
                if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } else if (IdempotentBusinessStatus.PROCEEDING == businessStatus) {
            Object businessReq = token.getReq();
            idempotentService.validateReq(businessReq, req);
            DefaultRespDTO resp = new DefaultRespDTO("PROCEEDING");
            return resp;
        } else if (IdempotentBusinessStatus.DONE == businessStatus) {
            Object businessReq = token.getReq();
            idempotentService.validateReq(businessReq, req);
            Object resp = token.getResp();
            return resp;
        }
        throw new RuntimeException("Unknown Exception.");
    }

}
