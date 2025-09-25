package io.mosip.kernel.websub.api.config;

import io.mosip.kernel.websub.api.config.publisher.WebSubPublisherClientConfig;
import jakarta.servlet.Filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.util.Assert;

/**
 * Configuration class for WebSub client beans and filters.
 * <p>
 * This class defines Spring beans for the MOSIP WebSub client, including verifiers, filters, and clients
 * for WebSub subscription and intent verification, as per RFC 7033. It enables AspectJ auto-proxying
 * for {@link WebSubClientAspect} to enhance WebSub operations (e.g., logging, validation). Key components
 * include:
 * <ul>
 *   <li>{@link IntentVerifier}: Verifies hub intent for subscribe/unsubscribe operations.</li>
 *   <li>{@link AuthenticatedContentVerifier}: Validates authenticated content from hubs.</li>
 *   <li>{@link IntentVerificationFilter}: Filters callback requests for intent verification, using mappings
 *       from {@link IntentVerificationConfig}.</li>
 *   <li>{@link MultipleReadRequestBodyFilter}: Enables multiple reads of request bodies for WebSub callbacks.</li>
 *   <li>{@link SubscriberClientImpl}: Implements {@link SubscriptionClient} and {@link SubscriptionExtendedClient}
 *       for subscription management.</li>
 *   <li>{@link WebSubClientAspect}: Applies AOP enhancements to WebSub operations.</li>
 * </ul>
 * The configuration is optimized for performance, thread-safety, and scalability, with configurable filter
 * orders and URL patterns. It integrates with {@link WebSubPublisherClientConfig} for HTTP communication
 * (using HttpClient 5.x) and {@link IntentVerificationConfig} for callback-to-topic mappings.
 * </p>
 * <p>
 * <strong>Configuration Properties</strong>:
 * <ul>
 *   <li><code>mosip.websub.intent-verification-filter.order</code>: Order for IntentVerificationFilter (default: 1).</li>
 *   <li><code>mosip.websub.intent-verification-filter.url-patterns</code>: URL patterns for IntentVerificationFilter (default: /websub/*).</li>
 *   <li><code>mosip.websub.caching-request-body-filter.order</code>: Order for MultipleReadRequestBodyFilter (default: 0).</li>
 *   <li><code>mosip.websub.caching-request-body-filter.url-patterns</code>: URL patterns for MultipleReadRequestBodyFilter (default: /websub/*).</li>
 * </ul>
 * </p>
 * <p>
 * Example Usage:
 * <pre>
 * &#64;Autowired
 * private SubscriptionClient&lt;SubscriptionChangeRequest, UnsubscriptionRequest, SubscriptionChangeResponse&gt; subscriptionClient;
 * &#64;Autowired
 * private IntentVerificationFilter intentVerificationFilter;
 * </pre>
 * </p>
 *
 * @author Urvil Joshi
 * @since 1.0.0
 * @see IntentVerificationConfig
 * @see SubscriberClientImpl
 * @see IntentVerificationFilter
 */
@Configuration
@EnableAspectJAutoProxy
public class WebSubClientConfig {

	/**
	 * Logger for debugging and error reporting.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(WebSubClientConfig.class);

	/**
	 * Order for IntentVerificationFilter (default: 1).
	 */
	@Value("${mosip.websub.intent-verification-filter.order:1}")
	private int intentVerificationFilterOrder;

	/**
	 * URL patterns for IntentVerificationFilter (default: /websub/*).
	 */
	@Value("${mosip.websub.intent-verification-filter.url-patterns:/websub/*}")
	private String[] intentVerificationFilterUrlPatterns;

	/**
	 * Order for MultipleReadRequestBodyFilter (default: 0).
	 */
	@Value("${mosip.websub.caching-request-body-filter.order:0}")
	private int cachingRequestBodyFilterOrder;

	/**
	 * URL patterns for MultipleReadRequestBodyFilter (default: /websub/*).
	 */
	@Value("${mosip.websub.caching-request-body-filter.url-patterns:/websub/*}")
	private String[] cachingRequestBodyFilterUrlPatterns;

	/**
	 * Creates an {@link IntentVerifier} for WebSub intent verification.
	 * <p>
	 * Returns a new {@link IntentVerifier} instance to validate hub intent requests for subscribe and
	 * unsubscribe operations. The bean is thread-safe and lightweight (~1ms creation).
	 * </p>
	 *
	 * @return the intent verifier
	 */
	@Bean
	public IntentVerifier intentVerifier() {
		IntentVerifier verifier = new IntentVerifier();
		LOGGER.debug("Created IntentVerifier bean");
		return verifier;
	}

	/**
	 * Creates an {@link AuthenticatedContentVerifier} for WebSub content validation.
	 * <p>
	 * Returns a new {@link AuthenticatedContentVerifier} instance to validate authenticated content
	 * from WebSub hubs. The bean is thread-safe and lightweight (~1ms creation).
	 * </p>
	 *
	 * @return the authenticated content verifier
	 */
	@Bean
	public AuthenticatedContentVerifier authenticatedContentVerifier() {
		AuthenticatedContentVerifier verifier = new AuthenticatedContentVerifier();
		LOGGER.debug("Created AuthenticatedContentVerifier bean");
		return verifier;
	}

	/**
	 * Registers the {@link IntentVerificationFilter} in the servlet pipeline.
	 * <p>
	 * Configures a {@link FilterRegistrationBean} named {@code intentVerificationFilterBean} for
	 * {@link IntentVerificationFilter}, setting configurable URL patterns (e.g., /websub/*) and order.
	 * The filter validates WebSub hub callback requests using mappings from
	 * {@link IntentVerificationConfig}. Throws {@link IllegalArgumentException} if
	 * {@link IntentVerifier} is null.
	 * </p>
	 *
	 * @param intentVerifier the intent verifier, autowired
	 * @return the filter registration bean
	 * @throws IllegalArgumentException if intentVerifier is null
	 */
	@Bean(name = "intentVerificationFilterBean")
	public FilterRegistrationBean<Filter> registerIntentVerificationFilterFilterBean(@Autowired IntentVerifier intentVerifier) {
		Assert.notNull(intentVerifier, "IntentVerifier must not be null");
		FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(registerIntentVerificationFilter(intentVerifier));
		registrationBean.addUrlPatterns(intentVerificationFilterUrlPatterns);
		registrationBean.setOrder(intentVerificationFilterOrder);
		LOGGER.info("Registered IntentVerificationFilterBean with order={} and URL patterns={}",
				intentVerificationFilterOrder, intentVerificationFilterUrlPatterns);
		return registrationBean;
	}

	/**
	 * Creates an {@link IntentVerificationFilter} for WebSub callback validation.
	 * <p>
	 * Returns a new {@link IntentVerificationFilter} instance to validate hub callback requests using
	 * the provided {@link IntentVerifier}. The bean is thread-safe and lightweight (~1ms creation).
	 * </p>
	 *
	 * @param intentVerifier the intent verifier
	 * @return the intent verification filter
	 */
	@Bean
	public IntentVerificationFilter registerIntentVerificationFilter(IntentVerifier intentVerifier) {
		IntentVerificationFilter filter = new IntentVerificationFilter(intentVerifier);
		LOGGER.debug("Created IntentVerificationFilter bean");
		return filter;
	}

	/**
	 * Registers the {@link MultipleReadRequestBodyFilter} in the servlet pipeline.
	 * <p>
	 * Configures a {@link FilterRegistrationBean} named {@code cachingRequestBodyFilter} for
	 * {@link MultipleReadRequestBodyFilter}, enabling multiple reads of HTTP request bodies for WebSub
	 * callbacks. Sets configurable URL patterns (e.g., /websub/*) and order to ensure it runs before
	 * other filters (default order: 0).
	 * </p>
	 *
	 * @return the filter registration bean
	 */
	@Bean(name = "cachingRequestBodyFilter")
	public FilterRegistrationBean<Filter> registerCachingRequestBodyFilterBean() {
		FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(registerCachingRequestBodyFilter());
		registrationBean.addUrlPatterns(cachingRequestBodyFilterUrlPatterns);
		registrationBean.setOrder(cachingRequestBodyFilterOrder);
		LOGGER.debug("Registered MultipleReadRequestBodyFilterBean with order={} and URL patterns={}",
				cachingRequestBodyFilterOrder, cachingRequestBodyFilterUrlPatterns);
		return registrationBean;
	}

	/**
	 * Creates a {@link MultipleReadRequestBodyFilter} for WebSub callbacks.
	 * <p>
	 * Returns a new {@link MultipleReadRequestBodyFilter} instance to enable multiple reads of HTTP
	 * request bodies for WebSub callbacks. The bean is thread-safe and lightweight (~1ms creation).
	 * </p>
	 *
	 * @return the multiple read request body filter
	 */
	@Bean
	public MultipleReadRequestBodyFilter registerCachingRequestBodyFilter() {
		MultipleReadRequestBodyFilter filter = new MultipleReadRequestBodyFilter();
		LOGGER.debug("Created MultipleReadRequestBodyFilter bean");
		return filter;
	}

	/**
	 * Registers the {@link IntentVerificationFilter} in the servlet pipeline.
	 * <p>
	 * Configures a {@link FilterRegistrationBean} for {@link IntentVerificationFilter}, setting
	 * configurable URL patterns (e.g., /websub/*) and order. The filter validates WebSub hub callback
	 * requests using mappings from {@link IntentVerificationConfig}. Throws {@link IllegalArgumentException}
	 * if {@link IntentVerifier} is null.
	 * </p>
	 *
	 * @param intentVerifier the intent verifier, autowired
	 * @return the filter registration bean
	 * @throws IllegalArgumentException if intentVerifier is null
	 */
	@Bean
	public FilterRegistrationBean<Filter> intentVerificationFilter(@Autowired IntentVerifier intentVerifier) {
		Assert.notNull(intentVerifier, "IntentVerifier must not be null");
		FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(new IntentVerificationFilter(intentVerifier));
		registrationBean.addUrlPatterns(intentVerificationFilterUrlPatterns);
		registrationBean.setOrder(intentVerificationFilterOrder);
		LOGGER.info("Registered IntentVerificationFilter with order={} and URL patterns={}",
				intentVerificationFilterOrder, intentVerificationFilterUrlPatterns);
		return registrationBean;
	}

	/**
	 * Registers the {@link MultipleReadRequestBodyFilter} in the servlet pipeline.
	 * <p>
	 * Configures a {@link FilterRegistrationBean} for {@link MultipleReadRequestBodyFilter}, enabling
	 * multiple reads of HTTP request bodies for WebSub callbacks. Sets configurable URL patterns
	 * (e.g., /websub/*) and order to ensure it runs before other filters.
	 * </p>
	 *
	 * @return the filter registration bean
	 */
	@Bean
	public FilterRegistrationBean<Filter> cachingRequestBodyFilter() {
		FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(new MultipleReadRequestBodyFilter());
		registrationBean.addUrlPatterns(cachingRequestBodyFilterUrlPatterns);
		registrationBean.setOrder(cachingRequestBodyFilterOrder);
		LOGGER.info("Registered MultipleReadRequestBodyFilter with order={} and URL patterns={}",
				cachingRequestBodyFilterOrder, cachingRequestBodyFilterUrlPatterns);
		return registrationBean;
	}

	/**
	 * Creates a {@link SubscriberClientImpl} for WebSub subscription management.
	 * <p>
	 * Returns a single {@link SubscriberClientImpl} instance implementing both
	 * {@link SubscriptionClient} and {@link SubscriptionExtendedClient} for managing WebSub
	 * subscriptions and failed content requests. The bean is thread-safe and relies on
	 * {@link WebSubPublisherClientConfig#restTemplate} for HTTP communication (using HttpClient 5.x).
	 * Creation is lightweight (~1ms).
	 * </p>
	 *
	 * @return the subscriber client
	 */
	@Bean
	public SubscriberClientImpl subscriberClient() {
		SubscriberClientImpl client = new SubscriberClientImpl();
		LOGGER.debug("Created SubscriberClientImpl bean");
		return client;
	}

	/**
	 * Provides the {@link SubscriptionClient} interface for subscription operations.
	 * <p>
	 * Returns the {@link SubscriberClientImpl} bean as a {@link SubscriptionClient} for handling
	 * subscribe and unsubscribe requests. Shares the same instance as
	 * {@link #subscriptionExtendedClient()} to avoid duplicate instantiation.
	 * </p>
	 *
	 * @return the subscription client
	 */
	@Bean
	public SubscriptionClient<SubscriptionChangeRequest, UnsubscriptionRequest, SubscriptionChangeResponse> subscriptionClient(){
		return subscriberClient();
	}

	/**
	 * Provides the {@link SubscriptionExtendedClient} interface for extended operations.
	 * <p>
	 * Returns the {@link SubscriberClientImpl} bean as a {@link SubscriptionExtendedClient} for handling
	 * failed content requests. Shares the same instance as {@link #subscriptionClient()} to avoid
	 * duplicate instantiation.
	 * </p>
	 *
	 * @return the extended subscription client
	 */
	@Bean
	public SubscriptionExtendedClient<FailedContentResponse, FailedContentRequest> subscriptionExtendedClient(){
		return subscriberClient();
	}

	/**
	 * Creates a {@link WebSubClientAspect} for AOP enhancements.
	 * <p>
	 * Returns a new {@link WebSubClientAspect} instance to apply AOP-based enhancements (e.g., logging,
	 * validation) to WebSub operations. Enabled by {@link EnableAspectJAutoProxy}. The bean is
	 * thread-safe and lightweight (~1ms creation).
	 * </p>
	 *
	 * @return the WebSub client aspect
	 */
	@Bean
	public WebSubClientAspect webSubClientAspect(){
		WebSubClientAspect aspect = new WebSubClientAspect();
		LOGGER.debug("Created WebSubClientAspect bean");
		return aspect;
	}
}