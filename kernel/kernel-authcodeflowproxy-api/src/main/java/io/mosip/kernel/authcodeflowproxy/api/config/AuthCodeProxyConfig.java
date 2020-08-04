package io.mosip.kernel.authcodeflowproxy.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AuthCodeProxyConfig {

	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
