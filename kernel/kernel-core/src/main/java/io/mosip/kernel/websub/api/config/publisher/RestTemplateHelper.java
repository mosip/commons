package io.mosip.kernel.websub.api.config.publisher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Selects the {@link RestTemplate} used by WebSub publisher and subscriber clients.
 * <p>
 * When {@code mosip.auth.filter_disable} is {@code true} (default), the plain
 * {@code websubRestTemplate} bean is used. Otherwise the IAM
 * {@code selfTokenRestTemplate} is used so hub calls carry a service token.
 * </p>
 *
 * @author Urvil Joshi
 */
@Component
public class RestTemplateHelper {

	/**
	 * When {@code true}, hub calls skip the auth-adapter token template
	 * ({@code mosip.auth.filter_disable}).
	 */
	@Value("${mosip.auth.filter_disable:true}")
	boolean isAuthFilterDisable;

	/**
	 * Unauthenticated template registered by {@link WebSubPublisherClientConfig}.
	 */
	@Qualifier("websubRestTemplate")
	@Autowired(required = false)
	private RestTemplate websubRestTemplate;

	/**
	 * Token-propagating template from kernel-auth-adapter when auth filter is enabled.
	 */
	@Autowired(required = false)
	@Qualifier("selfTokenRestTemplate") 
	private RestTemplate selfTokenRestTemplate;

	/**
	 * Returns the template selected by {@code mosip.auth.filter_disable}.
	 *
	 * @return {@code websubRestTemplate} if auth is disabled; otherwise {@code selfTokenRestTemplate}
	 */
	public RestTemplate getRestTemplate() {
		return isAuthFilterDisable?websubRestTemplate:selfTokenRestTemplate;
	}
}
