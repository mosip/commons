package io.mosip.kernel.cryptomanager.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.mosip.kernel.dataaccess.hibernate.config.HibernateDaoConfig;
import io.mosip.kernel.keymanager.softhsm.impl.KeyStoreImpl;
import io.mosip.kernel.pdfgenerator.itext.impl.PDFGeneratorImpl;

/**
 * Crypto manager application
 * 
 * @author Urvil Joshi
 * @since 1.0.0
 *
 */
@SpringBootApplication(exclude = { KeyStoreImpl.class, HibernateDaoConfig.class,
		PDFGeneratorImpl.class }, scanBasePackages = { "io.mosip.kernel.cryptomanager.*" })
public class CryptoManagerTestBootApplication {

	/**
	 * Main method to run spring boot application
	 * 
	 * @param args
	 *            args
	 */
	public static void main(String[] args) {
		SpringApplication.run(CryptoManagerTestBootApplication.class, args);
	}
}
