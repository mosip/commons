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

/**
 * Servlet filter that sets Access-Control headers so browser clients can call
 * authmanager across origins. Reflects the request {@code Origin} when present
 * and allows credentials. CORS preflight {@code OPTIONS} requests are answered
 * with headers only and are not forwarded down the chain.
 * 
 * @author Mindtree Ltd.
 *
 */
public class CorsFilter implements Filter {
	/**
	 * Default Constructor
	 */
	public CorsFilter() {
		// Default Constructor
	}

	/**
	 * Writes CORS response headers and continues the filter chain for non-OPTIONS
	 * methods.
	 *
	 * @param req   incoming servlet request
	 * @param res   servlet response used to set Access-Control headers
	 * @param chain remaining filters and the target resource
	 * @throws IOException      if the chain fails while writing
	 * @throws ServletException if the chain fails while processing
	 */
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		String origin = request.getHeader("Origin");
		if (origin != null && !origin.isEmpty()) {
			response.setHeader("Access-Control-Allow-Origin", origin);
		}
		response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT, PATCH");
		response.setHeader("Access-Control-Allow-Headers",
				"Date, Content-Type, Accept, X-Requested-With, Authorization, From, X-Auth-Token, Request-Id");
		response.setHeader("Access-Control-Expose-Headers", "Set-Cookie");
		response.setHeader("Access-Control-Allow-Credentials", "true");

		if (!"OPTIONS".equalsIgnoreCase(request.getMethod())) {
			chain.doFilter(req, res);
		}
	}

	/**
	 * Filter lifecycle hook; no initialization is required.
	 *
	 * @param filterConfig servlet filter configuration (unused)
	 */
	@Override
	public void init(FilterConfig filterConfig) {
		// init method from Filter
	}

	/**
	 * Filter lifecycle hook; no resources are held.
	 */
	@Override
	public void destroy() {
		// destroy method from Filter
	}
}
