package io.mosip.kernel.masterdata.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.mosip.kernel.masterdata.validator.OptionalStringFormatter;
import io.mosip.kernel.masterdata.validator.StringFormatter;
import io.mosip.kernel.masterdata.validator.ValidLangCode;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Request dto to create Machine
 * 
 * @author Megha Tanga
 * @author Ramadurai Pandian
 * @since 1.0.0
 *
 */

@Data
// @ApiModel(value = "Machine", description = "Machine Detail resource")
public class MachinePostReqDto {

	/**
	 * Field for machine id
	 */

	private String id;
	/**
	 * Field for machine name
	 */
	@NotNull
	@StringFormatter(min = 1, max = 64)
	@ApiModelProperty(value = "name", required = true, dataType = "java.lang.String")
	private String name;
	/**
	 * Field for machine serial number
	 */
	@OptionalStringFormatter( max = 64)
	@ApiModelProperty(value = "serialNum", required = false, dataType = "java.lang.String")
	private String serialNum;
	/**
	 * Field for machine mac address
	 */
	@OptionalStringFormatter( max = 64)
	@ApiModelProperty(value = "macAddress", required = true, dataType = "java.lang.String")
	private String macAddress;
	/**
	 * Field for machine IP address
	 */

	@NotNull
	@Size(max = 17)
	@ApiModelProperty(value = "ipAddress", required = true, dataType = "java.lang.String")
	private String ipAddress;
	/**
	 * Field for machine specification Id
	 */
	@NotNull
	@StringFormatter(min = 1, max = 36)
	@ApiModelProperty(value = "machineSpecId", required = true, dataType = "java.lang.String")
	private String machineSpecId;
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
	/**
	 * Field for is validity of the Device
	 */
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime validityDateTime;

	@NotNull
	@StringFormatter(min = 1, max = 36)
	@ApiModelProperty(value = "zoneCode", required = true, dataType = "java.lang.String")
	private String zoneCode;
	
	@StringFormatter(min = 0, max = 10)
	@ApiModelProperty(value = "regCenterId",  dataType = "java.lang.String")
	private String regCenterId;
	
	@ApiModelProperty(value = "publicKey", required = false, dataType = "java.lang.String")
	private String publicKey;

	@ApiModelProperty(value = "signPublicKey", required = false, dataType = "java.lang.String")
	private String signPublicKey;

}
