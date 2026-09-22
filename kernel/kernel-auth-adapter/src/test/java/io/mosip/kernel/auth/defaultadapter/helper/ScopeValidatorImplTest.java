package io.mosip.kernel.auth.defaultadapter.helper;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import io.mosip.kernel.openid.bridge.model.AuthUserDetails;

/**
 * Unit tests for {@link ScopeValidatorImpl} scope checks against the current
 * {@link SecurityContextHolder} authentication (authorities {@code SCOPE_aaa}
 * and {@code SCOPE_bbb}).
 */
@RunWith(MockitoJUnitRunner.class)
public class ScopeValidatorImplTest {

	/** Mock Spring Security authentication placed on the security context. */
	@Mock
	private Authentication authentication;

	/** Mock principal whose authorities supply the granted scopes. */
	@Mock
	private AuthUserDetails principal;

	/**
	 * Installs a security context whose principal has scopes {@code aaa} and
	 * {@code bbb}.
	 *
	 * @throws Exception if mock setup fails
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Before
	public void setUp() throws Exception {
		when(authentication.getPrincipal()).thenReturn(principal);
		when(principal.getAuthorities()).thenReturn((Collection) List.of(new SimpleGrantedAuthority("SCOPE_aaa"),  new SimpleGrantedAuthority("SCOPE_bbb")));
		SecurityContextHolder.getContext().setAuthentication(authentication);

	}

	/**
	 * Creates a fresh {@link ScopeValidatorImpl} under test.
	 *
	 * @return a new validator instance
	 */
	private ScopeValidatorImpl createTestSubject() {
		return new ScopeValidatorImpl();
	}

	/**
	 * Asserts {@code hasAllScopes} is false when the requested scope list is
	 * {@code null}.
	 *
	 * @throws Exception if the validator call fails
	 */
	@Test
	public void testHasAllScopes_nullScopes() throws Exception {
		ScopeValidatorImpl testSubject;
		List<String> scopes = null;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hasAllScopes(scopes);
		assertFalse(result);
	}

	/**
	 * Asserts {@code hasAllScopes} is false when the requested scope list is
	 * empty/{@code null}.
	 *
	 * @throws Exception if the validator call fails
	 */
	@Test
	public void testHasAllScopes_emptyScopes() throws Exception {
		ScopeValidatorImpl testSubject;
		List<String> scopes = null;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hasAllScopes(scopes);
		assertFalse(result);
	}

	/**
	 * Asserts {@code hasAllScopes} is true when every requested scope is granted.
	 *
	 * @throws Exception if the validator call fails
	 */
	@Test
	public void testHasAllScopes() throws Exception {
		ScopeValidatorImpl testSubject;
		List<String> scopes = List.of("aaa", "bbb");
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hasAllScopes(scopes);
		assertTrue(result);
	}

	/**
	 * Asserts {@code hasAllScopes} is false when none of the requested scopes are
	 * granted.
	 *
	 * @throws Exception if the validator call fails
	 */
	@Test
	public void testHasAllScopes_negative_allmissing() throws Exception {
		ScopeValidatorImpl testSubject;
		List<String> scopes = List.of("ccc", "ddd");
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hasAllScopes(scopes);
		assertFalse(result);
	}

	/**
	 * Asserts {@code hasAllScopes} is false when only some of the requested scopes
	 * are granted.
	 *
	 * @throws Exception if the validator call fails
	 */
	@Test
	public void testHasAllScopes_negative_somemissing() throws Exception {
		ScopeValidatorImpl testSubject;
		List<String> scopes = List.of("bbb", "ccc");
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hasAllScopes(scopes);
		assertFalse(result);
	}

	/**
	 * Asserts {@code hasAnyScopes} is true when all requested scopes are granted.
	 *
	 * @throws Exception if the validator call fails
	 */
	@Test
	public void testHasAnyScopes() throws Exception {
		ScopeValidatorImpl testSubject;
		List<String> scopes = List.of("aaa", "bbb");
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hasAnyScopes(scopes);
		assertTrue(result);
	}

	/**
	 * Asserts {@code hasAnyScopes} is true when at least one requested scope is
	 * granted.
	 *
	 * @throws Exception if the validator call fails
	 */
	@Test
	public void testHasAnyScopes_positive_somepresent() throws Exception {
		ScopeValidatorImpl testSubject;
		List<String> scopes = List.of("bbb", "ccc");
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hasAnyScopes(scopes);
		assertTrue(result);
	}

	/**
	 * Asserts {@code hasAnyScopes} is false when none of the requested scopes are
	 * granted.
	 *
	 * @throws Exception if the validator call fails
	 */
	@Test
	public void testHasAnyScopes_negative_nonepresent() throws Exception {
		ScopeValidatorImpl testSubject;
		List<String> scopes = List.of("ccc", "ddd");
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hasAnyScopes(scopes);
		assertFalse(result);
	}

	/**
	 * Asserts {@code hasScope} is true for a granted scope.
	 *
	 * @throws Exception if the validator call fails
	 */
	@Test
	public void testHasScope() throws Exception {
		ScopeValidatorImpl testSubject;
		String scope = "aaa";
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hasScope(scope);
		assertTrue(result);
	}

	/**
	 * Asserts {@code hasScope} is false for a scope that is not granted.
	 *
	 * @throws Exception if the validator call fails
	 */
	@Test
	public void testHasScope_negative() throws Exception {
		ScopeValidatorImpl testSubject;
		String scope = "ccc";
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hasScope(scope);
		assertFalse(result);
	}

	/**
	 * Asserts {@code hasScopes} with an {@code anyMatch} condition succeeds when
	 * the granted scopes overlap the requested list.
	 *
	 * @throws Exception if the validator call fails
	 */
	@Test
	public void testHasScopes() throws Exception {
		ScopeValidatorImpl testSubject;
		List<String> scopes = List.of("aaa", "bbb");
		BiPredicate<Stream<String>, Predicate<? super String>> condition = Stream::anyMatch;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hasScopes(scopes, condition);
		assertTrue(result);
	}

	/**
	 * When the principal is not {@link AuthUserDetails}, granted scopes are empty
	 * so {@code hasScope} is false.
	 */
	@Test
	public void testHasScopeWhenPrincipalIsNotAuthUserDetails() {
		when(authentication.getPrincipal()).thenReturn("anonymous");
		SecurityContextHolder.getContext().setAuthentication(authentication);
		assertFalse(createTestSubject().hasScope("aaa"));
	}
}
