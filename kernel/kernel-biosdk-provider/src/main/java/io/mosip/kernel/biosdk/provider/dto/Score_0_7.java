package io.mosip.kernel.biosdk.provider.dto;

import io.mosip.kernel.core.bioapi.model.KeyValuePair;
import lombok.Data;

@Data
public class Score_0_7 {
	
	private float scaleScore;	  
	private float internalScore;
	private KeyValuePair[] analyticsInfo;

}
