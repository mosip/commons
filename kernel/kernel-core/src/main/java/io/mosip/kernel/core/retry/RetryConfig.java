package io.mosip.kernel.core.retry;

import static io.mosip.kernel.core.retry.RetryConfigKeyConstants.KERNEL_NONRETRYABLE_EXCEPTIONS;
import static io.mosip.kernel.core.retry.RetryConfigKeyConstants.KERNEL_RETRYABLE_EXCEPTIONS;
import static io.mosip.kernel.core.retry.RetryConfigKeyConstants.KERNEL_RETRY_ATTEMPTS_LIMIT;
import static io.mosip.kernel.core.retry.RetryConfigKeyConstants.KERNEL_RETRY_EXPONENTIAL_BACKOFF_INITIAL_INTERVAL_MILLISECS;
import static io.mosip.kernel.core.retry.RetryConfigKeyConstants.KERNEL_RETRY_EXPONENTIAL_BACKOFF_MAX_INTERVAL_MILLISECS;
import static io.mosip.kernel.core.retry.RetryConfigKeyConstants.KERNEL_RETRY_EXPONENTIAL_BACKOFF_MULTIPLIER;
import static io.mosip.kernel.core.retry.RetryConfigKeyConstants.KERNEL_RETRY_TRAVERSE_ROOT_CAUSE_ENABLED;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;
import org.springframework.retry.RetryListener;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.CompositeRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import io.mosip.kernel.core.exception.ExceptionUtils;

/**
 * The Class RetryConfig - to configure retry template with retry/back off
 * policies as per configuration.
 * 
 * @author Loganathan Sekar
 */
@Configuration
@EnableAspectJAutoProxy
public class RetryConfig {
	
	private static final Logger logger = LoggerFactory.getLogger(ExceptionUtils.class);

	/** The Constant DEFAULT_RETRYABLE_EXCEPTIONS. */
	private static final String DEFAULT_RETRYABLE_EXCEPTIONS = Exception.class.getName();

	/** The Constant DEFAULT_NONRETRYABLE_EXCEPTIONS. */
	private static final String DEFAULT_NONRETRYABLE_EXCEPTIONS = Runtime.class.getName();

	/** The environment. */
	@Autowired
	private Environment environment;

	
	/**
	 * Retry policy.
	 *
	 * @param retryLimit the retry limit
	 * @return the retry policy
	 */
	@Bean
	public RetryPolicy retryPolicy(@Value("${" + KERNEL_RETRY_ATTEMPTS_LIMIT + ":5}") int retryLimit,
			@Value("${" + KERNEL_RETRY_TRAVERSE_ROOT_CAUSE_ENABLED + ":true}") boolean traverseRootCause) {
		int maxAttempts = retryLimit + 1;
		Map<Class<? extends Throwable>, Boolean> retryableExceptions = getRetryableExceptionsFromConfig();
		// Adding 1 as the limit is inclusive of the first attempt for this policy
		SimpleRetryPolicy simpleRetryPolicyForInclusiveExceptions = new SimpleRetryPolicy(maxAttempts,
				retryableExceptions, traverseRootCause, false); // Default is false, so that if an exception/subclass
																// not in list will not be retried
		Map<Class<? extends Throwable>, Boolean> nonRetryableExceptions = getNonRetryableExceptionsFromConfig();
		SimpleRetryPolicy simpleRetryPolicyForExclusiveExceptions = new SimpleRetryPolicy(maxAttempts,
				nonRetryableExceptions, traverseRootCause, true); // Default is true, if exception is not listed it will
																	// be allowed to retry
		CompositeRetryPolicy compositeRetryPolicy = new CompositeRetryPolicy();
		// Don't retry if any of the two policies says not to retry. Retry only if
		// inclusive and exclusive exceptions are matching
		compositeRetryPolicy.setOptimistic(false); 
		compositeRetryPolicy.setPolicies(
				new RetryPolicy[] { simpleRetryPolicyForInclusiveExceptions, simpleRetryPolicyForExclusiveExceptions });
		return compositeRetryPolicy;
	}

	/**
	 * Gets the retryable exceptions from config.
	 *
	 * @return the retryable exceptions from config
	 */
	protected Map<Class<? extends Throwable>, Boolean> getRetryableExceptionsFromConfig() {
		return getExceptionsMapFromConfig(KERNEL_RETRYABLE_EXCEPTIONS, DEFAULT_RETRYABLE_EXCEPTIONS, true);
	}

	/**
	 * Gets the non retryable exceptions from config.
	 *
	 * @return the non retryable exceptions from config
	 */
	private Map<Class<? extends Throwable>, Boolean> getNonRetryableExceptionsFromConfig() {
		return getExceptionsMapFromConfig(KERNEL_NONRETRYABLE_EXCEPTIONS, DEFAULT_NONRETRYABLE_EXCEPTIONS, false);
	}

	/**
	 * Gets the exceptions map from config.
	 *
	 * @param configProperty  the config property
	 * @param defaulPropValue the defaul prop value
	 * @param shouldRetry     the should retry
	 * @return the exceptions map from config
	 */
	private Map<Class<? extends Throwable>, Boolean> getExceptionsMapFromConfig(String configProperty,
			String defaulPropValue, boolean shouldRetry) {
		String propertyValue = environment.getProperty(configProperty, defaulPropValue);
		return Stream.of(propertyValue.split(",")).filter(str -> !str.isEmpty()).map(str -> {
			try {
				return Class.forName(str);
			} catch (ClassNotFoundException e) {
				logger.error("", this.getClass().getSimpleName(), "getExceptionsMapFromConfig",
						e.getMessage() , e);
			}
			return null;
		}).filter(Objects::nonNull).filter(cl -> Throwable.class.isAssignableFrom(cl))
				.map(cl -> (Class<? extends Throwable>) cl)
				.collect(Collectors.toMap(Function.identity(), arg -> shouldRetry));

	}

	/**
	 * Back off policy.
	 *
	 * @param initialIntervalMilliSecs the initial interval milli secs
	 * @param multiplier               the multiplier
	 * @param maxIntervalMilliSecs     the max interval milli secs
	 * @return the back off policy
	 */
	@Bean
	public BackOffPolicy backOffPolicy(
			@Value("${" + KERNEL_RETRY_EXPONENTIAL_BACKOFF_INITIAL_INTERVAL_MILLISECS
					+ ":200}") long initialIntervalMilliSecs,
			@Value("${" + KERNEL_RETRY_EXPONENTIAL_BACKOFF_MULTIPLIER + ":1.0}") double multiplier,
			@Value("${" + KERNEL_RETRY_EXPONENTIAL_BACKOFF_MAX_INTERVAL_MILLISECS + ":5000}") long maxIntervalMilliSecs) {
		ExponentialBackOffPolicy exponentialBackOffPolicy = new ExponentialBackOffPolicy();
		exponentialBackOffPolicy.setInitialInterval(initialIntervalMilliSecs);
		exponentialBackOffPolicy.setMultiplier(multiplier);
		exponentialBackOffPolicy.setMaxInterval(maxIntervalMilliSecs);
		return exponentialBackOffPolicy;
	}

	/**
	 * Retry template.
	 *
	 * @param retryPolicy   the retry policy
	 * @param backOffPolicy the back off policy
	 * @param retryListener the retry listener
	 * @return the retry template
	 */
	@Bean
	public RetryTemplate retryTemplate(RetryPolicy retryPolicy, BackOffPolicy backOffPolicy,
			RetryListener retryListener) {
		RetryTemplate retryTemplate = new RetryTemplate();
		retryTemplate.setRetryPolicy(retryPolicy);
		retryTemplate.setBackOffPolicy(backOffPolicy);
		retryTemplate.setThrowLastExceptionOnExhausted(true);
		retryTemplate.setListeners(new RetryListener[] { retryListener });
		return retryTemplate;
	}

}
