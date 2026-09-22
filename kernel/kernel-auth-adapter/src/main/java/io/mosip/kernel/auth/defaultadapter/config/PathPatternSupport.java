package io.mosip.kernel.auth.defaultadapter.config;

import org.springframework.core.env.Environment;
import org.springframework.http.server.PathContainer;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.util.UrlPathHelper;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Maps MOSIP no-auth path strings for Spring Security 7.
 * <p>
 * Uses the same Boot 3.4 switch as MVC:
 * {@code spring.mvc.pathmatch.matching-strategy}.
 * {@code ANT_PATH_MATCHER} keeps Ant ({@code **} in the middle of a path).
 * {@code PATH_PATTERN_PARSER} (Boot 4 default) uses {@link PathPatternParser}
 * against the servlet path (context path is ignored, so
 * {@code /v1/authmanager/actuator/health} matches {@code /actuator/**}).
 * <p>
 * PathPattern is relative to the servlet context path, so a leading Ant
 * any-depth prefix ({@code /}**{@code /}) is stripped. Double-star is only
 * valid as a full path segment unless Ant mode is on.
 */
public final class PathPatternSupport {

	/**
	 * Boot 3.4 / 4 property that selects Ant vs PathPattern.
	 */
	public static final String MATCHING_STRATEGY_PROPERTY = "spring.mvc.pathmatch.matching-strategy";

	/**
	 * PathPattern parser for Boot 4 default matching on {@link #servletPath}.
	 */
	private static final PathPatternParser PATH_PATTERNS = PathPatternParser.defaultInstance;

	/**
	 * Ant matcher used when {@link #MATCHING_STRATEGY_PROPERTY} is
	 * {@code ANT_PATH_MATCHER}.
	 */
	private static final AntPathMatcher ANT = new AntPathMatcher();

	/**
	 * Resolves the path within the servlet context (URI minus context path). Needed
	 * because DispatcherServlet mapped to {@code /} often leaves
	 * {@code getServletPath()} empty and puts the path in {@code getPathInfo()}.
	 */
	private static final UrlPathHelper URL_PATH = new UrlPathHelper();

	/**
	 * Prevents instantiation; all members are static.
	 */
	private PathPatternSupport() {
	}

	/**
	 * {@code true} when {@link #MATCHING_STRATEGY_PROPERTY} is Ant (Boot 3.4
	 * mosip-config). Missing or any other value is PathPattern.
	 *
	 * @param environment Spring environment, possibly {@code null}
	 * @return {@code true} to match with {@link AntPathMatcher}
	 */
	public static boolean isAntPathMatcher(Environment environment) {
		if (environment == null) {
			return false;
		}
		String strategy = environment.getProperty(MATCHING_STRATEGY_PROPERTY);
		if (strategy == null || strategy.isBlank()) {
			return false;
		}
		String normalized = strategy.trim().replace('-', '_');
		return "ant_path_matcher".equalsIgnoreCase(normalized)
				|| "antpathmatcher".equalsIgnoreCase(normalized.replace("_", ""));
	}

	/**
	 * Converts a MOSIP Ant-style exclude pattern into a Spring Security 7
	 * PathPattern string.
	 * <p>
	 * Blank, {@code null}, or {@code *} patterns become {@code /}**. A missing
	 * leading slash is added. Repeated {@code /}**{@code /} prefixes are stripped so the
	 * remainder is context-relative. A {@code **} that is not a full path segment
	 * is reduced to a single {@code *}.
	 *
	 * @param pattern the configured Ant-style path, possibly {@code null}
	 * @return a PathPattern string suitable for {@link PathPatternParser}
	 */
	public static String toPathPattern(String pattern) {
		if (pattern == null || pattern.isBlank() || "*".equals(pattern.trim())) {
			return "/**";
		}
		String path = pattern.trim();
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		while (path.startsWith("/**/")) {
			path = "/" + path.substring(4);
		}
		return path.replaceAll("([^/])\\*\\*", "$1*");
	}

	/**
	 * Leading-slash Ant pattern (no PathPattern rewrite). Blank or {@code *}
	 * becomes {@code /}**.
	 *
	 * @param pattern the configured path, possibly {@code null}
	 * @return an Ant pattern
	 */
	public static String toAntPattern(String pattern) {
		if (pattern == null || pattern.isBlank() || "*".equals(pattern.trim())) {
			return "/**";
		}
		String path = pattern.trim();
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		return path;
	}

	/**
	 * Security matcher for {@code pattern} using Ant or PathPattern per
	 * {@code ant}.
	 *
	 * @param pattern the configured MOSIP path
	 * @param ant     {@code true} for {@link AntPathMatcher}
	 * @return a request matcher
	 */
	public static RequestMatcher requestMatcher(String pattern, boolean ant) {
		if (ant) {
			String antPattern = toAntPattern(pattern);
			return request -> ANT.match(antPattern, servletPath(request));
		}
		try {
			PathPattern parsed = PATH_PATTERNS.parse(toPathPattern(pattern));
			return request -> parsed.matches(PathContainer.parsePath(servletPath(request)));
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return request -> false;
		}
	}

	/**
	 * PathPattern match (Boot 4 default).
	 *
	 * @param request the inbound servlet request
	 * @param pattern the configured Ant-style path
	 * @return {@code true} if the PathPattern matches the servlet path
	 */
	public static boolean matches(HttpServletRequest request, String pattern) {
		return matches(request, pattern, false);
	}

	/**
	 * Match using {@link #MATCHING_STRATEGY_PROPERTY} on {@code environment}.
	 *
	 * @param request     the inbound servlet request
	 * @param pattern     the configured path
	 * @param environment Spring environment
	 * @return {@code true} if the pattern matches
	 */
	public static boolean matches(HttpServletRequest request, String pattern, Environment environment) {
		return matches(request, pattern, isAntPathMatcher(environment));
	}

	/**
	 * Returns whether the request matches {@code pattern}.
	 * <p>
	 * Illegal PathPattern syntax is a non-match rather than thrown.
	 *
	 * @param request the inbound HTTP request
	 * @param pattern the configured path
	 * @param ant     {@code true} for Ant (Boot 3.4); {@code false} for PathPattern
	 * @return {@code true} if the pattern matches
	 */
	public static boolean matches(HttpServletRequest request, String pattern, boolean ant) {
		try {
			if (ant) {
				return ANT.match(toAntPattern(pattern), servletPath(request));
			}
			PathPattern parsed = PATH_PATTERNS.parse(toPathPattern(pattern));
			return parsed.matches(PathContainer.parsePath(servletPath(request)));
		} catch (IllegalArgumentException | IllegalStateException ex) {
			return false;
		}
	}

	/**
	 * Path within the application (context path stripped). Falls back to
	 * {@code /} when empty so Tomcat {@code /*} mappings still match.
	 *
	 * @param request the inbound request
	 * @return a non-empty path starting with {@code /}
	 */
	private static String servletPath(HttpServletRequest request) {
		String path = URL_PATH.getPathWithinApplication(request);
		if (path == null || path.isEmpty()) {
			String uri = request.getRequestURI();
			String contextPath = request.getContextPath();
			if (uri != null) {
				path = uri;
				if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
					path = path.substring(contextPath.length());
				}
				int semi = path.indexOf(';');
				if (semi >= 0) {
					path = path.substring(0, semi);
				}
			}
		}
		if (path == null || path.isEmpty()) {
			return "/";
		}
		return path;
	}
}
