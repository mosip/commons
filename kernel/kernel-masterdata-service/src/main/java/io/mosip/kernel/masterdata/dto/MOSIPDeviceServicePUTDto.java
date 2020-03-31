package io.mosip.kernel.masterdata.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.mosip.kernel.masterdata.validator.StringFormatter;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 
 * @author Megha Tanga
 *
 */

@Data
//@ApiModel(value = "MOSIP Device Service", description = "MOSIP Device Service Detail resource")
public class MOSIPDeviceServicePUTDto {

	
	@StringFormatter(min = 1, max = 36)
	@ApiModelProperty(value = "id", required = true, dataType = "java.lang.String")
	private String id;

	
	@StringFormatter(min = 1, max = 64)
	@ApiModelProperty(value = "softwareVersion", required = true, dataType = "java.lang.String")
	private String swVersion;

	
	@StringFormatter(min = 1, max = 36)
	@ApiModelProperty(value = "providerId", required = true, dataType = "java.lang.String")
	private String deviceProviderId;

	
	@StringFormatter(min = 1, max = 36)
	@ApiModelProperty(value = "deviceTypeCode", required = true, dataType = "java.lang.String")
	private String regDeviceTypeCode;

	
	@StringFormatter(min = 1, max = 36)
	@ApiModelProperty(value = "deviceSubCode", required = true, dataType = "java.lang.String")
	private String regDeviceSubCode;

	
	@StringFormatter(min = 1, max = 36)
	@ApiModelProperty(value = "make", required = true, dataType = "java.lang.String")
	private String make;

	
	@StringFormatter(min = 1, max = 36)
	@ApiModelProperty(value = "model", required = true, dataType = "java.lang.String")
	private String model;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime swCreateDateTime;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime swExpiryDateTime;

	
	@StringFormatter(min = 1, max = 36)
	@ApiModelProperty(value = "softBinaryHash", required = true, dataType = "java.lang.Byte")
	private String swBinaryHash;

	@NotNull
	@ApiModelProperty(value = "isActive", dataType = "java.lang.Boolean")
	private Boolean isActive;

}
