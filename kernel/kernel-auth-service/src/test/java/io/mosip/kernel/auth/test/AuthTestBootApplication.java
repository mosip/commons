package io.mosip.kernel.auth.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Minimal Spring Boot application used as the test context for
 * {@code kernel-auth-service} tests. Does not scan {@code io.mosip.kernel.auth}
 * as a whole: that would load {@code AuthBootApplication} and the baked-in
 * adapter ({@code defaultadapter}), which collide with {@code TestSecurityConfig}.
 */
@SpringBootApplication(scanBasePackages = { "io.mosip.kernel.auth.controller", "io.mosip.kernel.auth.config",
		"io.mosip.kernel.auth.defaultimpl", "io.mosip.kernel.auth.test" })
public class AuthTestBootApplication {

	/**
	 * Starts the auth-service test application.
	 *
	 * @param args command-line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(AuthTestBootApplication.class, args);
	}

}
