// Create new PaymentAsyncConfig.java
package id.ac.ui.cs.advprog.papikos.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class PaymentAsyncConfig {

    @Bean(name = "paymentTaskExecutor")
    public Executor paymentTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);        // 3 core threads for payment processing
        executor.setMaxPoolSize(8);         // Max 8 threads for peak load
        executor.setQueueCapacity(50);      // Queue up to 50 payment tasks
        executor.setThreadNamePrefix("payment-async-");
        executor.setKeepAliveSeconds(60);   // Keep threads alive for 60 seconds
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}