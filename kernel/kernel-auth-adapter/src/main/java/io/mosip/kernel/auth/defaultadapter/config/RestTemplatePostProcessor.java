package io.mosip.kernel.auth.defaultadapter.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Bean post-processor that attaches {@link RestTemplateInterceptor} to every
 * {@link RestTemplate} except {@code keycloakRestTemplate}.
 * <p>
 * Hosting MOSIP services therefore get client-side load balancing on their own
 * RestTemplate beans without registering the interceptor themselves.
 * <p>
 * This adapter is a library other MOSIP services put on the classpath.
 *
 * @author Anusha
 * @since 1.2.0-rc2-SNAPSHOT
 */
@Configuration
public class RestTemplatePostProcessor implements BeanPostProcessor {

    /**
     * Logger for RestTemplate post-processing.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RestTemplatePostProcessor.class);

    /**
     * Registers the shared load-balancing interceptor.
     *
     * @return a new {@link RestTemplateInterceptor}
     */
    @Bean
    public RestTemplateInterceptor restTemplateInterceptor() {
        return new RestTemplateInterceptor();
    }

    /**
     * Registers the default {@link ClientHttpRequestFactory} used when the
     * interceptor reconstructs a load-balanced request.
     *
     * @return a simple request factory
     */
    @Bean
    public SimpleClientHttpRequestFactory simpleClientHttpRequestFactory() {
        return new SimpleClientHttpRequestFactory();
    }

    /**
     * Pass-through before initialization.
     *
     * @param bean     the bean instance
     * @param beanName the bean name
     * @return {@code bean} unchanged
     * @throws BeansException never thrown by this implementation
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /**
     * After initialization, appends {@link RestTemplateInterceptor} to every
     * {@link RestTemplate} except {@code keycloakRestTemplate}.
     *
     * @param bean     the bean instance
     * @param beanName the bean name
     * @return the original bean, or the RestTemplate with the interceptor added
     * @throws BeansException never thrown by this implementation
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if(bean instanceof RestTemplate &&
                !beanName.equalsIgnoreCase("keycloakRestTemplate")) {
            LOGGER.info("Post processing REST_TEMPLATE bean : {} ", beanName);
            return getRestTemplate(bean);
        }
        return bean;
    }

    /**
     * Appends {@link #restTemplateInterceptor()} to the given RestTemplate's
     * interceptor list.
     *
     * @param bean a {@link RestTemplate} instance
     * @return the same RestTemplate with the interceptor attached
     */
    private RestTemplate getRestTemplate(Object bean) {
        final RestTemplate restTemplate = (RestTemplate) bean;
        List<ClientHttpRequestInterceptor> list = restTemplate.getInterceptors();
        list.add(restTemplateInterceptor());
        restTemplate.setInterceptors(list);
        return restTemplate;
    }
}
