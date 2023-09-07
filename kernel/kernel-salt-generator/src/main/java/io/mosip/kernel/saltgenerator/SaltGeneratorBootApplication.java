package io.mosip.kernel.saltgenerator;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.scheduling.ScheduledTasksEndpointAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.context.ApplicationContext;

/**
 * The Class SaltGeneratorBootApplication - Salt generator Job is a one-time job
 * which populates salts for hashing and encrypting data.
 *
 * @author Manoj SP
 */
@SpringBootApplication
@EnableBatchProcessing
@EnableAutoConfiguration(exclude = { JmxAutoConfiguration.class, ScheduledTasksEndpointAutoConfiguration.class })
public class SaltGeneratorBootApplication {

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		ApplicationContext applicationContext = SpringApplication.run(SaltGeneratorBootApplication.class, args);
		System.exit(SpringApplication.exit(applicationContext));
	}

}
