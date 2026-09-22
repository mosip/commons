package io.mosip.kernel.auth.defaultadapter.handler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.impl.TextCodec;
import io.mosip.kernel.auth.defaultadapter.constant.AuthAdapterErrorCode;
import io.mosip.kernel.auth.defaultadapter.exception.AuthManagerException;
import io.mosip.kernel.auth.defaultadapter.model.AuthToken;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.openid.bridge.model.AuthUserDetails;
import io.mosip.kernel.openid.bridge.model.MosipUserDto;

/**
 * Optional authentication provider that validates a locally signed JWT with
 * {@code prereg.auth.jwt.secret} (pre-registration flow).
 * <p>
 * Register this bean name via
 * {@code mosip.security.authentication.provider.beans.list.<app>} so
 * {@link io.mosip.kernel.auth.defaultadapter.config.SecurityConfig} includes
 * it in the {@code ProviderManager}.
 * <p>
 * This adapter is a library other MOSIP services put on the classpath.
 */
@Component("customJWTAuthProvider")
public class CustomJWTAuthHandler extends AbstractUserDetailsAuthenticationProvider {

	/**
	 * Logger for token parse failures.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomJWTAuthHandler.class);

	/**
	 * Base64-encoded HMAC secret used to verify the pre-registration JWT.
	 */
	@Value("${prereg.auth.jwt.secret:}")
	private String jwtSecret;

	/**
	 * No extra checks; token validity is established in {@link #retrieveUser}.
	 *
	 * @param userDetails                         unused
	 * @param usernamePasswordAuthenticationToken unused
	 * @throws AuthenticationException never thrown by this implementation
	 */
	@Override
	protected void additionalAuthenticationChecks(UserDetails userDetails,
			UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {
	}

	/**
	 * Parses the {@link AuthToken} JWT with {@link #jwtSecret} and maps
	 * {@code userId}, {@code user_name}, and {@code roles} claims to
	 * {@link AuthUserDetails}.
	 *
	 * @param userName                            unused (user comes from claims)
	 * @param usernamePasswordAuthenticationToken must be an {@link AuthToken}
	 * @return authenticated user details
	 * @throws AuthenticationException if the signature or JWT is invalid
	 */
	@Override
	protected UserDetails retrieveUser(String userName,
			UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {
		LOGGER.info("In retriveUser method of AuthenticationProvider class");
		MosipUserDto mosipUserDto = new MosipUserDto();
		byte[] secret = TextCodec.BASE64.decode(jwtSecret);
		String token = ((AuthToken)usernamePasswordAuthenticationToken).getToken();
		try {
			Jws<Claims> clamis = Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
			mosipUserDto.setUserId(clamis.getBody().get("userId").toString());
			mosipUserDto.setName(clamis.getBody().get("user_name").toString());
			mosipUserDto.setToken(token.toString());
			mosipUserDto.setRole(clamis.getBody().get("roles").toString());
		} catch (SignatureException | IllegalArgumentException ex) {
			LOGGER.error("validate token exception {}", ExceptionUtils.getStackTrace(ex));
			throw new AuthManagerException(AuthAdapterErrorCode.INVALID_TOKEN.getErrorCode(),
                            AuthAdapterErrorCode.INVALID_TOKEN.getErrorMessage());
		} catch (JwtException e) {
			LOGGER.error("exception while parsing the token {}", ExceptionUtils.getStackTrace(e));
			throw new AuthManagerException(AuthAdapterErrorCode.UNAUTHORIZED.getErrorCode(), 
                            AuthAdapterErrorCode.UNAUTHORIZED.getErrorMessage());
		} catch(Throwable t) {
			LOGGER.error("exception while parsing the token(throwable) {}", ExceptionUtils.getStackTrace(t));
			throw new AuthManagerException(AuthAdapterErrorCode.UNAUTHORIZED.getErrorCode(), 
                            AuthAdapterErrorCode.UNAUTHORIZED.getErrorMessage());
		}

		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, token.toString());
		List<GrantedAuthority> grantedAuthorities = AuthorityUtils.commaSeparatedStringToAuthorityList(mosipUserDto.getRole());
		authUserDetails.addRoleAuthorities(grantedAuthorities);
		return authUserDetails;
	}
}
