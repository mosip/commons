/**
 * 
 */
package io.mosip.kernel.auth.defaultimpl.service;

import io.mosip.kernel.core.authmanager.model.MosipUserDto;
import io.mosip.kernel.core.authmanager.model.OtpUser;

/**
 * @author Ramadurai Pandian
 *
 */
public interface UinService {

	MosipUserDto getDetailsFromUin(OtpUser otpUser) throws Exception;

	MosipUserDto getDetailsForValidateOtp(String uin) throws Exception;
}
