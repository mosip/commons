package io.mosip.kernel.auth.defaultimpl.util;

import java.time.Instant;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.mosip.kernel.auth.defaultimpl.config.MosipEnvironment;
import io.mosip.kernel.auth.defaultimpl.constant.AuthErrorCode;
import io.mosip.kernel.auth.defaultimpl.dto.BasicTokenDto;
import io.mosip.kernel.auth.defaultimpl.dto.TimeToken;
import io.mosip.kernel.auth.defaultimpl.exception.AuthManagerException;
import io.mosip.kernel.core.authmanager.model.MosipUserDto;

/**
 * Builds HS512 JWTs (access and refresh) from {@link MosipUserDto} claims using
 * {@link MosipEnvironment} secret, token base, and expiry. Used when authmanager
 * mints local tokens rather than returning Keycloak tokens.
 */
@Component
public class TokenGenerator {

	/**
	 * JWT secret, base prefix, and expiry settings.
	 */
	@Autowired
	MosipEnvironment mosipEnvironment;

	/**
	 * Subject, mobile, mail, role, name, and RID claims from the user.
	 *
	 * @param mosipUser user to encode
	 * @return JWT claims
	 */
	private Claims getBasicClaims(MosipUserDto mosipUser) {
		Claims claims = Jwts.claims().setSubject(mosipUser.getUserId());
		claims.put("mobile", mosipUser.getMobile());
		claims.put("mail", mosipUser.getMail());
		claims.put("role", mosipUser.getRole());
		claims.put("name", mosipUser.getName());
		claims.put("rId", mosipUser.getRId());
		return claims;
	}

	/**
	 * Signs claims as an access token with configured expiry and token-base prefix.
	 *
	 * @param claims JWT claims
	 * @return compact JWT prefixed with token base
	 */
	private String buildToken(Claims claims) {
		String secret = mosipEnvironment.getJwtSecret();
		String token_base = mosipEnvironment.getTokenBase();
		int token_expiry = mosipEnvironment.getTokenExpiry();

		long currentTimeInMs = System.currentTimeMillis();
		Date currentDate = new Date(currentTimeInMs);

		JwtBuilder builder = Jwts.builder().setClaims(claims).setIssuedAt(currentDate)
				.signWith(SignatureAlgorithm.HS512, secret);

		if (token_expiry >= 0) {
			long expTimeInMs = currentTimeInMs + token_expiry;
			builder.setExpiration(new Date(expTimeInMs));
		}

		return token_base.concat(builder.compact());
	}

	/**
	 * Access token for OTP login, with {@code isOtpRequired} and {@code isOtpVerified}.
	 *
	 * @param mosipUser         user claims
	 * @param isOtpVerifiedYet  whether OTP has already been verified
	 * @return compact access token
	 */
	public String generateForOtp(MosipUserDto mosipUser, Boolean isOtpVerifiedYet) {
		Claims claims = getBasicClaims(mosipUser);
		claims.put("isOtpRequired", true);
		claims.put("isOtpVerified", isOtpVerifiedYet);
		return buildToken(claims);
	}

	/**
	 * Refresh token for an OTP-verified session.
	 *
	 * @param mosipUser user claims
	 * @return compact refresh token
	 */
	public String refreshTokenForOTP(MosipUserDto mosipUser) {
		Claims claims = getBasicClaims(mosipUser);
		claims.put("isOtpRequired", true);
		claims.put("isOtpVerified", true);
		return buildRefreshTokenOTP(claims);
	}

	/**
	 * Signs OTP refresh claims with {@link MosipEnvironment#getRefreshTokenExpiry()}.
	 *
	 * @param claims JWT claims including OTP flags
	 * @return compact refresh token
	 */
	private String buildRefreshTokenOTP(Claims claims) {
		String secret = mosipEnvironment.getJwtSecret();
		String token_base = mosipEnvironment.getTokenBase();
		long token_expiry = mosipEnvironment.getRefreshTokenExpiry();

		long currentTimeInMs = System.currentTimeMillis();
		Date currentDate = new Date(currentTimeInMs);

		JwtBuilder builder = Jwts.builder().setClaims(claims).setIssuedAt(currentDate)
				.signWith(SignatureAlgorithm.HS512, secret);

		if (token_expiry >= 0) {
			long expTimeInMs = currentTimeInMs + token_expiry;
			builder.setExpiration(new Date(expTimeInMs));
		}

		return token_base.concat(builder.compact());
	}

	/**
	 * Access token (no OTP flags) with language claim and expiry.
	 *
	 * @param mosipUser user claims
	 * @return token and expiry DTO
	 */
	public BasicTokenDto basicGenerate(MosipUserDto mosipUser) {
		BasicTokenDto basicTokenDto = new BasicTokenDto();
		Claims claims = Jwts.claims().setSubject(mosipUser.getUserId());
		claims.put("mobile", mosipUser.getMobile());
		claims.put("mail", mosipUser.getMail());
		claims.put("role", mosipUser.getRole());
		claims.put("lang", mosipUser.getLangCode());
		claims.put("name", mosipUser.getName());
		claims.put("rId", mosipUser.getRId());
		TimeToken token = getToken(claims);
		// String refreshToken = buildRefreshToken(claims);
		basicTokenDto.setAuthToken(token.getToken());
		// basicTokenDto.setRefreshToken(refreshToken);
		basicTokenDto.setExpiryTime(token.getExpTime());
		return basicTokenDto;
	}

	/**
	 * Access token with OTP required/verified flags.
	 *
	 * @param mosipUser   user claims
	 * @param otpVerified OTP verification flag to embed
	 * @return token and expiry DTO
	 */
	public BasicTokenDto basicGenerateOTPToken(MosipUserDto mosipUser, boolean otpVerified) {
		BasicTokenDto basicTokenDto = new BasicTokenDto();
		Claims claims = Jwts.claims().setSubject(mosipUser.getUserId());
		claims.put("mobile", mosipUser.getMobile());
		claims.put("mail", mosipUser.getMail());
		claims.put("role", mosipUser.getRole());
		claims.put("lang", mosipUser.getLangCode());
		claims.put("name", mosipUser.getName());
		claims.put("rId", mosipUser.getRId());
		claims.put("isOtpRequired", true);
		claims.put("isOtpVerified", otpVerified);
		TimeToken token = getToken(claims);
		basicTokenDto.setAuthToken(token.getToken());
		basicTokenDto.setExpiryTime(token.getExpTime());
		return basicTokenDto;
	}

	/**
	 * Signs claims as an access token and returns compact token plus expiry millis.
	 *
	 * @param claims JWT claims
	 * @return token string and expiry
	 */
	private TimeToken getToken(Claims claims) {
		TimeToken timeToken = new TimeToken();
		long exptime = 0;
		String secret = mosipEnvironment.getJwtSecret();
		String token_base = mosipEnvironment.getTokenBase();
		int token_expiry = mosipEnvironment.getTokenExpiry();

		long currentTimeInMs = Instant.now().toEpochMilli();
		Date currentDate = new Date(currentTimeInMs);

		JwtBuilder builder = Jwts.builder().setClaims(claims).setIssuedAt(currentDate)
				.signWith(SignatureAlgorithm.HS512, secret);

		if (token_expiry >= 0) {
			exptime = currentTimeInMs + token_expiry;
			builder.setExpiration(new Date(exptime));
		}
		timeToken.setToken(token_base.concat(builder.compact()));
		timeToken.setExpTime(exptime);
		return timeToken;
	}

	/**
	 * Refresh token without OTP flags (includes language claim).
	 *
	 * @param mosipUser user claims
	 * @return compact refresh token
	 */
	public String refreshToken(MosipUserDto mosipUser) {
		Claims claims = Jwts.claims().setSubject(mosipUser.getUserId());
		claims.put("mobile", mosipUser.getMobile());
		claims.put("mail", mosipUser.getMail());
		claims.put("role", mosipUser.getRole());
		claims.put("lang", mosipUser.getLangCode());
		claims.put("name", mosipUser.getName());
		return buildRefreshToken(claims);
	}

	/**
	 * Signs claims with refresh-token expiry.
	 *
	 * @param claims JWT claims
	 * @return compact refresh token
	 */
	private String buildRefreshToken(Claims claims) {
		String secret = mosipEnvironment.getJwtSecret();
		String token_base = mosipEnvironment.getTokenBase();
		long token_expiry = mosipEnvironment.getRefreshTokenExpiry();

		long currentTimeInMs = System.currentTimeMillis();
		Date currentDate = new Date(currentTimeInMs);

		JwtBuilder builder = Jwts.builder().setClaims(claims).setIssuedAt(currentDate)
				.signWith(SignatureAlgorithm.HS512, secret);

		if (token_expiry >= 0) {
			long expTimeInMs = currentTimeInMs + token_expiry;
			builder.setExpiration(new Date(expTimeInMs));
		}

		return token_base.concat(builder.compact());
	}

	/**
	 * Re-issues an access token using claims parsed from an existing token.
	 *
	 * @param existingToken previously issued compact token (with token-base prefix)
	 * @return new token and expiry
	 */
	public TimeToken generateNewToken(String existingToken) {
		Claims claims = getClaims(existingToken);
		return getToken(claims);
	}

	/**
	 * Parses and verifies a compact token, stripping the configured token-base prefix.
	 *
	 * @param token compact token including token-base prefix
	 * @return JWT claims
	 * @throws AuthManagerException if the prefix is wrong or signature/parse fails
	 */
	private Claims getClaims(String token) {
		String token_base = mosipEnvironment.getTokenBase();
		String secret = mosipEnvironment.getJwtSecret();

		if (token == null || !token.startsWith(token_base)) {
			throw new AuthManagerException(AuthErrorCode.INVALID_TOKEN.getErrorCode(),
					AuthErrorCode.INVALID_TOKEN.getErrorMessage());
		}

		try {
			Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token.substring(token_base.length()))
					.getBody();

			return claims;
		} catch (Exception e) {
			throw new AuthManagerException(AuthErrorCode.UNAUTHORIZED.getErrorCode(), e.getMessage(), e);
		}
	}
}
