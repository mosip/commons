package io.mosip.kernel.auth.defaultadapter.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;


/**
 * {@link RestTemplate} interceptor that performs client-side load balancing
 * when a {@link LoadBalancerClient} is on the classpath.
 * <p>
 * {@link RestTemplatePostProcessor} attaches this interceptor to every
 * {@code RestTemplate} bean except {@code keycloakRestTemplate}. The request
 * host is treated as a service id; if an instance is chosen, the URI is
 * reconstructed and headers are copied onto a new request.
 * <p>
 * This adapter is a library other MOSIP services put on the classpath.
 *
 * @author Sabbu Uday Kumar
 * @author Ramadurai Saravana Pandian
 * @author Raj Jha
 * @since 1.0.0
 */
@Component
public class RestTemplateInterceptor implements ClientHttpRequestInterceptor {

	/**
	 * Logger for load-balancer resolution diagnostics.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(RestTemplateInterceptor.class);

	/**
	 * Factory used to create a replacement request when a service instance is
	 * chosen.
	 */
	@Autowired
	private ClientHttpRequestFactory requestFactory;

	/**
	 * Optional Spring Cloud load balancer; when absent, the original URI is
	 * used unchanged.
	 */
	@Autowired(required = false)
	private LoadBalancerClient loadBalancerClient;


	/**
	 * Resolves the request URI via the load balancer, then executes it.
	 *
	 * @param httpRequest                 the outbound HTTP request
	 * @param bytes                       the request body
	 * @param clientHttpRequestExecution  the remainder of the interceptor chain
	 * @return the HTTP response
	 * @throws IOException if the request fails to execute
	 */
	@Override
	public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes,
			ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
		
		httpRequest = resolveServiceId(httpRequest);
		ClientHttpResponse response = clientHttpRequestExecution.execute(httpRequest, bytes);
		return response;
	}

	/**
	 * Replaces the request URI with a load-balanced instance URI when
	 * {@link #loadBalancerClient} can choose a host for {@code request.getURI().getHost()}.
	 *
	 * @param request the original outbound request
	 * @return a new request targeting the chosen instance, or {@code request} if
	 *         no instance was chosen or resolution failed
	 */
	private HttpRequest resolveServiceId(HttpRequest request) {
		try {
			if(loadBalancerClient != null) {
				LOGGER.info("Injected load balancer : {} ", loadBalancerClient.toString());
				ServiceInstance instance = loadBalancerClient.choose(request.getURI().getHost());
				if (instance != null) {
					final ClientHttpRequest newRequest = requestFactory.createRequest(
							loadBalancerClient.reconstructURI(instance, request.getURI()), request.getMethod());
					newRequest.getHeaders().addAll(request.getHeaders());
					return newRequest;
				}
			}
		} catch (Exception ex) {
			LOGGER.warn("Failed to choose service instance : {}",ex.getMessage());
		}
		return request;
	}

}
