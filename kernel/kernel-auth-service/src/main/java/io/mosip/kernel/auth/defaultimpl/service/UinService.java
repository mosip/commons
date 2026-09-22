/**
 * 
 */
package io.mosip.kernel.auth.defaultimpl.service;

import io.mosip.kernel.core.authmanager.model.MosipUserDto;
import io.mosip.kernel.core.authmanager.model.OtpUser;

/**
 * Loads individual/UIN details from ID repository for OTP send and validate.
 *
 * @author Ramadurai Pandian
 *
 */
public interface UinService {

	/**
	 * Resolves user contact and identity details from a UIN on {@code otpUser}.
	 *
	 * @param otpUser OTP request containing UIN and channels
	 * @return MOSIP user built from ID repository
	 * @throws Exception if ID repository lookup fails
	 */
	MosipUserDto getDetailsFromUin(OtpUser otpUser) throws Exception;

	/**
	 * Loads user details for OTP validation using a UIN string.
	 *
	 * @param uin unique identification number
	 * @return MOSIP user for OTP validation
	 * @throws Exception if ID repository lookup fails
	 */
	MosipUserDto getDetailsForValidateOtp(String uin) throws Exception;
}
