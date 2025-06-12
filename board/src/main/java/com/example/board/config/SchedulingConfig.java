package com.example.board.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
public class SchedulingConfig implements SchedulingConfigurer {

    private final int POOL_SIZE = 10; // 필요에 따라 조정

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        // 스레드 풀 크기 설정
        threadPoolTaskScheduler.setPoolSize(POOL_SIZE);
        // 스레드 이름 접두사 설정 (로깅 식별용)
        threadPoolTaskScheduler.setThreadNamePrefix("scheduled-task-pool-");
        // 예외 발생 시 처리 방법 설정 (선택사항)
        threadPoolTaskScheduler.setErrorHandler(t ->
                System.err.println("스케줄 작업 실행 중 오류 발생: " + t.getMessage()));
        // 스케줄러 초기화 (필수)
        threadPoolTaskScheduler.initialize();
        threadPoolTaskScheduler.setWaitForTasksToCompleteOnShutdown(true);
        threadPoolTaskScheduler.setAwaitTerminationSeconds(30);
        // 스케줄러 등록
        taskRegistrar.setTaskScheduler(threadPoolTaskScheduler);
    }
}
