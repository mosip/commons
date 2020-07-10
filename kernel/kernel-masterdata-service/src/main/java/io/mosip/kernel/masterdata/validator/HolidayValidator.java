package io.mosip.kernel.masterdata.validator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.kernel.masterdata.constant.RequestErrorCode;
import io.mosip.kernel.masterdata.constant.ValidLangCodeErrorCode;
import io.mosip.kernel.masterdata.dto.HolidayDto;
import io.mosip.kernel.masterdata.dto.HolidayUpdateDto;
import io.mosip.kernel.masterdata.exception.RequestException;

@Component
public class HolidayValidator {
	@Value("${mosip.supported-languages}")
	private String supportedLanguages;
	
	public void validate(HolidayDto request) {
		if( EmptyCheckUtils.isNullEmpty(request.getLocationCode()) || 
				request.getIsActive()==null || request.getHolidayDate()==null || EmptyCheckUtils.isNullEmpty(request.getHolidayName()) 
				|| EmptyCheckUtils.isNullEmpty(request.getLangCode())) {
				throw new RequestException(RequestErrorCode.REQUEST_DATA_NOT_VALID.getErrorCode(), "One or more input parameter(s) is missing");
			}
			
			if( request.getLocationCode().trim().length()>128)
				throw new RequestException(RequestErrorCode.REQUEST_DATA_NOT_VALID.getErrorCode(), "location_code size must be between 1 and 128");
			
			if( request.getHolidayName().trim().length()>64)
				throw new RequestException(RequestErrorCode.REQUEST_DATA_NOT_VALID.getErrorCode(), "holiday_name size must be between 1 and 64");
			
			if( request.getHolidayDesc()!=null && (request.getHolidayDesc().trim().length()<1 || request.getHolidayDesc().trim().length()>128))
				throw new RequestException(RequestErrorCode.REQUEST_DATA_NOT_VALID.getErrorCode(), "holiday_desc size must be between 1 and 128");
			
			if(!isValidLanguage(request.getLangCode()))
				throw new RequestException(RequestErrorCode.REQUEST_DATA_NOT_VALID.getErrorCode(), "Language code is invalid");
		
	}

	public void validate(HolidayUpdateDto request) {
		if(request.getId()==null || EmptyCheckUtils.isNullEmpty(request.getLocationCode()) || 
			request.getIsActive()==null || request.getHolidayDate()==null || EmptyCheckUtils.isNullEmpty(request.getHolidayName()) 
			|| EmptyCheckUtils.isNullEmpty(request.getLangCode())) {
			throw new RequestException(RequestErrorCode.REQUEST_DATA_NOT_VALID.getErrorCode(), "One or more input parameter(s) is missing");
		}
		
		if( request.getLocationCode().trim().length()>128)
			throw new RequestException(RequestErrorCode.REQUEST_DATA_NOT_VALID.getErrorCode(), "location_code size must be between 1 and 128");
		
		if( request.getHolidayName().trim().length()>64)
			throw new RequestException(RequestErrorCode.REQUEST_DATA_NOT_VALID.getErrorCode(), "holiday_name size must be between 1 and 64");
		
		if( request.getHolidayDesc()!=null && (request.getHolidayDesc().trim().length()<1 || request.getHolidayDesc().trim().length()>128))
			throw new RequestException(RequestErrorCode.REQUEST_DATA_NOT_VALID.getErrorCode(), "holiday_desc size must be between 1 and 128");
		
		if(!isValidLanguage(request.getLangCode()))
			throw new RequestException(RequestErrorCode.REQUEST_DATA_NOT_VALID.getErrorCode(), "Language code is invalid");
	}
	
	public boolean isValidLanguage(String langCode) {
		if (langCode.trim().length() > 3) {
			throw new RequestException(RequestErrorCode.REQUEST_DATA_NOT_VALID.getErrorCode(), "lang_code size must be between 1 and 3");
		} else {
			try {
				String[] langArray = supportedLanguages.split(",");

				for (String string : langArray) {
					if (langCode.equals(string) || langCode.equals("all")) {
						return true;
					}
				}
			} catch (RestClientException e) {
				throw new RequestException(ValidLangCodeErrorCode.LANG_CODE_VALIDATION_EXCEPTION.getErrorCode(),
						ValidLangCodeErrorCode.LANG_CODE_VALIDATION_EXCEPTION.getErrorMessage() + " " + e.getMessage());
			}
			return false;
		}
	}

}
