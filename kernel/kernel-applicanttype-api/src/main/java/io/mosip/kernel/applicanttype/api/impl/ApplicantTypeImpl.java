package io.mosip.kernel.applicanttype.api.impl;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.json.JSONException;
import org.json.JSONObject;
import org.mvel2.MVEL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.applicanttype.api.constant.ApplicantTypeErrorCode;
import io.mosip.kernel.core.applicanttype.exception.InvalidApplicantArgumentException;
import io.mosip.kernel.core.applicanttype.spi.ApplicantType;

/**
 * Implementation for Applicant Type.
 * 
 * @author Bal Vikash Sharma
 *
 */
@Component
public class ApplicantTypeImpl implements ApplicantType {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicantTypeImpl.class);

	@Value("${mosip.regproc.packet.classifier.tagging.agegroup.ranges:{'INFANT':'0-5','MINOR':'6-17','ADULT':'18-200'}}")
	private String ageGroups;

	@Value("${mosip.kernel.config.server.file.storage.uri:https://localhost/config/}")
	private String configServerFileStorageURL;

	@Value("${mosip.kernel.applicantType.mvel.file:applicanttype.mvel}")
	private String mvelFile;

	@Autowired
	private RestTemplate restTemplate;

	
	private String script = "";

	@PostConstruct
	private String getScript() {
			script = restTemplate.getForObject(configServerFileStorageURL + mvelFile, String.class);
	        return script;
	}

	/**
	 *
	 * @param map contains attribute and its value
	 * @return
	 * @throws InvalidApplicantArgumentException
	 */
	@SuppressWarnings("unchecked")
	@Override
	public String getApplicantType(Map<String, Object> map) throws InvalidApplicantArgumentException {
		Map<String, Object> context = new HashMap<>();
		try {
			Map<String, String> ageGroupsMap = new HashMap<>();
			JSONObject ageGroupConfig = new JSONObject(ageGroups);
			ageGroupConfig.keys().forEachRemaining(key -> {
				try {
					ageGroupsMap.put((String) key, ageGroupConfig.getString((String) key));
				} catch (JSONException e) {
					LOGGER.error("Failed to parse age groups configuration", e);
				}
			});
			context.put("ageGroups", ageGroupsMap);
		} catch (JSONException e) {
			LOGGER.error("Failed to parse age groups configuration", e);
		}

		MVEL.eval(script, context);
		context.put("identity", map);
		final String code = MVEL.eval("return getApplicantType();", context, String.class);
		LOGGER.info("Evaluated applicant code : {}", code);

		switch (code) {
		case "KER-MSD-151":
			throw new InvalidApplicantArgumentException(
					ApplicantTypeErrorCode.INVALID_DATE_DOB_EXCEED_EXCEPTION.getErrorCode(),
					ApplicantTypeErrorCode.INVALID_DATE_DOB_EXCEED_EXCEPTION.getErrorMessage());
		case "KER-MSD-147":
			throw new InvalidApplicantArgumentException(ApplicantTypeErrorCode.INVALID_QUERY_EXCEPTION.getErrorCode(),
					ApplicantTypeErrorCode.INVALID_QUERY_EXCEPTION.getErrorMessage());
		default:
			return code;
		}
	}
}
