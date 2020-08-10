package io.mosip.kernel.masterdata.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class DeRegisterDeviceReqDto {
	@NotNull
	@Size(min=1,max=36)
	private String deviceCode;
	@NotNull
	private String env;
}
