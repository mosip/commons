
package io.mosip.kernel.auth.defaultadapter.test;

import static org.junit.Assert.assertTrue;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit4.SpringRunner;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import io.mosip.kernel.auth.defaultadapter.constant.AuthAdapterConstant;
import io.mosip.kernel.auth.defaultadapter.exception.AuthManagerException;
import io.mosip.kernel.auth.defaultadapter.handler.CustomJWTAuthHandler;
import io.mosip.kernel.auth.defaultadapter.model.AuthToken;
import io.mosip.kernel.openid.bridge.model.MosipUserDto;

/**
 * Tests {@link CustomJWTAuthHandler#retrieveUser} for a valid HS256 JWT and for
 * signature/expiry failures that must raise {@link AuthManagerException}.
 */
@SpringBootTest(classes = { AuthTestBootApplication.class })

@RunWith(SpringRunner.class)
public class CustomAuthHandlerTest extends CustomJWTAuthHandler {

	/**
	 * Asserts a correctly signed JWT yields a single granted authority.
	 *
	 * @throws Exception if JWT construction or retrieval fails
	 */
	@Test
	public void retrieveUserTest() throws Exception {

		MosipUserDto mosipUserDto = new MosipUserDto();
		mosipUserDto.setRole("PROCESSOR");
		mosipUserDto.setUserId("110005");
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

		Key signingKey = new SecretKeySpec(TextCodec.BASE64.decode("MVZNVFpvRFFyMmZrYm5WSGM4T2pzTk1TbXAzSzZhZ0w="),
				signatureAlgorithm.getJcaName());
		Map<String, Object> headers = new HashMap<>();
		headers.put("alg", "HS256");
		headers.put("typ", "JWT");
		String token = Jwts.builder().setHeader(headers).claim(AuthAdapterConstant.EMAIL, "mockuser!mosip.com")
				.claim(AuthAdapterConstant.MOBILE, "9210283991")
				.claim(AuthAdapterConstant.PREFERRED_USERNAME, "mock-user").claim(AuthAdapterConstant.ROLES, "ADMIN")
				.claim("userId", "mockuserid").claim("user_name", "mock-user").setSubject("mock-user")
				.setIssuedAt(Date.from(Instant.now())).setExpiration(Date.from(Instant.now().plusSeconds(345600)))
				.setAudience("account").signWith(SignatureAlgorithm.HS256, signingKey).compact();
		AuthToken authToken = new AuthToken(token);
		UserDetails authUserDetails = retrieveUser("110005", authToken);
		assertTrue(authUserDetails.getAuthorities().size() == 1);
	}
	
	
	/**
	 * Asserts {@link AuthManagerException} when the JWT is signed with a key that
	 * does not match the handler configuration.
	 *
	 * @throws Exception if JWT construction fails
	 */
	@Test(expected=AuthManagerException.class)
	public void retrieveUserSignExceptionTest() throws Exception {

		MosipUserDto mosipUserDto = new MosipUserDto();
		mosipUserDto.setRole("PROCESSOR");
		mosipUserDto.setUserId("110005");
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

		Key signingKey = new SecretKeySpec("12rTadwQr2fkbnVHc89jsnmSmp3K6bcK".getBytes(),
				signatureAlgorithm.getJcaName());
		Map<String, Object> headers = new HashMap<>();
		headers.put("alg", "HS256");
		headers.put("typ", "JWT");
		String token = Jwts.builder().setHeader(headers).claim(AuthAdapterConstant.EMAIL, "mockuser!mosip.com")
				.claim(AuthAdapterConstant.MOBILE, "9210283991")
				.claim(AuthAdapterConstant.PREFERRED_USERNAME, "mock-user").claim(AuthAdapterConstant.ROLES, "ADMIN")
				.claim("userId", "mockuserid").claim("user_name", "mock-user").setSubject("mock-user")
				.setIssuedAt(Date.from(Instant.now())).setExpiration(Date.from(Instant.now().plusSeconds(345600)))
				.setAudience("account").signWith(SignatureAlgorithm.HS256, signingKey).compact();
		AuthToken authToken = new AuthToken(token);
		UserDetails authUserDetails = retrieveUser("110005", authToken);
		assertTrue(authUserDetails.getAuthorities().size() == 1);
	}
	
	
	/**
	 * Asserts {@link AuthManagerException} when the JWT is already expired.
	 *
	 * @throws Exception if JWT construction fails
	 */
	@Test(expected=AuthManagerException.class)
	public void retrieveUserJWTExceptionTest() throws Exception {

		MosipUserDto mosipUserDto = new MosipUserDto();
		mosipUserDto.setRole("PROCESSOR");
		mosipUserDto.setUserId("110005");
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

		Key signingKey = new SecretKeySpec("1VMTZoDQr2fkbnVHc8OjsNMSmp3K6agL".getBytes(),
				signatureAlgorithm.getJcaName());
		Map<String, Object> headers = new HashMap<>();
		headers.put("alg", "HS256");
		headers.put("typ", "JWT");
		String token = Jwts.builder().setHeader(headers).claim(AuthAdapterConstant.EMAIL, "mockuser@mosip.com")
				.claim(AuthAdapterConstant.MOBILE, "9210283991")
				.claim(AuthAdapterConstant.PREFERRED_USERNAME, "mock-user").claim(AuthAdapterConstant.ROLES, "ADMIN")
				.claim("userId", "mockuserid").claim("user_name", "mock-user").setSubject("mock-user")
				.setIssuedAt(Date.from(Instant.now().minusSeconds(345600))).setExpiration(Date.from(Instant.now().minusSeconds(3456)))
				.setAudience("account").signWith(SignatureAlgorithm.HS256, signingKey).compact();
		AuthToken authToken = new AuthToken(token);
		UserDetails authUserDetails = retrieveUser("110005", authToken);
		assertTrue(authUserDetails.getAuthorities().size() == 1);
	}

}
