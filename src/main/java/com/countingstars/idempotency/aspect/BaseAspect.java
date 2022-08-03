package com.countingstars.idempotency.aspect;

import org.aspectj.lang.annotation.Pointcut;

public class BaseAspect {

    @Pointcut("@target(org.springframework.web.bind.annotation.RestController)")
    protected void onClass() {}

    @Pointcut("execution(public * com.countingstars.idempotency..*Controller.*(..))")
    protected void onControllerMethod() {}

}
