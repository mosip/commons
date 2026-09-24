package io.mosip.kernel.config.server.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Serves springdoc Swagger UI on the main port under {@code /swagger-ui/**}.
 * <p>
 * Config Server {@code EnvironmentController} maps {@code /{name}/{profiles}} and would
 * otherwise treat {@code /swagger-ui/index.html} as an application/profile lookup (404).
 * </p>
 */
@Configuration
public class SpringDocWebConfig {

	private static final String INDEX_HTML = """
			<!DOCTYPE html>
			<html lang="en">
			<head>
			  <meta charset="UTF-8">
			  <title>Config Server — Swagger UI</title>
			  <link rel="stylesheet" type="text/css" href="./swagger-ui.css">
			</head>
			<body>
			  <div id="swagger-ui"></div>
			  <script src="./swagger-ui-bundle.js"></script>
			  <script src="./swagger-ui-standalone-preset.js"></script>
			  <script>
			    window.onload = function () {
			      window.ui = SwaggerUIBundle({
			        url: "%s/apidocs",
			        dom_id: "#swagger-ui",
			        deepLinking: true,
			        validatorUrl: null,
			        presets: [SwaggerUIBundle.presets.apis, SwaggerUIStandalonePreset],
			        layout: "StandaloneLayout"
			      });
			    };
			  </script>
			</body>
			</html>
			""";

	/**
	 * Registers a highest-precedence filter for {@code /swagger-ui} assets.
	 *
	 * @return filter registration
	 */
	@Bean
	public FilterRegistrationBean<OncePerRequestFilter> swaggerUiFilter() {
		String version = resolveSwaggerUiVersion();
		ResourceHttpRequestHandler resourceHandler = new ResourceHttpRequestHandler();
		resourceHandler.setLocations(
				List.of(new ClassPathResource("META-INF/resources/webjars/swagger-ui/" + version + "/")));
		resourceHandler.setResourceResolvers(List.of(new PathResourceResolver()));

		OncePerRequestFilter filter = new OncePerRequestFilter() {
			@Override
			protected void initFilterBean() throws ServletException {
				initSwaggerUiResources(resourceHandler, getServletContext());
			}

			@Override
			protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
					FilterChain filterChain) throws ServletException, IOException {
				String contextPath = request.getContextPath();
				String path = request.getRequestURI().substring(contextPath.length());

				if ("/swagger-ui".equals(path) || "/swagger-ui/".equals(path)) {
					response.sendRedirect(contextPath + "/swagger-ui/index.html");
					return;
				}
				if ("/swagger-ui/index.html".equals(path)) {
					byte[] body = INDEX_HTML.formatted(contextPath).getBytes(StandardCharsets.UTF_8);
					response.setStatus(HttpServletResponse.SC_OK);
					response.setCharacterEncoding(StandardCharsets.UTF_8.name());
					response.setContentType("text/html;charset=UTF-8");
					response.setContentLength(body.length);
					response.getOutputStream().write(body);
					return;
				}
				if (path.startsWith("/swagger-ui/")) {
					String assetPath = path.substring("/swagger-ui".length());
					HttpServletRequest wrapped = new SwaggerUiRequestWrapper(request, contextPath, assetPath);
					wrapped.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, assetPath);
					wrapped.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/**");
					resourceHandler.handleRequest(wrapped, response);
					return;
				}
				filterChain.doFilter(request, response);
			}
		};

		FilterRegistrationBean<OncePerRequestFilter> registration = new FilterRegistrationBean<>();
		registration.setFilter(filter);
		registration.addUrlPatterns("/swagger-ui", "/swagger-ui/*");
		registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
		registration.setName("swaggerUiFilter");
		return registration;
	}

	private static String resolveSwaggerUiVersion() {
		return resolveSwaggerUiVersion(
				new ClassPathResource("META-INF/maven/org.webjars/swagger-ui/pom.properties"));
	}

	/** Package-visible for tests; falls back when the webjar properties file is absent. */
	static String resolveSwaggerUiVersion(ClassPathResource props) {
		try (InputStream in = props.getInputStream()) {
			Properties p = new Properties();
			p.load(in);
			return p.getProperty("version", "5.32.14");
		} catch (IOException e) {
			return "5.32.14";
		}
	}

	/** Package-visible for tests. */
	static void initSwaggerUiResources(ResourceHttpRequestHandler resourceHandler,
			jakarta.servlet.ServletContext servletContext) throws ServletException {
		try {
			resourceHandler.setServletContext(servletContext);
			resourceHandler.afterPropertiesSet();
		} catch (Exception e) {
			throw new ServletException("Failed to init swagger-ui resources", e);
		}
	}

	/**
	 * Rewrites {@code /swagger-ui/...} to the webjar-relative path for
	 * {@link ResourceHttpRequestHandler}.
	 */
	static final class SwaggerUiRequestWrapper extends HttpServletRequestWrapper {

		private final String contextPath;
		private final String assetPath;

		SwaggerUiRequestWrapper(HttpServletRequest request, String contextPath, String assetPath) {
			super(request);
			this.contextPath = contextPath;
			this.assetPath = assetPath;
		}

		@Override
		public String getRequestURI() {
			return contextPath + assetPath;
		}

		@Override
		public String getServletPath() {
			return assetPath;
		}

		@Override
		public String getPathInfo() {
			return null;
		}
	}
}
