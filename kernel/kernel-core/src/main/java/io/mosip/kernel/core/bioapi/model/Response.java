package io.mosip.kernel.core.bioapi.model;

import lombok.Data;

@Data
public class Response<T> {
	private Integer statusCode;
	private String statusMessage;
	private T response;
}
