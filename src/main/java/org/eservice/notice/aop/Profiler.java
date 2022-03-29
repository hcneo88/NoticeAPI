package org.eservice.notice.aop ;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
 

@Aspect
@Component
@Slf4j
@ConditionalOnExpression("${Profiler.enabled:false}")  //Defined in application.properties to turn on and off
public class Profiler {
       
    //The "Around" advise for this aspect will be executed when methods defined in classes which are "decorated"
    //by one of these annotations,  are invoked.
    @Pointcut( "within(@org.springframework.stereotype.Repository *)"               +
           " || within(@org.springframework.stereotype.Service *)"                  +
           " || within(@org.springframework.stereotype.Component *)"                +
           " || within(@org.springframework.web.bind.annotation.RestController *)"  +
           " || within(@org.eservice.notice.aop.ProfilerAnnotation *)"
            )
    public void allPointcut() {}

    @Around("allPointcut()")
    public Object aroundServiceMethodAdvice(final ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = pjp.proceed();
        long executionTime = System.currentTimeMillis() - start;

        log.debug(pjp.getSignature().toString() + "\t" + executionTime);
        return result ;
    }
}

