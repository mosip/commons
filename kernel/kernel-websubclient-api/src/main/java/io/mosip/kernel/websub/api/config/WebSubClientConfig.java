package io.mosip.kernel.websub.api.config;

import jakarta.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import io.mosip.kernel.core.websub.spi.SubscriptionClient;
import io.mosip.kernel.core.websub.spi.SubscriptionExtendedClient;
import io.mosip.kernel.websub.api.aspects.WebSubClientAspect;
import io.mosip.kernel.websub.api.client.SubscriberClientImpl;
import io.mosip.kernel.websub.api.filter.IntentVerificationFilter;
import io.mosip.kernel.websub.api.filter.MultipleReadRequestBodyFilter;
import io.mosip.kernel.websub.api.model.FailedContentRequest;
import io.mosip.kernel.websub.api.model.FailedContentResponse;
import io.mosip.kernel.websub.api.model.SubscriptionChangeRequest;
import io.mosip.kernel.websub.api.model.SubscriptionChangeResponse;
import io.mosip.kernel.websub.api.model.UnsubscriptionRequest;
import io.mosip.kernel.websub.api.verifier.AuthenticatedContentVerifier;
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

	@Bean
	public IntentVerifier intentVerifier() {
		return new IntentVerifier();
	}

	@Bean
	public AuthenticatedContentVerifier authenticatedContentVerifier() {
		return new AuthenticatedContentVerifier();
	}

	@Bean(name = "intentVerificationFilterBean")
	public FilterRegistrationBean<Filter> registerIntentVerificationFilterFilterBean(@Autowired IntentVerifier intentVerifier) {
		FilterRegistrationBean<Filter> reqResFilter = new FilterRegistrationBean<>();
		reqResFilter.setFilter(registerIntentVerificationFilter(intentVerifier));
		return reqResFilter;
	}

	@Bean
	public IntentVerificationFilter registerIntentVerificationFilter(IntentVerifier intentVerifier) {
		System.out.println("inside intentVerification filter");
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
	

	@Bean
	public SubscriptionClient<SubscriptionChangeRequest, UnsubscriptionRequest, SubscriptionChangeResponse> subscriptionClient(){
		return new SubscriberClientImpl();
	}

	@Bean
	public SubscriptionExtendedClient<FailedContentResponse, FailedContentRequest> subscriptionExtendedClient(){
		return new SubscriberClientImpl();
	}

	@Bean
	public WebSubClientAspect webSubClientAspect(){
		return new WebSubClientAspect();
	}
	

}
