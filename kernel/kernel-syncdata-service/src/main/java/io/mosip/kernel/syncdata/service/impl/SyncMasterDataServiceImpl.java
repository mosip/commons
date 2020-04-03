package io.mosip.kernel.syncdata.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import javax.persistence.PersistenceException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.syncdata.constant.MasterDataErrorCode;
import io.mosip.kernel.syncdata.dto.AppAuthenticationMethodDto;
import io.mosip.kernel.syncdata.dto.AppDetailDto;
import io.mosip.kernel.syncdata.dto.AppRolePriorityDto;
import io.mosip.kernel.syncdata.dto.ApplicantValidDocumentDto;
import io.mosip.kernel.syncdata.dto.ApplicationDto;
import io.mosip.kernel.syncdata.dto.BiometricAttributeDto;
import io.mosip.kernel.syncdata.dto.BiometricTypeDto;
import io.mosip.kernel.syncdata.dto.BlacklistedWordsDto;
import io.mosip.kernel.syncdata.dto.DeviceDto;
import io.mosip.kernel.syncdata.dto.DeviceProviderDto;
import io.mosip.kernel.syncdata.dto.DeviceServiceDto;
import io.mosip.kernel.syncdata.dto.DeviceSpecificationDto;
import io.mosip.kernel.syncdata.dto.DeviceSubTypeDPMDto;
import io.mosip.kernel.syncdata.dto.DeviceTypeDPMDto;
import io.mosip.kernel.syncdata.dto.DeviceTypeDto;
import io.mosip.kernel.syncdata.dto.DocumentCategoryDto;
import io.mosip.kernel.syncdata.dto.DocumentTypeDto;
import io.mosip.kernel.syncdata.dto.FoundationalTrustProviderDto;
import io.mosip.kernel.syncdata.dto.GenderDto;
import io.mosip.kernel.syncdata.dto.HolidayDto;
import io.mosip.kernel.syncdata.dto.IdTypeDto;
import io.mosip.kernel.syncdata.dto.IndividualTypeDto;
import io.mosip.kernel.syncdata.dto.LanguageDto;
import io.mosip.kernel.syncdata.dto.LocationDto;
import io.mosip.kernel.syncdata.dto.MachineDto;
import io.mosip.kernel.syncdata.dto.MachineSpecificationDto;
import io.mosip.kernel.syncdata.dto.MachineTypeDto;
import io.mosip.kernel.syncdata.dto.PostReasonCategoryDto;
import io.mosip.kernel.syncdata.dto.ProcessListDto;
import io.mosip.kernel.syncdata.dto.ReasonListDto;
import io.mosip.kernel.syncdata.dto.RegisteredDeviceDto;
import io.mosip.kernel.syncdata.dto.RegistrationCenterDeviceDto;
import io.mosip.kernel.syncdata.dto.RegistrationCenterDeviceHistoryDto;
import io.mosip.kernel.syncdata.dto.RegistrationCenterDto;
import io.mosip.kernel.syncdata.dto.RegistrationCenterMachineDeviceDto;
import io.mosip.kernel.syncdata.dto.RegistrationCenterMachineDeviceHistoryDto;
import io.mosip.kernel.syncdata.dto.RegistrationCenterMachineDto;
import io.mosip.kernel.syncdata.dto.RegistrationCenterMachineHistoryDto;
import io.mosip.kernel.syncdata.dto.RegistrationCenterTypeDto;
import io.mosip.kernel.syncdata.dto.RegistrationCenterUserDto;
import io.mosip.kernel.syncdata.dto.RegistrationCenterUserHistoryDto;
import io.mosip.kernel.syncdata.dto.RegistrationCenterUserMachineMappingDto;
import io.mosip.kernel.syncdata.dto.RegistrationCenterUserMachineMappingHistoryDto;
import io.mosip.kernel.syncdata.dto.ScreenAuthorizationDto;
import io.mosip.kernel.syncdata.dto.ScreenDetailDto;
import io.mosip.kernel.syncdata.dto.SyncJobDefDto;
import io.mosip.kernel.syncdata.dto.TemplateDto;
import io.mosip.kernel.syncdata.dto.TemplateFileFormatDto;
import io.mosip.kernel.syncdata.dto.TemplateTypeDto;
import io.mosip.kernel.syncdata.dto.TitleDto;
import io.mosip.kernel.syncdata.dto.UploadPublicKeyRequestDto;
import io.mosip.kernel.syncdata.dto.UploadPublicKeyResponseDto;
import io.mosip.kernel.syncdata.dto.ValidDocumentDto;
import io.mosip.kernel.syncdata.dto.response.MasterDataResponseDto;
import io.mosip.kernel.syncdata.dto.response.SyncDataBaseDto;
import io.mosip.kernel.syncdata.dto.response.SyncDataResponseDto;
import io.mosip.kernel.syncdata.entity.AppAuthenticationMethod;
import io.mosip.kernel.syncdata.entity.Machine;
import io.mosip.kernel.syncdata.entity.MachineHistory;
import io.mosip.kernel.syncdata.entity.RegistrationCenter;
import io.mosip.kernel.syncdata.entity.RegistrationCenterMachine;
import io.mosip.kernel.syncdata.exception.ParseResponseException;
import io.mosip.kernel.syncdata.exception.RequestException;
import io.mosip.kernel.syncdata.exception.SyncDataServiceException;
import io.mosip.kernel.syncdata.exception.SyncServiceException;
import io.mosip.kernel.syncdata.repository.MachineHistoryRepository;
import io.mosip.kernel.syncdata.repository.MachineRepository;
import io.mosip.kernel.syncdata.repository.RegistrationCenterMachineRepository;
import io.mosip.kernel.syncdata.repository.RegistrationCenterRepository;
import io.mosip.kernel.syncdata.service.SyncMasterDataService;
import io.mosip.kernel.syncdata.service.helper.ApplicationDataHelper;
import io.mosip.kernel.syncdata.service.helper.DeviceDataHelper;
import io.mosip.kernel.syncdata.service.helper.DocumentDataHelper;
import io.mosip.kernel.syncdata.service.helper.HistoryDataHelper;
import io.mosip.kernel.syncdata.service.helper.IndividualDataHelper;
import io.mosip.kernel.syncdata.service.helper.MachineDataHelper;
import io.mosip.kernel.syncdata.service.helper.MiscellaneousDataHelper;
import io.mosip.kernel.syncdata.service.helper.RegistrationCenterDataHelper;
import io.mosip.kernel.syncdata.service.helper.TemplateDataHelper;
import io.mosip.kernel.syncdata.utils.ExceptionUtils;
import io.mosip.kernel.syncdata.utils.MapperUtils;
import io.mosip.kernel.syncdata.utils.MetaDataUtils;
import io.mosip.kernel.syncdata.utils.SyncMasterDataServiceHelper;

import io.mosip.kernel.syncdata.entity.AppAuthenticationMethod;
import io.mosip.kernel.syncdata.entity.AppDetail;
import io.mosip.kernel.syncdata.entity.AppRolePriority;
import io.mosip.kernel.syncdata.entity.ApplicantValidDocument;
import io.mosip.kernel.syncdata.entity.Application;
import io.mosip.kernel.syncdata.entity.BaseEntity;
import io.mosip.kernel.syncdata.entity.BiometricAttribute;
import io.mosip.kernel.syncdata.entity.BiometricType;
import io.mosip.kernel.syncdata.entity.BlacklistedWords;
import io.mosip.kernel.syncdata.entity.Device;
import io.mosip.kernel.syncdata.entity.DeviceProvider;
import io.mosip.kernel.syncdata.entity.DeviceService;
import io.mosip.kernel.syncdata.entity.DeviceSpecification;
import io.mosip.kernel.syncdata.entity.DeviceSubTypeDPM;
import io.mosip.kernel.syncdata.entity.DeviceType;
import io.mosip.kernel.syncdata.entity.DeviceTypeDPM;
import io.mosip.kernel.syncdata.entity.DocumentCategory;
import io.mosip.kernel.syncdata.entity.DocumentType;
import io.mosip.kernel.syncdata.entity.FoundationalTrustProvider;
import io.mosip.kernel.syncdata.entity.Gender;
import io.mosip.kernel.syncdata.entity.Holiday;
import io.mosip.kernel.syncdata.entity.IdType;
import io.mosip.kernel.syncdata.entity.IndividualType;
import io.mosip.kernel.syncdata.entity.Language;
import io.mosip.kernel.syncdata.entity.Location;
import io.mosip.kernel.syncdata.entity.Machine;
import io.mosip.kernel.syncdata.entity.MachineHistory;
import io.mosip.kernel.syncdata.entity.MachineSpecification;
import io.mosip.kernel.syncdata.entity.MachineType;
import io.mosip.kernel.syncdata.entity.ProcessList;
import io.mosip.kernel.syncdata.entity.ReasonCategory;
import io.mosip.kernel.syncdata.entity.ReasonList;
import io.mosip.kernel.syncdata.entity.RegisteredDevice;
import io.mosip.kernel.syncdata.entity.RegistrationCenter;
import io.mosip.kernel.syncdata.entity.RegistrationCenterDevice;
import io.mosip.kernel.syncdata.entity.RegistrationCenterDeviceHistory;
import io.mosip.kernel.syncdata.entity.RegistrationCenterMachine;
import io.mosip.kernel.syncdata.entity.RegistrationCenterMachineDevice;
import io.mosip.kernel.syncdata.entity.RegistrationCenterMachineDeviceHistory;
import io.mosip.kernel.syncdata.entity.RegistrationCenterMachineHistory;
import io.mosip.kernel.syncdata.entity.RegistrationCenterType;
import io.mosip.kernel.syncdata.entity.RegistrationCenterUser;
import io.mosip.kernel.syncdata.entity.RegistrationCenterUserHistory;
import io.mosip.kernel.syncdata.entity.RegistrationCenterUserMachine;
import io.mosip.kernel.syncdata.entity.RegistrationCenterUserMachineHistory;
import io.mosip.kernel.syncdata.entity.ScreenAuthorization;
import io.mosip.kernel.syncdata.entity.ScreenDetail;
import io.mosip.kernel.syncdata.entity.SyncJobDef;
import io.mosip.kernel.syncdata.entity.Template;
import io.mosip.kernel.syncdata.entity.TemplateFileFormat;
import io.mosip.kernel.syncdata.entity.TemplateType;
import io.mosip.kernel.syncdata.entity.Title;
import io.mosip.kernel.syncdata.entity.ValidDocument;

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
	
	private Logger logger = LogManager.getLogger(SyncMasterDataServiceImpl.class);

	@Autowired
	SyncMasterDataServiceHelper serviceHelper;

	@Autowired
	RegistrationCenterMachineRepository registrationCenterMachineRepository;

	@Autowired
	RegistrationCenterRepository registrationCenterRepository;

	@Autowired
	private MachineRepository machineRepo;

	@Autowired
	private MachineHistoryRepository machineHistoryRepo;
	
	@Autowired
	private MapperUtils mapper;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.synchandler.service.MasterDataService#syncData(java.lang.
	 * String, java.time.LocalDate)
	 */

	@Override
	public MasterDataResponseDto syncData(String regCenterId, String macAddress, String serialNum,
			LocalDateTime lastUpdated, LocalDateTime currentTimeStamp, String keyIndex)
			throws InterruptedException, ExecutionException {
		String machineId = null;
		RegistrationCenterMachineDto regCenterMachineDto = null;
		if (regCenterId == null) {
			regCenterMachineDto = getRegistationMachineMapping(macAddress, serialNum, keyIndex);
		} else {

			regCenterMachineDto = getRegCenterMachineMappingWithRegCenterId(regCenterId, macAddress, serialNum,
					keyIndex);
		}

		machineId = regCenterMachineDto.getMachineId();
		regCenterId = regCenterMachineDto.getRegCenterId();
		MasterDataResponseDto response = new MasterDataResponseDto();
		CompletableFuture<List<MachineDto>> machineDetails = null;
		CompletableFuture<List<ApplicationDto>> applications = null;
		CompletableFuture<List<RegistrationCenterTypeDto>> registrationCenterTypes = null;
		CompletableFuture<List<RegistrationCenterDto>> registrationCenters = null;
		CompletableFuture<List<TemplateDto>> templates = null;
		CompletableFuture<List<TemplateFileFormatDto>> templateFileFormats = null;
		CompletableFuture<List<PostReasonCategoryDto>> reasonCategory = null;
		CompletableFuture<List<HolidayDto>> holidays = null;
		CompletableFuture<List<BlacklistedWordsDto>> blacklistedWords = null;
		CompletableFuture<List<BiometricTypeDto>> biometricTypes = null;
		CompletableFuture<List<BiometricAttributeDto>> biometricAttributes = null;
		CompletableFuture<List<TitleDto>> titles = null;
		CompletableFuture<List<LanguageDto>> languages = null;
		CompletableFuture<List<GenderDto>> genders = null;
		CompletableFuture<List<DeviceDto>> devices = null;
		CompletableFuture<List<DocumentCategoryDto>> documentCategories = null;
		CompletableFuture<List<DocumentTypeDto>> documentTypes = null;
		CompletableFuture<List<IdTypeDto>> idTypes = null;
		CompletableFuture<List<DeviceSpecificationDto>> deviceSpecifications = null;
		CompletableFuture<List<LocationDto>> locationHierarchy = null;
		CompletableFuture<List<MachineSpecificationDto>> machineSpecification = null;
		CompletableFuture<List<MachineTypeDto>> machineType = null;
		CompletableFuture<List<TemplateTypeDto>> templateTypes = null;
		CompletableFuture<List<DeviceTypeDto>> deviceTypes = null;
		CompletableFuture<List<ValidDocumentDto>> validDocumentsMapping = null;
		CompletableFuture<List<ReasonListDto>> reasonList = null;
		CompletableFuture<List<ApplicantValidDocumentDto>> applicantValidDocumentList = null;
		CompletableFuture<List<IndividualTypeDto>> individualTypeList = null;
		CompletableFuture<List<AppAuthenticationMethodDto>> appAuthenticationMethods = null;
		CompletableFuture<List<AppDetailDto>> appDetails = null;
		CompletableFuture<List<AppRolePriorityDto>> appRolePriorities = null;
		CompletableFuture<List<ScreenAuthorizationDto>> screenAuthorizations = null;
		CompletableFuture<List<ProcessListDto>> processList = null;
		CompletableFuture<List<DeviceProviderDto>> deviceProviders = null;
		CompletableFuture<List<DeviceServiceDto>> deviceServices = null;
		CompletableFuture<List<RegisteredDeviceDto>> registeredDevices = null;
		CompletableFuture<List<FoundationalTrustProviderDto>> ftps = null;
		CompletableFuture<List<DeviceTypeDPMDto>> deviceTypeDPMs = null;
		CompletableFuture<List<DeviceSubTypeDPMDto>> deviceSubTypeDPMs = null;

		CompletableFuture<List<RegistrationCenterMachineDto>> registrationCenterMachines = null;
		CompletableFuture<List<RegistrationCenterDeviceDto>> registrationCenterDevices = null;
		CompletableFuture<List<RegistrationCenterMachineDeviceDto>> registrationCenterMachineDevices = null;
		CompletableFuture<List<RegistrationCenterUserMachineMappingDto>> registrationCenterUserMachines = null;
		CompletableFuture<List<RegistrationCenterUserDto>> registrationCenterUsers = null;
		CompletableFuture<List<RegistrationCenterUserHistoryDto>> registrationCenterUserHistoryList = null;
		CompletableFuture<List<RegistrationCenterUserMachineMappingHistoryDto>> registrationCenterUserMachineMappingHistoryList = null;
		CompletableFuture<List<RegistrationCenterMachineDeviceHistoryDto>> registrationCenterMachineDeviceHistoryList = null;
		CompletableFuture<List<RegistrationCenterDeviceHistoryDto>> registrationCenterDeviceHistoryList = null;
		CompletableFuture<List<RegistrationCenterMachineHistoryDto>> registrationCenterMachineHistoryList = null;
		CompletableFuture<List<SyncJobDefDto>> syncJobDefDtos;
		CompletableFuture<List<ScreenDetailDto>> screenDetails;

		applications = serviceHelper.getApplications(lastUpdated, currentTimeStamp);
		machineDetails = serviceHelper.getMachines(regCenterId, lastUpdated, currentTimeStamp);
		registrationCenters = serviceHelper.getRegistrationCenter(machineId, lastUpdated, currentTimeStamp);
		registrationCenterTypes = serviceHelper.getRegistrationCenterType(machineId, lastUpdated, currentTimeStamp);
		templates = serviceHelper.getTemplates(lastUpdated, currentTimeStamp);
		templateFileFormats = serviceHelper.getTemplateFileFormats(lastUpdated, currentTimeStamp);
		reasonCategory = serviceHelper.getReasonCategory(lastUpdated, currentTimeStamp);
		holidays = serviceHelper.getHolidays(lastUpdated, machineId, currentTimeStamp);
		blacklistedWords = serviceHelper.getBlackListedWords(lastUpdated, currentTimeStamp);
		biometricTypes = serviceHelper.getBiometricTypes(lastUpdated, currentTimeStamp);
		biometricAttributes = serviceHelper.getBiometricAttributes(lastUpdated, currentTimeStamp);
		titles = serviceHelper.getTitles(lastUpdated, currentTimeStamp);
		languages = serviceHelper.getLanguages(lastUpdated, currentTimeStamp);
		genders = serviceHelper.getGenders(lastUpdated, currentTimeStamp);
		devices = serviceHelper.getDevices(regCenterId, lastUpdated, currentTimeStamp);
		documentCategories = serviceHelper.getDocumentCategories(lastUpdated, currentTimeStamp);
		documentTypes = serviceHelper.getDocumentTypes(lastUpdated, currentTimeStamp);
		idTypes = serviceHelper.getIdTypes(lastUpdated, currentTimeStamp);
		deviceSpecifications = serviceHelper.getDeviceSpecifications(regCenterId, lastUpdated, currentTimeStamp);
		locationHierarchy = serviceHelper.getLocationHierarchy(lastUpdated, currentTimeStamp);
		machineSpecification = serviceHelper.getMachineSpecification(regCenterId, lastUpdated, currentTimeStamp);
		machineType = serviceHelper.getMachineType(regCenterId, lastUpdated, currentTimeStamp);
		templateTypes = serviceHelper.getTemplateTypes(lastUpdated, currentTimeStamp);
		deviceTypes = serviceHelper.getDeviceType(regCenterId, lastUpdated, currentTimeStamp);
		reasonList = serviceHelper.getReasonList(lastUpdated, currentTimeStamp);
		applicantValidDocumentList = serviceHelper.getApplicantValidDocument(lastUpdated, currentTimeStamp);
		individualTypeList = serviceHelper.getIndividualType(lastUpdated, currentTimeStamp);
		validDocumentsMapping = serviceHelper.getValidDocuments(lastUpdated, currentTimeStamp);
		appAuthenticationMethods = serviceHelper.getAppAuthenticationMethodDetails(lastUpdated, currentTimeStamp);
		appDetails = serviceHelper.getAppDetails(lastUpdated, currentTimeStamp);
		appRolePriorities = serviceHelper.getAppRolePriorityDetails(lastUpdated, currentTimeStamp);
		processList = serviceHelper.getProcessList(lastUpdated, currentTimeStamp);
		screenAuthorizations = serviceHelper.getScreenAuthorizationDetails(lastUpdated, currentTimeStamp);
		registrationCenterMachines = serviceHelper.getRegistrationCenterMachines(regCenterId, lastUpdated,
				currentTimeStamp);
		registrationCenterDevices = serviceHelper.getRegistrationCenterDevices(regCenterId, lastUpdated,
				currentTimeStamp);
		registrationCenterMachineDevices = serviceHelper.getRegistrationCenterMachineDevices(regCenterId, lastUpdated,
				currentTimeStamp);
		registrationCenterUserMachines = serviceHelper.getRegistrationCenterUserMachines(regCenterId, lastUpdated,
				currentTimeStamp);
		registrationCenterUsers = serviceHelper.getRegistrationCenterUsers(regCenterId, lastUpdated, currentTimeStamp);
		registrationCenterUserHistoryList = serviceHelper.getRegistrationCenterUserHistory(regCenterId, lastUpdated,
				currentTimeStamp);
		registrationCenterUserMachineMappingHistoryList = serviceHelper
				.getRegistrationCenterUserMachineMapping(regCenterId, lastUpdated, currentTimeStamp);
		registrationCenterMachineDeviceHistoryList = serviceHelper
				.getRegistrationCenterMachineDeviceHistoryDetails(regCenterId, lastUpdated, currentTimeStamp);
		registrationCenterDeviceHistoryList = serviceHelper.getRegistrationCenterDeviceHistoryDetails(regCenterId,
				lastUpdated, currentTimeStamp);
		registrationCenterMachineHistoryList = serviceHelper.getRegistrationCenterMachineHistoryDetails(regCenterId,
				lastUpdated, currentTimeStamp);
		//
		syncJobDefDtos = serviceHelper.getSyncJobDefDetails(lastUpdated, currentTimeStamp);
		screenDetails = serviceHelper.getScreenDetails(lastUpdated, currentTimeStamp);
		registeredDevices = serviceHelper.getRegisteredDeviceDetails(regCenterId, lastUpdated, currentTimeStamp);
		deviceProviders = serviceHelper.getDeviceProviderDetails(lastUpdated, currentTimeStamp);
		deviceServices = serviceHelper.getDeviceServiceDetails(lastUpdated, currentTimeStamp);
		ftps = serviceHelper.getFPDetails(lastUpdated, currentTimeStamp);
		deviceTypeDPMs = serviceHelper.getDeviceTypeDetails(lastUpdated, currentTimeStamp);
		deviceSubTypeDPMs = serviceHelper.getDeviceSubTypeDetails(lastUpdated, currentTimeStamp);
		CompletableFuture<Void> future = CompletableFuture.allOf(machineDetails, applications, registrationCenterTypes,
				registrationCenters, templates, templateFileFormats, reasonCategory, reasonList, holidays,
				blacklistedWords, biometricTypes, biometricAttributes, titles, languages, devices, documentCategories,
				documentTypes, idTypes, deviceSpecifications, locationHierarchy, machineSpecification, machineType,
				templateTypes, deviceTypes, validDocumentsMapping, registrationCenterMachines,
				registrationCenterDevices, registrationCenterMachineDevices, registrationCenterUserMachines,
				registrationCenterUsers, registrationCenterUserHistoryList,
				registrationCenterUserMachineMappingHistoryList, registrationCenterMachineDeviceHistoryList,
				registrationCenterDeviceHistoryList, registrationCenterMachineHistoryList, applicantValidDocumentList,
				individualTypeList, appAuthenticationMethods, appDetails, appRolePriorities, processList,
				screenAuthorizations, syncJobDefDtos, screenDetails, registeredDevices, deviceProviders, deviceServices,
				ftps, deviceTypeDPMs, deviceSubTypeDPMs);

		try {
			future.join();
		} catch (CompletionException e) {
			if (e.getCause() instanceof SyncDataServiceException) {
				throw (SyncDataServiceException) e.getCause();
			} else if (e.getCause() instanceof SyncServiceException) {
				throw (SyncServiceException) e.getCause();
			} else if (e.getCause() instanceof ParseResponseException) {
				throw (ParseResponseException) e.getCause();
			} else {
				throw (RuntimeException) e.getCause();
			}
		}

		response.setMachineDetails(machineDetails.get());
		response.setApplications(applications.get());
		response.setRegistrationCenterTypes(registrationCenterTypes.get());
		response.setRegistrationCenter(registrationCenters.get());
		response.setTemplates(templates.get());
		response.setTemplateFileFormat(templateFileFormats.get());
		response.setReasonCategory(reasonCategory.get());
		response.setReasonList(reasonList.get());
		response.setHolidays(holidays.get());
		response.setBlackListedWords(blacklistedWords.get());
		response.setBiometricTypes(biometricTypes.get());
		response.setBiometricattributes(biometricAttributes.get());
		response.setTitles(titles.get());
		response.setLanguages(languages.get());
		response.setGenders(genders.get());
		response.setDevices(devices.get());
		response.setDocumentCategories(documentCategories.get());
		response.setDocumentTypes(documentTypes.get());
		response.setIdTypes(idTypes.get());
		response.setDeviceSpecifications(deviceSpecifications.get());
		response.setLocationHierarchy(locationHierarchy.get());
		response.setMachineSpecification(machineSpecification.get());
		response.setMachineType(machineType.get());
		response.setTemplatesTypes(templateTypes.get());
		response.setDeviceTypes(deviceTypes.get());
		response.setValidDocumentMapping(validDocumentsMapping.get());
		response.setApplicantValidDocuments(applicantValidDocumentList.get());
		response.setIndividualTypes(individualTypeList.get());
		response.setAppAuthenticationMethods(appAuthenticationMethods.get());
		response.setAppDetails(appDetails.get());
		response.setAppRolePriorities(appRolePriorities.get());
		response.setProcessList(processList.get());
		response.setScreenAuthorizations(screenAuthorizations.get());
		response.setSyncJobDefinitions(syncJobDefDtos.get());
		response.setScreenDetails(screenDetails.get());
		response.setRegisteredDevices(registeredDevices.get());
		response.setDeviceProviders(deviceProviders.get());
		response.setDeviceServices(deviceServices.get());
		response.setDeviceTypeDPMs(deviceTypeDPMs.get());
		response.setDeviceSubTypeDPMs(deviceSubTypeDPMs.get());
		response.setFunctionalTrustProviders(ftps.get());
		response.setRegistrationCenterMachines(registrationCenterMachines.get());
		response.setRegistrationCenterDevices(registrationCenterDevices.get());
		response.setRegistrationCenterMachineDevices(registrationCenterMachineDevices.get());
		response.setRegistrationCenterUserMachines(registrationCenterUserMachines.get());
		response.setRegistrationCenterUsers(registrationCenterUsers.get());
		response.setRegistrationCenterUserHistory(registrationCenterUserHistoryList.get());
		response.setRegistrationCenterUserMachineMappingHistory(registrationCenterUserMachineMappingHistoryList.get());
		response.setRegistrationCenterDeviceHistory(registrationCenterDeviceHistoryList.get());
		response.setRegistrationCenterMachineHistory(registrationCenterMachineHistoryList.get());
		response.setRegistrationCenterMachineDeviceHistory(registrationCenterMachineDeviceHistoryList.get());

		return response;

	}

	/**
	 * This method would return RegistrationCenterMachine mapping based on
	 * macaddress/serial number
	 * 
	 * @param macId     - mac address
	 * @param serialNum - serial number
	 * @return - {@link RegistrationCenterMachineDto}
	 */
	private RegistrationCenterMachineDto getRegistationMachineMapping(String macId, String serialNum, String keyIndex) {
		List<Object[]> machineList = null;
		RegistrationCenterMachineDto regMachineDto = null;

		try {
			if (macId != null && serialNum != null && keyIndex != null) {
				machineList = registrationCenterMachineRepository
						.getRegistrationCenterMachineWithMacAddressAndSerialNumAndKeyIndex(macId, serialNum, keyIndex);

			} else if (macId != null && keyIndex != null) {
				machineList = registrationCenterMachineRepository
						.getRegistrationCenterMachineWithMacAddressAndKeyIndex(macId, keyIndex);

			} else if (serialNum != null && keyIndex != null) {
				machineList = registrationCenterMachineRepository
						.getRegistrationCenterMachineWithSerialNumberAndKeyIndex(serialNum, keyIndex);

			} else if (macId != null && serialNum != null) {
				machineList = registrationCenterMachineRepository
						.getRegistrationCenterMachineWithMacAddressAndSerialNum(macId, serialNum);
			} else if (keyIndex != null) {
				machineList = registrationCenterMachineRepository.getRegistrationCenterMachineWithKeyIndex(keyIndex);
			} else if (macId != null) {
				machineList = registrationCenterMachineRepository.getRegistrationCenterMachineWithMacAddress(macId);
			} else if (serialNum != null) {
				machineList = registrationCenterMachineRepository
						.getRegistrationCenterMachineWithSerialNumber(serialNum);
			} else {
				throw new RequestException(MasterDataErrorCode.EMPTY_MAC_OR_SERIAL_NUMBER.getErrorCode(),
						MasterDataErrorCode.EMPTY_MAC_OR_SERIAL_NUMBER.getErrorMessage());
			}

		} catch (DataAccessException | DataAccessLayerException e) {
			throw new SyncDataServiceException(MasterDataErrorCode.REG_CENTER_MACHINE_FETCH_EXCEPTION.getErrorCode(),
					MasterDataErrorCode.REG_CENTER_MACHINE_FETCH_EXCEPTION.getErrorMessage());
		}

		return getRegistrationMachines(machineList, regMachineDto);
	}

	private RegistrationCenterMachineDto getRegistrationMachines(List<Object[]> machineList,
			RegistrationCenterMachineDto regMachineDto) {
		if (machineList.isEmpty()) {
			throw new RequestException(MasterDataErrorCode.INVALID_MAC_OR_SERIAL_NUMBER.getErrorCode(),
					MasterDataErrorCode.INVALID_MAC_OR_SERIAL_NUMBER.getErrorMessage());
		}
		for (Object[] objects : machineList) {
			regMachineDto = new RegistrationCenterMachineDto();
			regMachineDto.setMachineId((String) objects[1]);
			regMachineDto.setRegCenterId((String) objects[0]);
		}
		return regMachineDto;
	}

	/**
	 * This method would fetch RegistrationMachine mapping based on machine id if
	 * regCenterid is not available if regCenterId is present it would check for the
	 * mapping. If the mapping is not present and is not active it will throw error.
	 * 
	 * @param regCenterId - registration center id
	 * @param macId       - mac address
	 * @param serialNum   - serial address
	 * @return {@link RegistrationCenterMachineDto}
	 */
	private RegistrationCenterMachineDto getRegCenterMachineMappingWithRegCenterId(String regCenterId, String macId,
			String serialNum, String keyIndex) {
		RegistrationCenterMachineDto regCenterMachine = getRegistationMachineMapping(macId, serialNum, keyIndex);
		RegistrationCenterMachine registrationCenterMachine = null;

		try {
			List<RegistrationCenter> regCenterList = registrationCenterRepository
					.findRegistrationCenterByIdAndIsActiveIsTrue(regCenterId);
			if (regCenterList.isEmpty()) {
				throw new RequestException(MasterDataErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorCode(),
						MasterDataErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorMessage());
			}
			registrationCenterMachine = registrationCenterMachineRepository
					.getRegCenterIdWithRegIdAndMachineId(regCenterId, regCenterMachine.getMachineId());
		} catch (DataAccessException | DataAccessLayerException e) {
			throw new SyncDataServiceException(MasterDataErrorCode.REG_CENTER_MACHINE_FETCH_EXCEPTION.getErrorCode(),
					MasterDataErrorCode.REG_CENTER_MACHINE_FETCH_EXCEPTION.getErrorMessage());
		}

		if (registrationCenterMachine == null) {
			throw new RequestException(MasterDataErrorCode.REG_CENTER_UPDATED.getErrorCode(),
					MasterDataErrorCode.REG_CENTER_UPDATED.getErrorMessage());
		}

		MapperUtils.map(registrationCenterMachine, regCenterMachine);

		return regCenterMachine;
	}

	@Override
	@Transactional("syncDataTransactionManager")
	public UploadPublicKeyResponseDto uploadpublickey(UploadPublicKeyRequestDto uploadPublicKeyRequestDto) {
		final byte[] publicKey = CryptoUtil.decodeBase64(uploadPublicKeyRequestDto.getPublicKey());
		final String keyIndex = CryptoUtil.computeFingerPrint(publicKey, null);

		List<Machine> machineDetail = machineRepo
				.findByMachineNameAndIsActive(uploadPublicKeyRequestDto.getMachineName());
		if (machineDetail != null && !machineDetail.isEmpty()) {
			if (Arrays.equals(publicKey, machineDetail.get(0).getPublicKey())) {
				return new UploadPublicKeyResponseDto(machineDetail.get(0).getKeyIndex());
			} else if (machineDetail.get(0).getPublicKey() != null && machineDetail.get(0).getPublicKey().length != 0
					&& !Arrays.equals(publicKey, machineDetail.get(0).getPublicKey())) {
				throw new SyncDataServiceException(MasterDataErrorCode.MACHINE_PUBLIC_KEY_ALREADY_EXIST.getErrorCode(),
						MasterDataErrorCode.MACHINE_PUBLIC_KEY_ALREADY_EXIST.getErrorMessage());
			}
		}

		try {
			if (machineDetail != null && !machineDetail.isEmpty()) {
				List<Machine> updatedMachineList = new ArrayList<>();
				List<MachineHistory> updatedMachineHistoryList = new ArrayList<>();
				machineDetail.forEach(machineEntity -> {
					Machine machine = MetaDataUtils.setUpdateMetaData(machineEntity);
					machine.setPublicKey(publicKey);
					machine.setKeyIndex(keyIndex);
					MachineHistory machineHistory = MapperUtils.map(machine, MachineHistory.class);
					machineHistory.setEffectDateTime(machine.getUpdatedDateTime());
					MapperUtils.mapBaseFieldValue(machine, machineHistory);
					updatedMachineList.add(machine);
					updatedMachineHistoryList.add(machineHistory);
				});

				machineRepo.saveAll(updatedMachineList);
				machineHistoryRepo.saveAll(updatedMachineHistoryList);
			} else {
				throw new RequestException(MasterDataErrorCode.MACHINE_NOT_FOUND.getErrorCode(),
						MasterDataErrorCode.MACHINE_NOT_FOUND.getErrorMessage());
			}
		} catch (IllegalArgumentException | PersistenceException e) {
			throw new SyncDataServiceException(MasterDataErrorCode.MACHINE_PUBLIC_UPLOAD_EXCEPTION.getErrorCode(),
					MasterDataErrorCode.MACHINE_PUBLIC_UPLOAD_EXCEPTION.getErrorMessage()
							+ ExceptionUtils.parseException(e));
		}
		return new UploadPublicKeyResponseDto(keyIndex);
	}

	@Override
	public SyncDataResponseDto syncClientSettings(String regCenterId, String keyIndex,
			LocalDateTime lastUpdated, LocalDateTime currentTimestamp) 
					throws InterruptedException, ExecutionException {
				
		RegistrationCenterMachineDto regCenterMachineDto = getRegistrationCenterMachine(regCenterId, keyIndex);
		
		String machineId = regCenterMachineDto.getMachineId();
		String registrationCenterId = regCenterMachineDto.getRegCenterId();
		
		SyncDataResponseDto response = new SyncDataResponseDto();
		
		List<CompletableFuture> futures = new ArrayList<CompletableFuture>();
		
		ApplicationDataHelper applicationDataHelper = new ApplicationDataHelper(lastUpdated, currentTimestamp);
		applicationDataHelper.retrieveData(serviceHelper, futures);		
		
		MachineDataHelper machineDataHelper = new MachineDataHelper(registrationCenterId, lastUpdated, currentTimestamp);
		machineDataHelper.retrieveData(serviceHelper, futures);		
		
		DeviceDataHelper deviceDataHelper = new DeviceDataHelper(registrationCenterId, lastUpdated, currentTimestamp);
		deviceDataHelper.retrieveData(serviceHelper, futures);
		
		IndividualDataHelper individualDataHelper = new IndividualDataHelper(lastUpdated, currentTimestamp);
		individualDataHelper.retrieveData(serviceHelper, futures);
		
		RegistrationCenterDataHelper RegistrationCenterDataHelper = new RegistrationCenterDataHelper(registrationCenterId, machineId, 
				lastUpdated, currentTimestamp);
		RegistrationCenterDataHelper.retrieveData(serviceHelper, futures);
		
		TemplateDataHelper templateDataHelper = new TemplateDataHelper(lastUpdated, currentTimestamp);
		templateDataHelper.retrieveData(serviceHelper, futures);
		
		DocumentDataHelper documentDataHelper = new DocumentDataHelper(lastUpdated, currentTimestamp);
		documentDataHelper.retrieveData(serviceHelper, futures);
		
		HistoryDataHelper historyDataHelper = new HistoryDataHelper(registrationCenterId, lastUpdated, currentTimestamp);
		historyDataHelper.retrieveData(serviceHelper, futures);
		
		MiscellaneousDataHelper miscellaneousDataHelper = new MiscellaneousDataHelper(machineId, lastUpdated, currentTimestamp);
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
			
			List<Object[]> regCenterMachines = registrationCenterMachineRepository.getRegistrationCenterMachineWithKeyIndex(keyIndex);
			
			if (regCenterMachines.isEmpty()) {
				throw new RequestException(MasterDataErrorCode.INVALID_KEY_INDEX.getErrorCode(),
						MasterDataErrorCode.INVALID_KEY_INDEX.getErrorMessage());
			}
			
			String mappedRegCenterId = (String)((Object[])regCenterMachines.get(0))[0];
			
			if(mappedRegCenterId == null || ( registrationCenterId != null &&  !mappedRegCenterId.equals(registrationCenterId) ) )
				throw new RequestException(MasterDataErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorCode(),
						MasterDataErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorMessage());
			
			return new RegistrationCenterMachineDto(mappedRegCenterId, (String)((Object[])regCenterMachines.get(0))[1]);
			
					
		} catch (DataAccessException | DataAccessLayerException e) {
			logger.error("Failed to fetch registrationCenterMachine : ", e);
		}
		
		throw new SyncDataServiceException(MasterDataErrorCode.REG_CENTER_MACHINE_FETCH_EXCEPTION.getErrorCode(),
				MasterDataErrorCode.REG_CENTER_MACHINE_FETCH_EXCEPTION.getErrorMessage());
	}

}
