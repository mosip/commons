package io.mosip.kernel.biometrics.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * The Class QualityScore.
 * 
 * @author Sanjay Murali
 */
@Data
public class QualityScore {
	
	/** The score - 0 - 100 score that represents quality as a percentage */
	private float score;	
	private List<String> errors;
	/** The analytics info - detailed breakdown and other information */
	private Map<String, String> analyticsInfo;
	
	public QualityScore() {
		this.score = 0;
		this.errors = new ArrayList<>();
		this.analyticsInfo = new HashMap<String, String>();
	}
}