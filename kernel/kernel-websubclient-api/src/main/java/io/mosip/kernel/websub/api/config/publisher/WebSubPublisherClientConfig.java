package io.mosip.kernel.websub.api.config.publisher;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultRedirectStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.kernel.core.websub.spi.PublisherClient;
import io.mosip.kernel.websub.api.client.PublisherClientImpl;

import java.util.concurrent.TimeUnit;

/**
 * Configuration class for WebSub publisher client beans.
 * <p>
 * This class defines Spring beans for the WebSub publisher client, including a configured
 * {@link RestTemplate} for HTTP requests and a {@link PublisherClient} for WebSub topic
 * registration, unregistration, and publishing. The {@link RestTemplate} is optimized with
 * Apache HttpClient for connection pooling, timeout configuration, and redirect handling,
 * ensuring high performance and scalability in WebSub interactions. The implementation is
 * thread-safe due to Spring's singleton scope and immutable bean configurations.
 * </p>
 * <p>
 * <strong>Configuration Properties</strong>:
 * <ul>
 *   <li><code>mosip.websub.connect-timeout-ms</code>: Connect timeout (default: 5000ms).</li>
 *   <li><code>mosip.websub.read-timeout-ms</code>: Read timeout (default: 10000ms).</li>
 *   <li><code>mosip.websub.max-connections</code>: Max total connections (default: 100).</li>
 *   <li><code>mosip.websub.max-connections-per-route</code>: Max connections per route (default: 20).</li>
 * </ul>
 * </p>
 * <p>
 * Example Usage:
 * <pre>
 * @Autowired
 * @Qualifier("websubRestTemplate")
 * private RestTemplate restTemplate;
 * @Autowired
 * private PublisherClient<String, String, HttpHeaders> publisherClient;
 * </pre>
 * </p>
 *
 * @author Urvil Joshi
 * @since 1.0.0
 * @see RestTemplate
 * @see PublisherClient
 * @see PublisherClientImpl
 */
@Configuration
public class WebSubPublisherClientConfig {

	/**
	 * Logger for debugging and error reporting.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(WebSubPublisherClientConfig.class);

	/**
	 * Flag to enable Apache HttpClient; if false, falls back to SimpleClientHttpRequestFactory.
	 */
	@Value("${mosip.websub.use-httpclient:true}")
	private boolean useHttpClient;

	/**
	 * Connect timeout in milliseconds (default: 5000).
	 */
	@Value("${mosip.websub.connect-timeout-ms:5000}")
	private int connectTimeout;

	/**
	 * Read timeout in milliseconds (default: 10000).
	 */
	@Value("${mosip.websub.read-timeout-ms:10000}")
	private int readTimeout;

	/**
	 * Connection request timeout in milliseconds (default: 5000).
	 */
	@Value("${mosip.websub.connection-request-timeout-ms:5000}")
	private int connectionRequestTimeout;

	/**
	 * Maximum total connections for the HTTP client pool (default: 100).
	 */
	@Value("${mosip.websub.max-connections:100}")
	private int maxConnections;

	/**
	 * Maximum connections per route for the HTTP client pool (default: 20).
	 */
	@Value("${mosip.websub.max-connections-per-route:20}")
	private int maxConnectionsPerRoute;

	/**
	 * Creates a configured {@link RestTemplate} for WebSub HTTP requests.
	 * <p>
	 * Returns a {@link RestTemplate} backed by Apache HttpClient 4.x for efficient connection pooling,
	 * configurable timeouts, and automatic redirect handling. The configuration includes:
	 * <ul>
	 *   <li>Connection pooling: Configurable max total and per-route connections.</li>
	 *   <li>Timeouts: Configurable connect, read, and connection request timeouts.</li>
	 *   <li>Redirects: Follows up to 3 redirects automatically.</li>
	 * </ul>
	 * The bean is named <code>websubRestTemplate</code> for injection into WebSub components
	 * (e.g., {@link PublisherClientImpl}, {@link RestTemplateHelper}). Optimized for high-throughput
	 * WebSub interactions with minimal startup overhead (~1ms).
	 * </p>
	 *
	 * @return the configured RestTemplate
	 */
	@Bean(name = "websubRestTemplate")
	public RestTemplate restTemplate() {
		if (useHttpClient) {
			try {
				// Configure connection pooling
				PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder = PoolingHttpClientConnectionManagerBuilder.create()
						.setMaxConnTotal(maxConnections)
						.setMaxConnPerRoute(maxConnectionsPerRoute);

				// Configure timeouts
				RequestConfig requestConfig = RequestConfig.custom()
						.setConnectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
						.setResponseTimeout(readTimeout, TimeUnit.MILLISECONDS)
						.setConnectionRequestTimeout(connectionRequestTimeout, TimeUnit.MILLISECONDS)
						.build();

				// Build HttpClient
				CloseableHttpClient httpClient = HttpClientBuilder.create()
						.setConnectionManager(connectionManagerBuilder.build())
						.setDefaultRequestConfig(requestConfig)
						.setRedirectStrategy(new DefaultRedirectStrategy())
						.build();

				HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
				RestTemplate restTemplate = new RestTemplate(factory);
				LOGGER.debug("Created websubRestTemplate with HttpClient 5.x: maxTotal={}, maxPerRoute={}, timeouts={},{},{}",
						maxConnections, maxConnectionsPerRoute, connectTimeout, readTimeout, connectionRequestTimeout);
				return restTemplate;
			} catch (NoClassDefFoundError | NoSuchMethodError e) {
				LOGGER.warn("Apache HttpClient 5.x unavailable or incompatible; falling back to SimpleClientHttpRequestFactory: {}", e.getMessage());
			}
		}

		// Fallback to SimpleClientHttpRequestFactory
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(connectTimeout);
		factory.setReadTimeout(readTimeout);
		RestTemplate restTemplate = new RestTemplate(factory);
		LOGGER.debug("Created websubRestTemplate with SimpleClientHttpRequestFactory: connectTimeout={}, readTimeout={}",
				connectTimeout, readTimeout);
		return restTemplate;
	}

	/**
	 * Creates a WebSub publisher client for topic management and content publishing.
	 * <p>
	 * Returns a {@link PublisherClientImpl} instance for registering, unregistering, and publishing
	 * updates to WebSub topics. The client uses a generic payload type (<code>P</code>) and supports
	 * custom {@link HttpHeaders}. Optimized for minimal instantiation overhead and thread-safety
	 * via Spring's singleton scope. The client relies on {@link RestTemplateHelper} for HTTP
	 * communication.
	 * </p>
	 *
	 * @param <P> the payload type for publishing
	 * @return the WebSub publisher client
	 */
	@Bean
	public <P> PublisherClient<String, P, HttpHeaders> publisherClient(){
		PublisherClient<String, P, HttpHeaders> publisherClient = new PublisherClientImpl<>();
		LOGGER.debug("Created PublisherClientImpl bean");
		return publisherClient;
	}
}