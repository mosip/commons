package io.mosip.kernel.emailnotification.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

/**
 * Swagger / OpenAPI configuration for the notification HTTP service.
 * 
 * @author Sagar Mahapatra
 * @since 1.0.0
 *
 */
@Configuration
public class SwaggerConfig {

	/**
	 * Logger for OpenAPI bean construction diagnostics.
	 */
	private static final Logger logger = LoggerFactory.getLogger(SwaggerConfig.class);

	/**
	 * OpenAPI properties bound from {@code openapi.*} configuration.
	 */
	@Autowired
	private OpenApiProperties openApiProperties;

	/**
	 * Builds the OpenAPI bean from {@link OpenApiProperties} for Swagger UI under
	 * {@code /v1/notifier}.
	 *
	 * @return the configured OpenAPI model including info and servers
	 */
	@Bean
	public OpenAPI openApi() {
		OpenAPI api = new OpenAPI().components(new Components())
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
}
