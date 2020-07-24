package io.mosip.kernel.masterdata.dto;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class DeRegisterDevicePostDto {
	@NotBlank
	private String device;
}
