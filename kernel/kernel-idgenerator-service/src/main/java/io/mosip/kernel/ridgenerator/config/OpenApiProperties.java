package io.mosip.kernel.ridgenerator.config;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * Binds {@code openapi.*} properties used to build the RID Swagger {@code OpenAPI} bean.
 */
@Configuration
@ConfigurationProperties(prefix = "openapi")
@Data
public class OpenApiProperties {
    /**
     * API title, description, version, and license.
     */
    private InfoProperty info;
    /**
     * Published server URLs for the OpenAPI document.
     */
    private Service service;
}

/**
 * OpenAPI info block bound from {@code openapi.info.*}.
 */
@Data
class InfoProperty {
    /**
     * API title.
     */
    private String title;
    /**
     * API description.
     */
    private String description;
    /**
     * API version string.
     */
    private String version;
    /**
     * License name and URL.
     */
    private LicenseProperty license;
}

/**
 * License fields bound from {@code openapi.info.license.*}.
 */
@Data
class LicenseProperty {
    /**
     * License name.
     */
    private String name;
    /**
     * License URL.
     */
    private String url;
}

/**
 * Server list bound from {@code openapi.service.*}.
 */
@Data
class Service {
    /**
     * OpenAPI server entries.
     */
    private List<Server> servers;
}

/**
 * A single OpenAPI server URL and description.
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
