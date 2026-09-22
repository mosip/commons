package io.mosip.kernel.websub.api.config;

import jakarta.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import com.fasterxml.jackson.databind.ObjectMapper;

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

	/**
	 * Jackson mapper for failed-content JSON when no application {@link ObjectMapper} exists.
	 *
	 * @return new {@link ObjectMapper}
	 */
	@Bean
	@ConditionalOnMissingBean(ObjectMapper.class)
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

	/**
	 * Hub intent-verification helper (topic / mode match).
	 *
	 * @return singleton {@link IntentVerifier}
	 */
	@Bean
	public IntentVerifier intentVerifier() {
		return new IntentVerifier();
	}

	/**
	 * HMAC verifier for hub content-distribution bodies.
	 *
	 * @return singleton {@link AuthenticatedContentVerifier}
	 */
	@Bean
	public AuthenticatedContentVerifier authenticatedContentVerifier() {
		return new AuthenticatedContentVerifier();
	}

	/**
	 * Registers {@link IntentVerificationFilter} as a servlet filter.
	 *
	 * @param intentVerifier verifier injected into the filter
	 * @return filter registration
	 */
	@Bean(name = "intentVerificationFilterBean")
	public FilterRegistrationBean<Filter> registerIntentVerificationFilterFilterBean(@Autowired IntentVerifier intentVerifier) {
		FilterRegistrationBean<Filter> reqResFilter = new FilterRegistrationBean<>();
		reqResFilter.setFilter(registerIntentVerificationFilter(intentVerifier));
		return reqResFilter;
	}

	/**
	 * Intent-verification filter instance (also a Spring bean for mapping injection).
	 *
	 * @param intentVerifier topic/mode matcher
	 * @return filter
	 */
	@Bean
	public IntentVerificationFilter registerIntentVerificationFilter(IntentVerifier intentVerifier) {
		return new IntentVerificationFilter(intentVerifier);
	}

	/**
	 * Registers {@link MultipleReadRequestBodyFilter} first so HMAC verification can
	 * reread the body.
	 *
	 * @return filter registration with order {@code 0}
	 */
	@Bean(name = "cachingRequestBodyFilter")
	public FilterRegistrationBean<Filter> registerCachingRequestBodyFilterBean() {
		FilterRegistrationBean<Filter> reqResFilter = new FilterRegistrationBean<>();
		reqResFilter.setFilter(registerCachingRequestBodyFilter());
		reqResFilter.setOrder(0);
		return reqResFilter;
	}

	/**
	 * Filter that wraps the request so the body can be read more than once.
	 *
	 * @return caching body filter
	 */
	@Bean
	public MultipleReadRequestBodyFilter registerCachingRequestBodyFilter() {
		return new MultipleReadRequestBodyFilter();
	}
	

	/**
	 * Subscriber client bean used by MOSIP services to subscribe and unsubscribe.
	 *
	 * @return {@link SubscriberClientImpl} as {@link SubscriptionClient}
	 */
	@Bean
	public SubscriptionClient<SubscriptionChangeRequest, UnsubscriptionRequest, SubscriptionChangeResponse> subscriptionClient(){
		return new SubscriberClientImpl();
	}

	/**
	 * Extended subscriber bean for MOSIP failed-content pull.
	 *
	 * @return {@link SubscriberClientImpl} as {@link SubscriptionExtendedClient}
	 */
	@Bean
	public SubscriptionExtendedClient<FailedContentResponse, FailedContentRequest> subscriptionExtendedClient(){
		return new SubscriberClientImpl();
	}

	/**
	 * Aspect that HMAC-checks hub POSTs annotated with
	 * {@link io.mosip.kernel.websub.api.annotation.PreAuthenticateContentAndVerifyIntent}.
	 *
	 * @return WebSub content-auth aspect
	 */
	@Bean
	public WebSubClientAspect webSubClientAspect(){
		return new WebSubClientAspect();
	}
	

}
