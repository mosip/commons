package io.mosip.kernel.masterdata.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Range;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.mosip.kernel.masterdata.validator.StringFormatter;
import io.mosip.kernel.masterdata.validator.ValidLangCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Response dto for Device Detail
 * 
 * @author Megha Tanga
 * @author Sidhant Agarwal
 * @since 1.0.0
 *
 */

@Data
public class DeviceDto {

	/**
	 * Field for device id
	 */
	@ApiModelProperty(value = "id", required = false, dataType = "java.lang.String")
	private String id;
	/**
	 * Field for device name
	 */
	@NotNull
	@StringFormatter(min = 1, max = 64)
	@ApiModelProperty(value = "name", required = true, dataType = "java.lang.String")
	private String name;
	/**
	 * Field for device serial number
	 */
	@NotNull
	@StringFormatter(min = 1, max = 64)
	@ApiModelProperty(value = "serialNum", required = true, dataType = "java.lang.String")
	private String serialNum;
	/**
	 * Field for device device specification Id
	 */
	@NotNull
	@StringFormatter(min = 1, max = 36)
	@ApiModelProperty(value = "deviceSpecId", required = true, dataType = "java.lang.String")
	private String deviceSpecId;
	/**
	 * Field for device mac address
	 */
	@NotNull
	@StringFormatter(min = 1, max = 64)
	@ApiModelProperty(value = "macAddress", required = true, dataType = "java.lang.String")
	private String macAddress;
	/**
	 * Field for device ip address
	 */

	@Size(min = 0, max = 17)
	@ApiModelProperty(value = "ipAddress", required = true, dataType = "java.lang.String")
	private String ipAddress;
	/**
	 * Field for language code
	 */
	@ValidLangCode(message = "Language Code is Invalid")
	@ApiModelProperty(value = "langCode", required = true, dataType = "java.lang.String")
	private String langCode;
	/**
	 * Field for is active
	 */
	@NotNull
	private Boolean isActive;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime validityDateTime;

	@StringFormatter(min = 0, max = 36)
	private String zoneCode;
	
	@StringFormatter(min = 0, max = 10)
	private String regCenterId;

}