package io.mosip.kernel.biosdk.provider.dto;

import io.mosip.kernel.core.bioapi.model.KeyValuePair;
import lombok.Data;


@Data
public class CompositeScore_0_7 {

	private float scaledScore;
	private long internalScore;
	private Score_0_7[] individualScores;
	private KeyValuePair[] analyticsInfo;
	
}
