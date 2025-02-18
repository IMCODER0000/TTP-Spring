package org.ttp.ttpspring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "gameTaskExecutor")
    public Executor gameTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // CPU 코어 수에 기반한 스레드 풀 설정
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(corePoolSize);  // CPU 코어 수만큼 기본 스레드
        executor.setMaxPoolSize(corePoolSize * 2);   // 코어 수의 2배까지 확장
        executor.setQueueCapacity(50);  // 대기 큐는 적절한 크기로 제한
        executor.setThreadNamePrefix("GameThread-");
        executor.setKeepAliveSeconds(60); // 유휴 스레드 60초 후 정리
        executor.setAllowCoreThreadTimeOut(true); // 코어 스레드도 타임아웃 허용
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
