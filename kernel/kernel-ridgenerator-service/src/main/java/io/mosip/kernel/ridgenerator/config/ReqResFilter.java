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

	/*
	 * (non-Javadoc)
	 * 
	 * @see jakarta.servlet.Filter#init(jakarta.servlet.FilterConfig)
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// over-ridden method
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jakarta.servlet.Filter#doFilter(jakarta.servlet.ServletRequest,
	 * jakarta.servlet.ServletResponse, jakarta.servlet.FilterChain)
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
		requestWrapper = new ContentCachingRequestWrapper(httpServletRequest);
		responseWrapper = new ContentCachingResponseWrapper(httpServletResponse);
		chain.doFilter(requestWrapper, responseWrapper);
		responseWrapper.copyBodyToResponse();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jakarta.servlet.Filter#destroy()
	 */
	@Override
	public void destroy() {
		// over-ridden method
	}
}
