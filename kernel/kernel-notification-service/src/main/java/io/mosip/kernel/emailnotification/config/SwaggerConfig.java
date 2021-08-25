package io.mosip.kernel.emailnotification.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/**
 * Class for swagger configuration.
 * 
 * @author Sagar Mahapatra
 * @since 1.0.0
 *
 */
@Configuration
public class SwaggerConfig {

	/**
	 * Produce Docket bean
	 * 
	 * @return Docket bean
	 */
	public OpenAPI openAPI() {
		return new OpenAPI()
				.components(new Components())
				.info(new Info().title("Audit manager Service documentation").description(
						"Audit manager Service ").version("1.2.0"));
	}

}
