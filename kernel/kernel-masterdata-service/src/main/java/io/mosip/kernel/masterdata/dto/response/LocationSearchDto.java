package io.mosip.kernel.masterdata.dto.response;

import javax.persistence.Column;

import io.mosip.kernel.masterdata.dto.getresponse.extn.BaseDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class LocationSearchDto extends BaseDto {

	@ApiModelProperty(value = "code", required = true, dataType = "java.lang.String")
	private String code;
	
	@ApiModelProperty(value = "name", required = true, dataType = "java.lang.String")
	private String name;
	
	@ApiModelProperty(value = "region", required = true, dataType = "java.lang.String")
	private String region;

	@ApiModelProperty(value = "province", required = true, dataType = "java.lang.String")
	private String province;

	@ApiModelProperty(value = "city", required = true, dataType = "java.lang.String")
	private String city;
	@ApiModelProperty(value = "hierarchyLevel", required = true, dataType = "java.lang.Integer")
	private short hierarchyLevel;

	@ApiModelProperty(value = "hierarchyName", required = true, dataType = "java.lang.String")
	private String hierarchyName;
	
	@ApiModelProperty(value = "langCode", required = true, dataType = "java.lang.String")
	private String langCode;
	
	@ApiModelProperty(value = "zone", required = true, dataType = "java.lang.String")
	private String zone;
	
	@ApiModelProperty(value = "parentLocCode", required = true, dataType = "java.lang.String")
	private String parentLocCode;
	
	@ApiModelProperty(value = "postalCode", required = true, dataType = "java.lang.String")
	private String postalCode;

}
