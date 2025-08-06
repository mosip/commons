package io.mosip.kernel.emailnotification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * <h1>Mail Notifier Application</h1>
 *
 * <p>This Spring Boot application is responsible for sending email notifications.
 * with an asynchronous task executor to handle high-volume email delivery efficiently.</p>
 *
 * <p>Features:
 * <ul>
 *   <li>Asynchronous email sending for improved performance</li>
 *   <li>Thread pool configuration tuned for high throughput</li>
 *   <li>Excludes database auto-config to run as a lightweight microservice</li>
 * </ul>
 * </p>
 *
 * @author Sagar Mahapatra
 * @since 1.0.0
 *
 */
@SpringBootApplication(scanBasePackages = {
		"io.mosip.kernel.emailnotification.*",
		"${mosip.auth.adapter.impl.basepackage}",
		"io.mosip.kernel.core.logger.config",
		"io.mosip.kernel.smsserviceprovider.*"
})
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class })
@EnableAsync
public class NotificationBootApplication {

	/**
	 * Main method to start the Mail Notifier Application.
	 *
	 * @param args command-line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(NotificationBootApplication.class, args);
	}
}
