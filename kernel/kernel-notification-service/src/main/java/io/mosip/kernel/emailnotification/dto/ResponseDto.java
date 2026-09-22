package io.mosip.kernel.emailnotification.dto;

import lombok.Data;

/**
 * Response payload returned by the email send API after an email request is
 * accepted.
 * 
 * @author Sagar Mahapatra
 * @since 1.0.0
 */
@Data
public class ResponseDto {
	/**
	 * Delivery status, typically {@code success} when the request is accepted.
	 */
	private String status;

	/**
	 * Human-readable status message, typically {@code Email Request submitted}.
	 */
	private String message;
}
