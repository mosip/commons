package io.mosip.kernel.core.bioapi.model;

import lombok.Data;

/**
 * Generic Bio API envelope carrying an HTTP-like status and a typed body.
 * <p>
 * Contract: returned by every {@link io.mosip.kernel.core.bioapi.spi.IBioApi}
 * method. {@code statusCode} follows HTTP semantics (2xx success).
 * {@code response} may be null when the call failed. Does not perform I/O.
 * </p>
 *
 * @param <T> type of the success payload
 * @author Manoj SP
 */
@Data
public class Response<T> {
	
	/**
	 * HTTP-like status code; typically 200 on success; may be null if unset.
	 */
	private Integer statusCode;
	
	/**
	 * Human-readable status description; may be null or empty.
	 */
	private String statusMessage;
	
	/**
	 * Operation payload; null when the call failed.
	 */
	private T response;
}
