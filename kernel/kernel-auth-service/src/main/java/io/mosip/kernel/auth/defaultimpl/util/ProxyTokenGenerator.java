package io.mosip.kernel.auth.defaultimpl.util;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import io.mosip.kernel.auth.defaultimpl.constant.AuthConstant;

@Component
public class ProxyTokenGenerator {
	
	@Value("${auth.local.exp:1000000}")
	long localExp;
	
	@Value("${auth.local.mobileno}")
	String mobileNO;
	
	@Value("${auth.local.email.domain}")
	String emailDomain;
	
	@Value("${auth.local.userRoles}")
	String localUserRoles;
	
	public String getProxyToken(String subject,long exp) {
		return JWT.create().withSubject(subject).withClaim(AuthConstant.MOBILE, mobileNO)
				.withClaim(AuthConstant.EMAIL, subject.concat(emailDomain))
				.withClaim(AuthConstant.ROLES, localUserRoles).withExpiresAt(new Date(exp))
				.sign(Algorithm.none());
	}
}
