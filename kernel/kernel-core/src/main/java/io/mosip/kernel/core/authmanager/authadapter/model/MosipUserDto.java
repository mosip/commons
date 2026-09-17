package io.mosip.kernel.core.authmanager.authadapter.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User profile used by the Spring Security auth adapter, aligned with MOSIP
 * identity-store fields.
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
	 * Unique user identifier; may be null on partial DTOs.
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
	 * User password or password hash; sensitive; may be null.
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
	 * Session or access token when used post-login; sensitive; may be null.
	 */
	private String token;
}
