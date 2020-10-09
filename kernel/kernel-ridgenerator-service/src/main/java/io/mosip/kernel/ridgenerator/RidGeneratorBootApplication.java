package io.mosip.kernel.ridgenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main class for RID generator.
 * 
 * @author Ritesh Sinha
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = { "io.mosip.kernel.ridgenerator.*", "${mosip.auth.adapter.impl.basepackage}",
		"io.mosip.kernel.core.logger.config"})
public class RidGeneratorBootApplication {

	/**
	 * Main methods for RID generator.
	 * 
	 * @param args the arguments.
	 */
	public static void main(String[] args) {
		SpringApplication.run(RidGeneratorBootApplication.class, args);

	}

}
