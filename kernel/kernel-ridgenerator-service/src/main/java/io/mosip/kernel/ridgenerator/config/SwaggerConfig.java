package io.mosip.kernel.ridgenerator.config;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Configuration class for swagger config
 * 
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

	private static final Logger logger = LoggerFactory.getLogger(SwaggerConfig.class);

	/**
	 * Master service Version
	 */
	private static final String MASTER_SERVICE_VERSION = "1.0";
	/**
	 * Application Title
	 */
	private static final String TITLE = "RID Generator Service";
	/**
	 * Master Data Service
	 */
	private static final String DISCRIBTION = "RID Generator Service for CRUD";

	@Value("${application.env.local:false}")
	private Boolean localEnv;

	@Value("${swagger.base-url:#{null}}")
	private String swaggerBaseUrl;

	@Value("${server.port:8080}")
	private int serverPort;

	String proto = "http";
	String host = "localhost";
	int port = -1;
	String hostWithPort = "localhost:8080";

	/**
	 * Produces {@link ApiInfo}
	 * 
	 * @return {@link ApiInfo}
	 */
	private ApiInfo apiInfo() {
		return new ApiInfoBuilder().title(TITLE).description(DISCRIBTION).version(MASTER_SERVICE_VERSION).build();
	}

	/**
	 * Produce Docket bean
	 * 
	 * @return Docket bean
	 */
	@Bean
	public Docket api() {
		boolean swaggerBaseUrlSet = false;
		if (!localEnv && swaggerBaseUrl != null && !swaggerBaseUrl.isEmpty()) {
			try {
				proto = new URL(swaggerBaseUrl).getProtocol();
				host = new URL(swaggerBaseUrl).getHost();
				port = new URL(swaggerBaseUrl).getPort();
				if (port == -1) {
					hostWithPort = host;
				} else {
					hostWithPort = host + ":" + port;
				}
				swaggerBaseUrlSet = true;
			} catch (MalformedURLException e) {
				logger.error("SwaggerUrlException: ", e);
			}
		}

		Docket docket = new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo()).groupName(TITLE).select()
				.apis(RequestHandlerSelectors.any()).paths(PathSelectors.regex("(?!/(error).*).*")).build();

		if (swaggerBaseUrlSet) {
			docket.protocols(protocols()).host(hostWithPort);
			logger.info("Swagger Base URL: {}://{}", proto, hostWithPort);
		}

		return docket;
	}

	private Set<String> protocols() {
		Set<String> protocols = new HashSet<>();
		protocols.add(proto);
		return protocols;
	}
}
