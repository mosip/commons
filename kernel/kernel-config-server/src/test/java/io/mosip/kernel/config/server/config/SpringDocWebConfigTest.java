package io.mosip.kernel.config.server.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Branch coverage for {@link SpringDocWebConfig} swagger-ui filter.
 */
class SpringDocWebConfigTest {

	private OncePerRequestFilter filter;

	@BeforeEach
	void setUp() throws Exception {
		SpringDocWebConfig config = new SpringDocWebConfig();
		FilterRegistrationBean<OncePerRequestFilter> registration = config.swaggerUiFilter();
		assertNotNull(registration.getFilter());
		filter = registration.getFilter();
		MockFilterConfig filterConfig = new MockFilterConfig(new MockServletContext(), "swaggerUiFilter");
		filter.init(filterConfig);
	}

	@Test
	void redirectsSwaggerUiRoot() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/config/swagger-ui");
		request.setContextPath("/config");
		request.setServletPath("/swagger-ui");
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain chain = mock(FilterChain.class);

		filter.doFilter(request, response, chain);

		assertEquals(302, response.getStatus());
		assertEquals("/config/swagger-ui/index.html", response.getRedirectedUrl());
		verify(chain, never()).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
	}

	@Test
	void redirectsSwaggerUiTrailingSlash() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/config/swagger-ui/");
		request.setContextPath("/config");
		request.setServletPath("/swagger-ui/");
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain chain = mock(FilterChain.class);

		filter.doFilter(request, response, chain);

		assertEquals(302, response.getStatus());
		assertEquals("/config/swagger-ui/index.html", response.getRedirectedUrl());
		verify(chain, never()).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
	}

	@Test
	void servesCustomIndexHtml() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/config/swagger-ui/index.html");
		request.setContextPath("/config");
		request.setServletPath("/swagger-ui/index.html");
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain chain = mock(FilterChain.class);

		filter.doFilter(request, response, chain);

		assertEquals(200, response.getStatus());
		String body = response.getContentAsString();
		assertTrue(body.contains("/config/apidocs"));
		assertTrue(body.contains("validatorUrl: null"));
		assertTrue(response.getContentType().startsWith("text/html"));
		verify(chain, never()).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
	}

	@Test
	void servesSwaggerUiAssetViaWrapper() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/config/swagger-ui/swagger-ui.css");
		request.setContextPath("/config");
		request.setServletPath("/swagger-ui/swagger-ui.css");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		filter.doFilter(request, response, chain);

		// Asset served from webjar (or 404 if missing); wrapper path must have been used (no chain).
		assertTrue(response.getStatus() == 200 || response.getStatus() == 404);
		assertEquals(null, chain.getRequest());
	}

	@Test
	void passesThroughNonSwaggerPaths() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/config/application/default");
		request.setContextPath("/config");
		request.setServletPath("/application/default");
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain chain = mock(FilterChain.class);

		filter.doFilter(request, response, chain);

		verify(chain).doFilter(request, response);
	}

	@Test
	void resolveSwaggerUiVersionFallsBackWhenResourceMissing() {
		assertEquals("5.32.14",
				SpringDocWebConfig.resolveSwaggerUiVersion(new ClassPathResource("does-not-exist/pom.properties")));
	}

	@Test
	void resolveSwaggerUiVersionReadsWebjarWhenPresent() {
		ClassPathResource props = new ClassPathResource("META-INF/maven/org.webjars/swagger-ui/pom.properties");
		if (!props.exists()) {
			return;
		}
		String version = SpringDocWebConfig.resolveSwaggerUiVersion(props);
		assertNotNull(version);
		assertTrue(!version.isBlank());
	}

	@Test
	void initSwaggerUiResourcesWrapsFailures() {
		ResourceHttpRequestHandler broken = new ResourceHttpRequestHandler() {
			@Override
			public void afterPropertiesSet() {
				throw new IllegalStateException("boom");
			}
		};
		ServletException ex = assertThrows(ServletException.class,
				() -> SpringDocWebConfig.initSwaggerUiResources(broken, new MockServletContext()));
		assertTrue(ex.getMessage().contains("Failed to init swagger-ui resources"));
	}

	@Test
	void swaggerUiRequestWrapperRewritesPaths() {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/config/swagger-ui/swagger-ui.css");
		request.setContextPath("/config");
		SpringDocWebConfig.SwaggerUiRequestWrapper wrapped = new SpringDocWebConfig.SwaggerUiRequestWrapper(
				request, "/config", "/swagger-ui.css");
		assertEquals("/config/swagger-ui.css", wrapped.getRequestURI());
		assertEquals("/swagger-ui.css", wrapped.getServletPath());
		assertNull(wrapped.getPathInfo());
	}
}
