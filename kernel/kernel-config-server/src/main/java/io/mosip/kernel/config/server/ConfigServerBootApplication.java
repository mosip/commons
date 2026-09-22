package io.mosip.kernel.config.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Spring Boot entry point for MOSIP Config Server ({@code @EnableConfigServer}).
 * Serves configuration at servlet path {@code /config} (see Helm / deploy values).
 *
 * @author Swati Raj
 * @since 1.0.0
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigServerBootApplication {

	/**
	 * Starts the config-server process.
	 *
	 * @param args Spring Boot arguments (profiles, {@code spring.cloud.config.server.*})
	 */
	public static void main(String[] args) {
		SpringApplication.run(ConfigServerBootApplication.class, args);
	}
}
