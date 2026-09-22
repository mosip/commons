/**
 * Authorization response models for kernel auth-manager SPIs.
 */
package io.mosip.kernel.core.authmanager.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight authorization result containing status and a message.
 * <p>
 * Contract: used when an AuthZ check completes without returning a user
 * token. Fields may be null. Does not perform I/O.
 * </p>
 *
 * @author Ramadurai Pandian
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthZResponseDto {

	/**
	 * High-level outcome such as {@code SUCCESS} or {@code FAILURE}; may be null.
	 */
	private String status;
	/**
	 * Human-readable description of the outcome; may be null or empty.
	 */
	private String message;
}
