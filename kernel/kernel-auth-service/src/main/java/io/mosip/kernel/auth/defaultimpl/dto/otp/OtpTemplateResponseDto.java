package io.mosip.kernel.auth.defaultimpl.dto.otp;

import java.util.ArrayList;

/**
 * Masterdata template-list payload returned when fetching OTP SMS or email templates.
 */
public class OtpTemplateResponseDto {

	/**
	 * Template records matching the requested type and language.
	 */
	private ArrayList<OtpTemplateDto> templates;

	/**
	 * Returns the fetched OTP templates.
	 *
	 * @return template list, or {@code null} if unset
	 */
	public ArrayList<OtpTemplateDto> getTemplates() {
		return templates;
	}

	/**
	 * Sets the fetched OTP templates.
	 *
	 * @param templates template list to store
	 */
	public void setTemplates(ArrayList<OtpTemplateDto> templates) {
		this.templates = templates;
	}
}
