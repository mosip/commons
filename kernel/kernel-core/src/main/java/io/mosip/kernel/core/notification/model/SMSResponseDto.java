package io.mosip.kernel.core.notification.model;

import lombok.Data;

/**
 * The DTO class for sms notification response.
 * 
 * @author Urvil Joshi
 * @since 1.0.0
 *
 */
@Data
public class SMSResponseDto {

	/**
	 * Response status.
	 */
	private String status;

	/**
	 * Response message
	 */
	private String message;
}
