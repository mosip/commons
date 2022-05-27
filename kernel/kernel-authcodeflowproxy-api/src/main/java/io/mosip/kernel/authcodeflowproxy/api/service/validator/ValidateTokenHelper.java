package io.mosip.kernel.authcodeflowproxy.api.service.validator;

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

import javax.annotation.PostConstruct;

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
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import io.mosip.kernel.authcodeflowproxy.api.constants.AuthConstant;
import io.mosip.kernel.authcodeflowproxy.api.constants.AuthErrorCode;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.EmptyCheckUtils;

@Component
public class ValidateTokenHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(ValidateTokenHelper.class);

	private Map<String, PublicKey> publicKeys = new HashMap<>();

	@Value("${mosip.iam.certs_endpoint}")
	private String certsPathUrl;

	@Value("${auth.server.admin.issuer.domain.validate:true}")
	private boolean validateIssuerDomain;

	@Value("${auth.server.admin.audience.claim.validate:true}")
	private boolean validateAudClaim;

	// @Value("${auth.server.admin.allowed.audience:}")
	private List<String> allowedAudience;

	@Autowired
	private Environment environment;

	@PostConstruct
	@SuppressWarnings("unchecked")
	private void init() {
		String applName = getApplicationName();
		this.allowedAudience = (List<String>) environment.getProperty("auth.server.admin.allowed.audience." + applName,
				List.class,
				environment.getProperty("auth.server.admin.allowed.audience", List.class, Collections.EMPTY_LIST));
	}

	private String getApplicationName() {
		String appNames = environment.getProperty("spring.application.name");
		if (!EmptyCheckUtils.isNullEmpty(appNames)) {
			List<String> appNamesList = Stream.of(appNames.split(",")).collect(Collectors.toList());
			return appNamesList.get(0);
		} else {
			throw new RuntimeException("property spring.application.name not found");
		}
	}
	
	public ImmutablePair<Boolean, AuthErrorCode> isTokenValid(String jwtToken) {
		return isTokenValid(JWT.decode(jwtToken));
	}

	public ImmutablePair<Boolean, AuthErrorCode> isTokenValid(DecodedJWT decodedJWT) {
		PublicKey publicKey = getPublicKey(decodedJWT);
		// First, token expire
		LocalDateTime expiryTime = DateUtils
				.convertUTCToLocalDateTime(DateUtils.getUTCTimeFromDate(decodedJWT.getExpiresAt()));
		String userName = decodedJWT.getClaim(AuthConstant.PREFERRED_USERNAME).asString();
		if (!DateUtils.before(DateUtils.getUTCCurrentDateTime(), expiryTime)) {
			LOGGER.error("Provided Auth Token expired. Throwing Authentication Exception. UserName: " + userName);
			return ImmutablePair.of(Boolean.FALSE, AuthErrorCode.UNAUTHORIZED);
		}

		// Second, issuer domain check.
		boolean tokenDomainMatch = getTokenIssuerDomain(decodedJWT);
		if (validateIssuerDomain && !tokenDomainMatch) {
			LOGGER.error(
					"Provided Auth Token Issue domain does not match. Throwing Authentication Exception. UserName: "
							+ userName);
			return ImmutablePair.of(Boolean.FALSE, AuthErrorCode.UNAUTHORIZED);
		}

		// Third, signature validation.
		try {
			String tokenAlgo = decodedJWT.getAlgorithm();
			Algorithm algorithm = getVerificationAlgorithm(tokenAlgo, publicKey);
			algorithm.verify(decodedJWT);
		} catch (SignatureVerificationException signatureException) {
			LOGGER.error("Signature validation failed, Throwing Authentication Exception. UserName: " + userName,
					signatureException);
			return ImmutablePair.of(Boolean.FALSE, AuthErrorCode.UNAUTHORIZED);
		}

		// Fourth, audience | azp validation.
		boolean matchFound = validateAudience(decodedJWT);
		// No match found after comparing audience & azp
		if (!matchFound) {
			LOGGER.error("Provided Client Id does not match with Aud/AZP. Throwing Authorizaion Exception. UserName: "
					+ userName);
			return ImmutablePair.of(Boolean.FALSE, AuthErrorCode.FORBIDDEN);
		}
		return ImmutablePair.of(Boolean.TRUE, null);
	}

	private boolean validateAudience(DecodedJWT decodedJWT) {
		boolean matchFound = false;
		if (validateAudClaim) {

			List<String> tokenAudience = decodedJWT.getAudience();
			matchFound = tokenAudience.stream().anyMatch(allowedAudience::contains);

			// comparing with azp.
			String azp = decodedJWT.getClaim(AuthConstant.AZP).asString();
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
	 * @param decodedJWT
	 * @return
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
