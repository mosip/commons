package io.mosip.kernel.auth.defaultadapter.helper;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import io.mosip.kernel.auth.defaultadapter.constant.AuthAdapterConstant;
import io.mosip.kernel.auth.defaultadapter.constant.AuthAdapterErrorCode;
import io.mosip.kernel.auth.defaultadapter.exception.AuthRestException;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;

/**
 * Obtains OIDC client-credentials access tokens from Keycloak (or compatible
 * issuer) for the service self-token.
 * <p>
 * The RestTemplate overload posts form data; the WebClient overload uses
 * {@code retrieve()} and Jackson 2 on a String body (Boot 4 default codecs are
 * Jackson 3). Realm is resolved from
 * {@code mosip.kernel.auth.appids.realm.map}.
 * <p>
 * This adapter is a library other MOSIP services put on the classpath.
 */
@Component
public class TokenHelper {

	/**
	 * Logger for token-endpoint diagnostics.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(TokenHelper.class);

	/**
	 * Public OIDC issuer URI (used when the internal URI is blank).
	 */
	@Value("${auth.server.admin.issuer.uri:}")
	private String issuerURI;

	/**
	 * Internal OIDC issuer URI for in-cluster token requests.
	 */
	@Value("${auth.server.admin.issuer.internal.uri:}")
	private String issuerInternalURI;

	/**
	 * Parses the token-endpoint JSON body.
	 */
	@Autowired
	private ObjectMapper mapper;

	/**
	 * Map of MOSIP application id to Keycloak realm name.
	 */
	@Value("#{${mosip.kernel.auth.appids.realm.map}}")
	private Map<String, String> realmMap;

	/**
	 * Path appended after the realm, default OpenID Connect token endpoint.
	 */
	@Value("${auth.server.admin.oidc.token.path:/protocol/openid-connect/token}")
	private String tokenPath;

	/**
	 * Requests a client-credentials token using {@link RestTemplate}.
	 *
	 * @param clientId     OIDC client id
	 * @param clientSecret OIDC client secret
	 * @param appId        MOSIP application id used to resolve the realm
	 * @param restTemplate HTTP client
	 * @return the access token, or {@code null} if the issuer/realm is missing or
	 *         the call failed
	 * @throws AuthRestException if the body contains MOSIP {@link ServiceError}s
	 */
	public String getClientToken(String clientId, String clientSecret, String appId, RestTemplate restTemplate) {
		if ("".equals(issuerURI)) {
			LOGGER.warn("OIDC Service URL is not available in config file, not requesting for new auth token.");
			return null;
		}
		issuerInternalURI = issuerInternalURI.trim().isEmpty() ? issuerURI : issuerInternalURI;
		LOGGER.info("Requesting for new Token for the provided OIDC Service: {}", issuerInternalURI);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> valueMap = new LinkedMultiValueMap<String, String>();
		valueMap.add(AuthAdapterConstant.GRANT_TYPE, AuthAdapterConstant.CLIENT_CREDENTIALS);
		valueMap.add(AuthAdapterConstant.CLIENT_ID, clientId);
		valueMap.add(AuthAdapterConstant.CLIENT_SECRET, clientSecret);

		HttpEntity<String> response = null;
		try {
			HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(valueMap, headers);
			String realm = getRealmIdFromAppId(appId);
			if (Objects.isNull(realm))
				return null;
			String tokenUrl = new StringBuilder(issuerInternalURI).append(realm).append(tokenPath).toString();
			response = restTemplate.postForEntity(tokenUrl, request, String.class);
		} catch (HttpServerErrorException | HttpClientErrorException e) {
			LOGGER.error("error connecting to keycloak {}", e.getResponseBodyAsString());
		}
		if (response == null) {
			LOGGER.error("error connecting to keycloak {}",
					AuthAdapterErrorCode.CANNOT_CONNECT_TO_AUTH_SERVICE.getErrorMessage());
			return null;
		}
		String responseBody = response.getBody();
		List<ServiceError> validationErrorList = ExceptionUtils.getServiceErrorList(responseBody);
		if (!validationErrorList.isEmpty()) {
			throw new AuthRestException(validationErrorList);
		}
		try {
			JsonNode jsonNode = mapper.readTree(responseBody);
			String accessToken = jsonNode.get(AuthAdapterConstant.ACCESS_TOKEN).asText();
			if (Objects.nonNull(accessToken)) {
				LOGGER.info("Found Token in response body and returning the Token");
				return accessToken;
			}
		} catch (IOException e) {
			LOGGER.error("Error Parsing Response data {}", e.getMessage(), e);
		}

		LOGGER.error("Error connecting to OIDC service (RestTemplate) {} or UNKNOWN Error.",
				AuthAdapterErrorCode.CANNOT_CONNECT_TO_AUTH_SERVICE.getErrorMessage());
		return null;
	}

	/**
	 * Requests a client-credentials token using {@link WebClient}
	 * {@code retrieve()} and Jackson 2.
	 *
	 * @param clientId     OIDC client id
	 * @param clientSecret OIDC client secret
	 * @param appId        MOSIP application id used to resolve the realm
	 * @param webClient    reactive HTTP client
	 * @return the access token, or {@code null} if the issuer/realm is missing or
	 *         the call failed
	 */
	public String getClientToken(String clientId, String clientSecret, String appId, WebClient webClient) {
		if ("".equals(issuerURI)) {
			LOGGER.warn("OIDC Service URL is not available in config file, not requesting for new auth token.");
			return null;
		}
		issuerInternalURI = issuerInternalURI.trim().isEmpty()?issuerURI:issuerInternalURI;
		LOGGER.info("Requesting for new Token for the provided OIDC Service(WebClient): {}", issuerInternalURI);
		MultiValueMap<String, String> valueMap = new LinkedMultiValueMap<String, String>();
		valueMap.add(AuthAdapterConstant.GRANT_TYPE, AuthAdapterConstant.CLIENT_CREDENTIALS);
		valueMap.add(AuthAdapterConstant.CLIENT_ID, clientId);
		valueMap.add(AuthAdapterConstant.CLIENT_SECRET, clientSecret);

		String realm = getRealmIdFromAppId(appId);
		if (Objects.isNull(realm))
			return null;
		String tokenUrl = new StringBuilder(issuerInternalURI).append(realm).append(tokenPath).toString();
		try {
			String responseBody = webClient.method(HttpMethod.POST)
					.uri(UriComponentsBuilder.fromUriString(tokenUrl).toUriString())
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.body(BodyInserters.fromFormData(valueMap))
					.retrieve()
					.bodyToMono(String.class)
					.block();
			String accessToken = null;
			if (responseBody != null) {
				JsonNode jsonNode = mapper.readTree(responseBody);
				JsonNode tokenNode = jsonNode.get(AuthAdapterConstant.ACCESS_TOKEN);
				if (tokenNode != null && !tokenNode.isNull()) {
					accessToken = tokenNode.asText();
				}
			}
			if (Objects.nonNull(accessToken)) {
				LOGGER.info("Found Token in response body and returning the Token(WebClient)");
				return accessToken;
			}
		} catch (WebClientResponseException e) {
			LOGGER.error("Error connecting to OIDC service (WebClient) {} or UNKNOWN Error.",
					e.getStatusCode());
		} catch (IOException e) {
			LOGGER.error("Error Parsing Response data {}", e.getMessage(), e);
		}

		LOGGER.error("Error connecting to OIDC service (WebClient) {} or UNKNOWN Error.",
				AuthAdapterErrorCode.CANNOT_CONNECT_TO_AUTH_SERVICE.getErrorMessage());
		return null;
	}

	/**
	 * Lower-cases the realm name for {@code appId} from {@link #realmMap}.
	 *
	 * @param appId MOSIP application id
	 * @return the realm, or {@code null} if unmapped
	 */
	private String getRealmIdFromAppId(String appId) {

		if (realmMap.get(appId) != null) {
			return realmMap.get(appId).toLowerCase();
		}

		LOGGER.warn(
				"Realm not configured in configuration for appId: " + appId + ", not requesting for new auth token.");
		return null;
	}
}
