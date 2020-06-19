package io.mosip.kernel.biosdk.provider.dto;

import io.mosip.kernel.core.bioapi.model.KeyValuePair;
import lombok.Data;

@Data
public class QualityScore_0_7 {
	
	private long internalScore;	  
	KeyValuePair[] AnalyticsInfo;

}
