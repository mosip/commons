package io.mosip.kernel.websub.api.config.publisher;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.core.websub.spi.PublisherClient;
import io.mosip.kernel.websub.api.client.PublisherClientImpl;

/**
 * This class consist all the general and common configurations for this api.
 * 
 * @author Urvil Joshi
 *
 */
@Configuration
public class WebSubPublisherClientConfig {


	/**
	 * Unauthenticated {@link RestTemplate} used when {@code mosip.auth.filter_disable} is true.
	 *
	 * @return hub HTTP client
	 */
	@Bean(name = "websubRestTemplate")
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}


	/**
	 * Publisher client for register / unregister / publish / notify.
	 *
	 * @param <P> payload type for {@code publishUpdate}
	 * @return {@link PublisherClientImpl}
	 */
	@Bean
	public <P> PublisherClient<String, P, HttpHeaders> publisherClient(){
		return new PublisherClientImpl<>();
	}	

}
