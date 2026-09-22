package io.mosip.kernel.core.authmanager.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Holds the individual identifier (UIN or VID) associated with a MOSIP user.
 * <p>
 * Contract: returned by
 * {@link io.mosip.kernel.core.authmanager.spi.AuthService#getIndividualIdBasedOnUserID(String, String)}.
 * {@code individualId} is null when no mapping exists. Does not perform I/O.
 * </p>
 *
 * @author Srinivasan
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndividualIdDto {

	/**
	 * Individual identifier (UIN or VID) for the user; null if unmapped.
	 */
	private String individualId;
}
