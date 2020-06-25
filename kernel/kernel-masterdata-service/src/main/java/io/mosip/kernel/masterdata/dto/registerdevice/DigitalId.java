/**
 * 
 */
package io.mosip.kernel.masterdata.dto.registerdevice;

import java.time.LocalDateTime;

import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.mosip.kernel.masterdata.validator.StringFormatter;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Ramadurai Pandian
 *
 */
@Data
public class DigitalId {

	/** The serial no. */
	@StringFormatter(min = 1, max = 64)
	@ApiModelProperty(value = "serialNumber", required = true, dataType = "java.lang.String")
	private String serialNo;

	/** The Device Provider Name. */
	@StringFormatter(min = 1, max = 128)
	@ApiModelProperty(value = "providerName", required = true, dataType = "java.lang.String")
	private String deviceProvider;

	/** The Device Provider id. */
	@StringFormatter(min = 1, max = 36)
	@ApiModelProperty(value = "providerId", required = true, dataType = "java.lang.String")
	private String deviceProviderId;

	/** The make. */
	@StringFormatter(min = 1, max = 36)
	@ApiModelProperty(value = "make", required = true, dataType = "java.lang.String")
	private String make;

	/** The model. */
	@StringFormatter(min = 1, max = 36)
	@ApiModelProperty(value = "model", required = true, dataType = "java.lang.String")
	private String model;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime dateTime;

	/**
	 * Field for deviceTypeCode
	 */
	@StringFormatter(min = 1, max = 36)
	@ApiModelProperty(value = "type", required = true, dataType = "java.lang.String")
	private String type;

	/**
	 * Field for deviceSubTypeCode
	 */
	@StringFormatter(min = 1, max = 36)
	@ApiModelProperty(value = "deviceSubType", required = true, dataType = "java.lang.String")
	private String deviceSubType;

}
