package io.mosip.kernel.core.notification.spi;

/**
 * Sends MOSIP email notifications, optionally with attachments.
 * <p>
 * Contract: implementations perform SMTP or HTTP to an email gateway. Call
 * from {@code kernel-notification-service}. {@code mailTo} must be non-empty;
 * {@code mailCc} and {@code attachments} may be null or empty.
 * </p>
 *
 * @param <T> attachment payload type (for example a file list)
 * @param <D> result type returned by the gateway
 */
public interface EmailNotification<T, D> {

	/**
	 * Sends an email to the given recipients.
	 * <p>
	 * Contract: performs SMTP or HTTP. {@code mailTo} must contain at least one
	 * non-blank address. {@code mailSubject} and {@code mailContent} must be
	 * non-null.
	 * </p>
	 *
	 * @param mailTo      never-null, never-empty recipient addresses
	 * @param mailCc      CC addresses; may be null or empty
	 * @param mailSubject never-null subject line; may be empty
	 * @param mailContent never-null body; may be empty
	 * @param attachments optional attachments; may be null
	 * @return gateway result; never null on success (implementation-defined on
	 *         failure)
	 */
	public D sendEmail(String[] mailTo, String[] mailCc, String mailSubject, String mailContent, T attachments);
}
