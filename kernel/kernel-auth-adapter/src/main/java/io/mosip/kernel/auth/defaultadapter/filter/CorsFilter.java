package io.mosip.kernel.auth.defaultadapter.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * CORS filter registered before {@link AuthFilter} when
 * {@code mosip.security.cors-enable} is true.
 * <p>
 * Allowed origins come from {@code mosip.security.origins}. OPTIONS preflight
 * is answered with CORS headers and is not passed down the chain.
 * <p>
 * This adapter is a library other MOSIP services put on the classpath.
 *
 * @author Sabbu Uday Kumar
 * @since 1.0.0
 */
public class CorsFilter extends OncePerRequestFilter {
	
	/**
	 * Logger for missing Origin diagnostics.
	 */
	private static final Logger LOGGER= LoggerFactory.getLogger(CorsFilter.class);
	
	
	/**
	 * Allowed Origin header values (comma-split from configuration).
	 */
	private List<String> origins;
	
	/**
	 * Splits {@code origins} on commas into {@link #origins}.
	 *
	 * @param origins comma-separated allowed origins, e.g. {@code localhost:8080}
	 */
	public CorsFilter(String origins) {
		this.origins=Arrays.asList(origins.split(","));
		this.origins.parallelStream().forEach(x -> x.trim());
	}

	/**
	 * Sets CORS response headers and continues the chain except for OPTIONS.
	 *
	 * @param request     the inbound request
	 * @param response    the outbound response
	 * @param filterChain the remaining filter chain
	 * @throws ServletException if the chain throws
	 * @throws IOException      if the chain throws
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String origin=request.getHeader("Origin");
		if(origin==null || origin.isEmpty()) {
			LOGGER.info("origin {}",origin);
			LOGGER.info("requesturl {}", request.getRequestURL().toString());
		}
		else if (origins != null && !origins.isEmpty() && origins.contains(origin)) {
			response.setHeader("Access-Control-Allow-Origin", origin);
		} 
		response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT, PATCH");
		response.setHeader("Access-Control-Allow-Headers", 
				"Origin,Date, Content-Type, Accept, X-Requested-With, Authorization, From, X-Auth-Token, Request-Id");
		response.setHeader("Access-Control-Expose-Headers", "Set-Cookie");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		if (!"OPTIONS".equalsIgnoreCase(request.getMethod())) {
			filterChain.doFilter(request, response);
		}
	}
}
