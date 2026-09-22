package io.mosip.kernel.auth.config;

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
 * This class is for input logging of all parameters in HTTP requests.
 * <p>
 * Wraps the request with {@link ContentCachingRequestWrapper} (1 MiB cache
 * limit, Spring 7 constructor {@code ContentCachingRequestWrapper(request, 1024*1024)})
 * and the response with {@link ContentCachingResponseWrapper} so
 * {@link ResponseBodyAdviceConfig} can copy id/version from the cached body.
 * URIs ending with {@code .stream} skip wrapping.
 * 
 * @author Bal Vikash Sharma
 *
 */
public class ReqResFilter implements Filter {

	/**
	 * Filter lifecycle hook; no initialization is required.
	 *
	 * @param filterConfig servlet filter configuration (unused)
	 * @throws ServletException never thrown by this implementation
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// init method overriding
	}

	/**
	 * Wraps request and response for content caching, then copies the cached
	 * response body back to the client.
	 *
	 * @param request  incoming servlet request
	 * @param response servlet response
	 * @param chain    remaining filters and the target resource
	 * @throws IOException      if wrapping or copying the body fails
	 * @throws ServletException if the chain fails while processing
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
	 * Filter lifecycle hook; no resources are held.
	 */
	@Override
	public void destroy() {
		// Auto-generated method stub
	}
}
