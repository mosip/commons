/**
 * 
 */
package io.mosip.kernel.masterdata.dto.registerdevice;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Ramadurai Pandian
 *
 */
@Data
public class DigitalId {

	/** The serial no. */
	@NotBlank
	@Size(min = 0, max = 64)
	@ApiModelProperty(value = "serialNumber", required = true, dataType = "java.lang.String")
	private String serialNo;

	/** The Device Provider Name. */
	@NotBlank
	@Size(min = 0, max = 128)
	@ApiModelProperty(value = "providerName", required = true, dataType = "java.lang.String")
	private String deviceProvider;

	/** The Device Provider id. */
	@NotBlank
	@Size(min = 0, max = 36)
	@ApiModelProperty(value = "providerId", required = true, dataType = "java.lang.String")
	private String deviceProviderId;

	/** The make. */
	@NotBlank
	@Size(min = 0, max = 36)
	@ApiModelProperty(value = "make", required = true, dataType = "java.lang.String")
	private String make;

	/** The model. */
	@NotBlank
	@Size(min = 0, max = 36)
	@ApiModelProperty(value = "model", required = true, dataType = "java.lang.String")
	private String model;

	/** The date time. */
	private String dateTime;
	
	/**
	 * Field for deviceTypeCode
	 */
	@NotBlank
	@Size(min = 0, max = 36)
	@ApiModelProperty(value = "type", required = true, dataType = "java.lang.String")
	private String type;

	/**
	 * Field for deviceSubTypeCode
	 */
	@NotBlank
	@Size(min = 1, max = 36)
	@ApiModelProperty(value = "subType", required = true, dataType = "java.lang.String")
	private String subType;



}
