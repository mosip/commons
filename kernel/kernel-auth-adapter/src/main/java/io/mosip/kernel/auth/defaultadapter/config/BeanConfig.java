package io.mosip.kernel.auth.defaultadapter.config;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.util.Timeout;
import org.apache.http.conn.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import io.mosip.kernel.auth.defaultadapter.constant.AuthAdapterConstant;
import io.mosip.kernel.auth.defaultadapter.helper.TokenHelper;
import io.mosip.kernel.auth.defaultadapter.helper.TokenValidationHelper;
import io.mosip.kernel.auth.defaultadapter.model.TokenHolder;
import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.kernel.openid.bridge.model.AuthUserDetails;

/**
 * Registers HTTP clients used by hosting MOSIP services: requester-token
 * {@link RestTemplate}, self-token RestTemplate/WebClient, and a shared
 * {@link TokenHolder}.
 * <p>
 * When {@code mosip.kernel.auth.adapter.ssl-bypass} is true (default),
 * RestTemplates that call other internal services trust all certificates and
 * skip hostname verification via anonymous {@link HostnameVerifier}
 * implementations. Self-token WebClient uses
 * {@link SelfTokenExchangeFilterFunction}; TokenHelper uses
 * {@code retrieve()} for OIDC calls.
 * <p>
 * This adapter is a library other MOSIP services put on the classpath.
 */
@Configuration
@EnableScheduling
public class BeanConfig {

	/**
	 * Client-credentials token client injected into self-token interceptors.
	 */
	@Autowired
	private TokenHelper tokenHelper;

	/**
	 * Spring environment for per-application OIDC client properties.
	 */
	@Autowired
	private Environment environment;

	/**
	 * Load-balancing interceptor attached to {@link #plainRestTemplate()}.
	 */
	@Autowired 
	private RestTemplateInterceptor defaultInterceptor;

	/**
	 * When {@code true}, RestTemplates used for internal MOSIP calls trust all
	 * TLS certificates and skip hostname checks.
	 */
	@Value("${mosip.kernel.auth.adapter.ssl-bypass:true}")
	private boolean sslBypass;

	/**
	 * Max connections per route for {@link #restTemplate()}.
	 */
	@Value("${mosip.kernel.http.default.restTemplate.max-connection-per-route:20}")
	private Integer defaultRestTemplateMaxConnectionPerRoute;

	/**
	 * Max total connections for {@link #restTemplate()}.
	 */
	@Value("${mosip.kernel.http.default.restTemplate.total-max-connections:100}")
	private Integer defaultRestTemplateTotalMaxConnections;

	/**
	 * Max connections per route for {@link #selfTokenRestTemplate}.
	 */
	@Value("${mosip.kernel.http.selftoken.restTemplate.max-connection-per-route:20}")
	private Integer selfTokenRestTemplateMaxConnectionPerRoute;

	/**
	 * Max total connections for {@link #selfTokenRestTemplate}.
	 */
	@Value("${mosip.kernel.http.selftoken.restTemplate.total-max-connections:100}")
	private Integer selfTokenRestTemplateTotalMaxConnections;

	/**
	 * Max connections per route for {@link #plainRestTemplate()}.
	 */
	@Value("${mosip.kernel.http.plain.restTemplate.max-connection-per-route:20}")
	private Integer plainRestTemplateMaxConnectionPerRoute;

	/**
	 * Max total connections for {@link #plainRestTemplate()}.
	 */
	@Value("${mosip.kernel.http.plain.restTemplate.total-max-connections:100}")
	private Integer plainRestTemplateTotalMaxConnections;

	/**
	 * WebClient in-memory codec size in megabytes; {@code 0} keeps the default.
	 */
	@Value("${mosip.kernel.webclient.exchange.strategy.max-in-memory-size.mbs:0}")
	private Integer exchangeStrategyMaxMemory;

	/**
	 * Socket/response timeout in milliseconds for the self-token RestTemplate;
	 * {@code 0} leaves the client default.
	 */
	@Value("${mosip.kernel.http.selftoken.restTemplate.socket-timeout:0}")
	private Integer selfTokenRestTemplateSocketTimeout;

	/**
	 * Online token validator injected into self-token interceptors.
	 */
	@Autowired
	private TokenValidationHelper tokenValidationHelper;

	/**
	 * Logger for self-token timeout configuration.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(BeanConfig.class);
	
	/**
	 * Optional reactive load-balancer filter for {@link #plainWebClient()}.
	 */
	@Autowired(required = false)
	private ReactorLoadBalancerExchangeFilterFunction lbFilterFunction;

	/**
	 * Default RestTemplate that forwards the inbound requester token via
	 * {@link RequesterTokenRestInterceptor}. When {@link #sslBypass} is true,
	 * TLS trust and hostname verification are disabled for internal MOSIP hosts.
	 *
	 * @return the requester-token RestTemplate
	 * @throws NoSuchAlgorithmException if the SSL context cannot be built
	 * @throws KeyStoreException        if trust material cannot be loaded
	 * @throws KeyManagementException   if the SSL context cannot be initialized
	 */
	@SuppressWarnings("java:S5527") // added suppress for sonarcloud. 
	// Server hostname verification is not required because of 2 reasons:
	// 1. All services will not be enabled to reach to out side network to get data.
	// 2. All internal service will have custom host names Eg: identity.idrepo
	// sslBypass will be set to true by default because it will be ignore only for the restTemplate object 
	// which will be used to reach to other servcies.  
	@Bean
	public RestTemplate restTemplate() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		var connnectionManagerBuilder = PoolingHttpClientConnectionManagerBuilder.create()
			     .setMaxConnPerRoute(defaultRestTemplateMaxConnectionPerRoute)
			     .setMaxConnTotal(defaultRestTemplateTotalMaxConnections);
		
		RestTemplate restTemplate = null;
		if (sslBypass) {
			TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
			SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
					.loadTrustMaterial(acceptingTrustStrategy).build();
			SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new HostnameVerifier() {
				/**
				 * Always returns {@code true}; used only when {@code sslBypass} is enabled
				 * for internal MOSIP service hostnames.
				 *
				 * @param arg0 unused hostname
				 * @param arg1 unused SSL session
				 * @return {@code true}
				 */
				public boolean verify(String arg0, SSLSession arg1) {
					return true;
				}
			});
			connnectionManagerBuilder.setSSLSocketFactory(csf);
		}
		
		var connectionManager = connnectionManagerBuilder.build();
		
		HttpClientBuilder httpClientBuilder = HttpClients.custom()
				.setConnectionManager(connectionManager)
				.disableCookieManagement();
		
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setHttpClient(httpClientBuilder.build());
		restTemplate = new RestTemplate(requestFactory);
		restTemplate.setInterceptors(Collections.singletonList(new RequesterTokenRestInterceptor()));
		// interceptor added in RestTemplatePostProcessor
		return restTemplate;
	}

	/**
	 * RestTemplate used only to fetch and validate client-credentials tokens,
	 * with {@link RestTemplateInterceptor} for load balancing and no requester
	 * cookie.
	 *
	 * @return the plain RestTemplate
	 * @throws NoSuchAlgorithmException if the SSL context cannot be built
	 * @throws KeyStoreException        if trust material cannot be loaded
	 * @throws KeyManagementException   if the SSL context cannot be initialized
	 */
	@Bean
	public RestTemplate plainRestTemplate() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException{
		
		var connnectionManagerBuilder = PoolingHttpClientConnectionManagerBuilder.create()
			     .setMaxConnPerRoute(plainRestTemplateMaxConnectionPerRoute)
			     .setMaxConnTotal(plainRestTemplateTotalMaxConnections);
		var connectionManager = connnectionManagerBuilder.build();
		
		HttpClientBuilder httpClientBuilder = HttpClients.custom()
				.setConnectionManager(connectionManager)
				.disableCookieManagement();
		
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setHttpClient(httpClientBuilder.build());
		RestTemplate template = new RestTemplate(requestFactory);
		template.setInterceptors(Collections.singletonList(defaultInterceptor));
		return template;
	}

	/**
	 * Shared cache for the service's client-credentials access token.
	 *
	 * @return an empty {@link TokenHolder}
	 */
	@Bean
	public TokenHolder<String> cachedTokenObject() {
		return new TokenHolder<>();
	}

	/**
	 * RestTemplate that attaches and renews the service self-token via
	 * {@link SelfTokenRestInterceptor}. Optional socket timeout is applied when
	 * {@link #selfTokenRestTemplateSocketTimeout} is greater than zero.
	 *
	 * @param plainRestTemplate RestTemplate used by the interceptor to fetch tokens
	 * @param cachedTokenObject shared token cache
	 * @return the self-token RestTemplate
	 * @throws NoSuchAlgorithmException if the SSL context cannot be built
	 * @throws KeyStoreException        if trust material cannot be loaded
	 * @throws KeyManagementException   if the SSL context cannot be initialized
	 */
	@SuppressWarnings("java:S5527") // added suppress for sonarcloud.
	// Refer comments above.
	@Bean
	public RestTemplate selfTokenRestTemplate(@Autowired @Qualifier("plainRestTemplate") RestTemplate plainRestTemplate,
			@Autowired TokenHolder<String> cachedTokenObject)
			throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		var connnectionManagerBuilder = PoolingHttpClientConnectionManagerBuilder.create()
			     .setMaxConnPerRoute(selfTokenRestTemplateMaxConnectionPerRoute)
			     .setMaxConnTotal(selfTokenRestTemplateTotalMaxConnections);
		
		RestTemplate restTemplate = null;
		if (sslBypass) {
			TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
			SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
					.loadTrustMaterial(acceptingTrustStrategy).build();
			SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new HostnameVerifier() {
				/**
				 * Always returns {@code true}; used only when {@code sslBypass} is enabled
				 * for internal MOSIP service hostnames.
				 *
				 * @param arg0 unused hostname
				 * @param arg1 unused SSL session
				 * @return {@code true}
				 */
				public boolean verify(String arg0, SSLSession arg1) {
					return true;
				}
			});
			connnectionManagerBuilder.setSSLSocketFactory(csf);
		}
		
		var connectionManager = connnectionManagerBuilder.build();
		
		HttpClientBuilder httpClientBuilder = HttpClients.custom()
				.setConnectionManager(connectionManager)
				.disableCookieManagement();
		//Setting the timeout in case reading data from socket takes more time
		if(selfTokenRestTemplateSocketTimeout > 0){
			LOGGER.info("Setting selfTokenRestTemplateSocketTimeout :"+ selfTokenRestTemplateSocketTimeout);
			RequestConfig config = RequestConfig.custom().setResponseTimeout(Timeout.ofMilliseconds(selfTokenRestTemplateSocketTimeout)).build();
			httpClientBuilder.setDefaultRequestConfig(config);
		}
		String applName = getApplicationName();
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setHttpClient(httpClientBuilder.build());
		restTemplate = new RestTemplate(requestFactory);
		restTemplate.setInterceptors(Collections.singletonList(new SelfTokenRestInterceptor(environment,
				plainRestTemplate, cachedTokenObject, tokenHelper, tokenValidationHelper, applName)));
		// interceptor added in RestTemplatePostProcessor
		return restTemplate;
	}

	/**
	 * WebClient used to fetch tokens, optionally wrapped with a reactive
	 * load-balancer filter.
	 *
	 * @return the plain WebClient
	 */
	@Bean
	public WebClient plainWebClient() {
		ExchangeFilterFunction filterFunction = (lbFilterFunction != null)
				? lbFilterFunction
				: (req, next) -> {
					return next.exchange(req);
				};
		return WebClient.builder().filter(filterFunction).build();
	}

	/**
	 * Scheduler that renews the cached self-token using {@code plainWebClient}.
	 *
	 * @param cachedTokenObject shared token cache
	 * @param plainWebClient    WebClient used to request tokens
	 * @return the renewal executor
	 * @throws NoSuchAlgorithmException unused; retained for historical signature
	 * @throws KeyStoreException        unused; retained for historical signature
	 * @throws KeyManagementException   unused; retained for historical signature
	 */
	@Bean
	public SelfTokenRenewalTaskExecutor selfTokenRenewTaskExecutor(@Autowired TokenHolder<String> cachedTokenObject,
			@Autowired @Qualifier("plainWebClient") WebClient plainWebClient)
			throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		String applName = getApplicationName();
		return new SelfTokenRenewalTaskExecutor(cachedTokenObject, plainWebClient, tokenHelper, environment, applName);
	}

	/**
	 * WebClient that forwards the inbound requester's Authorization cookie from
	 * {@link SecurityContextHolder} when the principal is {@link AuthUserDetails}.
	 *
	 * @return the requester-token WebClient
	 */
	@Bean
	public WebClient webClient() {

		return WebClient.builder().filter((req, next) -> {
			ClientRequest filtered = null;
			if (SecurityContextHolder.getContext() != null
					&& SecurityContextHolder.getContext().getAuthentication().getPrincipal() != null
					&& SecurityContextHolder.getContext().getAuthentication()
							.getPrincipal() instanceof AuthUserDetails) {
				io.mosip.kernel.openid.bridge.model.AuthUserDetails userDetail = (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication()
						.getPrincipal();
				filtered = ClientRequest.from(req).header(AuthAdapterConstant.AUTH_HEADER_COOKIE,
						AuthAdapterConstant.AUTH_HEADER + userDetail.getToken()).build();
			}
			return next.exchange(filtered);
		}).build();
	}

	/**
	 * WebClient that attaches and renews the service self-token via
	 * {@link SelfTokenExchangeFilterFunction}. When
	 * {@link #exchangeStrategyMaxMemory} is positive, codec buffer size is raised
	 * (used by ID Repo encrypt calls).
	 *
	 * @param plainWebClient    WebClient used by the filter to fetch tokens
	 * @param cachedTokenObject shared token cache
	 * @return the self-token WebClient
	 */
	@Bean
	public WebClient selfTokenWebClient(@Autowired @Qualifier("plainWebClient") WebClient plainWebClient,
			@Autowired TokenHolder<String> cachedTokenObject) {
		String applName = getApplicationName();
		
		if (exchangeStrategyMaxMemory <= 0)
			return WebClient.builder()
							.filter(new SelfTokenExchangeFilterFunction(environment, plainWebClient,
									cachedTokenObject, tokenHelper, tokenValidationHelper, applName))
							.build();
		// Added ExchangeStrategies to increase the buffer size for requests between service.
		// Found size limitation issue in ID Repo service which is invoking encrypt API of keymanager service.
		int size = exchangeStrategyMaxMemory * 1024 * 1024;
		ExchangeStrategies strategies = ExchangeStrategies.builder()
										.codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(size))
										.build();
		return WebClient.builder()
						.filter(new SelfTokenExchangeFilterFunction(environment, plainWebClient,
								cachedTokenObject, tokenHelper, tokenValidationHelper, applName))
						.exchangeStrategies(strategies)
						.build();
	}

	/**
	 * Returns the first comma-separated {@code spring.application.name} value.
	 *
	 * @return the hosting application name
	 * @throws RuntimeException if the property is missing or blank
	 */
	@SuppressWarnings("java:S2259") // added suppress for sonarcloud. Null check is performed at line # 211
	private String getApplicationName() {
		String appNames = environment.getProperty("spring.application.name");
		if (appNames != null && !EmptyCheckUtils.isNullEmpty(appNames)) {
			List<String> appNamesList = Stream.of(appNames.split(",")).collect(Collectors.toList());
			return appNamesList.get(0);
		} else {
			throw new RuntimeException("Property spring.application.name not found");
		}
	}
}
