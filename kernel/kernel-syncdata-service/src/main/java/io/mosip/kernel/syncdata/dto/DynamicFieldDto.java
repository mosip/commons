package io.mosip.kernel.syncdata.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DynamicFieldDto extends BaseDto {
	
	private String id;
	private String name;
	private String dataType;
	private List<DynamicFieldValueDto> fieldVal;

}
