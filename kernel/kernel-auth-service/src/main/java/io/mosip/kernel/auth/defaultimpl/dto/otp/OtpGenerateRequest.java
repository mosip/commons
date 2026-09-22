package io.mosip.kernel.auth.defaultimpl.dto.otp;

import io.mosip.kernel.core.authmanager.model.MosipUserDto;

/**
 * OTP-manager generate request whose {@link #key} is the MOSIP user id.
 * Posted inside a {@code RequestWrapper} to the OTP generate API, and also used
 * to rebuild the same key when validating an OTP.
 */
public class OtpGenerateRequest {

	/**
	 * OTP store key, set to {@link MosipUserDto#getUserId()}.
	 */
	private String key;

	/**
	 * Builds a generate request from the authenticated MOSIP user.
	 *
	 * @param mosipUserDto user whose id becomes the OTP key
	 */
	public OtpGenerateRequest(MosipUserDto mosipUserDto) {
		this.key = mosipUserDto.getUserId();
	}

	/**
	 * Returns the OTP store key.
	 *
	 * @return user id used as the OTP key
	 */
	public String getKey() {
		return key;
	}
}
