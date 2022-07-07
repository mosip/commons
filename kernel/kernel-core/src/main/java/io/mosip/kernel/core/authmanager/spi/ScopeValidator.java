package io.mosip.kernel.core.authmanager.spi;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;
/**
 * Validator used to validate the scope in the token
 * 
 * @author Loganathan S
 *
 */
public interface ScopeValidator {
	
	public boolean hasAllScopes(List<String> scopes);
	
	public boolean hasAnyScopes(List<String> scopes);
	
	public boolean hasScope(String scope);
	
	public boolean hasScopes(List<String> scopes, BiPredicate<Stream<String>, Predicate<? super String>> condition);	

}
