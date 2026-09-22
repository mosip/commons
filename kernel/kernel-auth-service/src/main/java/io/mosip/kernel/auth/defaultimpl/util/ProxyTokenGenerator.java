package io.mosip.kernel.auth.defaultimpl.util;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import io.mosip.kernel.auth.defaultimpl.constant.AuthConstant;

/**
 * Issues unsigned local-profile JWTs for proxy/sandbox authmanager. Algorithm
 * none is used only for local profiles ({@code auth.local.*} properties).
 */
@Component
public class ProxyTokenGenerator {
	
	/**
	 * Default expiry offset in millis when callers pass a relative exp
	 * ({@code auth.local.exp}).
	 */
	@Value("${auth.local.exp:1000000}")
	long localExp;
	
	/**
	 * Mobile claim value for proxy tokens ({@code auth.local.mobileno}).
	 */
	@Value("${auth.local.mobileno}")
	String mobileNO;
	
	/**
	 * Email domain appended to the subject ({@code auth.local.email.domain}).
	 */
	@Value("${auth.local.email.domain}")
	String emailDomain;
	
	/**
	 * Roles claim value for proxy tokens ({@code auth.local.userRoles}).
	 */
	@Value("${auth.local.userRoles}")
	String localUserRoles;
	
	/**
	 * Builds an unsigned JWT with subject, mobile, email, roles, and expiry.
	 *
	 * @param subject JWT subject (user id)
	 * @param exp     expiry instant as epoch milliseconds
	 * @return compact JWT signed with {@link Algorithm#none()}
	 */
	@SuppressWarnings("java:S5659") // added suppress for sonarcloud. Algorithm none is used for local profiles only.
	public String getProxyToken(String subject,long exp) {
		return JWT.create().withSubject(subject).withClaim(AuthConstant.MOBILE, mobileNO)
				.withClaim(AuthConstant.EMAIL, subject.concat(emailDomain))
				.withClaim(AuthConstant.ROLES, localUserRoles).withExpiresAt(new Date(exp))
				.sign(Algorithm.none());
	}
}
