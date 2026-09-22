package io.mosip.kernel.core.authmanager.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.Data;

/**
 * Request to change a user's password from an old value to a new one.
 * <p>
 * Contract: {@code newPassword} and {@code userId} are required; passwords
 * must match the annotated complexity pattern. Treat password fields as
 * sensitive; do not log them. Used by password-change APIs over HTTP.
 * </p>
 */
@Data
public class PasswordDto {

	/**
	 * Current password; must match the complexity pattern when present.
	 */
	@Pattern(regexp = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{8,})", message = "password invalid")
	private String oldPassword;

	/**
	 * Replacement password; required and must match the complexity pattern.
	 */
	@NotBlank
	@Pattern(regexp = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{8,})", message = "password invalid")
	private String newPassword;

	/**
	 * User whose password is being changed; required, non-blank.
	 */
	@NotBlank
	private String userId;

	/**
	 * Optional hash algorithm name used by the identity store; may be null.
	 */
	private String hashAlgo;
}
