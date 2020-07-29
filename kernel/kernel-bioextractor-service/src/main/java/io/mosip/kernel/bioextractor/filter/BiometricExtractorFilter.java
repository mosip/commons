package io.mosip.kernel.bioextractor.filter;

import java.io.IOException;
import java.time.LocalDateTime;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.kernel.core.util.DateUtils;

public class BiometricExtractorFilter implements Filter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BiometricExtractorFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		String reqUrl = ((HttpServletRequest) request).getRequestURL().toString();
		if (reqUrl.contains("swagger") || reqUrl.contains("api-docs") || reqUrl.contains("actuator")) {
			chain.doFilter(request, response);
			return;
		}
		
		LocalDateTime requestTime = DateUtils.getUTCCurrentDateTime();
		LOGGER.info("Request at : " + requestTime);
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
	}

}
