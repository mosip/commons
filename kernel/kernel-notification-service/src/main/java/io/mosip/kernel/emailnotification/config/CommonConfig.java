package io.mosip.kernel.emailnotification.config;

import jakarta.servlet.Filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonConfig {
	
	/**
	 * Bean to register RequestResponse Filter.
	 * 
	 * @return reqResFilter.
	 */
	@Bean
	public FilterRegistrationBean<Filter> registerReqResFilter() {
		FilterRegistrationBean<Filter> reqResFilter = new FilterRegistrationBean<>();
		reqResFilter.setFilter(getReqResFilter());
		reqResFilter.setOrder(1);
		return reqResFilter;
	}

	/**
	 * Bean for RequestResponseFilter.
	 * 
	 * @return reqResFilter object.
	 */
	@Bean
	public Filter getReqResFilter() {
		return new ReqResFilter();
	}

}
