package io.mosip.kernel.auth.defaultadapter.model;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

/**
 * Unauthenticated token wrapper carrying the inbound JWT (and optional OpenID
 * Connect id token) through Spring Security's authentication manager.
 * <p>
 * {@link io.mosip.kernel.auth.defaultadapter.filter.AuthFilter} builds this
 * from cookies; {@link io.mosip.kernel.auth.defaultadapter.handler.AuthHandler}
 * reads {@link #token} and {@link #idToken}.
 * <p>
 * This adapter is a library other MOSIP services put on the classpath.
 *
 * @author Sabbu Uday Kumar
 * @since 1.0.0
 */
public class AuthToken extends UsernamePasswordAuthenticationToken {

	/**
	 * Serialization identifier.
	 */
	private static final long serialVersionUID = 4068560701182593212L;

	/**
	 * Access-token JWT extracted from the Authorization cookie.
	 */
	private String token;

	/**
	 * Optional OpenID Connect id token when {@code auth.validate.id-token} is
	 * enabled.
	 */
	private String idToken;

	/**
	 * Creates a token holding only the access JWT.
	 *
	 * @param token the Authorization cookie value
	 */
	public AuthToken(String token) {
		super(null, null);
		this.token = token;
	}

	/**
	 * Creates a token holding both the access JWT and the id token.
	 *
	 * @param token   the Authorization cookie value
	 * @param idToken the OpenID Connect id token cookie value
	 */
	public AuthToken(String token, String idToken){
		super(null, null);
		this.token = token;
		this.idToken = idToken;
	}

	/**
	 * Returns the OpenID Connect id token, or {@code null} if none was supplied.
	 *
	 * @return the id token
	 */
	public String getIdToken() {
		return idToken;
	}

	/**
	 * Sets the OpenID Connect id token.
	 *
	 * @param idToken the id token
	 */
	public void setIdToken(String idToken) {
		this.idToken = idToken;
	}

	/**
	 * Returns the access-token JWT.
	 *
	 * @return the access token
	 */
	public String getToken() {
		return token;
	}

	/**
	 * Sets the access-token JWT.
	 *
	 * @param token the access token
	 */
	public void setToken(String token) {
		this.token = token;
	}
}
