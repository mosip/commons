package io.mosip.kernel.auth.defaultimpl.dto;

/**
 * Pairing of a {@link MosipUser} principal with the JWT that was validated for it.
 * Returned by leftover OTP and basic token validation paths in {@code TokenValidator}.
 */
public class MosipUserToken {

	/**
	 * Validated JWT string.
	 */
	private String token;

	/**
	 * Principal extracted from or associated with {@link #token}.
	 */
	private MosipUser mosipUser;

	/**
	 * Creates an empty user-and-token pair.
	 */
	public MosipUserToken() {
	}

	/**
	 * Creates a pair from a principal and its validated token.
	 *
	 * @param mosipUser principal associated with the token
	 * @param token     validated JWT string
	 */
	public MosipUserToken(MosipUser mosipUser, String token) {
		this.mosipUser = mosipUser;
		this.token = token;
	}

	/**
	 * Returns the validated JWT.
	 *
	 * @return token string
	 */
	public String getToken() {
		return token;
	}

	/**
	 * Sets the validated JWT.
	 *
	 * @param token token string to store
	 */
	public void setToken(String token) {
		this.token = token;
	}

	/**
	 * Returns the principal associated with the token.
	 *
	 * @return MOSIP user principal
	 */
	public MosipUser getMosipUser() {
		return mosipUser;
	}

	/**
	 * Sets the principal associated with the token.
	 *
	 * @param mosipUser MOSIP user principal to store
	 */
	public void setMosipUser(MosipUser mosipUser) {
		this.mosipUser = mosipUser;
	}
}
