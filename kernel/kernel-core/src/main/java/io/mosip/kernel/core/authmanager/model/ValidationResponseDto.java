package io.mosip.kernel.core.authmanager.model;

import lombok.Data;

/**
 * Holds a simple validation outcome status string.
 * <p>
 * Contract: {@code status} is typically {@code VALID} or {@code INVALID} and
 * may be null if the provider omitted it. Does not perform I/O.
 * </p>
 *
 * @author Srinivasan
 */
@Data
public class ValidationResponseDto {

	/**
	 * Validation outcome such as {@code VALID} or {@code INVALID}; may be null.
	 */
	private String status;
}
