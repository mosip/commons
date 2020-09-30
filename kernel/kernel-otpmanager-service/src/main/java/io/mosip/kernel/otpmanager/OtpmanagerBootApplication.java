package io.mosip.kernel.otpmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * OTP Manager Application
 * 
 * @author Ritesh Sinha
 * @author Sagar Mahapatra
 * @since 1.0.0
 *
 */
@SpringBootApplication(scanBasePackages = { "io.mosip.kernel.otpmanager.*", "${mosip.auth.adapter.impl.basepackage}",
		"io.mosip.kernel.core.logger.config" })
public class OtpmanagerBootApplication {

	/**
	 * Main method to run spring boot application
	 * 
	 * @param args the argument
	 */
	public static void main(String[] args) {
		SpringApplication.run(OtpmanagerBootApplication.class, args);
	}
}
