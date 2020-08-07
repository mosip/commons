package io.mosip.kernel.bioextractor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BioExtractNotifyRequestDTO {
	
	private String promiseId;
	
	private String resourceURL;

}
