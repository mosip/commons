package io.mosip.kernel.websub.api.config;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private static final Logger logger = LoggerFactory.getLogger(IntentVerificationConfig.class);

	@Override
	public void setEmbeddedValueResolver(StringValueResolver resolver) {
		this.resolver = resolver;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		logger.debug("inside setApplicationContext");
		mappings = new HashMap<>();
		for (String beanName : applicationContext.getBeanDefinitionNames()) {
			//Skip processing this intentVerificationConfig bean.
			if(beanName.equals("intentVerificationConfig")) {
				continue;
			}
			if (!((ConfigurableApplicationContext) applicationContext).getBeanFactory().getBeanDefinition(beanName)
					.isLazyInit()) {
				Object obj = applicationContext.getBean(beanName);
				logger.debug("bean-"+ beanName);
				Class<?> objClazz = obj.getClass();
				logger.debug("objClazz"+ objClazz);
				if (AopUtils.isAopProxy(obj)) {

					objClazz = AopUtils.getTargetClass(obj);
				}

				for (Method method : objClazz.getDeclaredMethods()) {
					logger.debug("method name-"+method);
					if (method.isAnnotationPresent(PreAuthenticateContentAndVerifyIntent.class)) {
						PreAuthenticateContentAndVerifyIntent preAuthenticateContent = method
								.getAnnotation(PreAuthenticateContentAndVerifyIntent.class);

						String topic = preAuthenticateContent.topic();
						logger.debug("topic- "+topic);

						String callback = preAuthenticateContent.callback();
						logger.debug("callback- "+callback);
						if (topic.startsWith("${") && topic.endsWith("}")) {
							topic = resolver.resolveStringValue(topic);
						}

						if (callback.startsWith("${") && callback.endsWith("}")) {
							callback = resolver.resolveStringValue(callback);
						}
						mappings.put(callback, topic);
						logger.debug("mapping"+ mappings);
					}
				}
				IntentVerificationFilter intentVerificationFilter = applicationContext
						.getBean(IntentVerificationFilter.class);
				intentVerificationFilter.setMappings(mappings);
			}
		}
	}
}