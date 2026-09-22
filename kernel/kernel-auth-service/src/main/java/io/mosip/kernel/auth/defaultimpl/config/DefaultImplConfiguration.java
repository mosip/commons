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

/**
 * Default-impl Spring beans: primary {@link RestTemplate}, in-memory access-token
 * cache, and the interceptor that attaches IAM tokens to outbound calls.
 */
@Configuration
public class DefaultImplConfiguration {


	/**
	 * Primary RestTemplate named {@code authRestTemplate} for authmanager HTTP
	 * clients that do not use the Keycloak connection pool.
	 *
	 * @return a new {@link RestTemplate}
	 */
	@Primary
	@Bean(name = "authRestTemplate")
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	/**
	 * In-memory cache of Keycloak {@link AccessTokenResponse} values keyed by
	 * client/app, with a capacity of one generation as constructed.
	 *
	 * @return memory cache instance
	 */
	@Bean
	public MemoryCache<String, AccessTokenResponse> memoryCache() {
		return new MemoryCache<>(1);
	}


	/**
	 * Interceptor that validates/caches tokens and applies them to RestTemplate
	 * requests toward IAM.
	 *
	 * @param memoryCache    token cache
	 * @param tokenValidator JWT/IAM token validator
	 * @param restTemplate   {@code authRestTemplate} used by the interceptor
	 * @return configured {@link RestInterceptor}
	 */
	@Bean
	public RestInterceptor restInterceptor(@Autowired  MemoryCache<String, AccessTokenResponse> memoryCache,@Autowired TokenValidator tokenValidator,@Qualifier("authRestTemplate") @Autowired RestTemplate restTemplate) {
		return new RestInterceptor(memoryCache,tokenValidator,restTemplate);
	}
}
