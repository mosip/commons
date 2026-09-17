package io.mosip.kernel.applicanttype.api.impl;

import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.PostConstruct;

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
 * Evaluates applicant type from identity attributes using an MVEL script.
 * <p>
 * Loads {@code applicanttype.mvel} from config-server at startup. Age-group
 * ranges are injected into the MVEL context before {@code getApplicantType()}
 * is evaluated. Script return codes {@code KER-MSD-147} and {@code KER-MSD-151}
 * are raised as {@link InvalidApplicantArgumentException}; any other value is
 * the applicant-type code.
 * </p>
 *
 * @author Bal Vikash Sharma
 * @see ApplicantType
 */
@Component
public class ApplicantTypeImpl implements ApplicantType {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicantTypeImpl.class);

	/** JSON map of age-group name to inclusive year range (for example {@code INFANT:0-5}). */
	@Value("${mosip.regproc.packet.classifier.tagging.agegroup.ranges:{'INFANT':'0-5','MINOR':'6-17','ADULT':'18-200'}}")
	private String ageGroups;

	/** Base URI of config-server file storage from which the MVEL script is fetched. */
	@Value("${mosip.kernel.config.server.file.storage.uri:https://localhost/config/}")
	private String configServerFileStorageURL;

	/** File name of the applicant-type MVEL script on config-server. */
	@Value("${mosip.kernel.applicantType.mvel.file:applicanttype.mvel}")
	private String mvelFile;

	@Autowired
	private RestTemplate restTemplate;

	
	private String script = "";

	/**
	 * Fetches the MVEL script from config-server into {@link #script}.
	 *
	 * @return script source as loaded from config-server
	 */
	@PostConstruct
	private String getScript() {
			script = restTemplate.getForObject(configServerFileStorageURL + mvelFile, String.class);
	        return script;
	}

	/**
	 * Resolves the applicant-type code for the given identity attributes.
	 * <p>
	 * Builds an MVEL context with {@code ageGroups} and {@code identity}, then
	 * evaluates {@code getApplicantType()} from the loaded script.
	 * </p>
	 *
	 * @param map identity attributes (for example date of birth, gender, resident status)
	 * @return applicant-type code returned by the script
	 * @throws InvalidApplicantArgumentException if the script reports {@code KER-MSD-147} or {@code KER-MSD-151}
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
