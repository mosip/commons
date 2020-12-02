package io.mosip.kernel.masterdata.service.impl;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.core.util.JsonUtils;
import io.mosip.kernel.masterdata.constant.ApplicationErrorCode;
import io.mosip.kernel.masterdata.dto.getresponse.ApplicationConfigResponseDto;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;
import io.mosip.kernel.masterdata.service.ApplicationConfigService;
import io.mosip.kernel.masterdata.utils.ExceptionUtils;
@Component
public class ApplicationConfigServiceImpl implements ApplicationConfigService {
	@Value("${mosip.primary-language}")
	private String primaryLangCode;

	@Value("${mosip.secondary-language}")
	private String secondaryLang;
	
	@Value("${aplication.configuration.level.version}")
	private String appConfigVersion;
	
	
	
	@Override
	public ApplicationConfigResponseDto getLanguageConfigDetails() {
		ApplicationConfigResponseDto dto=new ApplicationConfigResponseDto();
		
		dto.setPrimaryLangCode(primaryLangCode);
		dto.setSecondaryLangCode(secondaryLang);
		dto.setVersion(appConfigVersion);
		
		
		return dto;
	}

}
