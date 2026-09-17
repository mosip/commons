package io.mosip.kernel.core.authmanager.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic authentication API response carrying status and a message.
 * <p>
 * Contract: used by logout and similar operations that do not return tokens.
 * Fields may be null. Does not perform I/O.
 * </p>
 *
 * @author Srinivasan
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {

	/**
	 * High-level outcome such as {@code SUCCESS} or {@code FAILURE}; may be null.
	 */
	private String status;

	/**
	 * Human-readable description of the outcome; may be null or empty.
	 */
	private String message;
}
