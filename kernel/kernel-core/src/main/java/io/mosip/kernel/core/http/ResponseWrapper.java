package io.mosip.kernel.core.http;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.mosip.kernel.core.exception.ServiceError;
import lombok.Data;

/**
 * Standard MOSIP HTTP response envelope wrapping a typed payload and errors.
 * <p>
 * Contract: used as the JSON body of MOSIP REST APIs. {@code response} is
 * present on success; {@code errors} is non-empty on failure.
 * {@code responsetime} defaults to UTC now. Does not perform I/O.
 * </p>
 *
 * @param <T> type of the inner success payload
 * @see ServiceError
 */
@Data
public class ResponseWrapper<T> {
	/**
	 * API identifier echoing the request; may be null.
	 */
	private String id;
	/**
	 * API version echoing the request; may be null.
	 */
	private String version;
	/**
	 * Response timestamp in UTC, formatted as {@code yyyy-MM-dd'T'HH:mm:ss.SSS'Z'}.
	 */
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime responsetime = LocalDateTime.now(ZoneId.of("UTC"));
	/**
	 * Optional opaque metadata; may be null.
	 */
	private Object metadata;
	/**
	 * Typed success payload; null when {@code errors} is populated.
	 */
	@NotNull
	@Valid
	private T response;

	/**
	 * Service errors; never null, empty on success.
	 */
	private List<ServiceError> errors = new ArrayList<>();

}
