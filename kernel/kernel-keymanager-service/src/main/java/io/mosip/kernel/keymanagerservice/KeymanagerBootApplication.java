package io.mosip.kernel.keymanagerservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Key Manager Application
 * 
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
@SpringBootApplication(scanBasePackages = { "io.mosip.kernel.cryptomanager.*", "io.mosip.kernel.keymanagerservice.*",
		"${mosip.auth.adapter.impl.basepackage}", "io.mosip.kernel.signature.*", "io.mosip.kernel.tokenidgenerator.*",
		"io.mosip.kernel.lkeymanager.*", "io.mosip.kernel.keymanager.*", "io.mosip.kernel.keygenerator.*",
		"io.mosip.kernel.crypto.*", "io.mosip.kernel.zkcryptoservice.*", "io.mosip.kernel.partnercertservice.*",
		"io.mosip.kernel.clientcrypto.*", "io.mosip.kernel.core.logger.config", "io.mosip.kernel.keymigrate.*"})
public class KeymanagerBootApplication {

	/**
	 * Main method to run spring boot application
	 * 
	 * @param args args
	 */

	public static void main(String[] args) {

		SpringApplication.run(KeymanagerBootApplication.class, args);
	}
}
