package io.mosip.kernel.emailnotification.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * <h1>SMS Executor Configuration</h1>
 *
 * <p>
 * This configuration class defines a dedicated {@link Executor} bean for handling
 * asynchronous SMS notification tasks in the MOSIP Kernel Notification service.
 * </p>
 *
 * <p>
 * The {@link ThreadPoolTaskExecutor} parameters are fully configurable through
 * external properties, making it easier to scale and optimize performance
 * across different deployment environments (e.g., 0.5 vCPU per pod vs multi-core setups).
 * </p>
 *
 * <h2>Property Mapping:</h2>
 * <ul>
 *   <li><b>sms.executor.core-pool-size</b> – Minimum number of threads maintained in the pool</li>
 *   <li><b>sms.executor.max-pool-size</b> – Maximum number of threads during peak load</li>
 *   <li><b>sms.executor.queue-capacity</b> – Size of the queue to hold pending SMS tasks</li>
 *   <li><b>sms.executor.keep-alive-seconds</b> – Time to keep idle threads alive before termination</li>
 *   <li><b>sms.executor.await-termination-seconds</b> – Grace period to wait for ongoing tasks before shutdown</li>
 *   <li><b>sms.executor.thread-name-prefix</b> – Custom prefix for naming worker threads for easier debugging</li>
 * </ul>
 *
 * <h2>Advantages:</h2>
 * <ul>
 *   <li>Dedicated executor prevents SMS and email tasks from competing for the same thread pool.</li>
 *   <li>Async execution improves throughput and reduces latency for bulk SMS campaigns.</li>
 *   <li>Dynamic configuration allows tuning per environment without code changes.</li>
 * </ul>
 *
 * <h2>Example Usage:</h2>
 * <pre>
 *     {@code
 *     @Async("smsExecutor")
 *     public void sendSms(...) {
 *         // SMS sending logic
 *     }
 *     }
 * </pre>
 *
 * @author Janardhan B S
 * @since 1.3.0
 */
@Configuration
@EnableAsync
public class SmsExecutorConfig {

    @Value("${sms.executor.core-pool-size:1}")
    private int corePoolSize;

    @Value("${sms.executor.max-pool-size:2}")
    private int maxPoolSize;

    @Value("${sms.executor.queue-capacity:500}")
    private int queueCapacity;

    @Value("${sms.executor.keep-alive-seconds:20}")
    private int keepAliveSeconds;

    @Value("${sms.executor.await-termination-seconds:20}")
    private int awaitTerminationSeconds;

    /**
     * Prefix to name each thread in this executor.
     * Helps in log tracing and debugging concurrent tasks.
     * Default: SmsSender-
     */
    @Value("${sms.executor.thread-name-prefix:SmsSender-}")
    private String threadNamePrefix;

    /**
     * Creates and configures a {@link ThreadPoolTaskExecutor} instance for
     * asynchronous SMS task execution.
     *
     * @return a configured {@link Executor} bean named "smsExecutor"
     */
    @Bean(name = "smsExecutor")
    public Executor smsExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        /**
         * Allows core threads to terminate when idle, reducing memory usage during low traffic.
         * Required for {@code keepAliveSeconds} to apply to core pool.
         * if set to true
         */
//        executor.setAllowCoreThreadTimeOut(true);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setAwaitTerminationSeconds(awaitTerminationSeconds);
        executor.initialize();
        return executor;
    }
}