package io.mosip.kernel.auth.defaultadapter.helper;

import static io.mosip.kernel.auth.defaultadapter.constant.AuthAdapterErrorCode.OFFLINE_AUTH_DEPRECATED;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.auth.defaultadapter.constant.AuthAdapterConstant;
import io.mosip.kernel.auth.defaultadapter.constant.AuthAdapterErrorCode;
import io.mosip.kernel.auth.defaultadapter.exception.AuthManagerException;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.util.DateUtils2;
import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.kernel.openid.bridge.model.MosipUserDto;
import jakarta.annotation.PostConstruct;

/**
 * Low-level JWT and OIDC user-info validation used by servlet and Vert.x
 * facades.
 * <p>
 * Online validation calls the user-info endpoint (RestTemplate or WebClient
 * {@code retrieve()} with Jackson 2 on a String body). Offline validation uses
 * JWKS, expiry, issuer host, RSA signature, and audience/{@code azp} checks.
 * Dates use {@code DateUtils2}.
 * <p>
 * This adapter is a library other MOSIP services put on the classpath.
 */
@Component
public class ValidateTokenHelper {

	/**
	 * Logger for validation failures.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ValidateTokenHelper.class);

	/**
	 * Cache of JWKS public keys keyed by JWT {@code kid}.
	 */
	private Map<String, PublicKey> publicKeys = new HashMap<>();

	/**
	 * Path appended after issuer+realm when {@link #certsUrl} is blank.
	 */
	@Value("${auth.server.admin.oidc.certs.path:/protocol/openid-connect/certs}")
	private String certsPath;

	/**
	 * Path appended after issuer+realm when {@link #userInfoUrl} is blank.
	 */
	@Value("${auth.server.admin.oidc.userinfo.path:/protocol/openid-connect/userinfo}")
	private String userInfo;

	/**
	 * When {@code true}, the JWT issuer host must match {@link #issuerURI}.
	 */
	@Value("${auth.server.admin.issuer.domain.validate:true}")
	private boolean validateIssuerDomain;

	/**
	 * Public issuer URI; must match the token {@code iss} host when domain
	 * validation is enabled.
	 */
	@Value("${auth.server.admin.issuer.uri:}")
	private String issuerURI;

	/**
	 * Absolute JWKS URL; when set, realm-relative certs path is not used.
	 */
	@Value("${auth.server.admin.oidc.certs.url:}")
	private String certsUrl;
	
	/**
	 * Absolute user-info URL; when set, realm-relative user-info path is not used.
	 */
	@Value("${auth.server.admin.oidc.userinfo.url:}")
	private String userInfoUrl;
	
	/**
	 * Internal issuer URI used to build certs/user-info URLs; falls back to
	 * {@link #issuerURI} when blank.
	 */
	@Value("${auth.server.admin.issuer.internal.uri:}")
	private String issuerInternalURI;

	/**
	 * When {@code true}, audience or {@code azp} must match {@link #allowedAudience}.
	 */
	@Value("${auth.server.admin.audience.claim.validate:true}")
	private boolean validateAudClaim;

	/**
	 * Allowed audience / AZP values, resolved per application name in {@link #init()}.
	 */
	// @Value("${auth.server.admin.allowed.audience:}")
	private List<String> allowedAudience;

	/**
	 * Parses OIDC error JSON from failed user-info calls.
	 */
	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * Used to resolve per-application allowed audience and application name.
	 */
	@Autowired
	private Environment environment;

	/**
	 * Loads {@link #allowedAudience} and defaults {@link #issuerInternalURI} to
	 * {@link #issuerURI} when blank.
	 */
	@PostConstruct
	@SuppressWarnings("unchecked")
	private void init() {
		String applName = getApplicationName();
		this.allowedAudience = (List<String>) environment.getProperty("auth.server.admin.allowed.audience." + applName,
				List.class,
				environment.getProperty("auth.server.admin.allowed.audience", List.class, Collections.EMPTY_LIST));
		issuerInternalURI = issuerInternalURI.trim().isEmpty() ? issuerURI : issuerInternalURI;
	}

	/**
	 * Returns the first comma-separated {@code spring.application.name} value.
	 *
	 * @return the hosting application name
	 * @throws RuntimeException if the property is missing or blank
	 */
	@SuppressWarnings("java:S2259") // added suppress for sonarcloud. Null check is performed at line # 211
	private String getApplicationName() {
		String appNames = environment.getProperty("spring.application.name");
		if (appNames != null && !EmptyCheckUtils.isNullEmpty(appNames)) {
			List<String> appNamesList = Stream.of(appNames.split(",")).collect(Collectors.toList());
			return appNamesList.get(0);
		} else {
			throw new RuntimeException("property spring.application.name not found");
		}
	}

	/**
	 * Local-profile offline validation is no longer supported.
	 *
	 * @param jwtToken unused
	 * @return never returns
	 * @throws AuthManagerException always, with
	 *                              {@link AuthAdapterErrorCode#OFFLINE_AUTH_DEPRECATED}
	 */
	public MosipUserDto doOfflineLocalTokenValidation(String jwtToken) {
		LOGGER.info("offline verification for local profile.");
		throw new AuthManagerException(OFFLINE_AUTH_DEPRECATED.getErrorCode(), OFFLINE_AUTH_DEPRECATED.getErrorMessage());
	}

	/**
	 * Checks expiry, issuer host, RSA signature, and audience/{@code azp}.
	 *
	 * @param decodedJWT the decoded access token
	 * @param publicKey  JWKS public key for the token {@code kid}
	 * @return {@code (true, null)} when valid, otherwise {@code (false, error)}
	 */
	public ImmutablePair<Boolean, AuthAdapterErrorCode> isTokenValid(DecodedJWT decodedJWT, PublicKey publicKey) {
		// First, token expire
		LocalDateTime expiryTime = DateUtils2
				.convertUTCToLocalDateTime(DateUtils2.getUTCTimeFromDate(decodedJWT.getExpiresAt()));
		String userName = decodedJWT.getClaim(AuthAdapterConstant.PREFERRED_USERNAME).asString();
		if (!DateUtils2.before(DateUtils2.getUTCCurrentDateTime(), expiryTime)) {
			LOGGER.error("Provided Auth Token expired. Throwing Authentication Exception. UserName: " + userName);
			return ImmutablePair.of(Boolean.FALSE, AuthAdapterErrorCode.UNAUTHORIZED);
		}

		// Second, issuer domain check.
		boolean tokenDomainMatch = getTokenIssuerDomain(decodedJWT);
		if (validateIssuerDomain && !tokenDomainMatch) {
			LOGGER.error(
					"Provided Auth Token Issue domain does not match. Throwing Authentication Exception. UserName: "
							+ userName);
			return ImmutablePair.of(Boolean.FALSE, AuthAdapterErrorCode.UNAUTHORIZED);
		}

		// Third, signature validation.
		try {
			String tokenAlgo = decodedJWT.getAlgorithm();
			Algorithm algorithm = getVerificationAlgorithm(tokenAlgo, publicKey);
			algorithm.verify(decodedJWT);
		} catch (SignatureVerificationException signatureException) {
			LOGGER.error("Signature validation failed, Throwing Authentication Exception. UserName: " + userName,
					signatureException);
			return ImmutablePair.of(Boolean.FALSE, AuthAdapterErrorCode.UNAUTHORIZED);
		}

		// Fourth, audience | azp validation.
		boolean matchFound = validateAudience(decodedJWT);
		// No match found after comparing audience & azp
		if (!matchFound) {
			LOGGER.error("Provided Client Id does not match with Aud/AZP. Throwing Authorizaion Exception. UserName: "
					+ userName);
			return ImmutablePair.of(Boolean.FALSE, AuthAdapterErrorCode.FORBIDDEN);
		}
		return ImmutablePair.of(Boolean.TRUE, null);
	}

	/**
	 * When {@link #validateAudClaim} is true, requires {@code aud} or {@code azp}
	 * to match {@link #allowedAudience}.
	 *
	 * @param decodedJWT the decoded access token
	 * @return {@code true} if audience checks are disabled or a match is found
	 */
	private boolean validateAudience(DecodedJWT decodedJWT) {
		boolean matchFound = false;
		if (validateAudClaim) {

			List<String> tokenAudience = decodedJWT.getAudience();
			matchFound = tokenAudience.stream().anyMatch(allowedAudience::contains);

			// comparing with azp.
			String azp = decodedJWT.getClaim(AuthAdapterConstant.AZP).asString();
			if (!matchFound) {
				matchFound = allowedAudience.stream().anyMatch(azp::equalsIgnoreCase);
			}
		}
		return matchFound;
	}

	/**
	 * This method validates if the issuer domain in the JWT matches the issuerURI
	 * configured in the properties.
	 *
	 * @param decodedJWT the decoded access token
	 * @return {@code true} if token issuer host equals {@link #issuerURI} host
	 */
	private boolean getTokenIssuerDomain(DecodedJWT decodedJWT) {
		String domain = decodedJWT.getClaim(AuthAdapterConstant.ISSUER).asString();
		try {
			String tokenHost = new URI(domain).getHost();
			String issuerHost = new URI(issuerURI).getHost();
			return tokenHost.equalsIgnoreCase(issuerHost);
		} catch (URISyntaxException synExp) {
			LOGGER.error("Unable to parse domain from issuer.", synExp);
		}
		return false;
	}

	/**
	 * Returns a cached or freshly downloaded JWKS public key for the token
	 * {@code kid}.
	 *
	 * @param decodedJWT the decoded access token
	 * @return the public key, or {@code null} if download failed
	 */
	public PublicKey getPublicKey(DecodedJWT decodedJWT) {
		String userName = decodedJWT.getClaim(AuthAdapterConstant.PREFERRED_USERNAME).asString();
		LOGGER.info("offline verification for environment profile. UserName: " + userName);

		String keyId = decodedJWT.getKeyId();
		PublicKey publicKey = publicKeys.get(keyId);

		if (Objects.isNull(publicKey)) {
			if(certsUrl == null || certsUrl.isEmpty()) {
				String realm = getRealM(decodedJWT);
				publicKey = getIssuerPublicKey(keyId, certsPath, realm);
			} else {
				publicKey = getIssuerPublicKey(keyId, certsUrl);
			}
			publicKeys.put(keyId, publicKey);
		}
		return publicKey;
	}

	/**
	 * Reads the Keycloak realm as the last path segment of the {@code iss} claim.
	 *
	 * @param decodedJWT the decoded access token
	 * @return the realm name
	 */
	private String getRealM(DecodedJWT decodedJWT) {
		String tokenIssuer = decodedJWT.getClaim(AuthAdapterConstant.ISSUER).asString();
		return tokenIssuer.substring(tokenIssuer.lastIndexOf("/") + 1);
	}

	/**
	 * Downloads the JWKS key from {@link #issuerInternalURI}{@code + realm + certsPathVal}.
	 *
	 * @param keyId        JWT {@code kid}
	 * @param certsPathVal certs path relative to issuer+realm
	 * @param realm        Keycloak realm
	 * @return the public key, or {@code null} on failure
	 */
	private PublicKey getIssuerPublicKey(String keyId, String certsPathVal, String realm) {
		return getIssuerPublicKey(keyId, issuerInternalURI + realm + certsPathVal);
	}
	
	/**
	 * Downloads the JWKS key identified by {@code keyId} from {@code certsUrIPath}.
	 *
	 * @param keyId        JWT {@code kid}
	 * @param certsUrIPath absolute JWKS URL
	 * @return the public key, or {@code null} on failure
	 */
	private PublicKey getIssuerPublicKey(String keyId, String certsUrIPath) {
		try {

			URI uri = new URI(certsUrIPath).normalize();
			JwkProvider provider = new UrlJwkProvider(uri.toURL());
			Jwk jwk = provider.get(keyId);
			return jwk.getPublicKey();
		} catch (JwkException | URISyntaxException | MalformedURLException e) {
			LOGGER.error("Error downloading Public key from server".concat(e.getMessage()));
		}
		return null;
	}

	/**
	 * Maps JWT {@code alg} to Auth0 RSA verification (RS256/RS384/RS512; default
	 * RS256).
	 *
	 * @param tokenAlgo JWT algorithm name
	 * @param publicKey RSA public key
	 * @return the verification algorithm
	 */
	private Algorithm getVerificationAlgorithm(String tokenAlgo, PublicKey publicKey) {
		// Later will add other Algorithms.
		switch (tokenAlgo) {
		case "RS256":
			return Algorithm.RSA256((RSAPublicKey) publicKey, null);
		case "RS384":
			return Algorithm.RSA384((RSAPublicKey) publicKey, null);
		case "RS512":
			return Algorithm.RSA512((RSAPublicKey) publicKey, null);
		default:
			return Algorithm.RSA256((RSAPublicKey) publicKey, null);
		}
	}

	/**
	 * Maps JWT claims to {@link MosipUserDto}, preferring {@code realm_access.roles}
	 * over a flat {@code roles} claim.
	 *
	 * @param decodedJWT the decoded access token
	 * @param jwtToken   original JWT string stored on the DTO
	 * @return the MOSIP user
	 */
	@SuppressWarnings("unchecked")
	public MosipUserDto buildMosipUser(DecodedJWT decodedJWT, String jwtToken) {
		MosipUserDto mosipUserDto = new MosipUserDto();
		String user = decodedJWT.getSubject();
		mosipUserDto.setToken(jwtToken);
		mosipUserDto.setMail(decodedJWT.getClaim(AuthAdapterConstant.EMAIL).asString());
		mosipUserDto.setMobile(decodedJWT.getClaim(AuthAdapterConstant.MOBILE).asString());
		mosipUserDto.setUserId(decodedJWT.getClaim(AuthAdapterConstant.PREFERRED_USERNAME).asString());
		Claim realmAccess = decodedJWT.getClaim(AuthAdapterConstant.REALM_ACCESS);
		if (!realmAccess.isNull()) {
			List<String> roles = (List<String>) realmAccess.asMap().get("roles");
			StringBuilder strBuilder = new StringBuilder();

			for (String role : roles) {
				strBuilder.append(role);
				strBuilder.append(AuthAdapterConstant.COMMA);
			}
			mosipUserDto.setRole(strBuilder.toString());
			mosipUserDto.setName(user);
		} else {
			mosipUserDto.setRole(decodedJWT.getClaim(AuthAdapterConstant.ROLES).asString());
			mosipUserDto.setName(user);
		}

		LOGGER.info("user (offline verification done): " + mosipUserDto.getUserId());
		return mosipUserDto;
	}

	/**
	 * Calls the OIDC user-info endpoint with a Bearer token via RestTemplate, then
	 * checks audience and maps the JWT to {@link MosipUserDto}.
	 *
	 * @param jwtToken     access-token JWT
	 * @param restTemplate HTTP client
	 * @return HTTP status plus user, or status with {@code null} user on failure
	 */
	public ImmutablePair<HttpStatus, MosipUserDto> doOnlineTokenValidation(String jwtToken, RestTemplate restTemplate) {
		if ("".equals(issuerURI) || "".equals(issuerInternalURI)) {
			LOGGER.warn("OIDC validate URL is not available in config file, not requesting for token validation.");
			return ImmutablePair.of(HttpStatus.EXPECTATION_FAILED, null);
		}

		DecodedJWT decodedJWT;
		try {
			decodedJWT = JWT.decode(jwtToken);
		} catch (JWTDecodeException e) {
			LOGGER.error("Malformed JWT: expected header.payload.signature");
			return ImmutablePair.of(HttpStatus.UNAUTHORIZED, null);
		}
		HttpHeaders headers = new HttpHeaders();
		headers.add(AuthAdapterConstant.AUTH_REQUEST_COOOKIE_HEADER, AuthAdapterConstant.BEARER_STR + jwtToken);
		HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
		ResponseEntity<String> response = null;
		HttpStatusCodeException statusCodeException = null;
		try {
			String userInfoPath = getUserInfoPath(decodedJWT);
			response = restTemplate.exchange(userInfoPath, HttpMethod.GET, entity, String.class);
		} catch (HttpClientErrorException | HttpServerErrorException e) {
			LOGGER.error("Token validation failed for accessToken {}", jwtToken, e);
			statusCodeException = e;
		}

		if (Objects.nonNull(statusCodeException)) {
			JsonNode errorNode;
			try {
				errorNode = objectMapper.readTree(statusCodeException.getResponseBodyAsString());
				LOGGER.error("Token validation failed error {} and message {}",
						errorNode.get(AuthAdapterConstant.ERROR), errorNode.get(AuthAdapterConstant.ERROR_DESC));
				return ImmutablePair.of(HttpStatus.valueOf(statusCodeException.getStatusCode().value()), null);
			} catch (IOException e) {
				LOGGER.error("IO Excepton in parsing response {}", e.getMessage());
			}
		}

		if (response != null && response.getStatusCode().is2xxSuccessful()) {
			// validating audience | azp claims.
			boolean matchFound = validateAudience(decodedJWT);
			if (!matchFound) {
				LOGGER.error("Provided Client Id does not match with Aud/AZP. Throwing Authorizaion Exception");
				return ImmutablePair.of(HttpStatus.FORBIDDEN, null);
			}
			MosipUserDto mosipUserDto = buildMosipUser(decodedJWT, jwtToken);
			return ImmutablePair.of(HttpStatus.OK, mosipUserDto);
		}
		return ImmutablePair.of(HttpStatus.UNAUTHORIZED, null);
	}

	/**
	 * Builds the user-info URL from {@link #userInfoUrl} or issuer+realm+
	 * {@link #userInfo}.
	 *
	 * @param decodedJWT the decoded access token (realm from {@code iss})
	 * @return the user-info URL
	 */
	private String getUserInfoPath(DecodedJWT decodedJWT) {
		String userInfoPath;
		if(userInfoUrl == null || userInfoUrl.isEmpty()) {
			String realm = getRealM(decodedJWT);
			userInfoPath = issuerInternalURI + realm + userInfo;
		} else {
			userInfoPath = userInfoUrl;
		}
		return userInfoPath;
	}

	/**
	 * Calls the OIDC user-info endpoint with a Bearer token via WebClient
	 * {@code retrieve()} and Jackson 2, then checks audience and maps the JWT.
	 *
	 * @param jwtToken  access-token JWT
	 * @param webClient reactive HTTP client
	 * @return HTTP status plus user, or status with {@code null} user on failure
	 */
	public ImmutablePair<HttpStatus, MosipUserDto> doOnlineTokenValidation(String jwtToken, WebClient webClient) {
		if ("".equals(issuerURI) || "".equals(issuerInternalURI)) {
			LOGGER.warn("OIDC validate URL is not available in config file, not requesting for token validation.");
			return ImmutablePair.of(HttpStatus.EXPECTATION_FAILED, null);
		}

		DecodedJWT decodedJWT;
		try {
			decodedJWT = JWT.decode(jwtToken);
		} catch (JWTDecodeException e) {
			LOGGER.error("Malformed JWT: expected header.payload.signature");
			return ImmutablePair.of(HttpStatus.UNAUTHORIZED, null);
		}
		HttpHeaders headers = new HttpHeaders();
		headers.add(AuthAdapterConstant.AUTH_REQUEST_COOOKIE_HEADER, AuthAdapterConstant.BEARER_STR + jwtToken);
		String userInfoPath = getUserInfoPath(decodedJWT);
		try {
			String rawBody = webClient.method(HttpMethod.GET).uri(userInfoPath).headers(httpHeaders -> {
				httpHeaders.addAll(headers);
			}).retrieve().bodyToMono(String.class).block();
			if (rawBody != null && !rawBody.isBlank()) {
				List<ServiceError> validationErrorsList = ExceptionUtils.getServiceErrorList(rawBody);
				if (!validationErrorsList.isEmpty()) {
					LOGGER.error("Error in validate token. Code {}, message {}",
							validationErrorsList.get(0).getErrorCode(), validationErrorsList.get(0).getMessage());
					return ImmutablePair.of(HttpStatus.UNAUTHORIZED, null);
				}
			}

			// validating audience | azp claims.
			boolean matchFound = validateAudience(decodedJWT);
			if (!matchFound) {
				LOGGER.error("Provided Client Id does not match with Aud/AZP. Throwing Authorizaion Exception");
				return ImmutablePair.of(HttpStatus.FORBIDDEN, null);
			}
			MosipUserDto mosipUserDto = buildMosipUser(decodedJWT, jwtToken);
			return ImmutablePair.of(HttpStatus.OK, mosipUserDto);
		} catch (WebClientResponseException e) {
			LOGGER.error("Token validation failed for accessToken {} status {}", jwtToken, e.getStatusCode());
		}
		LOGGER.error("user authentication failed for the provided token (WebClient).");
		return ImmutablePair.of(HttpStatus.UNAUTHORIZED, null);
	}

}
