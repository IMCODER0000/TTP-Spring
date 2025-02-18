package org.ttp.ttpspring.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Aspect
@Component
@Slf4j
public class PerformanceAspect {
    private final ConcurrentHashMap<String, AtomicLong> methodCallCount = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> methodTotalTime = new ConcurrentHashMap<>();

    @Around("execution(* org.ttp.ttpspring.Liar.service.LiarGameService.*(..))")
    public Object measureMethodPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        long startTime = System.currentTimeMillis();
        
        try {
            return joinPoint.proceed();
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            
            methodCallCount.computeIfAbsent(methodName, k -> new AtomicLong(0)).incrementAndGet();
            methodTotalTime.computeIfAbsent(methodName, k -> new AtomicLong(0)).addAndGet(executionTime);
            
            double avgTime = (double) methodTotalTime.get(methodName).get() / methodCallCount.get(methodName).get();
            
            log.info("[Performance] Method: {}, Execution Time: {}ms, Avg Time: {:.2f}ms, Total Calls: {}", 
                    methodName, executionTime, avgTime, methodCallCount.get(methodName).get());
        }
    }
}
