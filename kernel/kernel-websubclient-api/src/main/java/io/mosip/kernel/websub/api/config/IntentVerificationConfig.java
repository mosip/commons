package io.mosip.kernel.websub.api.config;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

import io.mosip.kernel.websub.api.annotation.PreAuthenticateContentAndVerifyIntent;
import io.mosip.kernel.websub.api.filter.IntentVerificationFilter;

/**
 * Configures WebSub intent verification by mapping callback URLs to topics.
 * <p>
 * This class implements {@link ApplicationContextAware} and {@link EmbeddedValueResolverAware} to scan
 * Spring beans for methods annotated with {@link PreAuthenticateContentAndVerifyIntent}. It extracts
 * {@code topic} and {@code callback} values, resolves property placeholders (e.g., {@code ${...}}),
 * and stores them in a thread-safe {@link ConcurrentHashMap} for use by
 * {@link IntentVerificationFilter} to validate WebSub hub intent verification requests (e.g., for
 * subscribe/unsubscribe operations per RFC 7033). The class is optimized for performance by:
 * <ul>
 *   <li>Using {@link ConcurrentHashMap} for thread-safe mappings.</li>
 *   <li>Filtering non-lazy beans and avoiding redundant {@link IntentVerificationFilter} updates.</li>
 *   <li>Validating resolved {@code topic} and {@code callback} values.</li>
 *   <li>Logging mapping details and errors with minimal overhead.</li>
 * </ul>
 * The implementation is thread-safe due to Spring's singleton scope and immutable dependencies. It
 * integrates with the MOSIP WebSub framework (e.g., {@link io.mosip.kernel.websub.api.client.SubscriberClientImpl})
 * and throws {@link BeansException} if the application context setup fails.
 * </p>
 * <p>
 * Example Usage:
 * <pre>
 * &#64;PreAuthenticateContentAndVerifyIntent(topic = "${mosip.websub.topic}", callback = "${mosip.websub.callback}")
 * public void handleIntentVerification(String challenge) {
 *     // Handle hub verification
 * }
 * </pre>
 * </p>
 *
 * @author Urvil Joshi
 * @since 1.0.0
 * @see PreAuthenticateContentAndVerifyIntent
 * @see IntentVerificationFilter
 * @see ApplicationContextAware
 * @see EmbeddedValueResolverAware
 */
@Component
public class IntentVerificationConfig implements ApplicationContextAware, EmbeddedValueResolverAware {

	/**
	 * Logger for debugging and error reporting.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(IntentVerificationConfig.class);

	/**
	 * Thread-safe map of callback URLs to topics for intent verification.
	 */
	private final Map<String, String> mappings = new ConcurrentHashMap<>();

	/**
	 * Resolver for property placeholders (e.g., ${...}).
	 */
	private StringValueResolver resolver = null;

	/**
	 * Spring application context used to scan beans.
	 */
	private ApplicationContext applicationContext;

	/**
	 * Filter for validating WebSub hub intent verification requests.
	 */
	@Autowired
	private IntentVerificationFilter intentVerificationFilter;

	/**
	 * Sets the string value resolver for resolving property placeholders.
	 * <p>
	 * Stores the provided {@link StringValueResolver} to resolve {@code topic} and {@code callback}
	 * values from {@link PreAuthenticateContentAndVerifyIntent} annotations (e.g., {@code ${mosip.websub.topic}}).
	 * </p>
	 *
	 * @param resolver the string value resolver
	 */
	@Override
	public void setEmbeddedValueResolver(StringValueResolver resolver) {
		this.resolver = resolver;
		LOGGER.debug("Set StringValueResolver for property placeholder resolution");
	}

	/**
	 * Stores the Spring application context for bean scanning.
	 * <p>
	 * Saves the provided {@link ApplicationContext} for use in {@link #initMappings()}. Logs a debug
	 * message to confirm storage. Scanning is deferred to {@link PostConstruct} to avoid circular
	 * dependencies during context initialization.
	 * </p>
	 *
	 * @param applicationContext the Spring application context
	 * @throws BeansException if the application context is invalid
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
		LOGGER.debug("Stored ApplicationContext for later use");
	}

	/**
	 * Initializes mappings by scanning beans for {@link PreAuthenticateContentAndVerifyIntent} annotations.
	 * <p>
	 * Executes after Spring context initialization to avoid circular dependencies. Scans only beans
	 * annotated with {@link PreAuthenticateContentAndVerifyIntent}, extracts {@code topic} and
	 * {@code callback} values, resolves property placeholders, and stores valid mappings in a
	 * {@link ConcurrentHashMap}. Sets the mappings on {@link IntentVerificationFilter} for hub intent
	 * verification. Optimizes performance by:
	 * <ul>
	 *   <li>Using {@link ApplicationContext#getBeanNamesForAnnotation} to scan only relevant beans.</li>
	 *   <li>Handling AOP proxies via {@link AopUtils}.</li>
	 *   <li>Validating resolved values and logging duplicates or invalid mappings.</li>
	 *   <li>Using atomic {@link ConcurrentHashMap#compute} for thread-safe updates.</li>
	 * </ul>
	 * Logs the number of mappings set or a message if no valid mappings are found.
	 * </p>
	 */
	@PostConstruct
	public void initMappings() {
		LOGGER.debug("Scanning beans for PreAuthenticateContentAndVerifyIntent annotations");
		ConfigurableApplicationContext configurableContext = (ConfigurableApplicationContext) applicationContext;
		String[] beanNames = applicationContext.getBeanNamesForAnnotation(PreAuthenticateContentAndVerifyIntent.class);

		for (String beanName : beanNames) {
			if (!configurableContext.getBeanFactory().getBeanDefinition(beanName).isLazyInit()) {
				Object bean = applicationContext.getBean(beanName);
				Class<?> beanClass = AopUtils.isAopProxy(bean) ? AopUtils.getTargetClass(bean) : bean.getClass();

				for (Method method : beanClass.getDeclaredMethods()) {
					if (method.isAnnotationPresent(PreAuthenticateContentAndVerifyIntent.class)) {
						PreAuthenticateContentAndVerifyIntent annotation = method.getAnnotation(PreAuthenticateContentAndVerifyIntent.class);
						String topic = resolveValue(annotation.topic(), "topic");
						String callback = resolveValue(annotation.callback(), "callback");

						if (!StringUtils.hasText(topic) || !StringUtils.hasText(callback)) {
							LOGGER.warn("Skipping invalid mapping for method {}.{}: topic='{}', callback='{}'",
									beanClass.getSimpleName(), method.getName(), topic, callback);
							continue;
						}

						mappings.compute(callback, (key, existingTopic) -> {
							if (existingTopic != null) {
								LOGGER.warn("Duplicate callback URL '{}' for topic '{}'; overwriting existing topic '{}'",
										callback, topic, existingTopic);
							}
							return topic;
						});
						LOGGER.debug("Mapped callback '{}' to topic '{}'", callback, topic);
					}
				}
			}
		}

		if (mappings.isEmpty()) {
			LOGGER.info("No valid PreAuthenticateContentAndVerifyIntent mappings found");
		} else {
			intentVerificationFilter.setMappings(mappings);
			LOGGER.info("Set {} mappings on IntentVerificationFilter", mappings.size());
		}
	}

	/**
	 * Resolves a property placeholder value using the {@link StringValueResolver}.
	 * <p>
	 * Resolves the input value if it’s a placeholder (e.g., {@code ${mosip.websub.topic}}). Returns
	 * the original value if not a placeholder or if resolution fails. Logs warnings for unresolved
	 * placeholders.
	 * </p>
	 *
	 * @param value the value to resolve
	 * @param fieldName the field name for logging (e.g., "topic", "callback")
	 * @return the resolved value, or the original value if not a placeholder
	 */
	private String resolveValue(String value, String fieldName) {
		if (StringUtils.hasText(value) && value.startsWith("${") && value.endsWith("}") && resolver != null) {
			String resolved = resolver.resolveStringValue(value);
			if (!StringUtils.hasText(resolved)) {
				LOGGER.warn("Failed to resolve {} placeholder: {}", fieldName, value);
			}
			return resolved;
		}
		return value;
	}
}