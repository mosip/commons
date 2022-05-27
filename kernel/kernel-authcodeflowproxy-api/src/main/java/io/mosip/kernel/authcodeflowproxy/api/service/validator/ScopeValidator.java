package io.mosip.kernel.authcodeflowproxy.api.service.validator;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;

@Component("scopeValidator")
public class ScopeValidator {
	
	private static final String SCOPE = "scope";

	public static boolean hasAllScopes(List<String> scopes) {
		return hasScopes(scopes, Stream::allMatch);
	}
	
	public static boolean hasAnyScopes(List<String> scopes) {
		return hasScopes(scopes, Stream::anyMatch);
	}
	
	public static boolean hasScope(String scope) {
		return hasAllScopes(List.of(scope));
	}
	
	public static boolean hasScopes(List<String> scopes, BiPredicate<Stream<String>, Predicate<? super String>> condition) {
		List<? extends String> scopesInToken = getScopes();
		return condition.test(scopes.stream(), scopesInToken::contains);
	}

	private static List<String> getScopes() {
		Object principal = SecurityContextHolder
		.getContext()
		.getAuthentication().getPrincipal();
		if(principal instanceof AuthUserDetails) {
			AuthUserDetails authUserDetails = (AuthUserDetails) principal;
			String jwtToken = authUserDetails.getToken();
	        DecodedJWT decodedJWT = JWT.decode(jwtToken);
	        String scpoeClaim = decodedJWT.getClaim(SCOPE).asString();
	        List<String> scopes = Stream.of( scpoeClaim.split(" ")).collect(Collectors.toList());
	        return scopes;
		}
		
		return List.of();
	}
	

}
