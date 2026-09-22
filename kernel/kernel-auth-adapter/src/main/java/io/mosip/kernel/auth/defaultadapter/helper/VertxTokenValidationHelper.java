package io.mosip.kernel.auth.defaultadapter.helper;

import java.io.IOException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.mosip.kernel.auth.defaultadapter.constant.AuthAdapterConstant;
import io.mosip.kernel.auth.defaultadapter.constant.AuthAdapterErrorCode;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.openid.bridge.model.MosipUserDto;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;

/**
 * Token validation for Vert.x routes: extracts the Authorization cookie,
 * validates online or offline, then checks required roles.
 * <p>
 * Failures are written as MOSIP JSON on the {@link RoutingContext} rather than
 * thrown to Spring MVC. Vert.x 3.9.16 remains a provided dependency.
 * <p>
 * This adapter is a library other MOSIP services put on the classpath.
 */
@Lazy
@Component
public class VertxTokenValidationHelper {
    
    /**
     * Logger for validation and error-body write failures.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(VertxTokenValidationHelper.class);

    /**
     * Historical admin validate URL (unused by current user-info validation).
     */
    @Value("${auth.server.admin.validate.url:}")
	private String adminValidateUrl;

    /**
     * When {@code true}, JWKS offline validation is attempted first.
     */
    @Value("${auth.server.admin.offline.vertx.token.validate:true}")
	private boolean offlineTokenValidate;

    /**
     * Active Spring profile; {@code local} selects deprecated local offline
     * validation.
     */
    @Value("${spring.profiles.active:}")
	String activeProfile;

    /**
     * Mapper used to write MOSIP error envelopes onto the Vert.x response.
     */
    @Autowired
	private ObjectMapper objectMapper;

    /**
     * Performs expiry, signature, audience, user-info, and MosipUser mapping.
     */
    @Autowired
    private ValidateTokenHelper validateTokenHelper;

    /**
     * Extracts the cookie token, validates it, and ensures {@code roles} intersect
     * the user's comma-separated roles.
     *
     * @param restTemplate   client for online/JWKS fallback
     * @param routingContext Vert.x request/response
     * @param roles          required roles
     * @return the MOSIP user, or {@code null} after writing an error response
     * @throws JsonParseException      if the request body cannot be parsed
     * @throws JsonMappingException    if the request body cannot be mapped
     * @throws IOException             if error JSON cannot be written
     */
    public MosipUserDto getTokenValidatedVertxUserResponse(RestTemplate restTemplate, RoutingContext routingContext, 
                String[] roles) throws JsonParseException, JsonMappingException, IOException {

		HttpServerRequest httpRequest = routingContext.request();
		String token = null;
		String cookies = httpRequest.getHeader(AuthAdapterConstant.AUTH_HEADER_COOKIE);
		if (cookies != null && !cookies.isEmpty() && cookies.contains(AuthAdapterConstant.AUTH_HEADER)) {
			token = cookies.replace(AuthAdapterConstant.AUTH_HEADER, "").trim();
		}
		if (token == null || token.isEmpty()) {
			sendErrors(routingContext, AuthAdapterErrorCode.UNAUTHORIZED, AuthAdapterConstant.NOTAUTHENTICATED);
			return null;
		}

		token = token.split(";")[0];
        MosipUserDto mosipUserDto = null;
        if (!offlineTokenValidate) {
            mosipUserDto = doOnlineTokenValidation(token, restTemplate, routingContext);
        } else {
            mosipUserDto = doOfflineTokenValidation(token, restTemplate, routingContext);
        }

        if (Objects.isNull(mosipUserDto)) {
            return null;    
        }

        boolean isAuthorized = false;
		String[] authorities = mosipUserDto.getRole().split(",");
		for (String role : roles) {
			for (String authority : authorities) {
				if (role.equals(authority)) {
					isAuthorized = true;
					break;
				}
			}
		}
		if (!isAuthorized) {
			sendErrors(routingContext, AuthAdapterErrorCode.FORBIDDEN, AuthAdapterConstant.UNAUTHORIZED);
			return null;
		}
        return mosipUserDto;
    }

    /**
     * Online user-info validation; writes Vert.x errors for 417/401/403/other.
     *
     * @param token          access-token JWT
     * @param restTemplate   HTTP client
     * @param routingContext Vert.x request/response
     * @return the MOSIP user, or {@code null} after writing an error
     * @throws JsonParseException      if the request body cannot be parsed
     * @throws JsonMappingException    if the request body cannot be mapped
     * @throws IOException             if error JSON cannot be written
     */
    private MosipUserDto doOnlineTokenValidation(String token, RestTemplate restTemplate, 
                    RoutingContext routingContext) throws JsonParseException, JsonMappingException, 
                    IOException {
        try {
            JWT.decode(token);
        } catch (JWTDecodeException e) {
            sendErrors(routingContext, AuthAdapterErrorCode.INVALID_TOKEN, AuthAdapterConstant.NOTAUTHENTICATED);
            return null;
        }
        ImmutablePair<HttpStatus, MosipUserDto> validateResp = validateTokenHelper.doOnlineTokenValidation(token, restTemplate);
        if (validateResp.getLeft() == HttpStatus.EXPECTATION_FAILED) {
            sendErrors(routingContext, AuthAdapterErrorCode.CONNECT_EXCEPTION, AuthAdapterConstant.INTERNEL_SERVER_ERROR);
        }
        
        if (validateResp.getLeft() == HttpStatus.UNAUTHORIZED) { 
            sendErrors(routingContext, AuthAdapterErrorCode.UNAUTHORIZED, AuthAdapterConstant.NOTAUTHENTICATED);
        }

        if (validateResp.getLeft() == HttpStatus.FORBIDDEN) {
            sendErrors(routingContext, AuthAdapterErrorCode.FORBIDDEN, AuthAdapterConstant.UNAUTHORIZED); 
        }

        if (validateResp.getLeft() != HttpStatus.OK) { 
            sendErrors(routingContext, AuthAdapterErrorCode.UNAUTHORIZED, AuthAdapterConstant.NOTAUTHENTICATED);
        }

        return validateResp.getRight();
    }

    /**
     * Local profile uses deprecated offline validation; otherwise JWKS offline
     * with online fallback.
     *
     * @param token          access-token JWT
     * @param restTemplate   HTTP client
     * @param routingContext Vert.x request/response
     * @return the MOSIP user, or {@code null} after writing an error
     * @throws JsonParseException      if the request body cannot be parsed
     * @throws JsonMappingException    if the request body cannot be mapped
     * @throws IOException             if error JSON cannot be written
     */
    private MosipUserDto doOfflineTokenValidation(String token, RestTemplate restTemplate, 
                RoutingContext routingContext) throws JsonParseException, JsonMappingException, 
                IOException {

        if(activeProfile.equalsIgnoreCase("local")) {
            return validateTokenHelper.doOfflineLocalTokenValidation(token);
        }
        return doOfflineEnvTokenValidation(token, restTemplate, routingContext);
    }

    /**
     * Validates signature with JWKS; falls back to online user-info when the
     * public key is unavailable.
     *
     * @param jwtToken       access-token JWT
     * @param restTemplate   HTTP client
     * @param routingContext Vert.x request/response
     * @return the MOSIP user, or {@code null} after writing an error
     * @throws JsonParseException      if the request body cannot be parsed
     * @throws JsonMappingException    if the request body cannot be mapped
     * @throws IOException             if error JSON cannot be written
     */
    private MosipUserDto doOfflineEnvTokenValidation(String jwtToken, RestTemplate restTemplate, 
                        RoutingContext routingContext) throws JsonParseException, JsonMappingException, 
                        IOException {

        DecodedJWT decodedJWT;
        try {
            decodedJWT = JWT.decode(jwtToken);
        } catch (JWTDecodeException e) {
            sendErrors(routingContext, AuthAdapterErrorCode.INVALID_TOKEN, AuthAdapterConstant.NOTAUTHENTICATED);
            return null;
        }

        PublicKey publicKey = validateTokenHelper.getPublicKey(decodedJWT);
        // Still not able to get the public key either from server or local cache,
        // proceed with online token validation.
        if (Objects.isNull(publicKey)) {
            return doOnlineTokenValidation(jwtToken, restTemplate, routingContext);
        }

        ImmutablePair<Boolean, AuthAdapterErrorCode> validateResp = validateTokenHelper.isTokenValid(decodedJWT, publicKey);
        if (validateResp.getLeft() == Boolean.FALSE) {
            int httpStatusCode = validateResp.getRight() == AuthAdapterErrorCode.UNAUTHORIZED ? 
                                    AuthAdapterConstant.NOTAUTHENTICATED : AuthAdapterConstant.UNAUTHORIZED;
            sendErrors(routingContext, validateResp.getRight(), httpStatusCode);
            return null;
        }
        return validateTokenHelper.buildMosipUser(decodedJWT, jwtToken);
    }

    /**
     * Writes a single {@link ServiceError} onto the Vert.x response.
     *
     * @param routingContext the Vert.x context
     * @param errorCode      MOSIP error code
     * @param statusCode     HTTP status
     */
    private void sendErrors(RoutingContext routingContext, AuthAdapterErrorCode errorCode, int statusCode) {
        
        List<ServiceError> errors = new ArrayList<>();
        ServiceError error = new ServiceError(errorCode.getErrorCode(), errorCode.getErrorMessage());
        LOGGER.error(error.getMessage());
        errors.add(error);
		sendErrors(routingContext, errors, statusCode);
	}

    /**
     * Writes a MOSIP {@link ResponseWrapper} of errors, copying {@code id} and
     * {@code version} from the Vert.x JSON body when present.
     *
     * @param routingContext the Vert.x context
     * @param errors         service errors
     * @param statusCode     HTTP status
     */
    private void sendErrors(RoutingContext routingContext, List<ServiceError> errors, int statusCode) {

		ResponseWrapper<ServiceError> errorResponse = new ResponseWrapper<>();
		errorResponse.getErrors().addAll(errors);
		objectMapper.registerModule(new JavaTimeModule());
		JsonNode reqNode;
		if (routingContext.getBodyAsJson() != null) {
			try {
				reqNode = objectMapper.readTree(routingContext.getBodyAsJson().toString());
				errorResponse.setId(reqNode.path("id").asText());
				errorResponse.setVersion(reqNode.path("version").asText());
			} catch (IOException exception) {
				LOGGER.error(exception.getMessage());
			}
		}
		try {
			routingContext.response().putHeader("content-type", "application/json").setStatusCode(statusCode)
					.end(objectMapper.writeValueAsString(errorResponse));

		} catch (JsonProcessingException exception) {
			LOGGER.error(exception.getMessage());
		}
	}
}   
