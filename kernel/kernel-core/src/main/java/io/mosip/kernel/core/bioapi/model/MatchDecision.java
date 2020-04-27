package io.mosip.kernel.core.bioapi.model;

import lombok.Data;

/**
 * The Class Score.
 * 
 * @author Sanjay Murali
 */
@Data
public class MatchDecision {
	boolean match; // true or false indicates matchers decision
	private KeyValuePair[] analyticsInfo; // detailed breakdown and other information
}
