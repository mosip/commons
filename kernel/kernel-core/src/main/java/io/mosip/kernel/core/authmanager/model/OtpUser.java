package io.mosip.kernel.core.authmanager.model;

import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * Request to send or generate an OTP for a user on one or more channels.
 * <p>
 * Contract: passed to
 * {@link io.mosip.kernel.core.authmanager.spi.AuthNService#authenticateWithOtp(OtpUser)}.
 * {@code userId}, {@code appId}, and {@code otpChannel} must be non-null and
 * non-empty. Template variables may be null. Does not perform I/O itself;
 * the service performs HTTP to the OTP provider.
 * </p>
 */
@Data
public class OtpUser {
	/**
	 * User identifier that will receive the OTP; must be non-blank.
	 */
	private String userId;
	/**
	 * Delivery channels such as {@code email} or {@code phone}; must be non-empty.
	 */
	private List<String> otpChannel;
	/**
	 * MOSIP application identifier requesting the OTP; must be non-blank.
	 */
	private String appId;
	/**
	 * Type of {@code userId} (for example UIN, VID, or USERID); may be null.
	 */
	private String useridtype;
	/**
	 * Extra placeholders for the OTP notification template; may be null or empty.
	 */
	private Map<String, Object> templateVariables;
	/**
	 * Calling context label used in templates or audit; may be null.
	 */
	private String context;
}
