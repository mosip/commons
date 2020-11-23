package io.mosip.kernel.keymanagerservice.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Crypto manager application
 * 
 * @author Urvil Joshi
 * @since 1.0.0
 *
 */
@SpringBootApplication(scanBasePackages = { "io.mosip.kernel.keymanagerservice.*","io.mosip.kernel.cryptomanager.*",
											"io.mosip.kernel.signature.*","io.mosip.kernel.tokenidgenerator.*", "io.mosip.kernel.lkeymanager.*",
											"io.mosip.kernel.keymanager.hsm.*", "io.mosip.kernel.keygenerator.*", 
											"io.mosip.kernel.crypto.jce.*", "io.mosip.kernel.partnercertservice.*"})
public class KeymanagerTestBootApplication {

	/**
	 * Main method to run spring boot application
	 * 
	 * @param args args
	 */
	public static void main(String[] args) {
		SpringApplication.run(KeymanagerTestBootApplication.class, args);
	}
}
