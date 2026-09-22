package io.mosip.kernel.auth.defaultadapter.model;

import lombok.Getter;

/**
 * Mutable holder for a cached client-credentials token shared by the self-token
 * RestTemplate interceptor, WebClient filter, and renewal scheduler.
 * <p>
 * This adapter is a library other MOSIP services put on the classpath.
 *
 * @param <T> the token payload type (this adapter uses {@link String})
 */
public class TokenHolder<T> {

	/**
	 * Currently cached token, or {@code null} when unset or removed.
	 */
	@Getter
	private T token;

	/**
	 * Replaces the cached token.
	 *
	 * @param token the new token, or {@code null} to clear
	 */
	public void setToken(T token) {
		this.token = token;
	}

	/**
	 * Clears the cached token by setting it to {@code null}.
	 */
	public void removeToken() {
		this.token = null;
	}

}
