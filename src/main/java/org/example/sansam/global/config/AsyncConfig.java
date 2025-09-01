package org.example.sansam.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "pushExecutor")
    public Executor pushExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);    // 기본 스레드 수 (동시 실행 가능 작업 수)
        executor.setMaxPoolSize(50);    // 최대 스레드 수 (큐가 가득 찼을 때 확장)
        executor.setQueueCapacity(500); // 작업 대기 큐 크기 (corePoolSize만큼 실행 중일 때 대기 가능 작업 수)
        executor.setThreadNamePrefix("SSE-PUSH-");   // 스레드 이름 prefix (디버깅 로그 확인에 유용)
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());// 스레드 풀이 포화 상태일 때, 새로운 작업을 거부하지 않고 "요청한 쪽 쓰레드에서 직접 실행"하는 정책
        executor.initialize();  // 설정 완료 후 초기화
        return executor;
    }

    @Bean(name = "broadcastExecutor")
    public Executor broadcastExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("SSE-BROADCAST");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    /*
    ThreadPoolTaskExecutor : Spring이 제동하는 TaskExecutor 인터페이스 구현체
    내부적으로는 JDK의 ThreadPoolExecutor 위에 스프링식 편의기능을 덧씌운 래퍼 클래스
    스프링에서 비동기(@Async) 작업을 안정적으로 실행하기 위한 기본 스레드 풀 구현체라고 보면 됨.
     */
}
