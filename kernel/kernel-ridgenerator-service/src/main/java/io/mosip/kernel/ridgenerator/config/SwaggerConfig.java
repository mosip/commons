package io.mosip.kernel.ridgenerator.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/**
 * Configuration class for swagger config
 * 
 * @author Dharmesh Khandelwal
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
				.info(new Info().title("RID generator Service").description(
						"This service is responsible for RID generation related funtionalities").version("1.2.0"));
}
}