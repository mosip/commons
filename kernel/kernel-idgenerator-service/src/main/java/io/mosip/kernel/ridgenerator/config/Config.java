package io.mosip.kernel.ridgenerator.config;

import jakarta.servlet.Filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers {@link ReqResFilter} so RID Spring MVC responses can copy request id/version.
 *
 * @author Sagar Mahapatra
 * @since 1.0.0
 *
 */
@Configuration
public class Config {
	/**
	 * Registers {@link ReqResFilter} at filter order 1.
	 *
	 * @return filter registration for {@link ReqResFilter}
	 */
	@Bean
	public FilterRegistrationBean<Filter> registerReqResFilter() {
		FilterRegistrationBean<Filter> reqResFilter = new FilterRegistrationBean<>();
		reqResFilter.setFilter(getReqResFilter());
		reqResFilter.setOrder(1);
		return reqResFilter;
	}

	/**
	 * Instantiates the request/response caching filter.
	 *
	 * @return {@link ReqResFilter} instance
	 */
	@Bean
	public Filter getReqResFilter() {
		return new ReqResFilter();
	}
}
