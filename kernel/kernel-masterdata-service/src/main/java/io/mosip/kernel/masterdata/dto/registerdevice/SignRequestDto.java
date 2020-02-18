package io.mosip.kernel.masterdata.dto.registerdevice;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignRequestDto {
	@NotBlank
	private String data;

}
