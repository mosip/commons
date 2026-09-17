package io.mosip.kernel.core.otpnotification.spi;

/**
 * Sends an OTP notification over configured channels (SMS and/or email).
 * <p>
 * Contract: implementations perform HTTP to notification and OTP services.
 * {@code request} must be non-null with a destination and channel list. Call
 * from auth-manager or registration when an OTP must be delivered.
 * </p>
 *
 * @param <T> notification response type
 * @param <D> notification request type
 * @author Ritesh Sinha
 * @since 1.0.0
 */
public interface OtpNotification<T, D> {

	/**
	 * Sends the OTP notification described by {@code request}.
	 *
	 * @param request never-null OTP notification payload
	 * @return never-null send result
	 */
	public T sendOtpNotification(D request);
}
