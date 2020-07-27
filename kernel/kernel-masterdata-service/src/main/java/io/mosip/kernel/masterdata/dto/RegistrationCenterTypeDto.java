package io.mosip.kernel.masterdata.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Range;

import io.mosip.kernel.masterdata.entity.RegistrationCenterType;
import io.mosip.kernel.masterdata.validator.StringFormatter;
import io.mosip.kernel.masterdata.validator.ValidLangCode;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO class for {@link RegistrationCenterType}.
 * 
 * @author Sagar Mahapatra
 * @since 1.0.0
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationCenterTypeDto {
	/**
	 * code of the registration center type.
	 */
	@NotNull
	@StringFormatter(min = 1, max = 36)
	private String code;
	/**
	 * language code of the registration center type.
	 */
	@ValidLangCode(message = "Language Code is Invalid")
	private String langCode;
	/**
	 * name of the registration center type.
	 */
	@NotNull
	@StringFormatter(min = 1, max = 64)
	private String name;
	/**
	 * description of the registration center type.
	 */

	@Size(min = 0, max = 128)
	private String descr;
	/**
	 * activeness of the registration center type.
	 */
	@NotNull
	@ApiModelProperty(value = "Application isActive Status", required = true, dataType = "java.lang.Boolean")
	private Boolean isActive;
}
