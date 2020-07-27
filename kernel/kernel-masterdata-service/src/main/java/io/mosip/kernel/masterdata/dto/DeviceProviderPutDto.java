package io.mosip.kernel.masterdata.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Range;

import io.mosip.kernel.masterdata.validator.StringFormatter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "Device Provider", description = "Device Provider Detail resource")
public class DeviceProviderPutDto {

	/** The id. */
	@NotNull
	@StringFormatter(min = 1, max = 36)
	@ApiModelProperty(value = "id", required = true, dataType = "java.lang.String")
	private String id;

	/** The vendor name. */
	@NotNull
	@StringFormatter(min = 1, max = 128)
	@ApiModelProperty(value = "vendorName", required = true, dataType = "java.lang.String")
	private String vendorName;

	/** The address. */
	@Size(min = 0, max = 512)
	@ApiModelProperty(value = "address", dataType = "java.lang.String")
	private String address;

	/** The email. */
	@Size(min = 0, max = 256)
	@ApiModelProperty(value = "email", dataType = "java.lang.String")
	private String email;

	/** The contact number. */
	@Size(min = 0, max = 16)
	@ApiModelProperty(value = "contactNumber", dataType = "java.lang.String")
	private String contactNumber;

	/** The certificate alias. */
	@Size(min = 0, max = 36)
	@ApiModelProperty(value = "certificateAlias", dataType = "java.lang.String")
	private String certificateAlias;

	/**
	 * Field for is active
	 */
	@NotNull
	private Boolean isActive;

}