package io.mosip.kernel.syncdata.dto.response;

import java.util.List;

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
import io.mosip.kernel.syncdata.dto.ValidDocumentDto;
import lombok.Data;

/**
 * Masterdata sync handler response dto
 * 
 * @author Abhishek Kumar
 * @author Neha
 * @since 1.0.0
 *
 */
@Data
public class MasterDataResponseDto {
	private String lastSyncTime;
	private List<RegistrationCenterDto> registrationCenter;
	private List<RegistrationCenterTypeDto> registrationCenterTypes;
	private List<MachineDto> machineDetails;
	private List<MachineSpecificationDto> machineSpecification;
	private List<MachineTypeDto> machineType;
	private List<DeviceDto> devices;
	private List<DeviceTypeDto> deviceTypes;
	private List<DeviceSpecificationDto> deviceSpecifications;
	private List<HolidayDto> holidays;
	private List<DocumentCategoryDto> documentCategories;
	private List<DocumentTypeDto> documentTypes;
	private List<ValidDocumentDto> validDocumentMapping;
	private List<TemplateDto> templates;
	private List<TemplateTypeDto> templatesTypes;
	private List<TemplateFileFormatDto> templateFileFormat;
	private List<PostReasonCategoryDto> reasonCategory;
	private List<ReasonListDto> reasonList;
	private List<BlacklistedWordsDto> blackListedWords;
	private List<LocationDto> locationHierarchy;
	private List<BiometricAttributeDto> biometricattributes;
	private List<BiometricTypeDto> biometricTypes;
	private List<ApplicationDto> applications;
	private List<IdTypeDto> idTypes;
	private List<TitleDto> titles;
	private List<GenderDto> genders;
	private List<LanguageDto> languages;
	private List<ApplicantValidDocumentDto> applicantValidDocuments;
	private List<IndividualTypeDto> individualTypes;
	private List<AppAuthenticationMethodDto> appAuthenticationMethods;
	private List<AppDetailDto> appDetails;
	private List<AppRolePriorityDto> appRolePriorities;
	private List<ScreenAuthorizationDto> screenAuthorizations;
	private List<ProcessListDto> processList;
	private List<SyncJobDefDto> syncJobDefinitions;
	private List<ScreenDetailDto> screenDetails;
	private List<DeviceProviderDto> deviceProviders = null;
	private List<DeviceServiceDto> deviceServices = null;
	private List<RegisteredDeviceDto> registeredDevices = null;
	private List<FoundationalTrustProviderDto> functionalTrustProviders = null;
	private List<DeviceTypeDPMDto> deviceTypeDPMs = null;
	private List<DeviceSubTypeDPMDto> deviceSubTypeDPMs = null;

	private List<RegistrationCenterMachineDto> registrationCenterMachines;
	private List<RegistrationCenterDeviceDto> registrationCenterDevices;
	private List<RegistrationCenterMachineDeviceDto> registrationCenterMachineDevices;
	private List<RegistrationCenterUserMachineMappingDto> registrationCenterUserMachines;
	private List<RegistrationCenterUserDto> registrationCenterUsers;
	private List<RegistrationCenterMachineHistoryDto> registrationCenterMachineHistory;
	private List<RegistrationCenterDeviceHistoryDto> registrationCenterDeviceHistory;
	private List<RegistrationCenterMachineDeviceHistoryDto> registrationCenterMachineDeviceHistory;
	private List<RegistrationCenterUserMachineMappingHistoryDto> registrationCenterUserMachineMappingHistory;
	private List<RegistrationCenterUserHistoryDto> registrationCenterUserHistory;

}
