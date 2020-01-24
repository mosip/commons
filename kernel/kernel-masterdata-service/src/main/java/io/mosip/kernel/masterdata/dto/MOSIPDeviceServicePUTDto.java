package io.mosip.kernel.masterdata.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModel;
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

	@NotBlank
	@Size(min = 0, max = 36)
	@ApiModelProperty(value = "id", required = true, dataType = "java.lang.String")
	private String id;

	@NotBlank
	@Size(min = 0, max = 64)
	@ApiModelProperty(value = "softwareVersion", required = true, dataType = "java.lang.String")
	private String swVersion;

	@NotBlank
	@Size(min = 0, max = 36)
	@ApiModelProperty(value = "providerId", required = true, dataType = "java.lang.String")
	private String deviceProviderId;

	@NotBlank
	@Size(min = 0, max = 36)
	@ApiModelProperty(value = "deviceTypeCode", required = true, dataType = "java.lang.String")
	private String regDeviceTypeCode;

	@NotBlank
	@Size(min = 0, max = 36)
	@ApiModelProperty(value = "deviceSubCode", required = true, dataType = "java.lang.String")
	private String regDeviceSubCode;

	@NotBlank
	@Size(min = 0, max = 36)
	@ApiModelProperty(value = "make", required = true, dataType = "java.lang.String")
	private String make;

	@NotBlank
	@Size(min = 0, max = 36)
	@ApiModelProperty(value = "model", required = true, dataType = "java.lang.String")
	private String model;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime swCreateDateTime;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime swExpiryDateTime;

	@NotBlank
	@ApiModelProperty(value = "softBinaryHash", required = true, dataType = "java.lang.Byte")
	private String swBinaryHash;
	
	@NotNull
	@ApiModelProperty(value = "isActive", dataType = "java.lang.Boolean")
	private Boolean isActive;
	

}

