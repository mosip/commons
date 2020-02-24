package io.mosip.kernel.syncdata.service;

import io.mosip.kernel.syncdata.dto.SyncUserDetailDto;
import io.mosip.kernel.syncdata.dto.SyncUserSaltDto;

/**
 * This service class handles CRUD opertaion method signature
 * 
 * @author Srinivasan
 * @author Megha Tanga
 *
 */
public interface SyncUserDetailsService {

	/**
	 * This method would fetch all user details for that registration center id
	 * 
	 * @param regId - registration center id
	 * @return {@link SyncUserDetailDto}
	 */
	SyncUserDetailDto getAllUserDetail(String regId);

	/**
	 * Gets the user salts.
	 *
	 * @param regId the reg id
	 * @return the user salts
	 */
	SyncUserSaltDto getUserSalts(String regId);
}
