package io.mosip.kernel.auth.defaultimpl.dto.otp;

import io.mosip.kernel.core.authmanager.model.MosipUserDto;

/**
 * Alternate OTP generate request used by {@code OTPGenerateServiceImpl} when calling
 * the OTP manager. {@link #key} is the MOSIP user id, same as {@link OtpGenerateRequest}.
 */
public class OtpGenerateRequestDto {

	/**
	 * OTP store key, set to {@link MosipUserDto#getUserId()}.
	 */
	private String key;

	/**
	 * Builds a generate request from the authenticated MOSIP user.
	 *
	 * @param mosipUserDto user whose id becomes the OTP key
	 */
	public OtpGenerateRequestDto(MosipUserDto mosipUserDto) {
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
