/**
 * 
 */
package io.mosip.kernel.auth.defaultimpl.dto;

import lombok.Data;

/**
 * JWT string plus its expiry timestamp as produced by {@code TokenGenerator}.
 * Used both when minting a new token and when rotating an existing one.
 *
 * @author Ramadurai Pandian
 *
 */
@Data
public class TimeToken {

	/**
	 * Compact JWT string.
	 */
	private String token;

	/**
	 * Absolute expiry time of {@link #token} as a numeric timestamp.
	 */
	private long expTime;

}
