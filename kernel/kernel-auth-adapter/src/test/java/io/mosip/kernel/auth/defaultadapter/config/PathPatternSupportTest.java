package io.mosip.kernel.auth.defaultadapter.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;

import org.junit.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Conversion and match behaviour for MOSIP path strings (PathPattern default,
 * Ant when matching-strategy is ANT_PATH_MATCHER).
 */
public class PathPatternSupportTest {

	@Test
	public void toPathPatternNormalizesBlankAndStar() {
		assertEquals("/**", PathPatternSupport.toPathPattern(null));
		assertEquals("/**", PathPatternSupport.toPathPattern("  "));
		assertEquals("/**", PathPatternSupport.toPathPattern("*"));
	}

	@Test
	public void toPathPatternAddsSlashStripsPrefixAndGluedDoubleStar() {
		assertEquals("/actuator/**", PathPatternSupport.toPathPattern("actuator/**"));
		assertEquals("/actuator/**", PathPatternSupport.toPathPattern("/**/actuator/**"));
		assertEquals("/favicon*", PathPatternSupport.toPathPattern("/favicon**"));
	}

	@Test
	public void matchesActuatorHealth() {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
		request.setServletPath("/actuator/health");
		assertTrue(PathPatternSupport.matches(request, "/actuator/**"));
		assertFalse(PathPatternSupport.matches(request, "/swagger-ui/**"));
	}

	@Test
	public void matchesSwaggerUiWithEmptyServletPath() {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/authmanager/swagger-ui/index.html");
		request.setContextPath("/v1/authmanager");
		request.setServletPath("");
		request.setPathInfo("/swagger-ui/index.html");
		assertTrue(PathPatternSupport.matches(request, "/swagger-ui/**"));
		assertTrue(PathPatternSupport.matches(request, "/**/swagger-ui/**"));
		assertTrue(PathPatternSupport.requestMatcher("/swagger-ui/**", false).matches(request));
		assertTrue(PathPatternSupport.matches(request, "/swagger-ui/**", true));
	}

	@Test
	public void matchesOpenApiDocsWithContextPath() {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/authmanager/v3/api-docs");
		request.setContextPath("/v1/authmanager");
		request.setServletPath("");
		request.setPathInfo("/v3/api-docs");
		assertTrue(PathPatternSupport.matches(request, "/v3/api-docs"));
		assertTrue(PathPatternSupport.matches(request, "/v3/api-docs/**"));
	}

	@Test
	public void matchesIllegalPatternIsFalse() {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/a/details");
		request.setServletPath("/api/a/details");
		assertFalse(PathPatternSupport.matches(request, "/api/**/details"));
	}

	@Test
	public void privateConstructorIsInvocable() throws Exception {
		Constructor<PathPatternSupport> constructor = PathPatternSupport.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		constructor.newInstance();
	}

	@Test
	public void toAntPatternKeepsMiddleDoubleStar() {
		assertEquals("/**", PathPatternSupport.toAntPattern(null));
		assertEquals("/**", PathPatternSupport.toAntPattern("*"));
		assertEquals("/api/**/details", PathPatternSupport.toAntPattern("api/**/details"));
	}

	@Test
	public void isAntPathMatcherReadsBoot34Property() {
		MockEnvironment env = new MockEnvironment();
		assertFalse(PathPatternSupport.isAntPathMatcher(null));
		assertFalse(PathPatternSupport.isAntPathMatcher(env));
		env.setProperty(PathPatternSupport.MATCHING_STRATEGY_PROPERTY, "PATH_PATTERN_PARSER");
		assertFalse(PathPatternSupport.isAntPathMatcher(env));
		env.setProperty(PathPatternSupport.MATCHING_STRATEGY_PROPERTY, "ANT_PATH_MATCHER");
		assertTrue(PathPatternSupport.isAntPathMatcher(env));
		env.setProperty(PathPatternSupport.MATCHING_STRATEGY_PROPERTY, "ant_path_matcher");
		assertTrue(PathPatternSupport.isAntPathMatcher(env));
		env.setProperty(PathPatternSupport.MATCHING_STRATEGY_PROPERTY, "ant-path-matcher");
		assertTrue(PathPatternSupport.isAntPathMatcher(env));
	}

	@Test
	public void antModeMatchesDoubleStarInMiddle() {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/a/details");
		request.setServletPath("/api/a/details");
		assertTrue(PathPatternSupport.matches(request, "/api/**/details", true));
		assertFalse(PathPatternSupport.matches(request, "/api/**/details", false));
		assertFalse(PathPatternSupport.matches(request, "/api/**/details"));
	}

	@Test
	public void matchesUsesEnvironmentStrategy() {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/a/details");
		request.setServletPath("/api/a/details");
		MockEnvironment env = new MockEnvironment();
		env.setProperty(PathPatternSupport.MATCHING_STRATEGY_PROPERTY, "ANT_PATH_MATCHER");
		assertTrue(PathPatternSupport.matches(request, "/api/**/details", env));
		env.setProperty(PathPatternSupport.MATCHING_STRATEGY_PROPERTY, "PATH_PATTERN_PARSER");
		assertFalse(PathPatternSupport.matches(request, "/api/**/details", env));
		assertTrue(PathPatternSupport.requestMatcher("/api/**/details", true).matches(request));
		assertFalse(PathPatternSupport.requestMatcher("/api/**/details", false).matches(request));
	}
}
