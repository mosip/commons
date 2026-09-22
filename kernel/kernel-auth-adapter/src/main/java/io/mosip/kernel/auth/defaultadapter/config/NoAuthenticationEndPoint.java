package io.mosip.kernel.auth.defaultadapter.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * Binds {@code mosip.global} and {@code mosip.service} no-auth path lists used
 * by {@link SecurityConfig} and {@link io.mosip.kernel.auth.defaultadapter.filter.AuthFilter}.
 * <p>
 * Patterns are stored as historical Ant-style strings. {@link PathPatternSupport}
 * applies them with {@code spring.mvc.pathmatch.matching-strategy} (Ant or
 * PathPattern), the same Boot 3.4 switch as MVC.
 * <p>
 * This adapter is a library other MOSIP services put on the classpath.
 *
 * @author GOVINDARAJ VELU
 */
@Configuration
@ConfigurationProperties(prefix = "mosip")
@Data
public class NoAuthenticationEndPoint {

	/**
	 * Platform-wide paths that skip authentication for every hosting service.
	 */
	private GlobalEndPoint global;
	/**
	 * Service-specific paths that skip authentication when the request context
	 * path matches {@link #serviceContext}.
	 */
	private ServiceEndPoint service;
	/**
	 * Servlet context path of the hosting service (compared to
	 * {@code HttpServletRequest.getServletContext().getContextPath()}).
	 */
	private String serviceContext;
	
	/**
	 * Nested property group for {@code mosip.global.end-points}.
	 */
	@Data
	public static class GlobalEndPoint {
		/**
		 * Ant-style paths permitted without authentication for all services.
		 */
		List<String> endPoints;
	}
	
	/**
	 * Nested property group for {@code mosip.service.end-points}.
	 */
	@Data
	public static class ServiceEndPoint {
		/**
		 * Ant-style paths permitted without authentication for this service,
		 * subject to allowed HTTP methods in AuthFilter.
		 */
		List<String> endPoints;
	}
}
