package io.mosip.kernel.ridgenerator.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

/**
 * Springdoc OpenAPI / Swagger UI configuration (embedded via {@code springdoc-openapi-starter-webmvc-ui}).
 * Servlet-only — skipped under Vert.x / non-web AnnotationConfig test contexts.
 */
@Configuration
@ConditionalOnWebApplication(type = Type.SERVLET)
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
		InfoProperty info = openApiProperties.getInfo();
		LicenseProperty license = info != null ? info.getLicense() : null;
		OpenAPI api = new OpenAPI().components(new Components())
				.info(new Info().title(info != null ? info.getTitle() : null)
						.version(info != null ? info.getVersion() : null)
						.description(info != null ? info.getDescription() : null)
						.license(new License().name(license != null ? license.getName() : null)
								.url(license != null ? license.getUrl() : null)));

		if (openApiProperties.getService() != null && openApiProperties.getService().getServers() != null) {
			openApiProperties.getService().getServers().forEach(server -> api
					.addServersItem(new Server().description(server.getDescription()).url(server.getUrl())));
		}
		return api;
	}

	/**
	 * Groups all servlet paths into one Swagger UI document ({@code openapi.group.*}).
	 *
	 * @return grouped OpenAPI definition
	 */
	@Bean
	public GroupedOpenApi groupedOpenApi() {
		Group group = openApiProperties.getGroup();
		String name = group != null && group.getName() != null ? group.getName() : "default";
		String[] paths = group != null && group.getPaths() != null
				? group.getPaths().toArray(String[]::new)
				: new String[] { "/**" };
		return GroupedOpenApi.builder().group(name).pathsToMatch(paths).build();
	}
}
