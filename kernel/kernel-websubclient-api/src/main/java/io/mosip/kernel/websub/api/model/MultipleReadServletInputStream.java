package io.mosip.kernel.websub.api.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import io.mosip.kernel.websub.api.constants.WebSubClientErrorCode;
import io.mosip.kernel.websub.api.exception.WebSubClientException;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for {@link ServletInputStream} to enable multiple reads of the request body.
 * <p>
 * Provides a reusable input stream from a cached byte array, used by
 * {@link MultipleReadHttpRequest} in conjunction with
 * {@link io.mosip.kernel.websub.api.filter.MultipleReadRequestBodyFilter} for WebSub callback processing
 * (RFC 7033). Integrates with {@link io.mosip.kernel.websub.api.filter.IntentVerificationFilter} for
 * intent verification.
 * </p>
 *
 * @author Urvil Joshi
 * @since 1.0.0
 * @see MultipleReadHttpRequest
 */
public class MultipleReadServletInputStream extends ServletInputStream {

	/**
	 * Logger for debugging and error reporting.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(MultipleReadServletInputStream.class);

	/**
	 * Cached input stream for the request body.
	 */
	private InputStream cachedBodyInputStream;

	/**
	 * Constructs a servlet input stream from a cached byte array.
	 *
	 * @param cachedBody the cached request body
	 */
	public MultipleReadServletInputStream(byte[] cachedBody) {
		this.cachedBodyInputStream = new ByteArrayInputStream(cachedBody);
	}

	/**
	 * Checks if the input stream is finished.
	 *
	 * @return true if no more data is available, false otherwise
	 * @throws WebSubClientException if an I/O error occurs
	 */
	@Override
	public boolean isFinished() {
		try {
			return cachedBodyInputStream.available() == 0;
		} catch (IOException e) {
			LOGGER.error("Error checking if stream is finished: {}", e.getMessage());
/*
			throw new WebSubClientException(WebSubClientErrorCode.IO_ERROR.getErrorCode(),
					WebSubClientErrorCode.IO_ERROR.getErrorMessage() + ": " + e.getMessage(), e);
*/
		}
		return false;
	}

	/**
	 * Checks if the input stream is ready to be read.
	 *
	 * @return true, as the cached stream is always ready
	 */
	@Override
	public boolean isReady() {
		return true;
	}

	/**
	 * Sets a read listener (not supported).
	 *
	 * @param listener the read listener
	 * @throws UnsupportedOperationException always, as listeners are not supported
	 */
	@Override
	public void setReadListener(ReadListener listener) {
		LOGGER.warn("setReadListener is not supported");
		throw new UnsupportedOperationException();
	}

	/**
	 * Reads the next byte from the input stream.
	 *
	 * @return the next byte, or -1 if the end is reached
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	public int read() throws IOException {
		return cachedBodyInputStream.read();
	}
}