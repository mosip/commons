package io.mosip.kernel.authcodeflowproxy.api.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AuthCodeProxyConfig {

	@ConditionalOnMissingBean(RestTemplate.class)
	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
