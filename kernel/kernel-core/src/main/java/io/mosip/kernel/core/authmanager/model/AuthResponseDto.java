package io.mosip.kernel.core.authmanager.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author Srinivasan
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {

	/** The status. */
	private String status;

	/** The message. */
	private String message;
}
