package io.mosip.kernel.masterdata.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Range;

import io.mosip.kernel.masterdata.validator.StringFormatter;
import io.mosip.kernel.masterdata.validator.ValidLangCode;
import lombok.Data;

/**
 * @author Sidhant Agarwal
 * @since 1.0.0
 */
@Data
public class UserDetailsDto {

	@NotNull
	@StringFormatter(min = 1, max = 36)
	private String id;


	@ValidLangCode(message = "Language Code is Invalid")
	private String langCode;

	@Size(min = 1, max = 28)
	private String uin;

	@NotNull
	@StringFormatter(min = 1, max = 64)
	private String name;

	@NotNull
	@StringFormatter(min = 1, max = 16)
	private String email;

	@NotNull
	@StringFormatter(min = 1, max = 16)
	private String mobile;

	@NotNull
	@StringFormatter(min = 1, max = 16)
	private String statusCode;
	
	@NotNull
	@StringFormatter(min = 1, max = 10)
	private String regCenterId;

	@NotNull
	private Boolean isActive;

}
