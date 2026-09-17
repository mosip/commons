package io.mosip.kernel.emailnotification.config;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * Binds OpenAPI metadata from {@code openapi.*} configuration properties used
 * by {@link SwaggerConfig} to build the notification service Swagger UI model.
 */
@Configuration
@ConfigurationProperties(prefix = "openapi")
@Data
public class OpenApiProperties {
    /**
     * API info block bound from {@code openapi.info.*} (title, description,
     * version, license).
     */
    private InfoProperty info;
    /**
     * Service-level OpenAPI settings bound from {@code openapi.service.*},
     * including advertised servers.
     */
    private Service service;
}

/**
 * OpenAPI info section bound from {@code openapi.info.*}.
 */
@Data
class InfoProperty {
    /**
     * API title shown in Swagger UI.
     */
    private String title;
    /**
     * API description shown in Swagger UI.
     */
    private String description;
    /**
     * API version shown in Swagger UI.
     */
    private String version;
    /**
     * License metadata bound from {@code openapi.info.license.*}.
     */
    private LicenseProperty license;
}

/**
 * License metadata bound from {@code openapi.info.license.*}.
 */
@Data
class LicenseProperty {
    /**
     * License name shown in Swagger UI.
     */
    private String name;
    /**
     * License URL shown in Swagger UI.
     */
    private String url;
}

/**
 * Service-level OpenAPI settings bound from {@code openapi.service.*}.
 */
@Data
class Service {
    /**
     * Servers advertised in the OpenAPI document.
     */
    private List<Server> servers;
}

/**
 * A single OpenAPI server entry bound from {@code openapi.service.servers}.
 */
@Data
class Server {
    /**
     * Human-readable server description.
     */
    private String description;
    /**
     * Server base URL.
     */
    private String url;
}
