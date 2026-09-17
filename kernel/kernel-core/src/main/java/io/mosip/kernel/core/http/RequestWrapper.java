package io.mosip.kernel.core.http;

import java.time.LocalDateTime;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Standard MOSIP HTTP request envelope wrapping a typed payload.
 * <p>
 * Contract: used as the JSON body of MOSIP REST APIs. {@code request} is
 * required and bean-validated. {@code id} and {@code version} identify the
 * API; {@code requesttime} is typically UTC. Does not perform I/O.
 * </p>
 *
 * @param <T> type of the inner request payload
 */
@Data
public class RequestWrapper<T> {
	/**
	 * API identifier as published in the service contract; may be null if the
	 * caller omitted it.
	 */
	private String id;
	/**
	 * API version string such as {@code v1}; may be null.
	 */
	private String version;
	/**
	 * Request timestamp in UTC; required by most MOSIP APIs.
	 */
	@ApiModelProperty(notes = "Request Timestamp", example = "2018-12-10T06:12:52.994Z", required = true)
	// @JsonFormat(shape = JsonFormat.Shape.STRING, pattern =
	// "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime requesttime;

	/**
	 * Optional opaque metadata; may be null.
	 */
	private Object metadata;

	/**
	 * Typed request payload; required and recursively validated.
	 */
	@NotNull
	@Valid
	private T request;
}
