package io.mosip.kernel.auth.defaultadapter.config;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import io.mosip.kernel.auth.defaultadapter.constant.AuthAdapterConstant;
import io.mosip.kernel.openid.bridge.model.AuthUserDetails;

/**
 * {@link RestTemplate} interceptor that forwards the inbound requester's
 * Authorization cookie to outbound HTTP calls.
 * <p>
 * When the Spring Security context holds an {@link AuthUserDetails} principal,
 * this interceptor sets the {@code Cookie} header to
 * {@code Authorization=<token>}. Hosting MOSIP services use this on the default
 * {@code restTemplate} bean so service-to-service calls run as the original
 * user.
 * <p>
 * This adapter is a library other MOSIP services put on the classpath.
 */
public class RequesterTokenRestInterceptor implements ClientHttpRequestInterceptor {

	/**
	 * Adds the requester token cookie when present, then executes the request.
	 *
	 * @param request   the outbound HTTP request
	 * @param body      the request body bytes
	 * @param execution the remainder of the interceptor chain
	 * @return the HTTP response
	 * @throws IOException if the request fails to execute
	 */
	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		addHeadersToRequest(request, body);
		return execution.execute(request, body);
	}

	/**
	 * Writes the {@code Cookie: Authorization=<token>} header from the current
	 * {@link AuthUserDetails}, if any.
	 *
	 * @param httpRequest the outbound request whose headers are mutated
	 * @param bytes       unused request body; retained to match the interceptor
	 *                    contract
	 */
	private void addHeadersToRequest(HttpRequest httpRequest, byte[] bytes) {
		HttpHeaders headers = httpRequest.getHeaders();
		AuthUserDetails authUserDetails = getAuthUserDetails();
			if (authUserDetails != null)
				headers.set(AuthAdapterConstant.AUTH_HEADER_COOKIE,
						AuthAdapterConstant.AUTH_HEADER + authUserDetails.getToken());
	}

	/**
	 * Reads {@link AuthUserDetails} from the Spring Security context, or
	 * {@code null} when no authenticated principal of that type is present.
	 *
	 * @return the current auth user details, or {@code null}
	 */
	private AuthUserDetails getAuthUserDetails() {
		AuthUserDetails authUserDetails = null;
		if (SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication() != null
				&& SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof AuthUserDetails)

			authUserDetails = (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return authUserDetails;
	}

}
