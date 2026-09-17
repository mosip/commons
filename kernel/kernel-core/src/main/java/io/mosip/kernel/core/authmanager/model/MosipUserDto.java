package io.mosip.kernel.core.authmanager.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MOSIP user profile transferred between auth-manager and consuming services.
 * <p>
 * Contract: fields may be null depending on the identity store. Treat
 * {@code userPassword} and {@code token} as sensitive; do not log them. Does
 * not perform I/O.
 * </p>
 *
 * @author Sabbu Uday Kumar
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MosipUserDto {
	/**
	 * Unique user identifier in the identity store; may be null on partial DTOs.
	 */
	private String userId;
	/**
	 * Mobile number; may be null if not registered.
	 */
	private String mobile;
	/**
	 * Email address; may be null if not registered.
	 */
	private String mail;
	/**
	 * Preferred language code (ISO 639); may be null.
	 */
	private String langCode;
	/**
	 * User password or password hash as stored by the provider; sensitive.
	 */
	private String userPassword;
	/**
	 * Display name; may be null.
	 */
	private String name;
	/**
	 * Comma-separated or single role name; may be null.
	 */
	private String role;
	/**
	 * Registration ID associated with the user; may be null.
	 */
	private String rId;
	/**
	 * Session or access token when the DTO is used post-login; sensitive.
	 */
	private String token;
}
