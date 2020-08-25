package io.mosip.kernel.auth.adapter.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
public class RestTemplatePostProcessor implements BeanPostProcessor {

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
        if(bean instanceof RestTemplate) {
            //log the bean name
           return getRestTemplate(bean);
        }
        return bean;
    }

    //@LoadBalanced
    private RestTemplate getRestTemplate(Object bean) {
        final RestTemplate restTemplate = (RestTemplate) bean;
        List<ClientHttpRequestInterceptor> list = restTemplate.getInterceptors();
        list.add(restTemplateInterceptor());
        restTemplate.setInterceptors(list);
        return restTemplate;
    }
}
