package io.mosip.kernel.auth.defaultimpl.dto.otp;

public class OtpGenerateResponseDto {
	private String otp;
	private String status;

	public String getOtp() {
		return otp;
	}

	public void setOtp(String otp) {
		this.otp = otp;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
