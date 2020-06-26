package io.mosip.kernel.masterdata.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.mosip.kernel.masterdata.validator.StringFormatter;
import io.mosip.kernel.masterdata.validator.ValidLangCode;
import lombok.Data;

/**
 * 
 * @author Megha Tanga
 *
 */
@Data
public class RegistrationCenterUserDto {

	@NotNull
	@StringFormatter(min = 1, max = 10)
	private String regCenterId;

	@NotNull
	@StringFormatter(min = 1, max = 36)
	private String userId;

	@ValidLangCode(message = "Language Code is Invalid")
	private String langCode;

	@NotNull
	private Boolean isActive;

}
