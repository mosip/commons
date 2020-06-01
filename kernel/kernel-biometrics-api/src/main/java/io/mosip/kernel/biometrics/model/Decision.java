package io.mosip.kernel.biometrics.model;

import java.util.List;

import io.mosip.kernel.biometrics.constant.Match;
import lombok.Data;

@Data
public class Decision {
	
	private Match match;
	private List<String> errors;

}
