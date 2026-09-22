package io.mosip.kernel.openid.bridge.api.service.validator;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;
/**
 * Validator used to validate the OIDC scope values present on the current
 * Spring Security principal.
 * <p>
 * Implementations read {@code SCOPE_}-prefixed {@link org.springframework.security.core.GrantedAuthority}
 * entries from {@link io.mosip.kernel.openid.bridge.model.AuthUserDetails}
 * in {@code SecurityContextHolder}. Used by MOSIP HTTP services that depend on
 * {@code kernel-auth-adapter}.
 * 
 * @author Loganathan S
 *
 */
public interface ScopeValidator {
	
	/**
	 * Returns {@code true} only when every required scope is present on the
	 * authenticated token.
	 *
	 * @param scopes required OIDC scope names (without the {@code SCOPE_} prefix)
	 * @return {@code true} if all of {@code scopes} are granted
	 */
	public boolean hasAllScopes(List<String> scopes);
	
	/**
	 * Returns {@code true} when at least one of the required scopes is present on
	 * the authenticated token.
	 *
	 * @param scopes OIDC scope names to test (without the {@code SCOPE_} prefix)
	 * @return {@code true} if any of {@code scopes} is granted
	 */
	public boolean hasAnyScopes(List<String> scopes);
	
	/**
	 * Returns {@code true} when the single required scope is present on the
	 * authenticated token.
	 *
	 * @param scope OIDC scope name (without the {@code SCOPE_} prefix)
	 * @return {@code true} if {@code scope} is granted
	 */
	public boolean hasScope(String scope);
	
	/**
	 * Tests {@code scopes} against the token's granted scopes using the supplied
	 * stream matcher (for example {@link Stream#allMatch} or {@link Stream#anyMatch}).
	 *
	 * @param scopes    OIDC scope names to test (without the {@code SCOPE_} prefix)
	 * @param condition matcher applied as {@code condition.test(scopes.stream(), tokenScopes::contains)}
	 * @return {@code true} if {@code scopes} is non-null and {@code condition} holds
	 */
	public boolean hasScopes(List<String> scopes, BiPredicate<Stream<String>, Predicate<? super String>> condition);	

}
