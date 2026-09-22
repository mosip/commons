/**
 * 
 */
package io.mosip.kernel.auth.defaultimpl.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import io.mosip.kernel.auth.defaultimpl.service.TokenGenerationService;
import io.mosip.kernel.core.authmanager.model.AuthNResponseDto;
import io.mosip.kernel.core.authmanager.model.ClientSecret;
import io.mosip.kernel.openid.bridge.api.service.AuthService;

/**
 * Obtains client-credential tokens for internal authmanager and IDA calls via
 * {@link AuthService#authenticateWithSecretKey(ClientSecret)}.
 *
 * @author Ramadurai Pandian
 *
 */

@Component
public class TokenGenerationServiceImpl implements TokenGenerationService {

	/**
	 * Auth manager service used to exchange client secret for a token.
	 */
	@Lazy
	@Autowired
	AuthService authService;

	/**
	 * Authmanager application id ({@code mosip.kernel.auth.app.id}).
	 */
	@Value("${mosip.kernel.auth.app.id}")
	private String authAppId;

	/**
	 * Authmanager OAuth client id.
	 */
	@Value("${mosip.kernel.auth.client.id}")
	private String clientId;

	/**
	 * Authmanager OAuth secret.
	 */
	@Value("${mosip.kernel.auth.secret.key}")
	private String secretKey;

	/**
	 * IDA application id ({@code mosip.kernel.ida.app.id}).
	 */
	@Value("${mosip.kernel.ida.app.id}")
	private String idaAppId;

	/**
	 * IDA OAuth client id.
	 */
	@Value("${mosip.kernel.ida.client.id}")
	private String idaClientId;

	/**
	 * IDA OAuth secret.
	 */
	@Value("${mosip.kernel.ida.secret.key}")
	private String idaSecretKey;

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.auth.service.TokenGenerationService#
	 * getInternalTokenGenerationService()
	 */
	/**
	 * Authenticates the authmanager client and returns its access token.
	 *
	 * @return internal access token
	 * @throws Exception if client-secret authentication fails
	 */
	@Override
	public String getInternalTokenGenerationService() throws Exception {
		ClientSecret clientSecret = new ClientSecret();
		clientSecret.setAppId(authAppId);
		clientSecret.setClientId(clientId);
		clientSecret.setSecretKey(secretKey);
		AuthNResponseDto authNResponseDto = authService.authenticateWithSecretKey(clientSecret);
		return authNResponseDto.getToken();
	}

	/**
	 * Authenticates the IDA client and returns its access token.
	 *
	 * @return IDA access token
	 * @throws Exception if client-secret authentication fails
	 */
	@Override
	public String getUINBasedToken() throws Exception {
		ClientSecret clientSecret = new ClientSecret();
		clientSecret.setAppId(idaAppId);
		clientSecret.setClientId(idaClientId);
		clientSecret.setSecretKey(idaSecretKey);
		AuthNResponseDto authNResponseDto = authService.authenticateWithSecretKey(clientSecret);
		return authNResponseDto.getToken();
	}

}
