package io.mosip.kernel.config.server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

/**
 * Springdoc OpenAPI configuration (Swagger UI is served on the main port by
 * {@link SpringDocWebConfig} so Config Server {@code EnvironmentController}
 * {@code /{name}/{profiles}} does not swallow {@code /swagger-ui/**}).
 */
@Configuration
public class SwaggerConfig {

	@Autowired
	private OpenApiProperties openApiProperties;

	/**
	 * Creates the OpenAPI document from {@code openapi.*} bootstrap properties.
	 *
	 * @return OpenAPI model
	 */
	@Bean
	public OpenAPI openApi() {
		OpenAPI api = new OpenAPI().components(new Components())
				.info(new Info().title(openApiProperties.getInfo().getTitle())
						.version(openApiProperties.getInfo().getVersion())
						.description(openApiProperties.getInfo().getDescription())
						.license(new License().name(openApiProperties.getInfo().getLicense().getName())
								.url(openApiProperties.getInfo().getLicense().getUrl())));

		openApiProperties.getService().getServers().forEach(server -> api
				.addServersItem(new Server().description(server.getDescription()).url(server.getUrl())));
		return api;
	}
}
