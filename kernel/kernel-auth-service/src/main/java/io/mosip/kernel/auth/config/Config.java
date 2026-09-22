package io.mosip.kernel.auth.config;

import jakarta.servlet.Filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

/**
 * Servlet filter registration and shared Jackson mapper beans for authmanager.
 *
 * @author Raj Jha
 * 
 * @since 1.0.0
 *
 */

@Configuration
public class Config {

	/**
	 * Registers {@link CorsFilter} as the first servlet filter (order {@code 0}).
	 *
	 * @return registration bean wrapping the CORS filter
	 */
	@Bean(name = "CorsFilter")
	public FilterRegistrationBean<Filter> registerCORSFilterBean() {
		FilterRegistrationBean<Filter> corsBean = new FilterRegistrationBean<>();
		corsBean.setFilter(registerCORSFilter());
		corsBean.setOrder(0);
		return corsBean;
	}

	/**
	 * Registers {@link ReqResFilter} after CORS (order {@code 1}) so request and
	 * response bodies can be cached for logging and {@code ResponseBodyAdvice}.
	 *
	 * @return registration bean wrapping the request/response wrapper filter
	 */
	@Bean(name = "ReqResponseFilter")
	public FilterRegistrationBean<Filter> registerReqResFilterBean() {
		FilterRegistrationBean<Filter> reqResFilter = new FilterRegistrationBean<>();
		reqResFilter.setFilter(getReqResFilter());
		reqResFilter.setOrder(1);
		return reqResFilter;
	}

	/**
	 * Instantiates the CORS servlet filter.
	 *
	 * @return a new {@link CorsFilter}
	 */
	@Bean
	public Filter registerCORSFilter() {
		return new CorsFilter();
	}

	/**
	 * Instantiates the request/response caching servlet filter.
	 *
	 * @return a new {@link ReqResFilter}
	 */
	@Bean
	public Filter getReqResFilter() {
		return new ReqResFilter();
	}

	/**
	 * Commons request logging filter that includes query string, payload, and
	 * headers. Payload is capped at 100000 bytes.
	 *
	 * @return configured {@link CommonsRequestLoggingFilter}
	 */
	@Bean
	public CommonsRequestLoggingFilter logFilter() {
		CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
		filter.setIncludeQueryString(true);
		filter.setIncludePayload(true);
		filter.setMaxPayloadLength(100000);
		filter.setIncludeHeaders(true);
		filter.setAfterMessagePrefix("REQUEST DATA : ");
		return filter;
	}

	/**
	 * Jackson {@link ObjectMapper} with Afterburner for faster introspection and
	 * {@link JavaTimeModule} for Java 8 date/time types.
	 *
	 * @return shared object mapper used by HTTP advice and services
	 */
	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper objectMapper = JsonMapper.builder()
			    .addModule(new AfterburnerModule())
			    .build();
		objectMapper.registerModule(new JavaTimeModule());
		return objectMapper;
	}
}
