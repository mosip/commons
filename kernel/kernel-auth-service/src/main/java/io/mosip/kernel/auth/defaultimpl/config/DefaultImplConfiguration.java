package io.mosip.kernel.auth.defaultimpl.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.auth.defaultimpl.dto.AccessTokenResponse;
import io.mosip.kernel.auth.defaultimpl.intercepter.RestInterceptor;
import io.mosip.kernel.auth.defaultimpl.util.MemoryCache;
import io.mosip.kernel.auth.defaultimpl.util.TokenValidator;

@Configuration
public class DefaultImplConfiguration {
	

	@Primary
	@Bean(name = "authRestTemplate")
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	public MemoryCache<String, AccessTokenResponse> memoryCache() {
		return new MemoryCache<>(1);
	}
	

	@Bean
	public RestInterceptor restInterceptor(@Autowired  MemoryCache<String, AccessTokenResponse> memoryCache,@Autowired TokenValidator tokenValidator,@Qualifier("authRestTemplate") @Autowired RestTemplate restTemplate) {
		return new RestInterceptor(memoryCache,tokenValidator,restTemplate);
	}
}