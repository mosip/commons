package io.mosip.kernel.emailnotification.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

/**
 * Springdoc OpenAPI / Swagger UI configuration (embedded via {@code springdoc-openapi-starter-webmvc-ui}).
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

	/**
	 * Groups all servlet paths into one Swagger UI document ({@code openapi.group.*}).
	 *
	 * @return grouped OpenAPI definition
	 */
	@Bean
	public GroupedOpenApi groupedOpenApi() {
		return GroupedOpenApi.builder().group(openApiProperties.getGroup().getName())
				.pathsToMatch(openApiProperties.getGroup().getPaths().toArray(String[]::new)).build();
	}
}
