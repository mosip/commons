package io.mosip.kernel.auth.defaultadapter.helper;

import java.security.PublicKey;
import java.util.Objects;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;

import io.mosip.kernel.auth.defaultadapter.constant.AuthAdapterErrorCode;
import io.mosip.kernel.auth.defaultadapter.exception.AuthManagerException;
import io.mosip.kernel.openid.bridge.model.MosipUserDto;

/**
 * Facade that chooses online user-info validation or JWKS offline validation
 * for servlet-stack callers.
 * <p>
 * {@code auth.server.admin.offline.comp.token.validate} defaults to offline
 * (JWKS) with fallback to online when the public key cannot be loaded. Local
 * profile offline validation is deprecated.
 * <p>
 * This adapter is a library other MOSIP services put on the classpath.
 */
@Component
public class TokenValidationHelper {
    
    /**
     * When {@code true}, JWKS offline validation is attempted first.
     */
    @Value("${auth.server.admin.offline.comp.token.validate:true}")
	private boolean offlineTokenValidate;

    /**
     * Active Spring profile; {@code local} selects deprecated local offline
     * validation.
     */
    @Value("${spring.profiles.active:}")
	String activeProfile;

    /**
     * Historical certs path property (unused by this class; JWKS path lives on
     * {@link ValidateTokenHelper}).
     */
    @Value("${auth.server.admin.certs.path:/protocol/openid-connect/certs}")
    String certsPath;

    /**
     * Performs expiry, signature, audience, user-info, and MosipUser mapping.
     */
    @Autowired
    private ValidateTokenHelper validateTokenHelper;

    /**
     * Logger for this facade.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TokenValidationHelper.class);

    /**
     * Validates {@code token} using the configured offline/online strategy.
     *
     * @param token        access-token JWT
     * @param restTemplate client for user-info and JWKS fallback
     * @return the mapped MOSIP user
     * @throws AuthManagerException if validation fails
     */
    public MosipUserDto getTokenValidatedUserResponse(String token, RestTemplate restTemplate) {

        if (!offlineTokenValidate) {
            return doOnlineTokenValidation(token, restTemplate);
        }
        return doOfflineTokenValidation(token, restTemplate);
    }

    /**
     * Always validates {@code token} via the OIDC user-info endpoint.
     *
     * @param token        access-token JWT
     * @param restTemplate HTTP client
     * @return the mapped MOSIP user
     * @throws AuthManagerException if validation fails
     */
    public MosipUserDto getOnlineTokenValidatedUserResponse(String token, RestTemplate restTemplate) {
        return doOnlineTokenValidation(token, restTemplate);
    }

    /**
     * Maps {@link ValidateTokenHelper} HTTP statuses to {@link AuthManagerException}.
     *
     * @param token        access-token JWT
     * @param restTemplate HTTP client
     * @return the mapped MOSIP user
     * @throws AuthManagerException on 401, 403, 417, or any non-200
     */
    private MosipUserDto doOnlineTokenValidation(String token, RestTemplate restTemplate) {
        try {
            JWT.decode(token);
        } catch (JWTDecodeException e) {
            throw new AuthManagerException(AuthAdapterErrorCode.INVALID_TOKEN.getErrorCode(),
                    AuthAdapterErrorCode.INVALID_TOKEN.getErrorMessage());
        }
        ImmutablePair<HttpStatus, MosipUserDto> validateResp = validateTokenHelper.doOnlineTokenValidation(token, restTemplate);

        if (validateResp.getLeft() == HttpStatus.EXPECTATION_FAILED || validateResp.getLeft() == HttpStatus.UNAUTHORIZED) {
            throw new AuthManagerException(AuthAdapterErrorCode.UNAUTHORIZED.getErrorCode(), 
                            AuthAdapterErrorCode.UNAUTHORIZED.getErrorMessage());
        }
        if (validateResp.getLeft() == HttpStatus.FORBIDDEN) { 
            throw new AuthManagerException(AuthAdapterErrorCode.FORBIDDEN.getErrorCode(), 
                            AuthAdapterErrorCode.FORBIDDEN.getErrorMessage());
        }
        if (validateResp.getLeft() != HttpStatus.OK) { 
            throw new AuthManagerException(AuthAdapterErrorCode.UNAUTHORIZED.getErrorCode(), 
                            AuthAdapterErrorCode.UNAUTHORIZED.getErrorMessage());
        }

		return validateResp.getRight();
	}

    /**
     * Local profile uses deprecated offline validation; otherwise JWKS offline
     * with online fallback.
     *
     * @param token        access-token JWT
     * @param restTemplate HTTP client for online fallback
     * @return the mapped MOSIP user
     */
    private MosipUserDto doOfflineTokenValidation(String token, RestTemplate restTemplate) {

        if(activeProfile.equalsIgnoreCase("local")) {
            return validateTokenHelper.doOfflineLocalTokenValidation(token);
        }
        return doOfflineEnvTokenValidation(token, restTemplate);
    }

    /**
     * Validates signature with JWKS; falls back to online user-info when the
     * public key is unavailable.
     *
     * @param jwtToken     access-token JWT
     * @param restTemplate HTTP client for online fallback
     * @return the mapped MOSIP user
     * @throws AuthManagerException if {@link ValidateTokenHelper#isTokenValid}
     *                              returns false
     */
    private MosipUserDto doOfflineEnvTokenValidation(String jwtToken, RestTemplate restTemplate) {
        DecodedJWT decodedJWT;
        try {
            decodedJWT = JWT.decode(jwtToken);
        } catch (JWTDecodeException e) {
            throw new AuthManagerException(AuthAdapterErrorCode.INVALID_TOKEN.getErrorCode(),
                    AuthAdapterErrorCode.INVALID_TOKEN.getErrorMessage());
        }
        PublicKey publicKey = validateTokenHelper.getPublicKey(decodedJWT);
        // Still not able to get the public key either from server or local cache,
        // proceed with online token validation.
        if (Objects.isNull(publicKey)) {
            return doOnlineTokenValidation(jwtToken, restTemplate);
        }

        ImmutablePair<Boolean, AuthAdapterErrorCode> validateResp = validateTokenHelper.isTokenValid(decodedJWT, publicKey);
        if (validateResp.getLeft() == Boolean.FALSE) { 
            throw new AuthManagerException(validateResp.getRight().getErrorCode(), validateResp.getRight().getErrorMessage());
        }
        return validateTokenHelper.buildMosipUser(decodedJWT, jwtToken);
    }

    /**
     * Validates {@code token} via WebClient user-info ({@code retrieve()} in
     * {@link ValidateTokenHelper}) and maps HTTP statuses to
     * {@link AuthManagerException}.
     *
     * @param token     access-token JWT
     * @param webClient reactive HTTP client
     * @return the mapped MOSIP user
     * @throws AuthManagerException on 401, 403, 417, or any non-200
     */
    public MosipUserDto doOnlineTokenValidation(String token, WebClient webClient) {
		try {
			JWT.decode(token);
		} catch (JWTDecodeException e) {
			throw new AuthManagerException(AuthAdapterErrorCode.INVALID_TOKEN.getErrorCode(),
					AuthAdapterErrorCode.INVALID_TOKEN.getErrorMessage());
		}
		ImmutablePair<HttpStatus, MosipUserDto> validateResp = validateTokenHelper.doOnlineTokenValidation(token, webClient);

        if (validateResp.getLeft() == HttpStatus.EXPECTATION_FAILED || validateResp.getLeft() == HttpStatus.UNAUTHORIZED) {
            throw new AuthManagerException(AuthAdapterErrorCode.UNAUTHORIZED.getErrorCode(), 
                            AuthAdapterErrorCode.UNAUTHORIZED.getErrorMessage());
        }
        if (validateResp.getLeft() == HttpStatus.FORBIDDEN) { 
            throw new AuthManagerException(AuthAdapterErrorCode.FORBIDDEN.getErrorCode(), 
                            AuthAdapterErrorCode.FORBIDDEN.getErrorMessage());
        }
        if (validateResp.getLeft() != HttpStatus.OK) { 
            throw new AuthManagerException(AuthAdapterErrorCode.UNAUTHORIZED.getErrorCode(), 
                            AuthAdapterErrorCode.UNAUTHORIZED.getErrorMessage());
        }

		return validateResp.getRight();
	}
}  
