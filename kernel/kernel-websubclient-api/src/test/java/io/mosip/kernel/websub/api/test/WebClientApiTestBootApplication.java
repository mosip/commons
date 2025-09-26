package io.mosip.kernel.websub.api.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.test.context.TestPropertySource;

/**
 * Spring Boot test application configuration for WebSub API components.
 * <p>
 * This class configures a Spring Boot application context for testing WebSub API components,
 * such as {@link io.mosip.kernel.websub.api.client.PublisherClientImpl}. It scans specific
 * subpackages under {@code io.mosip.kernel.websub.api} to load only the necessary components
 * for testing, minimizing bean creation overhead and avoiding circular dependencies. The
 * {@link DataSourceAutoConfiguration} is excluded to prevent unnecessary database configuration.
 * The implementation is optimized for test performance by:
 * <ul>
 *   <li>Using targeted component scanning to include only relevant beans.</li>
 *   <li>Excluding unnecessary auto-configurations to reduce context initialization time.</li>
 *   <li>Logging application startup for debugging.</li>
 * </ul>
 * This configuration is used in tests like {@code PublisherClientApiExceptionsTest} to provide
 * a lightweight Spring context for WebSub API functionality.
 * </p>
 *
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 * @see io.mosip.kernel.websub.api.client.PublisherClientImpl
 * @see SpringBootApplication
 */
@SpringBootApplication(scanBasePackages = { "io.mosip.kernel.websub.api.*" },
exclude={DataSourceAutoConfiguration.class})
@TestPropertySource(properties = {
		"mosip.kernel.websub-db-version-client-behaviour-enable=false"
})
public class WebClientApiTestBootApplication {

	/**
	 * Logger for debugging and error reporting.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(WebClientApiTestBootApplication.class);

	/**
	 * Main method to run the Spring Boot test application.
	 * <p>
	 * Initializes the Spring application context for testing WebSub API components.
	 * Logs the startup process for debugging purposes.
	 * </p>
	 *
	 * @param args command-line arguments
	 */
	public static void main(String[] args) {
		LOGGER.info("Starting WebClientApiTestBootApplication for WebSub API testing");
		SpringApplication.run(WebClientApiTestBootApplication.class, args);
		LOGGER.info("WebClientApiTestBootApplication started successfully");
	}
}