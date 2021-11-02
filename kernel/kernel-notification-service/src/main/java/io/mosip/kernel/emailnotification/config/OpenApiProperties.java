package io.mosip.kernel.emailnotification.config;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "openapi")
@Data
public class OpenApiProperties {
    private InfoProperty info;
    private Service service;
}

@Data
class InfoProperty {
    private String title;
    private String description;
    private String version;
    private LicenseProperty license;
}

@Data
class LicenseProperty {
    private String name;
    private String url;
}

@Data
class Service {
    private List<Server> servers;
}

@Data
class Server {
    private String description;
    private String url;
}