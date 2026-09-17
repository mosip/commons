/**
 * Authentication request and response models for kernel auth-manager SPIs.
 */
package io.mosip.kernel.core.authmanager.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight authentication result containing only status and a message.
 * <p>
 * Contract: used when the caller does not need tokens (for example logout or
 * invalidate). Fields may be null. Does not perform I/O.
 * </p>
 *
 * @author Ramadurai Pandian
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthNResponse {

	/**
	 * High-level outcome such as {@code SUCCESS} or {@code FAILURE}; may be null.
	 */
	private String status;

	/**
	 * Human-readable description of the outcome; may be null or empty.
	 */
	private String message;

}
