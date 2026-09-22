package io.mosip.kernel.auth.defaultadapter.helper;

import java.util.Collection;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import io.mosip.kernel.openid.bridge.api.service.validator.ScopeValidator;
import io.mosip.kernel.openid.bridge.model.AuthUserDetails;

/**
 * {@link ScopeValidator} that reads OAuth scope authorities from the current
 * {@link AuthUserDetails} (authorities prefixed with
 * {@link AuthUserDetails#SCOPE_AUTHORITY_PREFIX}).
 * <p>
 * Hosting services use this bean as {@code @scopeValidator} in method-security
 * expressions.
 * <p>
 * This adapter is a library other MOSIP services put on the classpath.
 *
 * @author Loganathan S
 */
@Component("scopeValidator")
public class ScopeValidatorImpl implements ScopeValidator {
	
	/**
	 * Checks for all scopes.
	 *
	 * @param scopes the scopes
	 * @return true, if successful
	 */
	public boolean hasAllScopes(List<String> scopes) {
		return hasScopes(scopes, Stream::allMatch);
	}
	
	/**
	 * Checks for any scopes.
	 *
	 * @param scopes the scopes
	 * @return true, if successful
	 */
	public boolean hasAnyScopes(List<String> scopes) {
		return hasScopes(scopes, Stream::anyMatch);
	}
	
	/**
	 * Checks for scope.
	 *
	 * @param scope the scope
	 * @return true, if successful
	 */
	public boolean hasScope(String scope) {
		return hasAllScopes(List.of(scope));
	}
	
	/**
	 * Checks for scopes.
	 *
	 * @param scopes the scopes
	 * @param condition the condition
	 * @return true, if successful
	 */
	public boolean hasScopes(List<String> scopes, BiPredicate<Stream<String>, Predicate<? super String>> condition) {
		List<? extends String> scopesInToken = getScopes();
		return scopes != null && condition.test(scopes.stream(), scopesInToken::contains);
	}

	/**
	 * Gets the scopes.
	 *
	 * @return the scopes
	 */
	private List<String> getScopes() {
		Object principal = SecurityContextHolder
		.getContext()
		.getAuthentication().getPrincipal();
		if(principal instanceof AuthUserDetails) {
			AuthUserDetails authUserDetails = (AuthUserDetails) principal;
			Collection<? extends GrantedAuthority> authorities = authUserDetails.getAuthorities();
	        List<String> scopes = authorities.stream()
	        						.map(GrantedAuthority::getAuthority)
	        						.filter(string -> string.startsWith(AuthUserDetails.SCOPE_AUTHORITY_PREFIX))
	        						.map(string -> string.substring(AuthUserDetails.SCOPE_AUTHORITY_PREFIX.length()))
	        						.collect(Collectors.toList());
	        return scopes;
		}
		
		return List.of();
	}
	

}
