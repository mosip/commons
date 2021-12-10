package io.mosip.kernel.ridgenerator.config;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

/**
 * Configuration class for swagger config
 * 
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
@Configuration
public class SwaggerConfig {

	@Autowired
	private OpenApiProperties openApiProperties;

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
