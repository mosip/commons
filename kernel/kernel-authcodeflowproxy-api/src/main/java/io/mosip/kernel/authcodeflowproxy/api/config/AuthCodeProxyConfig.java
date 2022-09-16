package io.mosip.kernel.authcodeflowproxy.api.config;

import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AuthCodeProxyConfig {
	
	@Value("${authcodeproxyflow.httpclient.connections.max.per.host:20}")
	private int maxConnectionPerRoute;

	@Value("${authcodeproxyflow.httpclient.connections.max:100}")
	private int totalMaxConnection;

	@ConditionalOnMissingBean(RestTemplate.class)
	@Bean
	RestTemplate restTemplate() {
		HttpClientBuilder httpClientBuilder = HttpClients.custom()
				.setMaxConnPerRoute(maxConnectionPerRoute)
				.setMaxConnTotal(totalMaxConnection).disableCookieManagement();
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setHttpClient(httpClientBuilder.build());
		return new RestTemplate(requestFactory);
	}
}
