/**
 * 
 */
package io.mosip.kernel.masterdata.dto.registerdevice;

import javax.validation.constraints.Size;

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
	@StringFormatter
	@Size(min = 1, max = 64)
	@ApiModelProperty(value = "serialNumber", required = true, dataType = "java.lang.String")
	private String serialNo;

	/** The Device Provider Name. */
	@StringFormatter
	@Size(min = 1, max = 128)
	@ApiModelProperty(value = "providerName", required = true, dataType = "java.lang.String")
	private String deviceProvider;

	/** The Device Provider id. */
	@StringFormatter
	@Size(min = 1, max = 36)
	@ApiModelProperty(value = "providerId", required = true, dataType = "java.lang.String")
	private String deviceProviderId;

	/** The make. */
	@StringFormatter
	@Size(min = 1, max = 36)
	@ApiModelProperty(value = "make", required = true, dataType = "java.lang.String")
	private String make;

	/** The model. */
	@StringFormatter
	@Size(min = 1, max = 36)
	@ApiModelProperty(value = "model", required = true, dataType = "java.lang.String")
	private String model;

	/** The date time. */
	private String dateTime;

	/**
	 * Field for deviceTypeCode
	 */
	@StringFormatter
	@Size(min = 1, max = 36)
	@ApiModelProperty(value = "type", required = true, dataType = "java.lang.String")
	private String type;

	/**
	 * Field for deviceSubTypeCode
	 */
	@StringFormatter
	@Size(min = 1, max = 36)
	@ApiModelProperty(value = "subType", required = true, dataType = "java.lang.String")
	private String subType;

}
