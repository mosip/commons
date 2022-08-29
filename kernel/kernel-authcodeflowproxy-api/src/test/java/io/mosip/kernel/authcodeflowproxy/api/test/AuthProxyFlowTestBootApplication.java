package io.mosip.kernel.authcodeflowproxy.api.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Audit manager application
 * 
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
@SpringBootApplication(scanBasePackages = { "io.mosip.kernel.authcodeflowproxy.api.*" })
public class AuthProxyFlowTestBootApplication {

	/**
	 * Main method to run spring boot application
	 * 
	 * @param args args
	 */
	public static void main(String[] args) {
		SpringApplication.run(AuthProxyFlowTestBootApplication.class, args);
	}
}
