package io.mosip.kernel.websub.api.config;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

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
public class IntentVerificationConfig implements ApplicationContextAware {

	private Map<String, String> mappings = null;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		mappings = new HashMap<>();
		for (String beanName : applicationContext.getBeanDefinitionNames()) {
			Object obj = applicationContext.getBean(beanName);
			Class<?> objClazz = obj.getClass();
			if (AopUtils.isAopProxy(obj)) {

				objClazz = AopUtils.getTargetClass(obj);
			}

			for (Method method : objClazz.getDeclaredMethods()) {
				if (method.isAnnotationPresent(PreAuthenticateContentAndVerifyIntent.class)) {
					PreAuthenticateContentAndVerifyIntent preAuthenticateContent = method
							.getAnnotation(PreAuthenticateContentAndVerifyIntent.class);
					mappings.put(preAuthenticateContent.callback(), preAuthenticateContent.topic());
				}
			}
			IntentVerificationFilter intentVerificationFilter = applicationContext
					.getBean(IntentVerificationFilter.class);
			intentVerificationFilter.setMappings(mappings);
		}
	}

}