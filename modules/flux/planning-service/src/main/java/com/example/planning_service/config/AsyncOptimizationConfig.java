package com.example.planning_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * ARCHITEKTURA: Async Thread Pool Configuration dla Regional Optimization
 * 
 * Konfiguruje thread pool dla równoległego solving regionów:
 * - Core Pool: 16 threads (1 per województwo/hub)
 * - Max Pool: 24 threads (dla overflow regionów)
 * - Queue: 100 capacity
 * 
 * Performance Target: 10-11x speedup dla 10,000+ orders
 */
@Configuration
@EnableAsync
public class AsyncOptimizationConfig {

    /**
     * Thread pool dedykowany dla regional optimization
     * Każdy thread = 1 region solver
     */
    @Bean(name = "regionalSolverPool")
    public Executor regionalSolverThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Core threads - zawsze aktywne
        executor.setCorePoolSize(16); // 16 województw w Polsce

        // Max threads - dla peak load
        executor.setMaxPoolSize(24); // Dodatkowe 8 dla overflow

        // Queue capacity - pending tasks
        executor.setQueueCapacity(100);

        // Thread naming
        executor.setThreadNamePrefix("RegionalSolver-");

        // Rejection policy - caller runs (fallback to synchronous)
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());

        // Wait for tasks to complete on shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();
        return executor;
    }

    /**
     * Thread pool dla general optimization tasks (batch processing, etc.)
     */
    @Bean(name = "optimizationThreadPool")
    public Executor optimizationThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("Optimization-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
