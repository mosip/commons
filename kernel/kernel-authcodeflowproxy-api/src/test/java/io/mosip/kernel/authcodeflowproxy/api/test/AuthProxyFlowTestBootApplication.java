package io.mosip.kernel.authcodeflowproxy.api.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Minimal Spring Boot application used as the test context for
 * {@code kernel-authcodeflowproxy-api} tests.
 * <p>
 * Scans {@code io.mosip.kernel.authcodeflowproxy.api.*} and excludes JDBC
 * auto-configuration via {@code excludeName} because Boot 4 moved
 * {@code DataSourceAutoConfiguration} to
 * {@code org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration}.
 *
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
@SpringBootApplication(scanBasePackages = { "io.mosip.kernel.authcodeflowproxy.api.*" },
		excludeName = "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration")
public class AuthProxyFlowTestBootApplication {

	/**
	 * Starts the auth-code-flow proxy test application.
	 *
	 * @param args command-line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(AuthProxyFlowTestBootApplication.class, args);
	}
}
