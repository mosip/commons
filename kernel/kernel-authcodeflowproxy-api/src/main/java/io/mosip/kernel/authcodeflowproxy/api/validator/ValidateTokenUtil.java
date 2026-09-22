package io.mosip.kernel.authcodeflowproxy.api.validator;

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

import jakarta.annotation.PostConstruct;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.impl.NullClaim;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import io.mosip.kernel.core.util.DateUtils2;
import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.kernel.openid.bridge.api.constants.AuthConstant;
import io.mosip.kernel.openid.bridge.api.constants.AuthErrorCode;
import io.mosip.kernel.openid.bridge.api.constants.Errors;
import io.mosip.kernel.openid.bridge.api.exception.ServiceException;

/**
 * Offline JWT validator for the authorization-code flow proxy.
 * <p>
 * Verifies Keycloak (or compatible IdP) access and ID tokens without calling the auth
 * manager: expiry via {@link DateUtils2}, optional issuer-host match against the JWKS
 * URL, RSA signature against keys downloaded from {@code mosip.iam.certs_endpoint}
 * ({@link UrlJwkProvider}), and optional {@code aud}/{@code azp} membership in the
 * configured audience list.
 * 
 * @author Loganathan S
 *
 */
@Component
public class ValidateTokenUtil {

	/**
	 * Logger for expiry, issuer, signature, and audience validation failures.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ValidateTokenUtil.class);

	/**
	 * JWKS public keys cached by JWT {@code kid}.
	 */
	private Map<String, PublicKey> publicKeys = new HashMap<>();

	/**
	 * Keycloak (or IdP) JWKS / certs URL used for signature verification and issuer-host
	 * comparison. Bound from {@code mosip.iam.certs_endpoint}.
	 */
	@Value("${mosip.iam.certs_endpoint:}")
	private String certsPathUrl;

	/**
	 * When {@code true}, the JWT {@code iss} host must match the host of
	 * {@link #certsPathUrl}. Bound from {@code auth.server.admin.issuer.domain.validate};
	 * defaults to {@code true}.
	 */
	@Value("${auth.server.admin.issuer.domain.validate:true}")
	private boolean validateIssuerDomain;

	/**
	 * When {@code true}, {@code aud} or {@code azp} must be in {@link #allowedAudience}.
	 * Bound from {@code auth.server.admin.audience.claim.validate}; defaults to {@code true}.
	 */
	@Value("${auth.server.admin.audience.claim.validate:true}")
	private boolean validateAudClaim;

	/**
	 * Allowed OAuth 2.0 audiences / authorized parties, loaded in {@link #init()} from
	 * {@code auth.server.admin.allowed.audience.&lt;app&gt;} or
	 * {@code auth.server.admin.allowed.audience}.
	 */
	// @Value("${auth.server.admin.allowed.audience:}")
	private List<String> allowedAudience;

	/**
	 * Spring environment used to resolve application name and allowed-audience lists.
	 */
	@Autowired
	private Environment environment;

	/**
	 * Loads {@link #allowedAudience} after property binding: prefers
	 * {@code auth.server.admin.allowed.audience.&lt;spring.application.name&gt;}, then
	 * {@code auth.server.admin.allowed.audience}, else an empty list.
	 */
	@PostConstruct
	@SuppressWarnings("unchecked")
	private void init() {
		String applName = getApplicationName();
		this.allowedAudience = (List<String>) environment.getProperty("auth.server.admin.allowed.audience." + applName,
				List.class,
				environment.getProperty("auth.server.admin.allowed.audience", List.class, Collections.EMPTY_LIST));
	}

	/**
	 * Returns the first comma-separated value of {@code spring.application.name}.
	 *
	 * @return application name used as the audience property suffix
	 * @throws RuntimeException if {@code spring.application.name} is missing or empty
	 */
	private String getApplicationName() {
		String appNames = environment.getProperty("spring.application.name");
		if (appNames != null && !appNames.isEmpty()) {
			List<String> appNamesList = Stream.of(appNames.split(",")).collect(Collectors.toList());
			return appNamesList.get(0);
		} else {
			throw new RuntimeException("property spring.application.name not found");
		}
	}
	
	/**
	 * Validates {@code accessToken} and throws if it is not valid.
	 *
	 * @param accessToken compact JWT access or ID token
	 * @throws ServiceException with {@link Errors#INVALID_TOKEN} when validation fails
	 */
	public void validateToken(String accessToken) {
		if(!isTokenValid(accessToken).getKey()){
			throw new ServiceException(Errors.INVALID_TOKEN.getErrorCode(), Errors.INVALID_TOKEN.getErrorMessage());
		}
	}
	
	/**
	 * Decodes {@code jwtToken} and runs the full offline validation pipeline.
	 *
	 * @param jwtToken compact JWT
	 * @return {@code (true, null)} if valid; otherwise {@code false} with
	 *         {@link AuthErrorCode#INVALID_TOKEN}, {@link AuthErrorCode#UNAUTHORIZED},
	 *         or {@link AuthErrorCode#FORBIDDEN}
	 */
	public ImmutablePair<Boolean, AuthErrorCode> isTokenValid(String jwtToken) {
		try {
			return isTokenValid(JWT.decode(jwtToken));
		} catch (JWTDecodeException e) {
			LOGGER.error("Malformed JWT: expected header.payload.signature");
			return ImmutablePair.of(Boolean.FALSE, AuthErrorCode.INVALID_TOKEN);
		}
	}

	/**
	 * Validates a decoded JWT: expiry ({@link DateUtils2}), optional issuer host, JWKS
	 * signature, and optional audience / AZP.
	 *
	 * @param decodedJWT decoded access or ID token
	 * @return {@code (true, null)} if valid; otherwise {@code false} with an
	 *         {@link AuthErrorCode}
	 */
	public ImmutablePair<Boolean, AuthErrorCode> isTokenValid(DecodedJWT decodedJWT) {
		PublicKey publicKey = getPublicKey(decodedJWT);
		// First, token expire
		LocalDateTime expiryTime = DateUtils2
				.convertUTCToLocalDateTime(DateUtils2.getUTCTimeFromDate(decodedJWT.getExpiresAt()));
		String userName = decodedJWT.getClaim(AuthConstant.PREFERRED_USERNAME).asString();
		if (!DateUtils2.before(DateUtils2.getUTCCurrentDateTime(), expiryTime)) {
			LOGGER.error("Provided Auth Token expired. Throwing Authentication Exception. UserName: " + userName);
			return ImmutablePair.of(Boolean.FALSE, AuthErrorCode.UNAUTHORIZED);
		}

		// Second, issuer domain check.
		if (validateIssuerDomain && !getTokenIssuerDomain(decodedJWT)) {
			LOGGER.error(
					"Provided Auth Token Issue domain does not match. Throwing Authentication Exception. UserName: "
							+ userName);
			return ImmutablePair.of(Boolean.FALSE, AuthErrorCode.UNAUTHORIZED);
		}

		// Third, signature validation.
		ImmutablePair<Boolean, AuthErrorCode> signatureVerificationResult = verifyJWTSignagure(decodedJWT);
		// If signature validation fails return the error code
		if(!signatureVerificationResult.getLeft()) {
			return signatureVerificationResult;
		}
		
		// Fourth, audience | azp validation.
		// No match found after comparing audience & azp
		if (validateAudClaim && !validateAudience(decodedJWT)) {
			LOGGER.error("Provided Client Id does not match with Aud/AZP. Throwing Authorizaion Exception. UserName: "
					+ userName);
			return ImmutablePair.of(Boolean.FALSE, AuthErrorCode.FORBIDDEN);
		}
		return ImmutablePair.of(Boolean.TRUE, null);
	}

	/**
	 * Returns whether {@code aud} contains any {@link #allowedAudience} entry, or else
	 * whether {@code azp} equals an allowed audience (case-insensitive).
	 *
	 * @param decodedJWT token whose audience claims are checked
	 * @return {@code true} if audience or AZP is allowed
	 */
	private boolean validateAudience(DecodedJWT decodedJWT) {
		boolean matchFound;

		List<String> tokenAudience =  decodedJWT.getAudience();
		matchFound = tokenAudience != null && tokenAudience.stream().anyMatch(allowedAudience::contains);

		// comparing with azp.
		if (!matchFound) {
			Claim azp = decodedJWT.getClaim(AuthConstant.AZP);
			matchFound = azp != null && !(azp instanceof NullClaim) && allowedAudience.stream().anyMatch(azp.asString()::equalsIgnoreCase);
		}
		
		return matchFound;
	}

	/**
	 * This method validates if the issuer domain in the JWT matches the issuerURI
	 * configured in the properties.
	 * <p>
	 * Compares the host of the {@code iss} claim with the host of {@link #certsPathUrl}.
	 * 
	 * @param decodedJWT token whose {@code iss} claim is parsed
	 * @return {@code true} when both hosts match (case-insensitive); {@code false} on
	 *         mismatch or {@link URISyntaxException}
	 */
	private boolean getTokenIssuerDomain(DecodedJWT decodedJWT) {
		String domain = decodedJWT.getClaim(AuthConstant.ISSUER).asString();
		try {
			String tokenHost = new URI(domain).getHost();
			return tokenHost.equalsIgnoreCase(new URI(certsPathUrl).getHost());
		} catch (URISyntaxException synExp) {
			LOGGER.error("Unable to parse domain from issuer.", synExp);
		}
		return false;
	}

	/**
	 * Returns the RSA public key for {@code decodedJWT}'s {@code kid}, downloading it
	 * from JWKS on a cache miss.
	 *
	 * @param decodedJWT token whose {@code kid} header selects the JWK
	 * @return cached or freshly downloaded {@link PublicKey}, or {@code null} if download
	 *         fails
	 */
	public PublicKey getPublicKey(DecodedJWT decodedJWT) {
		String userName = decodedJWT.getClaim(AuthConstant.PREFERRED_USERNAME).asString();
		LOGGER.info("offline verification for environment profile. UserName: " + userName);

		String keyId = decodedJWT.getKeyId();
		PublicKey publicKey = publicKeys.get(keyId);

		if (Objects.isNull(publicKey)) {
			publicKey = getIssuerPublicKey(keyId);
			publicKeys.put(keyId, publicKey);
		}
		return publicKey;
	}
	
	/**
	 * Verifies the JWT signature with the JWKS public key for the token {@code kid}.
	 * <p>
	 * Selects RS256, RS384, or RS512 from the token {@code alg} (unknown algorithms use
	 * RS256) and calls Auth0 {@link Algorithm#verify(DecodedJWT)}.
	 *
	 * @param decodedJWT decoded JWT whose signature is checked
	 * @return {@code (true, null)} if the signature is valid; {@code (false,}
	 *         {@link AuthErrorCode#UNAUTHORIZED}{@code )} on {@link SignatureVerificationException}
	 */
	public ImmutablePair<Boolean, AuthErrorCode> verifyJWTSignagure(DecodedJWT decodedJWT) {
		try {
			String tokenAlgo = decodedJWT.getAlgorithm();
			PublicKey publicKey = getPublicKey(decodedJWT);
			Algorithm algorithm = getVerificationAlgorithm(tokenAlgo, publicKey);
			algorithm.verify(decodedJWT);
		} catch (SignatureVerificationException signatureException) {
			LOGGER.error("Signature validation failed for User Info, Throwing Authentication Exception.",
					signatureException);
			return ImmutablePair.of(Boolean.FALSE, AuthErrorCode.UNAUTHORIZED);
		}

		return ImmutablePair.of(Boolean.TRUE, null);

	}

	/**
	 * Downloads the JWK for {@code keyId} from {@link #certsPathUrl} via
	 * {@link UrlJwkProvider}.
	 *
	 * @param keyId JWT {@code kid} header
	 * @return public key from the JWK, or {@code null} on JWKS / URL errors
	 */
	private PublicKey getIssuerPublicKey(String keyId) {
		try {

			URI uri = new URI(certsPathUrl).normalize();
			JwkProvider provider = new UrlJwkProvider(uri.toURL());
			Jwk jwk = provider.get(keyId);
			return jwk.getPublicKey();
		} catch (JwkException | URISyntaxException | MalformedURLException e) {
			LOGGER.error("Error downloading Public key from server".concat(e.getMessage()));
		}
		return null;
	}

	/**
	 * Maps the JWT {@code alg} to an Auth0 RSA verifier. Unknown algorithms use RS256.
	 *
	 * @param tokenAlgo JWT {@code alg} ({@code RS256}, {@code RS384}, or {@code RS512})
	 * @param publicKey RSA public key from JWKS
	 * @return Auth0 {@link Algorithm} used to verify the signature
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

}
