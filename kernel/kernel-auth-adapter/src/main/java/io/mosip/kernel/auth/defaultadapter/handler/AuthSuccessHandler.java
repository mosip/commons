package io.mosip.kernel.auth.defaultadapter.handler;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Success handler that does not write a login response.
 * <p>
 * {@link io.mosip.kernel.auth.defaultadapter.filter.AuthFilter} continues the
 * filter chain after a successful token authentication; this handler is a
 * no-op so Spring Security does not redirect or render a default success page.
 * <p>
 * This adapter is a library other MOSIP services put on the classpath.
 */
public class AuthSuccessHandler implements AuthenticationSuccessHandler {

	/**
	 * Intentionally empty: the filter chain proceeds via
	 * {@code AuthFilter.successfulAuthentication}.
	 *
	 * @param request        the authenticated request
	 * @param response       the response
	 * @param authentication the successful authentication
	 * @throws IOException      never thrown by this implementation
	 * @throws ServletException never thrown by this implementation
	 */
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
	}

}
