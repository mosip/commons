package io.mosip.kernel.auth.defaultadapter.config;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

import io.mosip.kernel.auth.defaultadapter.constant.AuthAdapterConstant;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;

@Configuration
public class BeanConfig {

	@Value("${mosip.kernel.auth.adapter.ssl-bypass:true}")
	private boolean sslBypass;

	@Bean
	public RestTemplate restTemplate() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		HttpClientBuilder httpClientBuilder = HttpClients.custom().disableCookieManagement();
		RestTemplate restTemplate = null;
		if (sslBypass) {
			TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
			SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
					.loadTrustMaterial(null, acceptingTrustStrategy).build();
			SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new HostnameVerifier() {
				public boolean verify(String arg0, SSLSession arg1) {
					return true;
				}
			});
			httpClientBuilder.setSSLSocketFactory(csf);			
		} 
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setHttpClient(httpClientBuilder.build());
		restTemplate = new RestTemplate(requestFactory);
		//interceptor added in RestTemplatePostProcessor
		return restTemplate;
	}

	@Bean
	public WebClient webClient() {
		return WebClient.builder().filter((req, next) -> {
			ClientRequest filtered = null;
			if (SecurityContextHolder.getContext() != null
					&& SecurityContextHolder.getContext().getAuthentication().getPrincipal() != null
					&& SecurityContextHolder.getContext().getAuthentication()
							.getPrincipal() instanceof AuthUserDetails) {
				AuthUserDetails userDetail = (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication()
						.getPrincipal();
				filtered = ClientRequest.from(req).header(AuthAdapterConstant.AUTH_HEADER_COOKIE,
						AuthAdapterConstant.AUTH_COOOKIE_HEADER + userDetail.getToken()).build();
			}
			return next.exchange(filtered);
		}).build();
	}
}
