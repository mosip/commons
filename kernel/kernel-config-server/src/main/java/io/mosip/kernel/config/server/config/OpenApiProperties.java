package io.mosip.kernel.config.server.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * Binds {@code openapi.*} properties used by {@link SwaggerConfig} (Biosdk / springdoc style).
 */
@Configuration
@ConfigurationProperties(prefix = "openapi")
@Data
public class OpenApiProperties {
	/** API title, description, version, and license. */
	private InfoProperty info;
	/** Published server URLs for the OpenAPI document. */
	private Service service;
	/** Springdoc group name and path matchers ({@code openapi.group.*}). */
	private Group group;
}

/** OpenAPI info block bound from {@code openapi.info.*}. */
@Data
class InfoProperty {
	private String title;
	private String description;
	private String version;
	private LicenseProperty license;
}

/** License fields bound from {@code openapi.info.license.*}. */
@Data
class LicenseProperty {
	private String name;
	private String url;
}

/** Server list bound from {@code openapi.service.*}. */
@Data
class Service {
	private List<Server> servers;
}

/** A single OpenAPI server URL and description. */
@Data
class Server {
	private String description;
	private String url;
}

/** GroupedOpenApi settings bound from {@code openapi.group.*}. */
@Data
class Group {
	private String name;
	private List<String> paths;
}
