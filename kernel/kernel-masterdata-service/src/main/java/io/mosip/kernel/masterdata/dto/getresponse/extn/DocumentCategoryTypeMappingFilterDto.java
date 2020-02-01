package io.mosip.kernel.masterdata.dto.getresponse.extn;

import io.mosip.kernel.masterdata.validator.FilterType;
import io.mosip.kernel.masterdata.validator.FilterTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
//@ApiModel(value = "DocumentCategoryTypeMapping", description = "DocumentCategoryTypeMapping resource representation")
public class DocumentCategoryTypeMappingFilterDto {

	@FilterType(types = { FilterTypeEnum.EQUALS, FilterTypeEnum.STARTSWITH, FilterTypeEnum.CONTAINS })
	private String docCategoryCode;

	private String description;

	private DocumentTypeExtnDto documentType;

	private Boolean isActive;

	private String langCode;

	private String name;

}