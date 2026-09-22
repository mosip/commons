package io.mosip.kernel.openid.bridge.api.constants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Constructor;

import org.junit.Test;

/**
 * Exercises enum accessors and private constructors counted by JaCoCo/Sonar.
 */
public class OpenIdBridgeConstantsTest {

	@Test
	public void errorsExposeCodeAndMessage() {
		for (Errors error : Errors.values()) {
			assertNotNull(error.getErrorCode());
			assertFalse(error.getErrorMessage().isEmpty());
		}
		assertEquals("KER-ACP-001", Errors.COOKIE_NOTPRESENT_ERROR.getErrorCode());
	}

	@Test
	public void authErrorCodeExposeCodeAndMessage() {
		assertEquals("KER-ATH-401", AuthErrorCode.UNAUTHORIZED.getErrorCode());
		assertEquals("Authentication Failed", AuthErrorCode.UNAUTHORIZED.getErrorMessage());
		assertEquals("KER-ATH-403", AuthErrorCode.FORBIDDEN.getErrorCode());
		assertNotNull(AuthErrorCode.FORBIDDEN.getErrorMessage());
	}

	@Test
	public void constantsHoldersAreConstructable() throws Exception {
		assertEquals("redirect_uri", Constants.REDIRECT_URI);
		assertEquals("redirect_uri", IAMConstants.REDIRECT_URI);
		assertEquals("preferred_username", AuthConstant.PREFERRED_USERNAME);
		assertEquals("azp", AuthConstant.AZP);
		assertEquals("iss", AuthConstant.ISSUER);

		Constructor<Constants> constantsCtor = Constants.class.getDeclaredConstructor();
		constantsCtor.setAccessible(true);
		assertNotNull(constantsCtor.newInstance());

		Constructor<IAMConstants> iamCtor = IAMConstants.class.getDeclaredConstructor();
		iamCtor.setAccessible(true);
		assertNotNull(iamCtor.newInstance());
	}
}
