package org.example.sansam.global.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class TimeTraceAop {

    @Around("execution(* org.example.sansam.order.service..*(..)) || execution(* org.example.sansam.payment.service..*(..))")
    public Object execute(ProceedingJoinPoint joinPoint)throws Throwable{

        long startTime=System.currentTimeMillis();
        System.out.println("START : "+joinPoint.toString());
        try{
            return joinPoint.proceed();
        }finally{
            long finishTime=System.currentTimeMillis();
            long timeMs = finishTime-startTime;
            System.out.println("END : "+joinPoint.toString()+" "+timeMs+"ms");
        }
    }
}
