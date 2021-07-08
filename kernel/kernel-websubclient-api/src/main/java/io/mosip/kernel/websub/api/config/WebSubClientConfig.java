package io.mosip.kernel.websub.api.config;

import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.websub.api.filter.IntentVerificationFilter;
import io.mosip.kernel.websub.api.filter.MultipleReadRequestBodyFilter;
import io.mosip.kernel.websub.api.verifier.IntentVerifier;

/**
 * This class consist all the general and common configurations for this api.
 * 
 * @author Urvil Joshi
 *
 */
@Configuration
@EnableAspectJAutoProxy
public class WebSubClientConfig {

	@Value("${mosip.auth.filter_disable:true}")
	private boolean isAuthFilterDisable;

	@Autowired
	private IntentVerifier intentVerifier;

	@Bean(name = "websubRestTemplate")
	public RestTemplate restTemplate() {

		return new RestTemplate();

	}

	@Bean(name = "intentVerificationFilterBean")
	public FilterRegistrationBean<Filter> registerIntentVerificationFilterFilterBean() {
		FilterRegistrationBean<Filter> reqResFilter = new FilterRegistrationBean<>();
		reqResFilter.setFilter(registerIntentVerificationFilter());
		return reqResFilter;
	}

	@Bean
	public IntentVerificationFilter registerIntentVerificationFilter() {
		return new IntentVerificationFilter(intentVerifier);
	}

	@Bean(name = "cachingRequestBodyFilter")
	public FilterRegistrationBean<Filter> registerCachingRequestBodyFilterBean() {
		FilterRegistrationBean<Filter> reqResFilter = new FilterRegistrationBean<>();
		reqResFilter.setFilter(registerCachingRequestBodyFilter());
		reqResFilter.setOrder(0);
		return reqResFilter;
	}

	@Bean
	public MultipleReadRequestBodyFilter registerCachingRequestBodyFilter() {
		return new MultipleReadRequestBodyFilter();
	}

}
