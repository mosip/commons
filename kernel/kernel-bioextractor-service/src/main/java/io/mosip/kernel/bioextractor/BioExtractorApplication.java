package io.mosip.kernel.bioextractor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.auth.adapter.handler.AuthHandler;

/**
 * Spring-boot class for Biometric Extractor Application.
 *
 * @author Loganathan Sekar
 */
@SpringBootApplication()
@Import(value = { RestTemplate.class })
@ComponentScan(basePackages={ "io.mosip.*" })
@EnableAsync
public class BioExtractorApplication {
	
	/**
	 * The main method.
	 *
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(BioExtractorApplication.class, args);
	}
	

}