package io.mosip.kernel.auth.defaultimpl.dto;

/**
 * Leftover MOSIP user principal used with {@link MosipUserToken} during local JWT
 * validation. Holds contact details, role, language, and an optional token string.
 */
public class MosipUser {

	/**
	 * Login user name.
	 */
	private String userName;

	/**
	 * Registered mobile number.
	 */
	private String mobile;

	/**
	 * Registered email address.
	 */
	private String mail;

	/**
	 * Realm or application role assigned to the user.
	 */
	private String role;

	/**
	 * Preferred language code.
	 */
	private String langCode;

	/**
	 * Optional authentication token associated with this principal.
	 */
	private String token;

	/**
	 * Creates an empty user principal.
	 */
	public MosipUser() {
	}

	/**
	 * Creates a user principal with identity and contact fields.
	 *
	 * @param userName login user name
	 * @param mobile   registered mobile number
	 * @param mail     registered email address
	 * @param role     assigned role
	 */
	public MosipUser(String userName, String mobile, String mail, String role) {
		this.userName = userName;
		this.mobile = mobile;
		this.mail = mail;
		this.role = role;
	}

	/**
	 * Creates a user principal including language preference.
	 *
	 * @param userName login user name
	 * @param mobile   registered mobile number
	 * @param mail     registered email address
	 * @param role     assigned role
	 * @param langCode preferred language code
	 */
	public MosipUser(String userName, String mobile, String mail, String role, String langCode) {
		this.userName = userName;
		this.mobile = mobile;
		this.mail = mail;
		this.role = role;
		this.langCode = langCode;
	}

	/**
	 * Returns the login user name.
	 *
	 * @return user name
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Sets the login user name.
	 *
	 * @param userName user name to store
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * Returns the registered mobile number.
	 *
	 * @return mobile number
	 */
	public String getMobile() {
		return mobile;
	}

	/**
	 * Sets the registered mobile number.
	 *
	 * @param mobile mobile number to store
	 */
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	/**
	 * Returns the registered email address.
	 *
	 * @return email address
	 */
	public String getMail() {
		return mail;
	}

	/**
	 * Sets the registered email address.
	 *
	 * @param mail email address to store
	 */
	public void setMail(String mail) {
		this.mail = mail;
	}

	/**
	 * Returns the assigned role.
	 *
	 * @return role name
	 */
	public String getRole() {
		return role;
	}

	/**
	 * Sets the assigned role.
	 *
	 * @param role role name to store
	 */
	public void setRole(String role) {
		this.role = role;
	}

	/**
	 * Returns the preferred language code.
	 *
	 * @return language code
	 */
	public String getLangCode() {
		return langCode;
	}

	/**
	 * Sets the preferred language code.
	 *
	 * @param langCode language code to store
	 */
	public void setLangCode(String langCode) {
		this.langCode = langCode;
	}

	/**
	 * Returns the optional authentication token.
	 *
	 * @return token string, or {@code null} if unset
	 */
	public String getToken() {
		return token;
	}

	/**
	 * Sets the optional authentication token.
	 *
	 * @param token token string to store
	 */
	public void setToken(String token) {
		this.token = token;
	}
}
