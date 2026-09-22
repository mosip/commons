package io.mosip.kernel.auth.defaultadapter.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Minimal Spring Boot application used as the test context for
 * {@code kernel-auth-adapter} tests.
 * <p>
 * Scans {@code io.mosip.kernel.auth.defaultadapter.*} and excludes JDBC
 * auto-configuration via {@code excludeName} because Boot 4 moved
 * {@code DataSourceAutoConfiguration} to
 * {@code org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration}.
 */
@SpringBootApplication(scanBasePackages = { "io.mosip.kernel.auth.defaultadapter.*" },
		excludeName = "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration")
public class AuthTestBootApplication {

	/**
	 * Starts the adapter test application.
	 *
	 * @param args command-line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(AuthTestBootApplication.class, args);
	}

}
