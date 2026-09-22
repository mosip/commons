package io.mosip.kernel.core.authmanager.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Holds the registration ID (RID) associated with a MOSIP user.
 * <p>
 * Contract: returned by
 * {@link io.mosip.kernel.core.authmanager.spi.AuthService#getRidBasedOnUid(String, String)}.
 * {@code rId} is null when no mapping exists. Does not perform I/O.
 * </p>
 *
 * @author Srinivasan
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RIdDto {

	/**
	 * Registration ID for the user; null if unmapped.
	 */
	private String rId;
}
