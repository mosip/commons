package io.mosip.kernel.masterdata.dto;


import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Range;

import io.mosip.kernel.masterdata.validator.StringFormatter;
import io.mosip.kernel.masterdata.validator.ValidLangCode;
import lombok.Data;

/**
 * Blacklisted word DTO.
 * 
 * @author Abhishek Kumar
 * @since 1.0.0
 */
@Data
public class BlacklistedWordsDto {
 
 
	@NotNull
	@StringFormatter(min = 1, max = 128)
	private String word;

	@ValidLangCode(message = "Language Code is Invalid")
	private String langCode;

	@Size(min = 0, max = 256)
	private String description;

	@NotNull
	private Boolean isActive;
}
