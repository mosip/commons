package io.mosip.kernel.auth.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;


/**
 * Configuration class for swagger config.
 * Builds the Springdoc {@link OpenAPI} bean from {@link OpenApiProperties}
 * bound under {@code openapi.*}.
 * 
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
@Configuration
public class SwaggerConfig {

	/**
	 * Logger reserved for this configuration class.
	 */
	private static final Logger logger = LoggerFactory.getLogger(SwaggerConfig.class);

	/**
	 * Scheme name shown by Swagger UI as Authorize (IDA-style Authorization apiKey).
	 */
	public static final String AUTHORIZATION_SCHEME = "Authorization";

	/**
	 * OpenAPI title, description, version, license, and server list from properties.
	 */
	@Autowired
	private OpenApiProperties openApiProperties;

	/**
	 * OpenAPI document used by Springdoc for Swagger UI under authmanager.
	 *
	 * @return configured {@link OpenAPI} with info, servers, and Authorize apiKey
	 */
	@Bean
	public OpenAPI openApi() {
		OpenAPI api = new OpenAPI()
				.components(new Components().addSecuritySchemes(AUTHORIZATION_SCHEME, authorizationApiKey()))
				.addSecurityItem(new SecurityRequirement().addList(AUTHORIZATION_SCHEME))
				.info(new Info().title(openApiProperties.getInfo().getTitle())
						.version(openApiProperties.getInfo().getVersion())
						.description(openApiProperties.getInfo().getDescription())
						.license(new License().name(openApiProperties.getInfo().getLicense().getName())
								.url(openApiProperties.getInfo().getLicense().getUrl())));

		openApiProperties.getService().getServers().forEach(server -> {
			api.addServersItem(new Server().description(server.getDescription()).url(server.getUrl()));
		});
		return api;
	}

	/**
	 * Header apiKey named {@code Authorization}, the same scheme ID Authentication
	 * uses so Swagger UI shows Authorize with Name/In/Value.
	 *
	 * @return the security scheme
	 */
	private static SecurityScheme authorizationApiKey() {
		return new SecurityScheme().type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER)
				.name(AUTHORIZATION_SCHEME);
	}
}
