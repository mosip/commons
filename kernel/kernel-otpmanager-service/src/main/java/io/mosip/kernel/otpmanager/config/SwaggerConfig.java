package io.mosip.kernel.otpmanager.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/**
 * Configuration class for swagger config
 * 
 * @author Sagar Mahapatra
 * @author Ritesh Sinha
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
				.info(new Info().title("OTP manager Service").description(
						"This service is responsible for OTP related funtionalities").version("1.2.0"));
	}
}
