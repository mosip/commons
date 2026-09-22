package io.mosip.kernel.auth.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * Binds {@code openapi.*} Spring properties used to populate the Springdoc
 * OpenAPI info block and server list for auth-manager.
 */
@Configuration
@ConfigurationProperties(prefix = "openapi")
@Data
public class OpenApiProperties {

	/**
	 * API title, description, version, and license metadata.
	 */
	private InfoProperty info;

	/**
	 * Server URLs advertised in the OpenAPI document.
	 */
	private Service service;

	/**
	 * OpenAPI {@code info} object: title, description, version, and license.
	 */
	@Data
	public static class InfoProperty {

		/**
		 * OpenAPI info title.
		 */
		private String title;

		/**
		 * OpenAPI info description.
		 */
		private String description;

		/**
		 * OpenAPI info version string.
		 */
		private String version;

		/**
		 * Optional license name and URL.
		 */
		private LicenseProperty license;
	}

	/**
	 * OpenAPI license name and URL under {@code info.license}.
	 */
	@Data
	public static class LicenseProperty {

		/**
		 * License display name.
		 */
		private String name;

		/**
		 * License document URL.
		 */
		private String url;
	}

	/**
	 * OpenAPI {@code servers} list wrapper bound from {@code openapi.service}.
	 */
	@Data
	public static class Service {

		/**
		 * Server entries (description plus URL) published in the OpenAPI document.
		 */
		private List<Server> servers;
	}

	/**
	 * A single OpenAPI server object.
	 */
	@Data
	public static class Server {

		/**
		 * Human-readable server description.
		 */
		private String description;

		/**
		 * Server base URL.
		 */
		private String url;
	}
}
