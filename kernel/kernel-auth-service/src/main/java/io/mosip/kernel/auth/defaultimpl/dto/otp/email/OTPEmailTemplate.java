/**
 * 
 */
package io.mosip.kernel.auth.defaultimpl.dto.otp.email;

import lombok.Data;

/**
 * Rendered OTP email ready to post as multipart fields ({@code mailTo},
 * {@code mailSubject}, {@code mailContent}) to the kernel email notification API.
 *
 * @author Ramadurai Pandian
 *
 */
@Data
public class OTPEmailTemplate {

	/**
	 * Email subject line after template substitution.
	 */
	private String emailSubject;

	/**
	 * Email HTML or text body after template substitution, including the OTP.
	 */
	private String emailContent;

	/**
	 * Destination address used when sending the mail.
	 */
	private String emailTo;

}
