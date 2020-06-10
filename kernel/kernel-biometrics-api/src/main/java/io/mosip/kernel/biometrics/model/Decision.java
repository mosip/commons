package io.mosip.kernel.biometrics.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.mosip.kernel.biometrics.constant.Match;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class Decision {
	
	private Match match;
	private List<String> errors;
	private Map<String, String> analyticsInfo;
	
	public Decision() {
		this.errors = new ArrayList<>();
		this.analyticsInfo = new HashMap<String, String>();
	}

}
