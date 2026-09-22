package io.mosip.kernel.core.authmanager.model;

import java.util.List;

/**
 * Wrapper for a list of {@link MosipUserDto} records returned by user-search
 * APIs.
 * <p>
 * Contract: {@code mosipUserDtoList} may be null or empty when no users match.
 * Does not perform I/O.
 * </p>
 *
 * @author Sabbu Uday Kumar
 * @since 1.0.0
 */
public class MosipUserListDto {
	/**
	 * Matched users; may be null or empty.
	 */
	List<MosipUserDto> mosipUserDtoList;

	/**
	 * Returns the list of MOSIP users in this response.
	 *
	 * @return the user list; may be null or empty
	 */
	public List<MosipUserDto> getMosipUserDtoList() {
		return mosipUserDtoList;
	}

	/**
	 * Replaces the list of MOSIP users in this response.
	 *
	 * @param mosipUserDtoList user list to store; may be null
	 */
	public void setMosipUserDtoList(List<MosipUserDto> mosipUserDtoList) {
		this.mosipUserDtoList = mosipUserDtoList;
	}
}
