package io.mosip.kernel.masterdata.service;

import io.mosip.kernel.masterdata.dto.request.RequestDTO;
import io.mosip.kernel.masterdata.dto.response.ResponseDTO;

public interface ApplicantTypeService {

	/**
	 * This method return the applicant id.
	 * 
	 * @param dto Request dto
	 * @return applicant id
	 */
	public ResponseDTO getApplicantType(RequestDTO dto);

}
