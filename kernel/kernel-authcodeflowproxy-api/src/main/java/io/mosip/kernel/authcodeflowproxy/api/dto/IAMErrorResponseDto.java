package io.mosip.kernel.authcodeflowproxy.api.dto;

import lombok.Data;

@Data
public class IAMErrorResponseDto {

	/** The error. */
	private String error;

	/** The error description. */
	private String error_description;
}