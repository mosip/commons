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

import io.mosip.kernel.bioextractor.logger.BioExtractorLogger;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;

public class BiometricExtractorFilter implements Filter {
	
	private static final Logger LOGGER = BioExtractorLogger.getLogger(BiometricExtractorFilter.class);

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
		LOGGER.info("", this.getClass().getSimpleName(), "doFilter", "Request at : " + requestTime);
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
	}

}
