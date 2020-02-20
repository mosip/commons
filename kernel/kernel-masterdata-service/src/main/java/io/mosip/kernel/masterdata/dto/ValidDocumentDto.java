package io.mosip.kernel.masterdata.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.mosip.kernel.masterdata.validator.ValidLangCode;
import lombok.Data;

/**
 * @author Ritesh Sinha
 * @since 1.0.0
 *
 */
@Data

public class ValidDocumentDto {

	@NotBlank
	@Size(min = 1, max = 36)
	private String docTypeCode;

	@NotBlank
	@Size(min = 1, max = 36)
	private String docCategoryCode;

	@ValidLangCode(message = "Language Code is Invalid")
	// @NotBlank
	// @Size(min = 1, max = 3)
	private String langCode;

	@NotNull
	private Boolean isActive;
}
