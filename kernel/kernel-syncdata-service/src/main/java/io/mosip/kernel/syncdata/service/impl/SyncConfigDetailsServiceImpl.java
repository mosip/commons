package io.mosip.kernel.syncdata.service.impl;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.syncdata.constant.SyncConfigDetailsErrorCode;
import io.mosip.kernel.syncdata.dto.ConfigDto;
import io.mosip.kernel.syncdata.dto.PublicKeyResponse;
import io.mosip.kernel.syncdata.exception.SyncDataServiceException;
import io.mosip.kernel.syncdata.exception.SyncInvalidArgumentException;
import io.mosip.kernel.syncdata.service.SyncConfigDetailsService;
import net.minidev.json.JSONObject;

/**
 * Implementation class
 * 
 * @author Bal Vikash Sharma
 *
 */
@RefreshScope
@Service
public class SyncConfigDetailsServiceImpl implements SyncConfigDetailsService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SyncConfigDetailsServiceImpl.class);

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * Environment instance
	 */
	@Autowired
	private Environment env;

	/**
	 * file name referred from the properties file
	 */
	@Value("${mosip.kernel.syncdata.registration-center-config-file}")
	private String regCenterfileName;

	/**
	 * file name referred from the properties file
	 */
	@Value("${mosip.kernel.syncdata.global-config-file}")
	private String globalConfigFileName;

	/**
	 * URL read from properties file
	 */
	@Value("${mosip.kernel.keymanager-service-publickey-url}")
	private String publicKeyUrl;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.syncdata.service.SyncConfigDetailsService#getConfigDetails()
	 */
	@Override
	public ConfigDto getConfigDetails() {
		LOGGER.info("getConfigDetails() started");
		JSONObject config = new JSONObject();
		JSONObject globalConfig = getConfigDetailsResponse(globalConfigFileName);
		JSONObject regConfig = getConfigDetailsResponse(regCenterfileName);
		config.put("globalConfiguration", globalConfig);
		config.put("registrationConfiguration", regConfig);
		ConfigDto configDto = new ConfigDto();
		configDto.setConfigDetail(config);
		LOGGER.info("getConfigDetails() completed");
		return configDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.syncdata.service.SyncConfigDetailsService#
	 * getGlobalConfigDetails()
	 */
	@Override
	public JSONObject getGlobalConfigDetails() {

		return getConfigDetailsResponse(globalConfigFileName);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.syncdata.service.SyncConfigDetailsService#
	 * getRegistrationCenterConfigDetails(java.lang.String)
	 */
	@Override
	public JSONObject getRegistrationCenterConfigDetails(String regId) {

		return getConfigDetailsResponse(regCenterfileName);

	}

	public ConfigDto getConfiguration(String registrationCenterId) {
		ConfigDto configDto = null;
		configDto = new ConfigDto();
		configDto.setGlobalConfig(getGlobalConfigDetails());
		configDto.setRegistrationCenterConfiguration(getRegistrationCenterConfigDetails(registrationCenterId));
		return configDto;
	}

	/**
	 * This method will consume a REST API based on the filename passed.
	 * 
	 * @param fileName - name of the file
	 * @return JSONObject
	 */
	private JSONObject getConfigDetailsResponse(String fileName) {
		String configServerUri = env.getProperty("spring.cloud.config.uri");
		String configLabel = env.getProperty("spring.cloud.config.label");
		String configProfile = env.getProperty("spring.profiles.active");
		String configAppName = env.getProperty("spring.application.name");
		JSONObject result = null;
		StringBuilder uriBuilder = null;
		if (fileName != null) {
			uriBuilder = new StringBuilder();
			uriBuilder.append(configServerUri + "/").append(configAppName + "/").append(configProfile + "/")
					.append(configLabel + "/").append(fileName);
		} else {
			throw new SyncDataServiceException(
					SyncConfigDetailsErrorCode.SYNC_CONFIG_DETAIL_INPUT_PARAMETER_EXCEPTION.getErrorCode(),
					SyncConfigDetailsErrorCode.SYNC_CONFIG_DETAIL_INPUT_PARAMETER_EXCEPTION.getErrorMessage());
		}
		try {
			String str = restTemplate.getForObject(uriBuilder.toString(), String.class);
			Properties prop = parsePropertiesString(str);
			result = new JSONObject();
			for (Entry<Object, Object> e : prop.entrySet()) {
				result.put(String.valueOf(e.getKey()), e.getValue());
			}
		} catch (RestClientException | IOException e) {
			throw new SyncDataServiceException(
					SyncConfigDetailsErrorCode.SYNC_CONFIG_DETAIL_REST_CLIENT_EXCEPTION.getErrorCode(),
					SyncConfigDetailsErrorCode.SYNC_CONFIG_DETAIL_REST_CLIENT_EXCEPTION.getErrorMessage() + " "
							+ ExceptionUtils.buildMessage(e.getMessage(), e.getCause()));

		}

		return result;

	}

	public Properties parsePropertiesString(String s) throws IOException {
		final Properties p = new Properties();
		p.load(new StringReader(s));
		return p;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.syncdata.service.SyncConfigDetailsService#getPublicKey(java.
	 * lang.String, java.lang.String, java.util.Optional)
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public PublicKeyResponse<String> getPublicKey(String applicationId, String timeStamp, String referenceId) {
		ResponseEntity<String> publicKeyResponseEntity = null;

		ResponseWrapper<PublicKeyResponse> publicKeyResponseMapped = null;
		Map<String, String> uriParams = new HashMap<>();
		uriParams.put("applicationId", applicationId);
		try {
			// Query parameters
			UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(publicKeyUrl)
					// Add query parameter
					.queryParam("referenceId", referenceId).queryParam("timeStamp", timeStamp);

			publicKeyResponseEntity = restTemplate.getForEntity(builder.buildAndExpand(uriParams).toUri(),
					String.class);
			List<ServiceError> validationErrorsList = null;
			validationErrorsList = ExceptionUtils.getServiceErrorList(publicKeyResponseEntity.getBody());

			if (!validationErrorsList.isEmpty()) {
				throw new SyncInvalidArgumentException(validationErrorsList);
			}

		} catch (HttpClientErrorException | HttpServerErrorException ex) {

			throw new SyncDataServiceException(
					SyncConfigDetailsErrorCode.SYNC_CONFIG_DETAIL_REST_CLIENT_EXCEPTION.getErrorCode(),
					SyncConfigDetailsErrorCode.SYNC_CONFIG_DETAIL_REST_CLIENT_EXCEPTION.getErrorMessage() + " "
							+ ExceptionUtils.buildMessage(ex.getMessage(), ex.getCause()));

		}

		try {
			objectMapper.registerModule(new JavaTimeModule());
			publicKeyResponseMapped = objectMapper.readValue(publicKeyResponseEntity.getBody(),
					new TypeReference<ResponseWrapper<PublicKeyResponse<String>>>() {
					});

		} catch (IOException | NullPointerException e) {
			throw new SyncDataServiceException(SyncConfigDetailsErrorCode.SYNC_IO_EXCEPTION.getErrorCode(),
					SyncConfigDetailsErrorCode.SYNC_IO_EXCEPTION.getErrorMessage(), e);
		}

		return publicKeyResponseMapped.getResponse();

	}

}
