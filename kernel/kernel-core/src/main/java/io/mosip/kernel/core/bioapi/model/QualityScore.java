package io.mosip.kernel.core.bioapi.model;

import lombok.Data;

/**
 * The Class QualityScore.
 * 
 * @author Sanjay Murali
 */
@Data
public class QualityScore {
	float score; // 0 - 100 score that represents quality as a percentage
	KeyValuePair[] analyticsInfo; // detailed breakdown and other information
}