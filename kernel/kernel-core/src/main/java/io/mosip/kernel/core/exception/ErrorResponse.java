package io.mosip.kernel.core.exception;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * HTTP error envelope carrying a timestamp, status, and typed error list.
 * <p>
 * Contract: {@code timestamp} defaults to epoch millis at construction.
 * {@code errors} is never null (starts empty). Used by REST exception
 * handlers; does not perform I/O.
 * </p>
 *
 * @param <T> error item type, typically {@link ServiceError}
 * @author Bal Vikash Sharma
 * @since 1.0.0
 */
@Data
public class ErrorResponse<T> {
	/**
	 * Epoch milliseconds when this response was created.
	 */
	private long timestamp = Instant.now().toEpochMilli();
	/**
	 * HTTP-like status code such as 400 or 500; {@code 0} if unset.
	 */
	private int status;
	/**
	 * Error items; never null, empty when none are present.
	 */
	private List<T> errors = new ArrayList<>();

}
