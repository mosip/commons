package io.mosip.kernel.auth.defaultimpl.util;

import java.time.ZoneOffset;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.www.NonceExpiredException;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import io.mosip.kernel.auth.defaultimpl.config.MosipEnvironment;
import io.mosip.kernel.auth.defaultimpl.constant.AuthErrorCode;
import io.mosip.kernel.auth.defaultimpl.dto.MosipUser;
import io.mosip.kernel.auth.defaultimpl.dto.MosipUserToken;
import io.mosip.kernel.auth.defaultimpl.dto.RealmAccessDto;
import io.mosip.kernel.auth.defaultimpl.exception.AuthManagerException;
import io.mosip.kernel.auth.defaultimpl.service.TokenService;
import io.mosip.kernel.core.authmanager.model.MosipUserDto;
import io.mosip.kernel.core.authmanager.model.MosipUserTokenDto;
import io.mosip.kernel.core.util.DateUtils2;

/**
 * Token validator
 * 
 * @author Raj Kumar Jha
 * @author Ramadurai Pandian
 * @author Urvil Joshi
 *
 */
@Component
public class TokenValidator {

	/**
	 * Logger for expiry and admin-claim diagnostics.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(TokenValidator.class);

	/**
	 * JWT secret and token-base prefix for locally minted tokens.
	 */
	@Autowired
	MosipEnvironment mosipEnvironment;

	/**
	 * Token datastore (injected; used by callers of this validator's ecosystem).
	 */
	@Autowired
	TokenService customTokenServices;

	/**
	 * OTP tokens must have {@code isOtpVerified} when {@code isOtpRequired} is true.
	 *
	 * @param claims JWT claims
	 * @return {@code true} if OTP constraints are satisfied or not applicable
	 */
	private Boolean validateOtpDetails(Claims claims) {
		if (claims.get("isOtpRequired") == null) {
			return true;
		}

		Boolean isOtpVerified = (Boolean) claims.get("isOtpVerified");
		Boolean isOtpRequired = (Boolean) claims.get("isOtpRequired");
		if (isOtpRequired && !isOtpVerified) {
			return false;
		}
		return true;
	}

	/**
	 * Builds a {@link MosipUser} from subject, mobile, mail, and role claims.
	 *
	 * @param claims JWT claims
	 * @return user projection
	 */
	private MosipUser buildMosipUser(Claims claims) {
		return new MosipUser(claims.getSubject(), (String) claims.get("mobile"), (String) claims.get("mail"),
				(String) claims.get("role"));
	}

	/**
	 * Decodes a Keycloak access token (no signature check) into a {@link MosipUserDto}
	 * using {@code realm_access.roles} and standard claims.
	 *
	 * @param token compact JWT
	 * @return user DTO including semicolon-separated roles
	 */
	public MosipUserDto getAdminClaims(String token) {
		DecodedJWT decodedJWT = decodeJwt(token);
		Claim realmAccess = decodedJWT.getClaim("realm_access");
		RealmAccessDto access = realmAccess.as(RealmAccessDto.class);
		String[] roles = access.getRoles();
		StringBuilder builder = new StringBuilder();

		LOGGER.debug("invoked getAdminClaims");
		for (String r : roles) {
			builder.append(r);
			builder.append(";");
			LOGGER.debug("Roles " + r);
		}
		MosipUserDto dto = new MosipUserDto();
		dto.setUserId(decodedJWT.getClaim("preferred_username").asString());
		dto.setMail(decodedJWT.getClaim("email").asString());
		dto.setMobile(decodedJWT.getClaim("contactno").asString());
		dto.setName(decodedJWT.getClaim("preferred_username").asString());
		dto.setRId(decodedJWT.getClaim("rid").asString());
		dto.setRole(builder.toString());
		return dto;
	}

	/**
	 * Returns true if token if expired else false
	 * 
	 * @param token the token
	 * @return true if token if expired else false
	 */
	public boolean isExpired(String token) {
		DecodedJWT decodedJWT = decodeJwt(token);
		long expiryEpochTime = decodedJWT.getClaim("exp").asLong();
		long currentEpoch = DateUtils2.getUTCCurrentDateTime().toEpochSecond(ZoneOffset.UTC);
		LOGGER.debug("invoked isExpired token " + expiryEpochTime + " currentEpoch " + currentEpoch);
		return currentEpoch > expiryEpochTime;
	}

	/**
	 * Keycloak realm name parsed from the JWT {@code iss} claim path.
	 *
	 * @param token compact JWT
	 * @return last path segment of the issuer URL
	 */
	public String getKeycloakRealm(String token) {
		String issuer = getissuer(token);
		return issuer.substring(issuer.lastIndexOf("/") + 1);
	}

	/**
	 * Issuer URL from the JWT {@code iss} claim.
	 *
	 * @param token compact JWT
	 * @return issuer string
	 */
	public String getissuer(String token) {
		DecodedJWT decodedJWT = decodeJwt(token);
		return decodedJWT.getClaim("iss").asString();
	}

	/**
	 * Decodes a compact JWT ({@code header.payload.signature}). A one-part or
	 * two-part string is not a JWT; Auth0 raises
	 * {@link com.auth0.jwt.exceptions.JWTDecodeException}, which
	 * {@link io.mosip.kernel.auth.defaultimpl.exception.AuthManagerExceptionHandler}
	 * maps to HTTP 401.
	 *
	 * @param token compact JWT
	 * @return decoded token
	 */
	private DecodedJWT decodeJwt(String token) {
		return JWT.decode(token);
	}

	/**
	 * Parses a locally minted token (token-base prefix + HS512 signature).
	 *
	 * @param token compact token including token-base prefix
	 * @return JWT claims
	 * @throws Exception {@link NonceExpiredException} or {@link AuthManagerException}
	 *                   on invalid prefix, signature, or expiry
	 */
	private Claims getClaims(String token) throws Exception {
		String token_base = mosipEnvironment.getTokenBase();
		String secret = mosipEnvironment.getJwtSecret();
		Claims claims = null;

		if (token == null || !token.startsWith(token_base)) {
			throw new NonceExpiredException("Invalid Token");
		}
		try {
			claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token.substring(token_base.length())).getBody();

		} catch (SignatureException e) {
			throw new AuthManagerException(AuthErrorCode.UNAUTHORIZED.getErrorCode(), e.getMessage());
		} catch (JwtException e) {
			if (e instanceof ExpiredJwtException) {
				LOGGER.error("invoked validate token expired message " + e.getMessage() + " Token " + token);
				throw new AuthManagerException(AuthErrorCode.TOKEN_EXPIRED.getErrorCode(),
						AuthErrorCode.TOKEN_EXPIRED.getErrorMessage());
			} else {
				throw new AuthManagerException(AuthErrorCode.UNAUTHORIZED.getErrorCode(), e.getMessage());
			}

		}
		return claims;
	}

	/**
	 * Validates a token that must have {@code isOtpRequired} set (OTP pending/complete).
	 *
	 * @param token compact local token
	 * @return user and token wrapper
	 * @throws Exception if claims cannot be parsed or OTP is not required
	 */
	public MosipUserToken validateForOtpVerification(String token) throws Exception {
		Claims claims = getClaims(token);
		Boolean isOtpRequired = (Boolean) claims.get("isOtpRequired");
		if (isOtpRequired) {
			MosipUser mosipUser = buildMosipUser(claims);
			LOGGER.info("Otp required " + mosipUser.getUserName());
			return new MosipUserToken(mosipUser, token);
		} else {
			throw new AuthManagerException(AuthErrorCode.INVALID_TOKEN.getErrorCode(),
					AuthErrorCode.INVALID_TOKEN.getErrorMessage());
		}
	}

	/**
	 * Validates a local token including OTP-required/verified flags.
	 *
	 * @param token compact local token
	 * @return user and token wrapper
	 * @throws Exception if parse or OTP flags fail
	 */
	public MosipUserToken basicValidate(String token) throws Exception {
		Claims claims = getClaims(token);
		Boolean isOtpValid = validateOtpDetails(claims);
		if (isOtpValid) {
			MosipUser mosipUser = buildMosipUser(claims);
			return new MosipUserToken(mosipUser, token);
		} else {
			throw new AuthManagerException(AuthErrorCode.INVALID_TOKEN.getErrorCode(),
					AuthErrorCode.INVALID_TOKEN.getErrorMessage());
		}
	}

	/**
	 * Parses a local token into {@link MosipUserTokenDto} without OTP-flag checks.
	 *
	 * @param token compact local token
	 * @return user DTO plus original token
	 * @throws Exception if parse fails
	 */
	public MosipUserTokenDto validateToken(String token) throws Exception {
		Claims claims = getClaims(token);
		MosipUserDto mosipUserDto = buildDto(claims);
		return new MosipUserTokenDto(mosipUserDto, token, null, 0, null, null, 0);
	}

	/**
	 * Maps local-token claims onto {@link MosipUserDto}.
	 *
	 * @param claims JWT claims
	 * @return user DTO
	 */
	private MosipUserDto buildDto(Claims claims) {
		MosipUserDto mosipUserDto = new MosipUserDto();
		mosipUserDto.setUserId(claims.getSubject());
		mosipUserDto.setName((String) claims.get("name"));
		mosipUserDto.setRole((String) claims.get("role"));
		mosipUserDto.setMail((String) claims.get("mail"));
		mosipUserDto.setMobile((String) claims.get("mobile"));
		mosipUserDto.setRId((String) claims.get("rId"));
		return mosipUserDto;
	}

	/**
	 * Validates a token that represents an OTP session ({@code isOtpRequired}).
	 *
	 * @param otp compact local token carrying OTP flags
	 * @return user DTO plus token
	 * @throws Exception if parse fails or OTP is not required
	 */
	public MosipUserTokenDto validateOTP(String otp) throws Exception {
		Claims claims = getClaims(otp);
		Boolean isOtpRequired = (Boolean) claims.get("isOtpRequired");
		if (isOtpRequired) {
			MosipUserDto mosipUserDto = buildDto(claims);
			return new MosipUserTokenDto(mosipUserDto, otp, null, 0, null, null, 0);
		} else {
			throw new AuthManagerException(AuthErrorCode.UNAUTHORIZED.getErrorCode(),
					AuthErrorCode.UNAUTHORIZED.getErrorMessage());
		}
	}

	/**
	 * Whether the local token's {@code exp} claim is still in the future.
	 *
	 * @param token compact local token
	 * @return {@code true} if not expired
	 * @throws Exception if parse fails
	 */
	public boolean validateExpiry(String token) throws Exception {
		Claims claims = getClaims(token);
		if (claims != null) {
			Integer expTime = (Integer) claims.get("exp");
			long currentTime = new Date().getTime();
			long exp = expTime.longValue() * 1000;
			if (expTime != 0 && currentTime < exp) {
				LOGGER.debug("Token Valid " + claims.getId());
				return true;
			}
			LOGGER.debug("Token Invalid " + claims.getId());
		}

		return false;
	}

}
