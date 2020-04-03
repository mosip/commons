package io.mosip.kernel.syncdata.test.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;


import io.mosip.kernel.core.signatureutil.model.SignatureResponse;
import io.mosip.kernel.core.signatureutil.spi.SignatureUtil;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.syncdata.entity.AppAuthenticationMethod;
import io.mosip.kernel.syncdata.entity.AppDetail;
import io.mosip.kernel.syncdata.entity.AppRolePriority;
import io.mosip.kernel.syncdata.entity.ApplicantValidDocument;
import io.mosip.kernel.syncdata.entity.Application;
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
import io.mosip.kernel.syncdata.entity.MachineSpecification;
import io.mosip.kernel.syncdata.entity.MachineType;
import io.mosip.kernel.syncdata.entity.ProcessList;
import io.mosip.kernel.syncdata.entity.ReasonCategory;
import io.mosip.kernel.syncdata.entity.ReasonList;
import io.mosip.kernel.syncdata.entity.RegisteredDevice;
import io.mosip.kernel.syncdata.entity.RegistrationCenter;
import io.mosip.kernel.syncdata.entity.RegistrationCenterDevice;
import io.mosip.kernel.syncdata.entity.RegistrationCenterDeviceHistory;
import io.mosip.kernel.syncdata.entity.RegistrationCenterDeviceHistoryPk;
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
import io.mosip.kernel.syncdata.entity.Template;
import io.mosip.kernel.syncdata.entity.TemplateFileFormat;
import io.mosip.kernel.syncdata.entity.TemplateType;
import io.mosip.kernel.syncdata.entity.Title;
import io.mosip.kernel.syncdata.entity.ValidDocument;
import io.mosip.kernel.syncdata.entity.id.ApplicantValidDocumentID;
import io.mosip.kernel.syncdata.entity.id.CodeAndLanguageCodeID;
import io.mosip.kernel.syncdata.entity.id.HolidayID;
import io.mosip.kernel.syncdata.entity.id.RegistrationCenterDeviceID;
import io.mosip.kernel.syncdata.entity.id.RegistrationCenterMachineDeviceHistoryID;
import io.mosip.kernel.syncdata.entity.id.RegistrationCenterMachineDeviceID;
import io.mosip.kernel.syncdata.entity.id.RegistrationCenterMachineHistoryID;
import io.mosip.kernel.syncdata.entity.id.RegistrationCenterMachineID;
import io.mosip.kernel.syncdata.entity.id.RegistrationCenterMachineUserID;
import io.mosip.kernel.syncdata.entity.id.RegistrationCenterUserID;
import io.mosip.kernel.syncdata.repository.AppAuthenticationMethodRepository;
import io.mosip.kernel.syncdata.repository.AppDetailRepository;
import io.mosip.kernel.syncdata.repository.AppRolePriorityRepository;
import io.mosip.kernel.syncdata.repository.ApplicantValidDocumentRespository;
import io.mosip.kernel.syncdata.repository.ApplicationRepository;
import io.mosip.kernel.syncdata.repository.BiometricAttributeRepository;
import io.mosip.kernel.syncdata.repository.BiometricTypeRepository;
import io.mosip.kernel.syncdata.repository.BlacklistedWordsRepository;
import io.mosip.kernel.syncdata.repository.DeviceProviderRepository;
import io.mosip.kernel.syncdata.repository.DeviceRepository;
import io.mosip.kernel.syncdata.repository.DeviceServiceRepository;
import io.mosip.kernel.syncdata.repository.DeviceSpecificationRepository;
import io.mosip.kernel.syncdata.repository.DeviceSubTypeDPMRepository;
import io.mosip.kernel.syncdata.repository.DeviceTypeDPMRepository;
import io.mosip.kernel.syncdata.repository.DeviceTypeRepository;
import io.mosip.kernel.syncdata.repository.DocumentCategoryRepository;
import io.mosip.kernel.syncdata.repository.DocumentTypeRepository;
import io.mosip.kernel.syncdata.repository.FoundationalTrustProviderRepository;
import io.mosip.kernel.syncdata.repository.GenderRepository;
import io.mosip.kernel.syncdata.repository.HolidayRepository;
import io.mosip.kernel.syncdata.repository.IdTypeRepository;
import io.mosip.kernel.syncdata.repository.IndividualTypeRepository;
import io.mosip.kernel.syncdata.repository.LanguageRepository;
import io.mosip.kernel.syncdata.repository.LocationRepository;
import io.mosip.kernel.syncdata.repository.MachineHistoryRepository;
import io.mosip.kernel.syncdata.repository.MachineRepository;
import io.mosip.kernel.syncdata.repository.MachineSpecificationRepository;
import io.mosip.kernel.syncdata.repository.MachineTypeRepository;
import io.mosip.kernel.syncdata.repository.ProcessListRepository;
import io.mosip.kernel.syncdata.repository.ReasonCategoryRepository;
import io.mosip.kernel.syncdata.repository.ReasonListRepository;
import io.mosip.kernel.syncdata.repository.RegisteredDeviceRepository;
import io.mosip.kernel.syncdata.repository.RegistrationCenterDeviceHistoryRepository;
import io.mosip.kernel.syncdata.repository.RegistrationCenterDeviceRepository;
import io.mosip.kernel.syncdata.repository.RegistrationCenterMachineDeviceHistoryRepository;
import io.mosip.kernel.syncdata.repository.RegistrationCenterMachineDeviceRepository;
import io.mosip.kernel.syncdata.repository.RegistrationCenterMachineHistoryRepository;
import io.mosip.kernel.syncdata.repository.RegistrationCenterMachineRepository;
import io.mosip.kernel.syncdata.repository.RegistrationCenterRepository;
import io.mosip.kernel.syncdata.repository.RegistrationCenterTypeRepository;
import io.mosip.kernel.syncdata.repository.RegistrationCenterUserHistoryRepository;
import io.mosip.kernel.syncdata.repository.RegistrationCenterUserMachineHistoryRepository;
import io.mosip.kernel.syncdata.repository.RegistrationCenterUserMachineRepository;
import io.mosip.kernel.syncdata.repository.RegistrationCenterUserRepository;
import io.mosip.kernel.syncdata.repository.ScreenAuthorizationRepository;
import io.mosip.kernel.syncdata.repository.ScreenDetailRepository;
import io.mosip.kernel.syncdata.repository.TemplateFileFormatRepository;
import io.mosip.kernel.syncdata.repository.TemplateRepository;
import io.mosip.kernel.syncdata.repository.TemplateTypeRepository;
import io.mosip.kernel.syncdata.repository.TitleRepository;
import io.mosip.kernel.syncdata.repository.ValidDocumentRepository;
import io.mosip.kernel.syncdata.test.TestBootApplication;
import io.mosip.kernel.syncdata.utils.MapperUtils;

@SpringBootTest(classes = TestBootApplication.class)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class SyncClientSettingsIntegrationTest {
		
	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private MapperUtils mapper;
	
	private Machine machine;
	private List<Application> applications;
	private List<Machine> machines;
	private List<MachineSpecification> machineSpecification;
	private List<MachineType> machineType;
	private List<RegistrationCenter> registrationCenters;
	private List<RegistrationCenterType> registrationCenterType;
	private List<Device> devices;
	private List<DeviceSpecification> deviceSpecification;
	private List<DeviceType> deviceType;
	private List<Holiday> holidays;
	private List<BlacklistedWords> blackListedWords;
	private List<Title> titles;
	private List<Gender> genders;
	private List<Language> languages;
	private List<Template> templates;
	private List<TemplateFileFormat> templateFileFormats;
	private List<TemplateType> templateTypes;
	private List<BiometricAttribute> biometricAttributes;
	private List<BiometricType> biometricTypes;
	private List<DocumentCategory> documentCategories;
	private List<DocumentType> documentTypes;
	private List<ValidDocument> validDocuments;
	private List<ReasonCategory> reasonCategories;
	private List<ReasonList> reasonLists;
	private List<IdType> idTypes;
	private List<Location> locations;
	private List<RegistrationCenterMachine> registrationCenterMachines;
	private List<RegistrationCenterDevice> registrationCenterDevices;
	private List<RegistrationCenterMachineDevice> registrationCenterMachineDevices;
	private List<RegistrationCenterUserMachine> registrationCenterUserMachines;
	private List<RegistrationCenterUser> registrationCenterUsers;
	private List<RegistrationCenterMachineHistory> registrationCenterMachineHistory;
	private List<RegistrationCenterDeviceHistory> registrationCenterDeviceHistory;
	private List<RegistrationCenterUserHistory> registrationCenterUserHistory;
	private List<RegistrationCenterMachineDeviceHistory> registrationCenterMachineDeviceHistory;
	private List<RegistrationCenterUserMachineHistory> registrationCenterUserMachineHistory;
	private List<ApplicantValidDocument> applicantValidDocumentList;
	private List<IndividualType> individualTypeList;
	private List<Object[]> objectArrayList;
	private List<AppAuthenticationMethod> appAuthenticationMethods = null;
	private List<AppDetail> appDetails = null;
	private List<AppRolePriority> appRolePriorities = null;
	private List<ScreenAuthorization> screenAuthorizations = null;
	private List<ProcessList> processList = null;
	private List<ScreenDetail> screenDetailList = null;
	private DeviceService deviceService;
	private DeviceProvider deviceProvider;
	private RegisteredDevice registeredDevice;
	private DeviceTypeDPM deviceTypeDPM;
	private DeviceSubTypeDPM deviceSubTypeDPM;
	private FoundationalTrustProvider foundationalTrustProvider;
	
	@MockBean
	private ApplicationRepository applicationRepository;
	@MockBean
	private MachineRepository machineRepository;
	@MockBean
	private MachineTypeRepository machineTypeRepository;
	@MockBean
	private RegistrationCenterRepository registrationCenterRepository;
	@MockBean
	private RegistrationCenterTypeRepository registrationCenterTypeRepository;
	@MockBean
	private TemplateRepository templateRepository;
	@MockBean
	private TemplateFileFormatRepository templateFileFormatRepository;
	@MockBean
	private ReasonCategoryRepository reasonCategoryRepository;
	@MockBean
	private HolidayRepository holidayRepository;
	@MockBean
	private BlacklistedWordsRepository blacklistedWordsRepository;
	@MockBean
	private BiometricTypeRepository biometricTypeRepository;
	@MockBean
	private BiometricAttributeRepository biometricAttributeRepository;
	@MockBean
	private TitleRepository titleRepository;
	@MockBean
	private LanguageRepository languageRepository;
	@MockBean
	private GenderRepository genderTypeRepository;
	@MockBean
	private DeviceRepository deviceRepository;
	@MockBean
	private DocumentCategoryRepository documentCategoryRepository;
	@MockBean
	private DocumentTypeRepository documentTypeRepository;
	@MockBean
	private IdTypeRepository idTypeRepository;
	@MockBean
	private DeviceSpecificationRepository deviceSpecificationRepository;
	@MockBean
	private LocationRepository locationRepository;
	@MockBean
	private TemplateTypeRepository templateTypeRepository;
	@MockBean
	private MachineSpecificationRepository machineSpecificationRepository;
	@MockBean
	private DeviceTypeRepository deviceTypeRepository;
	@MockBean
	private ValidDocumentRepository validDocumentRepository;
	@MockBean
	private ReasonListRepository reasonListRepository;
	@MockBean
	private RegistrationCenterMachineRepository registrationCenterMachineRepository;
	@MockBean
	private RegistrationCenterDeviceRepository registrationCenterDeviceRepository;
	@MockBean
	private RegistrationCenterMachineDeviceRepository registrationCenterMachineDeviceRepository;
	@MockBean
	private RegistrationCenterUserMachineRepository registrationCenterUserMachineRepository;
	@MockBean
	private RegistrationCenterUserHistoryRepository registrationCenterUserHistoryRepository;
	@MockBean
	private RegistrationCenterUserMachineHistoryRepository registrationCenterUserMachineHistoryRepository;
	@MockBean
	private RegistrationCenterMachineDeviceHistoryRepository registrationCenterMachineDeviceHistoryRepository;
	@MockBean
	private RegistrationCenterDeviceHistoryRepository registrationCenterDeviceHistoryRepository;
	@MockBean
	private RegistrationCenterMachineHistoryRepository registrationCenterMachineHistoryRepository;

	@MockBean
	private AppAuthenticationMethodRepository appAuthenticationMethodRepository;
	@MockBean
	private AppDetailRepository appDetailRepository;
	@MockBean
	private AppRolePriorityRepository appRolePriorityRepository;
	@MockBean
	private ScreenAuthorizationRepository screenAuthorizationRepository;
	@MockBean
	private ProcessListRepository processListRepository;

	@MockBean
	private MachineHistoryRepository machineHistoryRepository;

	@MockBean
	private ScreenDetailRepository screenDetailRepo;
	@MockBean
	private SignatureUtil signatureUtil;
	@MockBean
	private DeviceProviderRepository deviceProviderRepository;
	@MockBean
	private DeviceServiceRepository deviceServiceRepository;
	@MockBean
	private RegisteredDeviceRepository registeredDeviceRepository;
	@MockBean
	private FoundationalTrustProviderRepository foundationalTrustProviderRepository;
	@MockBean
	private DeviceTypeDPMRepository deviceTypeDPMRepository;
	@MockBean
	private DeviceSubTypeDPMRepository deviceSubTypeDPMRepository;	
	@MockBean
	private RegistrationCenterUserRepository registrationCenterUserRepository;
	@MockBean
	private ApplicantValidDocumentRespository applicantValidDocumentRespository;
	@MockBean
	private IndividualTypeRepository individualTypeRepository;
	
	
	private String encodedTPMPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAn4A-U6V4SpSeJmjl0xtBDgyFaHn1CvglvnpbczxiDakH6ks8tPvIYT4jDOU-9XaUYKuMFhLxS7G8qwJhv7GKpDQXphSXjgwv_l8A--KV6C1UVaHoAs4XuJPFdXneSd9uMH94GO6lWucyOyfaZLrf5F_--2Rr4ba4rBWw20OrAl1c7FrzjIQjzYXgnBMrvETXptxKKrMELwOOsuyc1Ju4wzPJHYjI0Em4q2BOcQLXqYjhsZhcYeTqBFxXjCOM3WQKLCIsh9RN8Hz-s8yJbQId6MKIS7HQNCTbhbjl1jdfwqRwmBaZz0Gt73I4_8SVCcCQzJWVsakLC1oJAFcmi3l_mQIDAQAB";
	private byte[] tpmPublicKey = CryptoUtil.decodeBase64(encodedTPMPublicKey);
	private String keyIndex = CryptoUtil.computeFingerPrint(tpmPublicKey, null);
	
	private SignatureResponse signResponse;
				
	@Before
	public void setup() {
		
		LocalDateTime localdateTime = LocalDateTime.parse("2018-11-01T01:01:01");
		LocalTime localTime = LocalTime.parse("09:00:00");
		applications = new ArrayList<>();
		applications.add(new Application("101", "ENG", "MOSIP", "MOSIP"));
		machines = new ArrayList<>();
		machine = new Machine("1001", "Laptop", "9876427", "172.12.01.128", "21:21:21:12", "1001", "ENG", localdateTime,
				tpmPublicKey, keyIndex, "ZONE", null);
		machines.add(machine);
		machineSpecification = new ArrayList<>();
		machineSpecification.add(
				new MachineSpecification("1001", "Laptop", "Lenovo", "T480", "1001", "1.0", "Laptop", "ENG", null));
		machineType = new ArrayList<>();
		machineType.add(new MachineType("1001", "ENG", "System", "System"));
		devices = new ArrayList<>();
		Device device = new Device();
		device.setId("1000");
		device.setName("Printer");
		device.setLangCode("eng");
		device.setIsActive(true);
		device.setMacAddress("127.0.0.0");
		device.setIpAddress("127.0.0.10");
		device.setSerialNum("234");
		device.setDeviceSpecId("234");
		device.setValidityDateTime(localdateTime);
		devices.add(device);

		deviceSpecification = new ArrayList<>();
		deviceSpecification.add(new DeviceSpecification("1011", "SP-1011", "HP", "E1011", "T1011", "1.0", "HP-SP1011",
				"Hp Printer", null));
		deviceType = new ArrayList<>();
		deviceType.add(new DeviceType("T1011", "ENG", "device", "deviceDescriptiom"));
		registrationCenters = new ArrayList<>();
		RegistrationCenter registrationCenter = new RegistrationCenter();
		registrationCenter.setId("1011");
		registrationCenter.setAddressLine1("address-line1");
		registrationCenter.setAddressLine2("address-line2");
		registrationCenter.setAddressLine3("address-line3");
		registrationCenter.setCenterEndTime(localTime);
		registrationCenter.setCenterStartTime(localTime);
		registrationCenter.setCenterTypeCode("T1011");
		registrationCenter.setContactPerson("admin");
		registrationCenter.setContactPhone("9865123456");
		registrationCenter.setHolidayLocationCode("LOC01");
		registrationCenter.setIsActive(true);
		registrationCenter.setLangCode("ENG");
		registrationCenter.setWorkingHours("9");
		registrationCenter.setLunchEndTime(localTime);
		registrationCenter.setLunchStartTime(localTime);
		registrationCenters.add(registrationCenter);

		registrationCenterType = new ArrayList<>();
		RegistrationCenterType regCenterType = new RegistrationCenterType();
		regCenterType.setCode("T01");
		registrationCenterType.add(regCenterType);

		templates = new ArrayList<>();
		Template template = new Template();
		template.setId("T222");
		template.setLangCode("eng");
		template.setName("Email template");
		template.setTemplateTypeCode("EMAIL");
		template.setFileFormatCode("XML");
		template.setModuleId("preregistation");
		template.setIsActive(Boolean.TRUE);
		templates.add(template);
		templateFileFormats = new ArrayList<>();
		templateFileFormats.add(new TemplateFileFormat("T101", "ENG", "Email"));
		templateTypes = new ArrayList<>();
		templateTypes.add(new TemplateType("T101", "ENG", "Description"));
		holidays = new ArrayList<>();
		Holiday holiday = new Holiday();
		LocalDate date = LocalDate.of(2018, Month.NOVEMBER, 7);
		holiday = new Holiday();
		holiday.setHolidayId(new HolidayID("KAR", date, "eng", "Diwali"));
		holiday.setId(1);
		holiday.setCreatedBy("John");
		holiday.setCreatedDateTime(localdateTime);
		holiday.setHolidayDesc("Diwali");
		holiday.setIsActive(true);

		Holiday holiday2 = new Holiday();
		holiday2.setHolidayId(new HolidayID("KAH", date, "eng", "Durga Puja"));
		holiday2.setId(1);
		holiday2.setCreatedBy("John");
		holiday2.setCreatedDateTime(localdateTime);
		holiday2.setHolidayDesc("Diwali");
		holiday2.setIsActive(true);

		holidays.add(holiday);
		holidays.add(holiday2);
		blackListedWords = new ArrayList<>();
		blackListedWords.add(new BlacklistedWords("ABC", "ENG", "description"));
		titles = new ArrayList<>();
		titles.add(new Title(new CodeAndLanguageCodeID("1011", "ENG"), "title", "titleDescription"));
		genders = new ArrayList<>();
		genders.add(new Gender("G1011", "MALE", "description"));
		languages = new ArrayList<>();
		languages.add(new Language("ENG", "english", "family", "native name"));
		idTypes = new ArrayList<>();
		idTypes.add(new IdType("ID101", "ENG", "ID", "descr"));
		validDocuments = new ArrayList<>();
		validDocuments.add(new ValidDocument("D101", "DC101", null, null, "ENG"));
		biometricAttributes = new ArrayList<>();
		biometricAttributes.add(new BiometricAttribute("B101", "101", "Fingerprint", "description", "BT101", null));
		biometricTypes = new ArrayList<>();
		biometricTypes.add(new BiometricType("BT101", "ENG", "name", "description"));
		documentCategories = new ArrayList<>();
		documentCategories.add(new DocumentCategory("DC101", "ENG", "DC name", "description"));
		documentTypes = new ArrayList<>();
		documentTypes.add(new DocumentType("DT101", "ENG", "DT Type", "description"));
		reasonCategories = new ArrayList<>();
		reasonCategories.add(new ReasonCategory("RC101", "101", "R-1", "description", null));
		reasonLists = new ArrayList<>();
		reasonLists.add(new ReasonList("RL101", "RL1", "ENG", "RL", "description", null));
		locations = new ArrayList<>();
		Location locationHierarchy = new Location();
		locationHierarchy.setCode("PAT");
		locationHierarchy.setName("PATANA");
		locationHierarchy.setHierarchyLevel(2);
		locationHierarchy.setHierarchyName("Distic");
		locationHierarchy.setParentLocCode("BHR");
		locationHierarchy.setLangCode("ENG");
		locationHierarchy.setCreatedBy("admin");
		locationHierarchy.setUpdatedBy("admin");
		locationHierarchy.setIsActive(true);
		locations.add(locationHierarchy);
		registrationCenterMachines = new ArrayList<>();
		RegistrationCenterMachineID rmId = new RegistrationCenterMachineID();
		rmId.setMachineId("10001");
		rmId.setRegCenterId("10001");
		RegistrationCenterMachine registrationCenterMachine = new RegistrationCenterMachine();
		registrationCenterMachine.setRegistrationCenterMachinePk(rmId);
		registrationCenterMachine.setIsActive(true);
		registrationCenterMachine.setLangCode("eng");
		registrationCenterMachine.setCreatedBy("admin");
		registrationCenterMachine.setCreatedDateTime(LocalDateTime.now(ZoneId.of("UTC")));
		registrationCenterMachine.setIsDeleted(false);
		registrationCenterMachines.add(registrationCenterMachine);
		registrationCenterDevices = new ArrayList<>();
		RegistrationCenterDevice registrationCenterDevice = new RegistrationCenterDevice();
		RegistrationCenterDeviceID rcId = new RegistrationCenterDeviceID();
		rcId.setDeviceId("10001");
		rcId.setRegCenterId("10001");
		registrationCenterDevice.setRegistrationCenterDevicePk(rcId);
		registrationCenterDevice.setIsActive(true);
		registrationCenterDevice.setLangCode("eng");
		registrationCenterDevice.setCreatedBy("admin");
		registrationCenterDevice.setCreatedDateTime(LocalDateTime.now(ZoneId.of("UTC")));
		registrationCenterDevice.setIsDeleted(false);
		registrationCenterDevices.add(registrationCenterDevice);
		RegistrationCenterMachineDevice registrationCenterMachineDevice = new RegistrationCenterMachineDevice();
		RegistrationCenterMachineDeviceID rcmdId = new RegistrationCenterMachineDeviceID();
		rcmdId.setDeviceId("101");
		rcmdId.setMachineId("1789");
		rcmdId.setRegCenterId("1");
		registrationCenterMachineDevice.setRegistrationCenterMachineDevicePk(rcmdId);
		registrationCenterMachineDevice.setIsActive(true);
		registrationCenterMachineDevice.setLangCode("eng");
		registrationCenterMachineDevice.setCreatedDateTime(LocalDateTime.now(ZoneId.of("UTC")));
		registrationCenterMachineDevice.setCreatedBy("admin");

		registrationCenterMachineDevices = new ArrayList<>();
		registrationCenterMachineDevices.add(registrationCenterMachineDevice);
		RegistrationCenterUserMachine registrationCenterUserMachine = new RegistrationCenterUserMachine();
		RegistrationCenterMachineUserID registrationCenterMachineUserID = new RegistrationCenterMachineUserID();
		registrationCenterMachineUserID.setCntrId("REG001");
		registrationCenterMachineUserID.setUsrId("QC001");
		registrationCenterMachineUserID.setMachineId("MAC001");
		registrationCenterUserMachine.setLangCode("eng");
		registrationCenterUserMachine.setRegistrationCenterMachineUserID(registrationCenterMachineUserID);
		registrationCenterUserMachines = new ArrayList<>();
		registrationCenterUserMachines.add(registrationCenterUserMachine);
		registrationCenterUsers = new ArrayList<>();
		registrationCenterUsers.add(new RegistrationCenterUser(new RegistrationCenterUserID("01010", "qc001"), "eng"));

		registrationCenterDeviceHistory = new ArrayList<>();
		registrationCenterDeviceHistory.add(new RegistrationCenterDeviceHistory(
				new RegistrationCenterDeviceHistoryPk("1001", "1001", LocalDateTime.now()), "eng"));

		registrationCenterMachineDeviceHistory = new ArrayList<>();
		registrationCenterMachineDeviceHistory.add(new RegistrationCenterMachineDeviceHistory(
				new RegistrationCenterMachineDeviceHistoryID("1001", "1001", "1001", LocalDateTime.now()), "eng"));

		registrationCenterMachineHistory = new ArrayList<>();
		registrationCenterMachineHistory.add(new RegistrationCenterMachineHistory(
				new RegistrationCenterMachineHistoryID("1001", "1001", LocalDateTime.now()), "eng"));

		registrationCenterUserHistory = new ArrayList<>();
		registrationCenterUserHistory
				.add(new RegistrationCenterUserHistory("1001", "1001", LocalDateTime.now(), "eng"));

		registrationCenterUserMachineHistory = new ArrayList<>();
		registrationCenterUserMachineHistory
				.add(new RegistrationCenterUserMachineHistory("10001", "1001", "10001", LocalDateTime.now(), "eng"));

		registrationCenterDeviceHistory = new ArrayList<>();
		registrationCenterDeviceHistory.add(new RegistrationCenterDeviceHistory(
				new RegistrationCenterDeviceHistoryPk("1001", "1001", LocalDateTime.now()), "eng"));

		registrationCenterMachineDeviceHistory = new ArrayList<>();
		registrationCenterMachineDeviceHistory.add(new RegistrationCenterMachineDeviceHistory(
				new RegistrationCenterMachineDeviceHistoryID("1001", "1001", "1001", LocalDateTime.now()), "eng"));

		registrationCenterMachineHistory = new ArrayList<>();
		registrationCenterMachineHistory.add(new RegistrationCenterMachineHistory(
				new RegistrationCenterMachineHistoryID("1001", "1001", LocalDateTime.now()), "eng"));

		registrationCenterUserHistory = new ArrayList<>();
		registrationCenterUserHistory
				.add(new RegistrationCenterUserHistory("1001", "1001", LocalDateTime.now(), "eng"));

		registrationCenterUserMachineHistory = new ArrayList<>();
		registrationCenterUserMachineHistory
				.add(new RegistrationCenterUserMachineHistory("10001", "1001", "10001", LocalDateTime.now(), "eng"));
		IndividualType individualType = new IndividualType();
		CodeAndLanguageCodeID codeLangCode = new CodeAndLanguageCodeID();
		codeLangCode.setCode("FR");
		codeLangCode.setLangCode("ENG");
		individualType.setName("Foreigner");
		individualType.setCodeAndLanguageCodeId(codeLangCode);
		individualTypeList = new ArrayList<>();
		individualTypeList.add(individualType);
		ApplicantValidDocument applicantValidDoc = new ApplicantValidDocument();
		ApplicantValidDocumentID appId = new ApplicantValidDocumentID();
		appId.setAppTypeCode("001");
		appId.setDocCatCode("POA");
		appId.setDocTypeCode("RNC");
		applicantValidDoc.setApplicantValidDocumentId(appId);
		applicantValidDoc.setLangCode("eng");
		applicantValidDocumentList = new ArrayList<>();
		applicantValidDocumentList.add(applicantValidDoc);
		Object[] objects = { "10001", "10001" };
		objectArrayList = new ArrayList<>();
		objectArrayList.add(objects);
		AppAuthenticationMethod appAuthenticationMethod = new AppAuthenticationMethod();
		appAuthenticationMethod.setAppId("REGISTRATION");
		appAuthenticationMethod.setAuthMethodCode("sddd");
		appAuthenticationMethod.setMethodSequence(1000);
		appAuthenticationMethods = new ArrayList<>();
		appAuthenticationMethods.add(appAuthenticationMethod);
		AppDetail appDetail = new AppDetail();
		appDetail.setDescr("reg");
		appDetail.setId("1");
		appDetail.setLangCode("eng");
		appDetail.setName("reg");
		appDetails = new ArrayList<>();
		appDetails.add(appDetail);
		AppRolePriority appRolePriority = new AppRolePriority();
		appRolePriority.setAppId("10001");
		appRolePriority.setLangCode("eng");
		appRolePriority.setPriority(1);
		appRolePriority.setProcessId("login_auth");
		appRolePriority.setRoleCode("OFFICER");
		appRolePriorities = new ArrayList<>();
		appRolePriorities.add(appRolePriority);
		ScreenAuthorization screenAuthorization = new ScreenAuthorization();
		screenAuthorization.setIsPermitted(true);
		screenAuthorization.setRoleCode("OFFICER");
		screenAuthorization.setScreenId("loginroot");
		screenAuthorizations = new ArrayList<>();
		screenAuthorizations.add(screenAuthorization);
		ProcessList processListObj = new ProcessList();
		processListObj.setDescr("Packet authentication");
		processListObj.setName("packet authentication");
		processListObj.setLangCode("eng");
		processList = new ArrayList<>();
		processList.add(processListObj);
		ScreenDetail screenDetail = new ScreenDetail();
		screenDetail.setAppId("REGISTRATION");
		screenDetail.setId("REG");
		screenDetail.setDescr("registration");
		screenDetail.setLangCode("eng");
		screenDetailList = new ArrayList<>();
		screenDetailList.add(screenDetail);

		deviceService = new DeviceService();
		deviceService.setId("1111");
		deviceService.setDProviderId("10001");
		deviceService.setSwVersion("0.1v");

		deviceProvider = new DeviceProvider();
		deviceProvider.setId("1111");

		registeredDevice = new RegisteredDevice();
		registeredDevice.setDeviceId("10001");
		registeredDevice.setStatusCode("Registered");
		registeredDevice.setExpiryDate(LocalDateTime.now());

		deviceTypeDPM = new DeviceTypeDPM();
		deviceTypeDPM.setCode("1111");
		deviceTypeDPM.setName("devicetype");

		deviceSubTypeDPM = new DeviceSubTypeDPM();
		deviceSubTypeDPM.setCode("1234");
		deviceSubTypeDPM.setDtypeCode("1111");
		deviceSubTypeDPM.setName("deviceSubType");

		foundationalTrustProvider = new FoundationalTrustProvider();
		foundationalTrustProvider.setId("11111");
		foundationalTrustProvider.setName("ftps");
		
		signResponse = new SignatureResponse();
		signResponse.setData("asdasdsadf4e");
		signResponse.setTimestamp(LocalDateTime.now(ZoneOffset.UTC));
	}

	private void mockSuccess() {
		when(registrationCenterMachineRepository
				.getRegistrationCenterMachineWithKeyIndex(Mockito.anyString()))
						.thenReturn(objectArrayList);
				
		when(registrationCenterRepository.findRegistrationCenterByIdAndIsActiveIsTrue(Mockito.anyString()))
				.thenReturn(registrationCenters);
		
		when(registrationCenterMachineRepository.getRegCenterIdWithRegIdAndMachineId(Mockito.anyString(),
				Mockito.anyString())).thenReturn(registrationCenterMachines.get(0));
		
		when(applicationRepository.findAll()).thenReturn(applications);
		when(applicationRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(), Mockito.any()))
				.thenReturn(applications);
		when(machineRepository.findMachineById(Mockito.anyString())).thenReturn(machines);
		when(machineRepository.findAllLatestCreatedUpdateDeleted(Mockito.anyString(), Mockito.any(), Mockito.any()))
				.thenReturn(machines);
		when(machineSpecificationRepository.findByMachineId(Mockito.anyString())).thenReturn(machineSpecification);
		when(machineSpecificationRepository.findLatestByRegCenterId(Mockito.anyString(), Mockito.any(), Mockito.any()))
				.thenReturn(machineSpecification);
		when(machineTypeRepository.findAllByMachineId(Mockito.anyString())).thenReturn(machineType);
		when(machineTypeRepository.findLatestByRegCenterId(Mockito.anyString(), Mockito.any(), Mockito.any()))
				.thenReturn(machineType);
		when(templateRepository.findAll()).thenReturn(templates);
		when(templateRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(), Mockito.any())).thenReturn(templates);
		when(templateFileFormatRepository.findAllTemplateFormat()).thenReturn(templateFileFormats);
		when(templateFileFormatRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(), Mockito.any()))
				.thenReturn(templateFileFormats);
		when(templateTypeRepository.findAll()).thenReturn(templateTypes);
		when(templateTypeRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(), Mockito.any()))
				.thenReturn(templateTypes);
		when(holidayRepository.findAllByMachineId(Mockito.anyString())).thenReturn(holidays);
		when(holidayRepository.findAllLatestCreatedUpdateDeletedByMachineId(Mockito.anyString(), Mockito.any(),
				Mockito.any())).thenReturn(holidays);
		when(blacklistedWordsRepository.findAll()).thenReturn(blackListedWords);
		when(blacklistedWordsRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(), Mockito.any()))
				.thenReturn(blackListedWords);
		when(registrationCenterRepository.findRegistrationCenterByMachineId(Mockito.anyString()))
				.thenReturn(registrationCenters);
		when(registrationCenterRepository.findLatestRegistrationCenterByMachineId(Mockito.anyString(), Mockito.any(),
				Mockito.any())).thenReturn(registrationCenters);
		when(registrationCenterTypeRepository.findRegistrationCenterTypeByMachineId(Mockito.anyString()))
				.thenReturn(registrationCenterType);
		when(registrationCenterTypeRepository.findLatestRegistrationCenterTypeByMachineId(Mockito.anyString(),
				Mockito.any(), Mockito.any())).thenReturn(registrationCenterType);
		when(genderTypeRepository.findAll()).thenReturn(genders);
		when(genderTypeRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(), Mockito.any())).thenReturn(genders);
		when(idTypeRepository.findAll()).thenReturn(idTypes);
		when(idTypeRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(), Mockito.any())).thenReturn(idTypes);
		when(deviceRepository.findDeviceByMachineId(Mockito.anyString())).thenReturn(devices);
		when(deviceRepository.findLatestDevicesByRegCenterId(Mockito.anyString(), Mockito.any(), Mockito.any()))
				.thenReturn(devices);
		when(deviceSpecificationRepository.findDeviceTypeByMachineId(Mockito.anyString()))
				.thenReturn(deviceSpecification);
		when(deviceSpecificationRepository.findLatestDeviceTypeByRegCenterId(Mockito.anyString(), Mockito.any(),
				Mockito.any())).thenReturn(deviceSpecification);
		when(deviceTypeRepository.findDeviceTypeByMachineId(Mockito.anyString())).thenReturn(deviceType);
		when(deviceTypeRepository.findLatestDeviceTypeByRegCenterId(Mockito.anyString(), Mockito.any(), Mockito.any()))
				.thenReturn(deviceType);
		when(languageRepository.findAll()).thenReturn(languages);
		when(languageRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(), Mockito.any())).thenReturn(languages);
		when(reasonCategoryRepository.findAllReasons()).thenReturn(reasonCategories);
		when(reasonCategoryRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(), Mockito.any()))
				.thenReturn(reasonCategories);
		when(reasonListRepository.findAll()).thenReturn(reasonLists);
		when(reasonListRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(), Mockito.any()))
				.thenReturn(reasonLists);
		when(documentCategoryRepository.findAll()).thenReturn(documentCategories);
		when(documentCategoryRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(), Mockito.any()))
				.thenReturn(documentCategories);
		when(documentTypeRepository.findAll()).thenReturn(documentTypes).thenReturn(documentTypes);
		when(documentTypeRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(), Mockito.any()))
				.thenReturn(documentTypes);
		when(validDocumentRepository.findAll()).thenReturn(validDocuments);
		when(validDocumentRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(), Mockito.any()))
				.thenReturn(validDocuments);
		when(biometricAttributeRepository.findAll()).thenReturn(biometricAttributes);
		when(biometricAttributeRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(), Mockito.any()))
				.thenReturn(biometricAttributes);
		when(biometricTypeRepository.findAll()).thenReturn(biometricTypes);
		when(titleRepository.findAll()).thenReturn(titles);
		when(titleRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(), Mockito.any())).thenReturn(titles);
		when(locationRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(), Mockito.any())).thenReturn(locations);
		when(locationRepository.findAll()).thenReturn(locations);
		when(registrationCenterMachineRepository.findAllByMachineId(Mockito.any()))
				.thenReturn(registrationCenterMachines);
		when(registrationCenterMachineRepository.findAllLatestCreatedUpdatedDeleted(Mockito.any(), Mockito.any(),
				Mockito.any())).thenReturn(registrationCenterMachines);
		when(registrationCenterDeviceRepository.findAllByRegistrationCenter(Mockito.any()))
				.thenReturn(registrationCenterDevices);
		when(registrationCenterDeviceRepository.findAllLatestByRegistrationCenterCreatedUpdatedDeleted(Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(registrationCenterDevices);
		when(registrationCenterMachineDeviceRepository.findAllByRegistrationCenterId(Mockito.any()))
				.thenReturn(registrationCenterMachineDevices);
		when(registrationCenterMachineDeviceRepository.findAllByRegistrationCenterIdCreatedUpdatedDeleted(Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(registrationCenterMachineDevices);
		when(registrationCenterUserMachineRepository.findAllByRegistrationCenterId(Mockito.any()))
				.thenReturn(registrationCenterUserMachines);
		when(registrationCenterUserMachineRepository.findAllByRegistrationCenterIdCreatedUpdatedDeleted(Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(registrationCenterUserMachines);
		when(registrationCenterUserRepository.findAllByRegistrationCenterId(Mockito.any()))
				.thenReturn(registrationCenterUsers);
		when(registrationCenterUserRepository.findAllByRegistrationCenterIdCreatedUpdatedDeleted(Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(registrationCenterUsers);
		when(registrationCenterUserHistoryRepository.findLatestRegistrationCenterUserHistory(Mockito.anyString(),
				Mockito.any(), Mockito.any())).thenReturn(registrationCenterUserHistory);
		when(registrationCenterUserMachineHistoryRepository
				.findLatestRegistrationCenterUserMachineHistory(Mockito.anyString(), Mockito.any(), Mockito.any()))
						.thenReturn(registrationCenterUserMachineHistory);
		when(registrationCenterDeviceHistoryRepository.findLatestRegistrationCenterDeviceHistory(Mockito.anyString(),
				Mockito.any(), Mockito.any())).thenReturn(registrationCenterDeviceHistory);
		when(registrationCenterMachineHistoryRepository.findLatestRegistrationCenterMachineHistory(Mockito.anyString(),
				Mockito.any(), Mockito.any())).thenReturn(registrationCenterMachineHistory);
		when(registrationCenterMachineDeviceHistoryRepository
				.findLatestRegistrationCenterMachineDeviceHistory(Mockito.anyString(), Mockito.any(), Mockito.any()))
						.thenReturn(registrationCenterMachineDeviceHistory);
		
		when(applicantValidDocumentRespository.findAllByTimeStamp(Mockito.any(), Mockito.any()))
				.thenReturn(applicantValidDocumentList);
		when(individualTypeRepository.findAllIndvidualTypeByTimeStamp(Mockito.any(), Mockito.any()))
				.thenReturn(individualTypeList);
		when(appAuthenticationMethodRepository.findByLastUpdatedAndCurrentTimeStamp(Mockito.any(), Mockito.any()))
				.thenReturn(appAuthenticationMethods);
		when(appDetailRepository.findByLastUpdatedTimeAndCurrentTimeStamp(Mockito.any(), Mockito.any()))
				.thenReturn(appDetails);
		when(appRolePriorityRepository.findByLastUpdatedAndCurrentTimeStamp(Mockito.any(), Mockito.any()))
				.thenReturn(appRolePriorities);
		when(screenAuthorizationRepository.findByLastUpdatedAndCurrentTimeStamp(Mockito.any(), Mockito.any()))
				.thenReturn(screenAuthorizations);
		when(processListRepository.findByLastUpdatedTimeAndCurrentTimeStamp(Mockito.any(), Mockito.any()))
				.thenReturn(processList);

		when(screenDetailRepo.findByLastUpdatedAndCurrentTimeStamp(Mockito.any(), Mockito.any()))
				.thenReturn(screenDetailList);
		
		when(deviceProviderRepository.findAllLatestCreatedUpdateDeleted(Mockito.any() , Mockito.any()))
			.thenReturn(Arrays.asList(deviceProvider));
		
		when(deviceServiceRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(),
		  Mockito.any())) .thenReturn(Arrays.asList(deviceService));
		
		when(registeredDeviceRepository.findAllLatestCreatedUpdateDeleted(Mockito.anyString(), Mockito.any(),
		  Mockito.any())).thenReturn(Arrays.asList(registeredDevice));
		 
		when(foundationalTrustProviderRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(), Mockito.any()))
		  .thenReturn(Arrays.asList(foundationalTrustProvider));
		
		when(deviceTypeDPMRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(), Mockito.any()))
			.thenReturn(Arrays.asList(deviceTypeDPM));
		
		when(deviceSubTypeDPMRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(), 
				Mockito.any())) .thenReturn(Arrays.asList(deviceSubTypeDPM));
		
		when(signatureUtil.sign(Mockito.anyString(), Mockito.anyString())).thenReturn(signResponse);
	}
	
		
	private String syncDataUrl = "/clientsettings?lastupdated=2018-11-01T12:10:01.021Z&keyindex=abcd";	
		
	private String syncDataUrlWithoutInput = "/clientsettings";
	private String syncDataUrlWithOnlyLastUpdated = "/clientsettings?lastupdated=2018-11-01T12:10:01.021Z";	
	private String syncDataUrlWithOnlyKeyIndex = "/clientsettings?keyindex=abcd";
	
	private String syncDataUrlRegCenterId = "/clientsettings/{regcenterId}";
	private String syncDataUrlRegCenterIdWithKeyIndex = "/clientsettings/{regcenterId}?keyindex=abcd";
	private String syncDataUrlRegCenterIdWithKeyIndexAndLastUpdated = "/clientsettings/{regcenterId}?keyindex=abcd&lastupdated=2018-11-01T12:10:01.021Z";	
	
	private String syncDataUrlWithInvalidTimestamp = "/clientsettings?lastupdated=2018-15-01T123:101:01.021Z&keyindex=abcd";

	

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncSuccess() throws Exception {
		mockSuccess();
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isOk()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertNotNull(jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");
		}
	}
	
	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncSuccessWithOnlyKeyIndex() throws Exception {
		mockSuccess();
		mockMvc.perform(get(syncDataUrlWithOnlyKeyIndex)).andExpect(status().isOk());
	}
	
	
		
	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncSuccessBasedOnRegCenterIdWithKeyIndex() throws Exception {
		mockSuccess();
		mockMvc.perform(get(syncDataUrlRegCenterIdWithKeyIndex, "1001")).andExpect(status().isOk());
	}
	
	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncSuccessBasedOnRegCenterIdWithKeyIndexAndLastUpdated() throws Exception {
		mockSuccess();
		mockMvc.perform(get(syncDataUrlRegCenterIdWithKeyIndexAndLastUpdated, "1001")).andExpect(status().isOk());
	}
	
	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncFailureWithoutAnyInput() throws Exception {
		mockSuccess();
		MvcResult result = mockMvc.perform(get(syncDataUrlWithoutInput)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");
		}
	}	
	
	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncFailureWithOnlyRegCenterId() throws Exception {
		mockSuccess();		
		MvcResult result = mockMvc.perform(get(syncDataUrlRegCenterId, "1001")).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");
		}
	}	
	
	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncWithOnlyUpdatedTime() throws Exception {
		mockSuccess();
		MvcResult result = mockMvc.perform(get(syncDataUrlWithOnlyLastUpdated)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");
		}
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncFailureWithInvalidTimeStamp() throws Exception {
		mockSuccess();
		MvcResult result = mockMvc.perform(get(syncDataUrlWithInvalidTimestamp)).andExpect(status().isOk()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");
		}
	}
	

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncApplicationFetchException() throws Exception {
		mockSuccess();
		when(applicationRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(), Mockito.any()))
				.thenThrow(DataRetrievalFailureException.class);
		
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");
		}
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDataMachineFetchException() throws Exception {
		mockSuccess();
		when(machineRepository.findAllLatestCreatedUpdateDeleted(Mockito.anyString(), Mockito.any(), Mockito.any()))
				.thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");
		}
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDataMachineSpecFetchException() throws Exception {
		mockSuccess();
		when(machineSpecificationRepository.findLatestByRegCenterId(Mockito.anyString(), Mockito.any(), Mockito.any()))
				.thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDataMachineTypeFetchException() throws Exception {
		mockSuccess();
		when(machineTypeRepository.findLatestByRegCenterId(Mockito.anyString(), Mockito.any(), Mockito.any()))
				.thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDataDeviceFetchException() throws Exception {
		mockSuccess();
		when(deviceRepository.findLatestDevicesByRegCenterId(Mockito.anyString(), Mockito.any(), Mockito.any()))
				.thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDataDeviceSpecFetchException() throws Exception {
		mockSuccess();
		when(deviceSpecificationRepository.findLatestDeviceTypeByRegCenterId(Mockito.anyString(), Mockito.any(),
				Mockito.any())).thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDataDeviceTypeFetchException() throws Exception {
		mockSuccess();
		when(deviceTypeRepository.findLatestDeviceTypeByRegCenterId(Mockito.anyString(), Mockito.any(), Mockito.any()))
				.thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDataTemplateFetchException() throws Exception {
		mockSuccess();
		when(templateRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(), Mockito.any()))
				.thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDataTemplateFileFormatFetchException() throws Exception {
		mockSuccess();
		when(templateFileFormatRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(), Mockito.any()))
				.thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDataTemplateTypeFetchException() throws Exception {
		mockSuccess();
		when(templateTypeRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(), Mockito.any()))
				.thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDataHolidayFetchException() throws Exception {
		mockSuccess();
		when(holidayRepository.findAllLatestCreatedUpdateDeletedByMachineId(Mockito.anyString(), Mockito.any(),
				Mockito.any())).thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDataBiometricAttrFetchException() throws Exception {
		mockSuccess();
		when(biometricAttributeRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(), Mockito.any()))
				.thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDataBiometricTypeFetchException() throws Exception {
		mockSuccess();
		when(biometricTypeRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(), Mockito.any()))
				.thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDataDocCategoryFetchException() throws Exception {
		mockSuccess();
		when(documentCategoryRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(), Mockito.any()))
				.thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDataDocTypeFetchException() throws Exception {
		mockSuccess();
		when(documentTypeRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(), Mockito.any()))
				.thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDataLanguageFetchException() throws Exception {
		mockSuccess();
		when(languageRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(), Mockito.any()))
				.thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDataGenderFetchException() throws Exception {
		mockSuccess();
		when(genderTypeRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(), Mockito.any()))
				.thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDataLocationFetchException() throws Exception {
		mockSuccess();
		when(locationRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(), Mockito.any()))
				.thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDataIdTypesFetchException() throws Exception {
		mockSuccess();
		when(idTypeRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(), Mockito.any()))
				.thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDataRegistrationCenterFetchException() throws Exception {
		mockSuccess();
		when(registrationCenterRepository.findLatestRegistrationCenterByMachineId(Mockito.anyString(), Mockito.any(),
				Mockito.any())).thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDataRegistrationCenterTypeFetchException() throws Exception {
		mockSuccess();
		when(registrationCenterTypeRepository.findLatestRegistrationCenterTypeByMachineId(Mockito.anyString(),
				Mockito.any(), Mockito.any())).thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDataValidFetchException() throws Exception {
		mockSuccess();
		when(registrationCenterTypeRepository.findLatestRegistrationCenterTypeByMachineId(Mockito.anyString(),
				Mockito.any(), Mockito.any())).thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDataBlackListedWordFetchException() throws Exception {
		mockSuccess();
		when(blacklistedWordsRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(), Mockito.any()))
				.thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDataReasonCatFetchException() throws Exception {
		mockSuccess();
		when(reasonCategoryRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(), Mockito.any()))
				.thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDataReasonListFetchException() throws Exception {
		mockSuccess();
		when(reasonListRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(), Mockito.any()))
				.thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDataTitleFetchException() throws Exception {
		mockSuccess();
		when(titleRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(), Mockito.any()))
				.thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDatavalidDocumentFetchException() throws Exception {
		mockSuccess();
		when(validDocumentRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(), Mockito.any()))
				.thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDataRegistrationCenterMachineFetchException() throws Exception {
		mockSuccess();
		when(registrationCenterMachineRepository.findAllLatestCreatedUpdatedDeleted(Mockito.anyString(), Mockito.any(),
				Mockito.any())).thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDataRegistrationCenterDeviceFetchException() throws Exception {
		mockSuccess();
		when(registrationCenterDeviceRepository.findAllLatestByRegistrationCenterCreatedUpdatedDeleted(
				Mockito.anyString(), Mockito.any(), Mockito.any())).thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDataRegistrationCenterMachineDeviceFetchException() throws Exception {
		mockSuccess();
		when(registrationCenterMachineDeviceRepository
				.findAllByRegistrationCenterIdCreatedUpdatedDeleted(Mockito.anyString(), Mockito.any(), Mockito.any()))
						.thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDataRegistrationCenterUserMachineFetchException() throws Exception {
		mockSuccess();
		when(registrationCenterUserMachineRepository
				.findAllByRegistrationCenterIdCreatedUpdatedDeleted(Mockito.anyString(), Mockito.any(), Mockito.any()))
						.thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDataRegistrationCenterUserFetchException() throws Exception {
		mockSuccess();
		when(registrationCenterUserRepository.findAllByRegistrationCenterIdCreatedUpdatedDeleted(Mockito.anyString(),
				Mockito.any(), Mockito.any())).thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDataRegistrationCenterUserHistoryFetchException() throws Exception {
		mockSuccess();
		when(registrationCenterUserHistoryRepository.findLatestRegistrationCenterUserHistory(Mockito.anyString(),
				Mockito.any(), Mockito.any())).thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDataRegistrationCenterUserMachineHistoryFetchException() throws Exception {
		mockSuccess();
		when(registrationCenterUserMachineHistoryRepository
				.findLatestRegistrationCenterUserMachineHistory(Mockito.anyString(), Mockito.any(), Mockito.any()))
						.thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	@Ignore
	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDataRegistrationCenterWithDeviceProviderException() throws Exception {
		mockSuccess();
		when(deviceProviderRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(), Mockito.any()))
				.thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	@Ignore
	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDataRegistrationCenterWithDeviceServiceException() throws Exception {
		mockSuccess();
		when(deviceServiceRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(), Mockito.any()))
				.thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	@Ignore
	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDataRegistrationCenterWithRegisteredDeviceException() throws Exception {
		mockSuccess();
		when(registeredDeviceRepository.findAllLatestCreatedUpdateDeleted(Mockito.anyString(), Mockito.any(),
				Mockito.any())).thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	@Ignore
	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDataRegistrationCenterWithFTPException() throws Exception {
		mockSuccess();
		when(foundationalTrustProviderRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(), Mockito.any()))
				.thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	@Ignore
	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDataRegistrationCenterWithDeviceTypeException() throws Exception {
		mockSuccess();
		when(deviceTypeDPMRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(), Mockito.any()))
				.thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	@Ignore
	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDataRegistrationCenterWithDeviceSubTypeException() throws Exception {
		mockSuccess();
		when(deviceSubTypeDPMRepository.findAllLatestCreatedUpdateDeleted(Mockito.any(), Mockito.any()))
				.thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDataRegistrationCenterMachineHistoryFetchException() throws Exception {
		mockSuccess();
		when(registrationCenterMachineHistoryRepository.findLatestRegistrationCenterMachineHistory(Mockito.anyString(),
				Mockito.any(), Mockito.any())).thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDataRegistrationCenterDeviceHistoryFetchException() throws Exception {
		mockSuccess();
		when(registrationCenterDeviceHistoryRepository.findLatestRegistrationCenterDeviceHistory(Mockito.anyString(),
				Mockito.any(), Mockito.any())).thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterDataRegistrationCenterMachineDeviceHistoryFetchException() throws Exception {
		mockSuccess();
		when(registrationCenterMachineDeviceHistoryRepository
				.findLatestRegistrationCenterMachineDeviceHistory(Mockito.anyString(), Mockito.any(), Mockito.any()))
						.thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}
	

	@Ignore
	@WithUserDetails(value = "reg-officer")
	public void IsMachineIdPresentServiceExceptionTest() throws Exception {
		when(machineRepository.findByMachineIdAndIsActive(Mockito.anyString()))
				.thenThrow(DataRetrievalFailureException.class);

		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	
	@Test
	@WithUserDetails(value = "reg-officer")
	public void findApplicantValidDocServiceExceptionTest() throws Exception {
		mockSuccess();
		when(applicantValidDocumentRespository.findAllByTimeStamp(Mockito.any(), Mockito.any()))
				.thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void individualTypeExceptionTest() throws Exception {

		mockSuccess();
		when(individualTypeRepository.findAllIndvidualTypeByTimeStamp(Mockito.any(), Mockito.any()))
				.thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}

	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void registrationCetnerDevicesServiceExceptionTest() throws Exception {

		mockSuccess();
		when(registrationCenterDeviceRepository.findAllLatestByRegistrationCenterCreatedUpdatedDeleted(
				Mockito.anyString(), Mockito.any(), Mockito.any())).thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}

	}
	
	@Test
	@WithUserDetails(value = "reg-officer")
	public void appAuthMethodExceptionTest() throws Exception {

		mockSuccess();
		when(appAuthenticationMethodRepository.findByLastUpdatedAndCurrentTimeStamp(Mockito.any(), Mockito.any()))
				.thenThrow(DataRetrievalFailureException.class);

		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}

	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void registrationCenterTest() throws Exception {

		mockSuccess();
		when(registrationCenterRepository.findRegistrationCenterByIdAndIsActiveIsTrue(Mockito.anyString()))
				.thenReturn(new ArrayList<RegistrationCenter>());
		mockMvc.perform(get(syncDataUrl)).andExpect(status().isOk());

	}	
	
	@Test
	@WithUserDetails(value = "reg-officer")
	public void appDetailExceptionTest() throws Exception {

		mockSuccess();
		when(appDetailRepository.findByLastUpdatedTimeAndCurrentTimeStamp(Mockito.any(), Mockito.any()))
				.thenThrow(DataRetrievalFailureException.class);

		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}

	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void appPriorityExceptionTest() throws Exception {
		mockSuccess();
		when(appRolePriorityRepository.findByLastUpdatedAndCurrentTimeStamp(Mockito.any(), Mockito.any()))
				.thenThrow(DataRetrievalFailureException.class);

		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}

	}
	
	@Test
	@WithUserDetails(value = "reg-officer")
	public void screenAuthExceptionTest() throws Exception {

		mockSuccess();
		when(screenAuthorizationRepository.findByLastUpdatedAndCurrentTimeStamp(Mockito.any(), Mockito.any()))
				.thenThrow(DataRetrievalFailureException.class);

		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}
	
	@Test
	@WithUserDetails(value = "reg-officer")
	public void processListExceptionTest() throws Exception {

		mockSuccess();
		when(processListRepository.findByLastUpdatedTimeAndCurrentTimeStamp(Mockito.any(), Mockito.any()))
				.thenThrow(DataRetrievalFailureException.class);

		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}

	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void screenDetailException() throws Exception {
		mockSuccess();
		when(screenDetailRepo.findByLastUpdatedAndCurrentTimeStamp(Mockito.any(), Mockito.any()))
				.thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}

	}

	// test cases to find if valid registration center machine is available for provided keyIndex and regCenterId
	
	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncMasterdataWithMachineListEmpty() throws Exception {
		mockSuccess();
		when(registrationCenterMachineRepository.getRegistrationCenterMachineWithKeyIndex(Mockito.anyString()))
				.thenReturn(new ArrayList<Object[]>());
		mockMvc.perform(get(syncDataUrl)).andExpect(status().isOk());
	}	

	
	@WithUserDetails(value = "reg-officer")
	public void syncMasterdataWithServiceException() throws Exception {
		mockSuccess();
		when(registrationCenterMachineRepository.getRegistrationCenterMachineWithKeyIndex(Mockito.anyString()))
				.thenThrow(DataRetrievalFailureException.class);
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");}
	}	
	
	@Test
	@WithUserDetails(value= "reg-officer")
	public void testwithRuntimeExceptioninAsyncMethod() throws Exception {
		mockSuccess();
		when(machineRepository.findAllLatestCreatedUpdateDeleted(Mockito.anyString(), Mockito.any(), Mockito.any())).
			thenThrow(RuntimeException.class);
		
		MvcResult result = mockMvc.perform(get(syncDataUrl)).andExpect(status().isInternalServerError()).andReturn();
		try {
			JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
			assertEquals(JSONObject.NULL,jsonObject.get("response"));
		} catch(Throwable t) {
			Assert.fail("Not expected response!");
		}
	}

}
