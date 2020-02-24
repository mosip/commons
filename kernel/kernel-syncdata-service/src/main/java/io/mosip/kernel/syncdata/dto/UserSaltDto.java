package io.mosip.kernel.syncdata.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The Class UserSaltDto.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSaltDto {

	/** The user id. */
	private String userId;

	/** The salt. */
	private String salt;
}
