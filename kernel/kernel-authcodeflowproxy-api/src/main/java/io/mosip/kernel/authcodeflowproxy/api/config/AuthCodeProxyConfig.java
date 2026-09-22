package io.mosip.kernel.authcodeflowproxy.api.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.client.RestTemplate;

/**
 * Spring configuration for the authorization-code flow proxy.
 * <p>
 * Registers a default {@link RestTemplate} used to call Keycloak's token endpoint and,
 * when configured, the auth-manager validate URL, plus an {@link AntPathMatcher} for
 * allow-list URL patterns. Beans are created only when the host application has not
 * already defined them.
 */
@Configuration
public class AuthCodeProxyConfig {

	/**
	 * Fallback {@link RestTemplate} for Keycloak token exchange and online token
	 * validation when the host application has not already provided one.
	 *
	 * @return a new {@link RestTemplate}
	 */
	@ConditionalOnMissingBean(RestTemplate.class)
	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	/**
	 * Ant matcher for {@code auth.allowed.urls} pattern entries. Hosts may replace it.
	 *
	 * @return a default {@link AntPathMatcher}
	 */
	@ConditionalOnMissingBean(AntPathMatcher.class)
	@Bean
	AntPathMatcher antPathMatcher() {
		return new AntPathMatcher();
	}
}
