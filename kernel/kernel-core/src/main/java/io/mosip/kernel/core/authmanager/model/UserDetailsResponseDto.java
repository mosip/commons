package io.mosip.kernel.core.authmanager.model;

import java.util.List;

import lombok.Data;

/**
 * Wrapper for a list of {@link UserDetailsDto} records.
 * <p>
 * Contract: {@code userDetails} may be null or empty when no users match.
 * Does not perform I/O.
 * </p>
 *
 * @author Srinivasan
 * @since 1.0.0
 */
@Data
public class UserDetailsResponseDto {

	/**
	 * Matched user profiles; may be null or empty.
	 */
	List<UserDetailsDto> userDetails;
}
