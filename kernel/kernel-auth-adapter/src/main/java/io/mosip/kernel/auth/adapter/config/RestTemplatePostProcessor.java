package io.mosip.kernel.auth.adapter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
public class RestTemplatePostProcessor implements BeanPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestTemplatePostProcessor.class);

    @Bean
    public RestTemplateInterceptor restTemplateInterceptor() {
        return new RestTemplateInterceptor();
    }

    @Bean
    public SimpleClientHttpRequestFactory simpleClientHttpRequestFactory() {
        return new SimpleClientHttpRequestFactory();
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if(bean instanceof RestTemplate &&
                !beanName.equalsIgnoreCase("keycloakRestTemplate")) {
            LOGGER.info("Post processing REST_TEMPLATE bean : {} ", beanName);
            return getRestTemplate(bean);
        }
        return bean;
    }

    private RestTemplate getRestTemplate(Object bean) {
        final RestTemplate restTemplate = (RestTemplate) bean;
        List<ClientHttpRequestInterceptor> list = restTemplate.getInterceptors();
        list.add(restTemplateInterceptor());
        restTemplate.setInterceptors(list);
        return restTemplate;
    }
}
