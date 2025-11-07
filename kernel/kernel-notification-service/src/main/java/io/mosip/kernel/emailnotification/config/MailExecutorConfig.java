package io.mosip.kernel.emailnotification.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * <h1>Mail Executor Configuration</h1>
 *
 * <p>
 * This configuration class defines a dedicated {@link Executor} bean for handling
 * asynchronous email notification tasks in the MOSIP Kernel Email Notification service.
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
 *   <li><b>mail.executor.core-pool-size</b> – Minimum number of threads maintained in the pool</li>
 *   <li><b>mail.executor.max-pool-size</b> – Maximum number of threads during peak load</li>
 *   <li><b>mail.executor.queue-capacity</b> – Size of the queue to hold pending email tasks</li>
 *   <li><b>mail.executor.keep-alive-seconds</b> – Time to keep idle threads alive before termination</li>
 *   <li><b>mail.executor.await-termination-seconds</b> – Grace period to wait for ongoing tasks before shutdown</li>
 *   <li><b>mail.executor.thread-name-prefix</b> – Custom prefix for naming worker threads for easier debugging</li>
 * </ul>
 *
 * <h2>Advantages:</h2>
 * <ul>
 *   <li>Async execution prevents blocking the main application thread.</li>
 *   <li>Dynamic configuration supports multiple environments without code changes.</li>
 *   <li>Thread names allow easy tracking of active tasks in logs and thread dumps.</li>
 * </ul>
 *
 * <h2>Example Usage:</h2>
 * <pre>
 *     {@code
 *     @Async("mailExecutor")
 *     public void sendEmail(...) {
 *         // email sending logic
 *     }
 *     }
 * </pre>
 *
 * @author Janardhan B S
 * @since 1.3.0
 */
@Configuration
@EnableAsync
public class MailExecutorConfig {

    @Value("${mail.executor.core-pool-size:2}")
    private int corePoolSize;

    @Value("${mail.executor.max-pool-size:3}")
    private int maxPoolSize;

    @Value("${mail.executor.queue-capacity:1000}")
    private int queueCapacity;

    @Value("${mail.executor.keep-alive-seconds:30}")
    private int keepAliveSeconds;

    @Value("${mail.executor.await-termination-seconds:30}")
    private int awaitTerminationSeconds;

    /**
     * Prefix to name each thread in this executor.
     * Helps in log tracing and debugging concurrent tasks.
     * Default: MailSender-
     */
    @Value("${mail.executor.thread-name-prefix:MailSender-}")
    private String threadNamePrefix;

    /**
     * Creates and configures a {@link ThreadPoolTaskExecutor} instance for
     * asynchronous email task execution.
     *
     * @return a configured {@link Executor} bean named "mailExecutor"
     */
    @Bean(name = "mailExecutor")
    public Executor mailExecutor() {
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
//        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setAwaitTerminationSeconds(awaitTerminationSeconds);
        executor.initialize();
        return executor;
    }
}