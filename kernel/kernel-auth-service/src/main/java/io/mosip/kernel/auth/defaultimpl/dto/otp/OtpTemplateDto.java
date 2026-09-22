package io.mosip.kernel.auth.defaultimpl.dto.otp;

import lombok.Data;

/**
 * Masterdata template record used to render OTP SMS or email content.
 * Maps the kernel template service JSON (id, type code, language, file text, and flags).
 */
@Data
public class OtpTemplateDto {

	/**
	 * Template identifier from masterdata.
	 */
	private String id;

	/**
	 * Template display name.
	 */
	private String name;

	/**
	 * Template description from masterdata.
	 */
	private String description;

	/**
	 * File format code of the template body (for example text or HTML).
	 */
	private String fileFormatCode;

	/**
	 * Template model or engine identifier.
	 */
	private String model;

	/**
	 * Template body text, typically Velocity markup with OTP placeholders.
	 */
	private String fileText;

	/**
	 * Masterdata module identifier that owns the template.
	 */
	private String moduleId;

	/**
	 * Masterdata module name that owns the template.
	 */
	private String moduleName;

	/**
	 * Template type code used to select SMS versus email OTP templates.
	 */
	private String templateTypeCode;

	/**
	 * Language code of this template variant.
	 */
	private String langCode;

	/**
	 * Whether this template is active and eligible for OTP rendering.
	 */
	private Boolean isActive;
}
