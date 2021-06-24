package io.mosip.kernel.auth.defaultadapter.config;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import io.mosip.kernel.auth.defaultadapter.constant.AuthAdapterConstant;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;


/***********************************************************************************************************************
 * It is used to intercept any http calls made using rest template from this
 * application.
 *
 * CONFIG: This is added to the list of interceptors in the RestTemplate bean
 * created in the SecurityConfig.
 *
 * TASKS: 1. Intercept all the requests from the application and do the below
 * tasks. 2. Intercept a request to add auth token to the "Authorization"
 * header. 3. Intercept a response to modify the stored token with the
 * "Authorization" header of the response.
 *
 * @author Sabbu Uday Kumar
 * @author Ramadurai Saravana Pandian
 * @author Raj Jha
 * @since 1.0.0
 **********************************************************************************************************************/

@Component
public class RestTemplateInterceptor implements ClientHttpRequestInterceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(RestTemplateInterceptor.class);

	@Autowired
	private ClientHttpRequestFactory requestFactory;

	@Autowired(required = false)
	private LoadBalancerClient loadBalancerClient;


	@Override
	public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes,
			ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
		
		httpRequest = resolveServiceId(httpRequest);
		ClientHttpResponse response = clientHttpRequestExecution.execute(httpRequest, bytes);
		return response;
	}


	

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