package io.mosip.kernel.auth.defaultadapter.config;

import java.io.IOException;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import io.mosip.kernel.auth.defaultadapter.constant.AuthAdapterConstant;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;

public class RequesterTokenRestInterceptor implements ClientHttpRequestInterceptor {

	/*
	 * This config is introduced as a patch to fix the use of expired token from
	 * security context rather than taking token from actual request. Later proper
	 * default behaviour to be coded and this config to be removed
	 */
	//TODO
	private boolean skipContextTokenReuse;

	public RequesterTokenRestInterceptor(Environment environment) {
		skipContextTokenReuse = environment.getProperty("auth.adapter.rest.template.skip.context.token.reuse",
				Boolean.class, false);
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		addHeadersToRequest(request, body);
		return execution.execute(request, body);
	}

	private void addHeadersToRequest(HttpRequest httpRequest, byte[] bytes) {
		HttpHeaders headers = httpRequest.getHeaders();
		AuthUserDetails authUserDetails = getAuthUserDetails();
			if (!skipContextTokenReuse && authUserDetails != null)
				headers.set(AuthAdapterConstant.AUTH_HEADER_COOKIE,
						AuthAdapterConstant.AUTH_HEADER_COOKIE + authUserDetails.getToken());
	}

	private AuthUserDetails getAuthUserDetails() {
		AuthUserDetails authUserDetails = null;
		if (SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication() != null
				&& SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof AuthUserDetails)

			authUserDetails = (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return authUserDetails;
	}

	// This methods set token send by resource server to context
	private void getHeadersFromResponse(ClientHttpResponse clientHttpResponse) {
		HttpHeaders headers = clientHttpResponse.getHeaders();
		String responseToken = headers.get(AuthAdapterConstant.AUTH_HEADER_COOKIE).get(0)
				.replaceAll(AuthAdapterConstant.AUTH_HEADER_COOKIE, "");
		getAuthUserDetails().setToken(responseToken);
	}

}
