package io.mosip.kernel.auth.defaultimpl.dto.otp;

import io.mosip.kernel.core.authmanager.model.MosipUserDto;

public class OtpGenerateRequestDto {
	private String key;

	public OtpGenerateRequestDto(MosipUserDto mosipUserDto) {
		this.key = mosipUserDto.getUserId();
	}

	public String getKey() {
		return key;
	}
}
