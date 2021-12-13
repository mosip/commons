package io.mosip.kernel.websub.api.config;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringValueResolver;

import io.mosip.kernel.websub.api.annotation.PreAuthenticateContentAndVerifyIntent;
import io.mosip.kernel.websub.api.filter.IntentVerificationFilter;

/**
 * This class is resposible for loading all the metadata with help of
 * {@link PreAuthenticateContentAndVerifyIntent} annotation after application
 * context is ready for handling get endpoint request sent by hub for intent
 * verifications after subscribe and unsubscribe operation.
 * 
 * @author Urvil Joshi
 *
 */
@Component
public class IntentVerificationConfig implements ApplicationContextAware, EmbeddedValueResolverAware {

	private Map<String, String> mappings = null;
	private StringValueResolver resolver = null;

	@Override
	public void setEmbeddedValueResolver(StringValueResolver resolver) {
		this.resolver = resolver;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		mappings = new HashMap<>();
		for (String beanName : applicationContext.getBeanDefinitionNames()) {
			if (!((ConfigurableApplicationContext) applicationContext).getBeanFactory().getBeanDefinition(beanName)
					.isLazyInit()) {
				Object obj = applicationContext.getBean(beanName);
				Class<?> objClazz = obj.getClass();
				if (AopUtils.isAopProxy(obj)) {

					objClazz = AopUtils.getTargetClass(obj);
				}

				for (Method method : objClazz.getDeclaredMethods()) {
					if (method.isAnnotationPresent(PreAuthenticateContentAndVerifyIntent.class)) {
						PreAuthenticateContentAndVerifyIntent preAuthenticateContent = method
								.getAnnotation(PreAuthenticateContentAndVerifyIntent.class);

						String topic = preAuthenticateContent.topic();
						String callback = preAuthenticateContent.callback();
						if (topic.startsWith("${") && topic.endsWith("}")) {
							topic = resolver.resolveStringValue(topic);
						}

						if (callback.startsWith("${") && callback.endsWith("}")) {
							callback = resolver.resolveStringValue(callback);
						}
						mappings.put(callback, topic);
					}
				}
				IntentVerificationFilter intentVerificationFilter = applicationContext
						.getBean(IntentVerificationFilter.class);
				intentVerificationFilter.setMappings(mappings);
			}
		}
	}
}