package io.mosip.kernel.authcodeflowproxy.api.service;

import io.mosip.kernel.openid.bridge.api.service.LoginService;

/**
 * Authorization-code flow login service with an extra {@code ui_locales} parameter.
 * <p>
 * Extends {@link LoginService} with {@link #loginV2(String, String, String)} so Keycloak
 * (or a compatible IdP) can receive an OIDC UI locale hint when building the
 * authorization-endpoint URL.
 *
 * @author Aravindhan A
 *
 */
public interface LoginServiceV2 extends LoginService{

	/**
	 * Builds the Keycloak OAuth 2.0 authorization-endpoint URL for the authorization-code
	 * flow, including optional {@code ui_locales} and configured {@code claims}.
	 *
	 * @param redirectURI Base64-encoded application redirect URI suffix appended to the
	 *                    configured module {@code redirect_uri}
	 * @param state CSRF {@code state} value (UUID) echoed to Keycloak
	 * @param uiLocales optional OIDC {@code ui_locales} query parameter; omitted when
	 *                  {@code null}
	 * @return fully expanded authorization URL to which the browser should be redirected
	 * @throws io.mosip.kernel.openid.bridge.api.exception.ServiceException if configured
	 *         {@code claims} cannot be UTF-8 encoded
	 */
	String loginV2(String redirectURI, String state, String uiLocales);

}
