package io.mosip.kernel.masterdata.dto.getresponse;

import java.time.LocalDateTime;
import java.util.List;

import io.mosip.kernel.masterdata.dto.DynamicFieldValueDto;
import lombok.Data;

@Data
public class DynamicFieldResponseDto {
	
	private String id;	
	private String name;	
	private String langCode;	
	private String dataType;
	private String description;
	private List<DynamicFieldValueDto> fieldVal;
	private boolean isActive;
	private String createdBy;
	private String updatedBy;
	private LocalDateTime createdOn;
	private LocalDateTime updatedOn;

}
