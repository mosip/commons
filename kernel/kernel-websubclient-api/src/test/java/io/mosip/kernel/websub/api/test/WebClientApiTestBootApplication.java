package io.mosip.kernel.websub.api.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Audit manager application
 * 
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
@SpringBootApplication(scanBasePackages = { "io.mosip.kernel.websub.api.*" })
public class WebClientApiTestBootApplication {

	/**
	 * Main method to run spring boot application
	 * 
	 * @param args args
	 */
	public static void main(String[] args) {
		SpringApplication.run(WebClientApiTestBootApplication.class, args);
	}
}
