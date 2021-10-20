package io.mosip.kernel.applicanttype.api.impl;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.mvel2.MVEL;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.integration.impl.MapVariableResolverFactory;
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

	@Value("${mosip.kernel.applicant.type.age.limit}")
	private String ageLimit;

	@Value("${mosip.kernel.config.server.file.storage.uri}")
	private String configServerFileStorageURL;

	@Value("${mosip.kernel.applicantType.mvel.file}")
	private String mvelFile;

	@Autowired
	private RestTemplate restTemplate;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.applicanttype.spi.ApplicantCodeService#getApplicantType(
	 * java.util.Map)
	 */
	public String getApplicantType(Map<String, Object> m) throws InvalidApplicantArgumentException {
		LOGGER.info("Getting code for applicant type");
		try {
			LOGGER.info("Getting data from MVEL file->"+configServerFileStorageURL + mvelFile);
			String mvelExpression = restTemplate.getForObject(configServerFileStorageURL + mvelFile, String.class);

			Map<String, Object> context = new HashMap();

			context.put("map", m);
			context.put("agelimit", ageLimit == null ? "18" : ageLimit);

			VariableResolverFactory functionFactory = new MapVariableResolverFactory();
			MVEL.eval(mvelExpression, context, functionFactory);

			VariableResolverFactory myVarFactory = new MapVariableResolverFactory();
			myVarFactory.setNextFactory(functionFactory);

			Serializable s = MVEL.compileExpression("getApplicantType(map,agelimit);", context);

			String code = (String) MVEL.executeExpression(s, m, myVarFactory);

			if (code.equalsIgnoreCase("KER-MSD-147")) {
				LOGGER.error("Error while fetching applicant code");
				throw new InvalidApplicantArgumentException(
						ApplicantTypeErrorCode.INVALID_QUERY_EXCEPTION.getErrorCode(),
						ApplicantTypeErrorCode.INVALID_QUERY_EXCEPTION.getErrorMessage());
			}
			LOGGER.info("Code for applicant type  is " + code);
			return code;
		} catch (Exception ex) {
			LOGGER.error("Error while executing mvel file" + ex.getMessage());
			throw ex;
		}
		
	}

}