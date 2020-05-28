package io.mosip.kernel.biometrics.model;

import lombok.Data;

/**
 * The Class Response.
 *
 * @author Manoj SP
 * @param <T> the generic type
 */
@Data
public class Response<T> {
	
	/** The status code. */
	private Integer statusCode;
	
	/** The status message. */
	private String statusMessage;
	
	/** The response. */
	private T response;
}
