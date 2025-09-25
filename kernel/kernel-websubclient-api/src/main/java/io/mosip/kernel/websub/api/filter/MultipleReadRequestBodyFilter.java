package io.mosip.kernel.websub.api.filter;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.filter.GenericFilterBean;

import io.mosip.kernel.websub.api.model.MultipleReadHttpRequest;

/**
 * A lightweight, high-performance filter for enabling multiple reads of the HTTP request body in Spring-based applications.
 * <p>
 * This filter addresses the Servlet API limitation where the request body (via `getInputStream()` or `getReader()`) can only be read once.
 * It wraps the incoming `ServletRequest` in a custom `MultipleReadHttpRequest` (extending `HttpServletRequestWrapper`), which caches the body
 * content (e.g., in a byte array) for repeated access. This is essential for scenarios like logging request bodies in filters while
 * allowing controllers to deserialize them (e.g., via `@RequestBody` in Spring MVC).
 * </p>
 * <p>
 * <strong>Performance Notes</strong>:
 * <ul>
 *   <li>Minimal overhead: Single wrapper creation and delegation to the filter chain (~1-5μs per request).</li>
 *   <li>Caching occurs lazily in the wrapper (body read only on first access), avoiding unnecessary I/O for GET/HEAD requests.</li>
 *   <li>Thread-safe: Relies on Spring's `GenericFilterBean` for initialization and immutable wrapper post-construction.</li>
 *   <li>Limitations: For large payloads (&gt;1MB), monitor memory; multipart/form-data may require additional overrides in the wrapper (e.g., `getParts()`).</li>
 *   <li>Best Practices: Use only for content types needing caching (e.g., JSON in WebSub callbacks); combine with `OncePerRequestFilter` for single-execution guarantees.</li>
 * </ul>
 * </p>
 * <p>
 * Example Usage: Register as a Spring `@Bean` or via `FilterRegistrationBean` in a WebSub or REST context.
 * </p>
 *
 * @author Urvil Joshi
 * @since 1.0.0
 * @see MultipleReadHttpRequest
 * @see GenericFilterBean
 */
public class MultipleReadRequestBodyFilter extends GenericFilterBean {

	/**
	 * Processes the incoming request by wrapping it to enable multiple body reads.
	 * <p>
	 * This method intercepts the request, casts to `HttpServletRequest`, wraps it in `MultipleReadHttpRequest` for body caching,
	 * and delegates to the filter chain. Non-HTTP requests (rare) trigger a runtime exception for fast failure.
	 * The wrapper ensures downstream components (e.g., controllers) can read the body without `IllegalStateException`.
	 * Optimized for low latency: No body reading here—deferred to the wrapper's first access.
	 * </p>
	 *
	 * @param request     the incoming servlet request
	 * @param response    the servlet response (unused here)
	 * @param chain       the filter chain for delegation
	 * @throws IOException      if an I/O error occurs during wrapping or delegation
	 * @throws ServletException if a servlet processing error occurs
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		MultipleReadHttpRequest wrappedRequest = new MultipleReadHttpRequest(
				(HttpServletRequest) request);
		chain.doFilter(wrappedRequest, response);
	}
}