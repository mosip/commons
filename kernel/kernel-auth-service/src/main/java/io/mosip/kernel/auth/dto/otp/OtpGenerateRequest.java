package io.mosip.kernel.auth.dto.otp;

import io.mosip.kernel.core.authmanager.model.MosipUserDto;

public class OtpGenerateRequest {
	private String key;

	public OtpGenerateRequest(MosipUserDto mosipUserDto) {
		this.key = mosipUserDto.getUserId();
	}

	public String getKey() {
		return key;
	}
}
