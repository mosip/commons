package io.mosip.kernel.syncdata.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.syncdata.dto.*;
import io.mosip.kernel.syncdata.dto.response.*;
import io.mosip.kernel.syncdata.exception.*;
import io.mosip.kernel.syncdata.service.helper.KeymanagerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.syncdata.constant.MasterDataErrorCode;
import io.mosip.kernel.syncdata.entity.Machine;
import io.mosip.kernel.syncdata.repository.MachineRepository;
import io.mosip.kernel.syncdata.service.SyncMasterDataService;
import io.mosip.kernel.syncdata.service.helper.ApplicationDataHelper;
import io.mosip.kernel.syncdata.service.helper.DeviceDataHelper;
import io.mosip.kernel.syncdata.service.helper.DocumentDataHelper;
import io.mosip.kernel.syncdata.service.helper.HistoryDataHelper;
import io.mosip.kernel.syncdata.service.helper.IdentitySchemaHelper;
import io.mosip.kernel.syncdata.service.helper.IndividualDataHelper;
import io.mosip.kernel.syncdata.service.helper.MachineDataHelper;
import io.mosip.kernel.syncdata.service.helper.MiscellaneousDataHelper;
import io.mosip.kernel.syncdata.service.helper.RegistrationCenterDataHelper;
import io.mosip.kernel.syncdata.service.helper.TemplateDataHelper;
import io.mosip.kernel.syncdata.utils.MapperUtils;
import io.mosip.kernel.syncdata.utils.SyncMasterDataServiceHelper;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Masterdata sync handler service impl
 * 
 * @author Abhishek Kumar
 * @author Bal Vikash Sharma
 * @author Srinivasan
 * @author Urvil Joshi
 * @since 1.0.0
 */
@Service
public class SyncMasterDataServiceImpl implements SyncMasterDataService {
	
	private Logger logger = LoggerFactory.getLogger(SyncMasterDataServiceImpl.class);

	@Autowired
	private SyncMasterDataServiceHelper serviceHelper;

	@Autowired
	private MachineRepository machineRepo;
	
	@Autowired
	private MapperUtils mapper;
	
	@Autowired
	private IdentitySchemaHelper identitySchemaHelper;

	@Autowired
	private KeymanagerHelper keymanagerHelper;

	@Value("${mosip.kernel.syncdata-service-machine-url}")
	private String machineUrl;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ObjectMapper objectMapper;


	@Override
	public SyncDataResponseDto syncClientSettings(String regCenterId, String keyIndex,
			LocalDateTime lastUpdated, LocalDateTime currentTimestamp) 
					throws InterruptedException, ExecutionException {
				
		RegistrationCenterMachineDto regCenterMachineDto = getRegistrationCenterMachine(regCenterId, keyIndex);
		
		String machineId = regCenterMachineDto.getMachineId();
		String registrationCenterId = regCenterMachineDto.getRegCenterId();

		List<Machine> machines = machineRepo.findByMachineIdAndIsActive(machineId);
		if(machines == null || machines.isEmpty())
			throw new RequestException(MasterDataErrorCode.MACHINE_NOT_FOUND.getErrorCode(),
					MasterDataErrorCode.MACHINE_NOT_FOUND.getErrorMessage());
		
		SyncDataResponseDto response = new SyncDataResponseDto();
		
		List<CompletableFuture> futures = new ArrayList<CompletableFuture>();
		
		ApplicationDataHelper applicationDataHelper = new ApplicationDataHelper(lastUpdated, currentTimestamp, machines.get(0).getPublicKey());
		applicationDataHelper.retrieveData(serviceHelper, futures);		
		
		MachineDataHelper machineDataHelper = new MachineDataHelper(registrationCenterId, lastUpdated, currentTimestamp, machines.get(0).getPublicKey());
		machineDataHelper.retrieveData(serviceHelper, futures);		
		
		DeviceDataHelper deviceDataHelper = new DeviceDataHelper(registrationCenterId, lastUpdated, currentTimestamp, machines.get(0).getPublicKey());
		deviceDataHelper.retrieveData(serviceHelper, futures);
		
		IndividualDataHelper individualDataHelper = new IndividualDataHelper(lastUpdated, currentTimestamp, machines.get(0).getPublicKey());
		individualDataHelper.retrieveData(serviceHelper, futures);
		
		RegistrationCenterDataHelper RegistrationCenterDataHelper = new RegistrationCenterDataHelper(registrationCenterId, machineId, 
				lastUpdated, currentTimestamp, machines.get(0).getPublicKey());
		RegistrationCenterDataHelper.retrieveData(serviceHelper, futures);
		
		TemplateDataHelper templateDataHelper = new TemplateDataHelper(lastUpdated, currentTimestamp, machines.get(0).getPublicKey());
		templateDataHelper.retrieveData(serviceHelper, futures);
		
		DocumentDataHelper documentDataHelper = new DocumentDataHelper(lastUpdated, currentTimestamp, machines.get(0).getPublicKey());
		documentDataHelper.retrieveData(serviceHelper, futures);
		
		HistoryDataHelper historyDataHelper = new HistoryDataHelper(registrationCenterId, lastUpdated, currentTimestamp, machines.get(0).getPublicKey());
		historyDataHelper.retrieveData(serviceHelper, futures);
		
		MiscellaneousDataHelper miscellaneousDataHelper = new MiscellaneousDataHelper(machineId, lastUpdated, currentTimestamp, machines.get(0).getPublicKey());
		miscellaneousDataHelper.retrieveData(serviceHelper, futures);		
		
		CompletableFuture array [] = new CompletableFuture[futures.size()];
		CompletableFuture<Void> future = CompletableFuture.allOf(futures.toArray(array));		

		try {
			future.join();
		} catch (CompletionException e) {
			if (e.getCause() instanceof SyncDataServiceException) {
				throw (SyncDataServiceException) e.getCause();
			} else {
				throw (RuntimeException) e.getCause();
			}
		}
		
		List<SyncDataBaseDto> list = new ArrayList<SyncDataBaseDto>();		
		applicationDataHelper.fillRetrievedData(serviceHelper, list);
		machineDataHelper.fillRetrievedData(serviceHelper, list);
		deviceDataHelper.fillRetrievedData(serviceHelper, list);
		individualDataHelper.fillRetrievedData(serviceHelper, list);
		RegistrationCenterDataHelper.fillRetrievedData(serviceHelper, list);
		templateDataHelper.fillRetrievedData(serviceHelper, list);
		documentDataHelper.fillRetrievedData(serviceHelper, list);
		historyDataHelper.fillRetrievedData(serviceHelper, list);
		miscellaneousDataHelper.fillRetrievedData(serviceHelper, list);
		
		//Fills dynamic field data
		identitySchemaHelper.fillRetrievedData(list, machines.get(0).getPublicKey());
		
		response.setDataToSync(list);
		return response;
	}
	
	/**
	 * This method queries registrationCenterMachineRepository to fetch active registrationCenterMachine 
	 * with input keyIndex.
	 * 
	 * KeyIndex is mandatory param
	 * registrationCenterId is optional, if provided validates, if this matches the mapped registration center
	 * 
	 * @param registrationCenterId
	 * @param keyIndex
	 * @return RegistrationCenterMachineDto(machineId , registrationCenterId)
	 * @throws SyncDataServiceException
	 */
	private RegistrationCenterMachineDto getRegistrationCenterMachine(String registrationCenterId, String keyIndex) throws SyncDataServiceException {
		try {			
			
			List<Object[]> regCenterMachines = machineRepo.getRegistrationCenterMachineWithKeyIndex(keyIndex);
			
			if (regCenterMachines.isEmpty()) {
				throw new RequestException(MasterDataErrorCode.INVALID_KEY_INDEX.getErrorCode(),
						MasterDataErrorCode.INVALID_KEY_INDEX.getErrorMessage());
			}
			
			String mappedRegCenterId = (String)((Object[])regCenterMachines.get(0))[0];
			
			if(mappedRegCenterId == null)
				throw new RequestException(MasterDataErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorCode(),
						MasterDataErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorMessage());
			
			if(registrationCenterId != null &&  !mappedRegCenterId.equals(registrationCenterId))
				throw new RequestException(MasterDataErrorCode.REG_CENTER_UPDATED.getErrorCode(),
						MasterDataErrorCode.REG_CENTER_UPDATED.getErrorMessage());
			
			return new RegistrationCenterMachineDto(mappedRegCenterId, (String)((Object[])regCenterMachines.get(0))[1]);
			
					
		} catch (DataAccessException | DataAccessLayerException e) {
			logger.error("Failed to fetch registrationCenterMachine : ", e);
		}
		
		throw new SyncDataServiceException(MasterDataErrorCode.REG_CENTER_MACHINE_FETCH_EXCEPTION.getErrorCode(),
				MasterDataErrorCode.REG_CENTER_MACHINE_FETCH_EXCEPTION.getErrorMessage());
	}
	
	
	@Override
	public UploadPublicKeyResponseDto validateKeyMachineMapping(UploadPublicKeyRequestDto dto) {
		List<Machine> machines = machineRepo.findByMachineNameAndIsActive(dto.getMachineName());
		
		if(machines == null || machines.isEmpty())
			throw new RequestException(MasterDataErrorCode.MACHINE_NOT_FOUND.getErrorCode(),
					MasterDataErrorCode.MACHINE_NOT_FOUND.getErrorMessage());
		
		if(machines.get(0).getPublicKey() == null || machines.get(0).getPublicKey().length() == 0)
			throw new RequestException(MasterDataErrorCode.MACHINE_PUBLIC_KEY_NOT_WHITELISTED.getErrorCode(),
					MasterDataErrorCode.MACHINE_PUBLIC_KEY_NOT_WHITELISTED.getErrorMessage());
		
		if(Arrays.equals(CryptoUtil.decodeBase64(dto.getPublicKey()), 
				CryptoUtil.decodeBase64(machines.get(0).getPublicKey()))) {
			return new UploadPublicKeyResponseDto(machines.get(0).getKeyIndex());
		}
		
		throw new RequestException(MasterDataErrorCode.MACHINE_PUBLIC_KEY_NOT_WHITELISTED.getErrorCode(),
				MasterDataErrorCode.MACHINE_PUBLIC_KEY_NOT_WHITELISTED.getErrorMessage());
	}
	
	@Override
	public IdSchemaDto getLatestPublishedIdSchema(LocalDateTime lastUpdated, double schemaVersion) {
		return identitySchemaHelper.getLatestIdentitySchema(lastUpdated, schemaVersion);		
	}

	@Override
	public KeyPairGenerateResponseDto getCertificate(String applicationId, Optional<String> referenceId) {
		return keymanagerHelper.getCertificate(applicationId, referenceId);
	}

	@Override
	public ClientPublicKeyResponseDto getClientPublicKey(String machineId) {
		MachineResponseDto machineResponseDto = getMachineById(machineId);
		List<MachineDto> machines = machineResponseDto.getMachines();

		if(machines == null || machines.isEmpty())
			throw new RequestException(MasterDataErrorCode.MACHINE_NOT_FOUND.getErrorCode(),
					MasterDataErrorCode.MACHINE_NOT_FOUND.getErrorMessage());

		return new ClientPublicKeyResponseDto(machines.get(0).getSignPublicKey(), machines.get(0).getPublicKey());
	}

	private MachineResponseDto getMachineById(String machineId) {
		try {
			UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(String.format(machineUrl, machineId));
			ResponseEntity<String> responseEntity = restTemplate.getForEntity(builder.build().toUri(), String.class);

			objectMapper.registerModule(new JavaTimeModule());
			ResponseWrapper<MachineResponseDto> resp = objectMapper.readValue(responseEntity.getBody(),
					new TypeReference<ResponseWrapper<MachineResponseDto>>() {});

			if(resp.getErrors() != null && !resp.getErrors().isEmpty())
				throw new SyncInvalidArgumentException(resp.getErrors());

			return resp.getResponse();
		} catch (Exception e) {
			throw new SyncDataServiceException(MasterDataErrorCode.MACHINE_DETAIL_FETCH_EXCEPTION.getErrorMessage(),
					MasterDataErrorCode.MACHINE_DETAIL_FETCH_EXCEPTION.getErrorMessage() + " : " +
							ExceptionUtils.buildMessage(e.getMessage(), e.getCause()));
		}
	}

}
