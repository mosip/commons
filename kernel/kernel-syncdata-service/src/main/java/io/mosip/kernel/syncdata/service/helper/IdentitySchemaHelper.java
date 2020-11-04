package io.mosip.kernel.syncdata.service.helper;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.syncdata.constant.MasterDataErrorCode;
import io.mosip.kernel.syncdata.dto.DynamicFieldDto;
import io.mosip.kernel.syncdata.dto.IdSchemaDto;
import io.mosip.kernel.syncdata.dto.PageDto;
import io.mosip.kernel.syncdata.dto.response.SyncDataBaseDto;
import io.mosip.kernel.syncdata.exception.SyncDataServiceException;
import io.mosip.kernel.syncdata.exception.SyncInvalidArgumentException;
import io.mosip.kernel.syncdata.utils.SyncMasterDataServiceHelper;



@Component
public class IdentitySchemaHelper {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(IdentitySchemaHelper.class);
	
	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ObjectMapper objectMapper;
	
	@Value("${mosip.kernel.syncdata-service-idschema-url}")
	private String idSchemaUrl;
	
	@Value("${mosip.kernel.syncdata-service-dynamicfield-url}")
	private String dynamicfieldUrl;
	
	@Autowired
	private SyncMasterDataServiceHelper serviceHelper;
	
	public void fillRetrievedData(final List<SyncDataBaseDto> list, String publicKey)
			throws InterruptedException, ExecutionException {
		List<DynamicFieldDto> fields = getAllDynamicFields();
				
		Map<String, List<DynamicFieldDto>> data = new HashMap<String, List<DynamicFieldDto>>();
		for(DynamicFieldDto dto : fields) {
			if(!data.containsKey(dto.getName())) {
				List<DynamicFieldDto> langBasedData = new ArrayList<DynamicFieldDto>();
				langBasedData.add(dto);
				data.put(dto.getName(), langBasedData);
			}
			else
				data.get(dto.getName()).add(dto);			
		}	
		
		for(String key : data.keySet()) {			
			list.add(serviceHelper.getSyncDataBaseDto(key, "dynamic", data.get(key), publicKey));
		}
	}
	
	public IdSchemaDto getLatestIdentitySchema(LocalDateTime lastUpdated, double schemaVersion) {
		try {
			UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(idSchemaUrl);
			builder.queryParam("schemaVersion", schemaVersion);
			ResponseEntity<String> responseEntity = restTemplate.getForEntity(builder.build().toUri(), String.class);
			
			objectMapper.registerModule(new JavaTimeModule());
			ResponseWrapper<IdSchemaDto> resp = objectMapper.readValue(responseEntity.getBody(), 
					new TypeReference<ResponseWrapper<IdSchemaDto>>() {});
			
			if(resp.getErrors() != null && !resp.getErrors().isEmpty())
				throw new SyncInvalidArgumentException(resp.getErrors());			
			
			return resp.getResponse();
		} catch (Exception e) {
			LOGGER.error("Failed to fetch latest schema", e);
			throw new SyncDataServiceException(MasterDataErrorCode.SCHEMA_FETCH_FAILED.getErrorCode(), 
					MasterDataErrorCode.SCHEMA_FETCH_FAILED.getErrorMessage() + " : " + 
							ExceptionUtils.buildMessage(e.getMessage(), e.getCause()));
		}		
	}
	
	@SuppressWarnings("unchecked")
	public List<DynamicFieldDto> getAllDynamicFields() {
		try {
			UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(dynamicfieldUrl);
			ResponseEntity<String> responseEntity = restTemplate.getForEntity(builder.build().toUri(), String.class);
						
			objectMapper.registerModule(new JavaTimeModule());
			ResponseWrapper<PageDto<DynamicFieldDto>> resp = objectMapper.readValue(responseEntity.getBody(), 
					new TypeReference<ResponseWrapper<PageDto<DynamicFieldDto>>>() {});
			
			if(resp.getErrors() != null && !resp.getErrors().isEmpty())
				throw new SyncInvalidArgumentException(resp.getErrors());
			
			PageDto<DynamicFieldDto> pageDto = resp.getResponse();
			return pageDto.getData();
			
		} catch (Exception e) {
			LOGGER.error("Failed to fetch latest schema", e);
			throw new SyncDataServiceException(MasterDataErrorCode.SCHEMA_FETCH_FAILED.getErrorCode(), 
					MasterDataErrorCode.SCHEMA_FETCH_FAILED.getErrorMessage() + " : " + 
							ExceptionUtils.buildMessage(e.getMessage(), e.getCause()));
		}		
	}

}
