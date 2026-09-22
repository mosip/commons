package io.mosip.kernel.core.authmanager.spi;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;
/**
 * Validates OAuth scopes present on the current security principal's token.
 * <p>
 * Contract: implementations read scopes from the authenticated context (no
 * extra HTTP). Call from resource methods that require specific scopes.
 * Empty or null scope lists are treated as not satisfied.
 * </p>
 *
 * @author Loganathan S
 */
public interface ScopeValidator {
	
	/**
	 * Returns whether the current token contains every listed scope.
	 *
	 * @param scopes never-null list of required scope names; empty list is
	 *               treated as vacuously true or false per implementation
	 * @return {@code true} if all scopes are present
	 */
	public boolean hasAllScopes(List<String> scopes);
	
	/**
	 * Returns whether the current token contains at least one listed scope.
	 *
	 * @param scopes never-null list of acceptable scope names; empty list
	 *               yields {@code false}
	 * @return {@code true} if any listed scope is present
	 */
	public boolean hasAnyScopes(List<String> scopes);
	
	/**
	 * Returns whether the current token contains the given scope.
	 *
	 * @param scope never-null, never-blank scope name
	 * @return {@code true} if the scope is present
	 */
	public boolean hasScope(String scope);
	
	/**
	 * Tests the current token's scopes with a caller-supplied stream predicate.
	 *
	 * @param scopes    never-null list of scope names to evaluate
	 * @param condition never-null bi-predicate receiving the token scope stream
	 *                  and a membership predicate
	 * @return {@code true} if {@code condition} holds for the current scopes
	 */
	public boolean hasScopes(List<String> scopes, BiPredicate<Stream<String>, Predicate<? super String>> condition);	

}
