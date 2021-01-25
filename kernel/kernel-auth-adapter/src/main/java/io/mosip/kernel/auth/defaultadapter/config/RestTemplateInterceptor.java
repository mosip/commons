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

	// This config is introduced as a patch to fix the use of expired token from security context 
	// rather than taking token from actual request. Later proper default behaviour to be 
	// coded and this config to be removed
	@Value("${auth.adapter.rest.template.skip.context.token.reuse:false}")
	private boolean skipContextTokenReuse;

	@Override
	public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes,
			ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
		addHeadersToRequest(httpRequest, bytes);
		httpRequest = resolveServiceId(httpRequest);
		ClientHttpResponse response = clientHttpRequestExecution.execute(httpRequest, bytes);
		// getHeadersFromResponse(response);
		return response;
	}

	private void addHeadersToRequest(HttpRequest httpRequest, byte[] bytes) {
		HttpHeaders headers = httpRequest.getHeaders();
		if(!skipContextTokenReuse) {
			AuthUserDetails authUserDetails = getAuthUserDetails();
			if (authUserDetails != null)
				headers.set(AuthAdapterConstant.AUTH_HEADER_COOKIE,
						AuthAdapterConstant.AUTH_COOOKIE_HEADER + authUserDetails.getToken());
		}
	}

	private AuthUserDetails getAuthUserDetails() {
		AuthUserDetails authUserDetails = null;
		if (SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication() != null
				&& SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof AuthUserDetails)

			authUserDetails = (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return authUserDetails;
	}

	private void getHeadersFromResponse(ClientHttpResponse clientHttpResponse) {
		HttpHeaders headers = clientHttpResponse.getHeaders();
		String responseToken = headers.get(AuthAdapterConstant.AUTH_HEADER_SET_COOKIE).get(0)
				.replaceAll(AuthAdapterConstant.AUTH_COOOKIE_HEADER, "");
		getAuthUserDetails().setToken(responseToken);
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
			LOGGER.warn("Failed to choose service instance : " + ex.getMessage());
			LOGGER.debug("Failed to choose service instance", ex);
		}
		return request;
	}

}