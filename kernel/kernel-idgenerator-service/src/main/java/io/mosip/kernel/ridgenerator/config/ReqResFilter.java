package io.mosip.kernel.ridgenerator.config;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 * Request Response Filter class that implements {@link Filter}.
 * 
 * @author Sagar Mahapatra
 * @since 1.0.0
 *
 */
public class ReqResFilter implements Filter {

	/**
	 * No-op filter initialization.
	 *
	 * @param filterConfig servlet filter configuration
	 * @throws ServletException never thrown
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// over-ridden method
	}

	/**
	 * Wraps the request and response so {@link ResponseBodyAdviceConfig} can read the cached body.
	 * <p>
	 * URIs ending with {@code .stream} skip wrapping.
	 * </p>
	 *
	 * @param request  servlet request
	 * @param response servlet response
	 * @param chain    remaining filter chain
	 * @throws IOException      when wrapping or copying the body fails
	 * @throws ServletException when the chain throws
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		HttpServletResponse httpServletResponse = (HttpServletResponse) response;
		ContentCachingRequestWrapper requestWrapper = null;
		ContentCachingResponseWrapper responseWrapper = null;

		// Default processing for url ends with .stream
		if (httpServletRequest.getRequestURI().endsWith(".stream")) {
			chain.doFilter(request, response);
			return;
		}
		requestWrapper = new ContentCachingRequestWrapper(httpServletRequest, 1024 * 1024);
		responseWrapper = new ContentCachingResponseWrapper(httpServletResponse);
		chain.doFilter(requestWrapper, responseWrapper);
		responseWrapper.copyBodyToResponse();

	}

	/**
	 * No-op filter destruction.
	 */
	@Override
	public void destroy() {
		// over-ridden method
	}
}
