/*
 * 
 * 
 * 
 * 
 */
package io.mosip.kernel.cryptomanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import io.mosip.kernel.keymanagerservice.service.impl.KeymanagerServiceImpl;
import io.mosip.kernel.keymanagerservice.util.KeymanagerUtil;

/**
 * Crypto-Manager-Service Boot Application
 * 
 * @author Urvil Joshi
 *
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = { "io.mosip.kernel.cryptomanager.*", "io.mosip.kernel.auth.*" })
@Import(value = { KeymanagerServiceImpl.class, KeymanagerUtil.class })
public class CryptoManagerBootApplication {

	/**
	 * Main method for this application
	 * 
	 * @param args
	 *            arguments to pass
	 */
	public static void main(String[] args) {
		SpringApplication.run(CryptoManagerBootApplication.class, args);
	}
}
