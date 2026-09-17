package io.mosip.kernel.core.packetuploader.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception thrown when the SFTP server cannot be reached.
 * <p>
 * Contract: wraps network, timeout, or handshake failures from
 * {@link io.mosip.kernel.core.packetuploader.spi.PacketUploader#createSFTPChannel(Object)}.
 * </p>
 *
 * @author Urvil Joshi
 * @since 1.0.0
 */
public class ConnectionException extends BaseUncheckedException {

	/**
	 * Unique id for serialization
	 */
	private static final long serialVersionUID = 3585613514626311385L;

	/**
	 * Constructor with errorCode, errorMessage, and rootCause
	 * 
	 * @param errorCode    The error code for this exception
	 * @param errorMessage The error message for this exception
	 * @param cause        Cause of this exception
	 */
	public ConnectionException(String errorCode, String errorMessage, Throwable cause) {
		super(errorCode, errorMessage, cause);
	}

}
