package io.mosip.kernel.masterdata.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.mosip.kernel.masterdata.validator.StringFormatter;
import io.mosip.kernel.masterdata.validator.registereddevice.ValidType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Innner Json for DigitalId
 * 
 * @author Srinivasan
 * @author Megha Tanga
 * @since 1.0.0
 *
 */
@Data
public class DigitalIdDto {

	/** The serial no. */

	@NotNull
	@StringFormatter(min = 1, max = 64)
	@ApiModelProperty(value = "serialNo", required = true, dataType = "java.lang.String")
	private String serialNo;

	/** The Device Provider Name. */
	@NotNull
	@StringFormatter(min = 1, max = 128)
	@ApiModelProperty(value = "dp", required = true, dataType = "java.lang.String")
	private String dp;

	@NotNull
	@StringFormatter(min = 1, max = 36)
	@ApiModelProperty(value = "dpId", required = true, dataType = "java.lang.String")
	private String dpId;

	/** The make. */

	@NotNull
	@StringFormatter(min = 1, max = 36)
	@ApiModelProperty(value = "make", required = true, dataType = "java.lang.String")
	private String make;

	/** The model. */

	@NotNull
	@StringFormatter(min = 1, max = 36)
	@ApiModelProperty(value = "mpdel", required = true, dataType = "java.lang.String")
	private String model;

	/** type *//*
				 * @ApiModelProperty(value = "type", dataType = "java.lang.String") private
				 * String type;
				 * 
				 * /** type
				 */
	@NotBlank
	@ApiModelProperty(value = "deviceSubType", dataType = "java.lang.String")
	private String deviceSubType;
	// @ValidType(message = "Type Value is Invalid")
	@NotBlank
	@ApiModelProperty(value = "type", dataType = "java.lang.String")
	private String type;

	/** The date time. */

	private String dateTime;

}
