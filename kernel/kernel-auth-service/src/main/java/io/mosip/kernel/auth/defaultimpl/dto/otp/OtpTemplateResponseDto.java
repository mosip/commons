package io.mosip.kernel.auth.defaultimpl.dto.otp;

import java.util.ArrayList;

public class OtpTemplateResponseDto {
	private ArrayList<OtpTemplateDto> templates;

	public ArrayList<OtpTemplateDto> getTemplates() {
		return templates;
	}

	public void setTemplates(ArrayList<OtpTemplateDto> templates) {
		this.templates = templates;
	}
}
