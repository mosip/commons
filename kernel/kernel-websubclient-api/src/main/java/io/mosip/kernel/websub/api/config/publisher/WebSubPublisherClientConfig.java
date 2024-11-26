package io.mosip.kernel.websub.api.config.publisher;

import java.util.Collections;

import javax.net.ssl.SSLSession;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
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

	@Value("${websub.httpclient.connections.max.per.host:20}")
	private int maxConnectionPerRoute;

	@Value("${websub.httpclient.connections.max:100}")
	private int totalMaxConnection;

	@Bean(name = "websubRestTemplate")
	public RestTemplate restTemplate() {
		HttpClientBuilder httpClientBuilder = HttpClients.custom()
				.setMaxConnPerRoute(maxConnectionPerRoute)
				.setMaxConnTotal(totalMaxConnection).disableCookieManagement();
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setHttpClient(httpClientBuilder.build());
		return new RestTemplate(requestFactory);
	}

	@Bean
	public <P> PublisherClient<String, P, HttpHeaders> publisherClient() {
		return new PublisherClientImpl<>();
	}

}
