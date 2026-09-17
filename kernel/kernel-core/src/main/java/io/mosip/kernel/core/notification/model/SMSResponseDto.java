package io.mosip.kernel.core.notification.model;

import lombok.Data;

/**
 * Acknowledgement returned after an SMS send attempt.
 * <p>
 * Contract: returned by
 * {@link io.mosip.kernel.core.notification.spi.SMSServiceProvider#sendSms}.
 * Fields may be null if the vendor omitted them. Does not perform I/O.
 * </p>
 *
 * @author Urvil Joshi
 * @since 1.0.0
 */
@Data
public class SMSResponseDto {

	/**
	 * Vendor status such as {@code success} or {@code failure}; may be null.
	 */
	private String status;

	/**
	 * Human-readable vendor message; may be null or empty.
	 */
	private String message;
}
