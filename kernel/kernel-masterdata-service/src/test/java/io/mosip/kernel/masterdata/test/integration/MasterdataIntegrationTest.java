package io.mosip.kernel.masterdata.test.integration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.idgenerator.spi.MachineIdGenerator;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.masterdata.constant.MachinePutReqDto;
import io.mosip.kernel.masterdata.dto.BiometricAttributeDto;
import io.mosip.kernel.masterdata.dto.BlacklistedWordsDto;
import io.mosip.kernel.masterdata.dto.DeviceDto;
import io.mosip.kernel.masterdata.dto.DeviceProviderDto;
import io.mosip.kernel.masterdata.dto.DeviceProviderPutDto;
import io.mosip.kernel.masterdata.dto.DevicePutReqDto;
import io.mosip.kernel.masterdata.dto.DeviceSpecificationDto;
import io.mosip.kernel.masterdata.dto.DeviceTypeDto;
import io.mosip.kernel.masterdata.dto.DigitalIdDeviceRegisterDto;
import io.mosip.kernel.masterdata.dto.DocumentCategoryDto;
import io.mosip.kernel.masterdata.dto.DocumentTypeDto;
import io.mosip.kernel.masterdata.dto.DocumentTypePutReqDto;
import io.mosip.kernel.masterdata.dto.ExceptionalHolidayPutPostDto;
import io.mosip.kernel.masterdata.dto.FoundationalTrustProviderDto;
import io.mosip.kernel.masterdata.dto.FoundationalTrustProviderPutDto;
import io.mosip.kernel.masterdata.dto.GenderTypeDto;
import io.mosip.kernel.masterdata.dto.HolidayDto;
import io.mosip.kernel.masterdata.dto.IdTypeDto;
import io.mosip.kernel.masterdata.dto.IndividualTypeDto;
import io.mosip.kernel.masterdata.dto.LanguageDto;
import io.mosip.kernel.masterdata.dto.MOSIPDeviceServiceDto;
import io.mosip.kernel.masterdata.dto.MachineDto;
import io.mosip.kernel.masterdata.dto.MachinePostReqDto;
import io.mosip.kernel.masterdata.dto.MachineSpecificationDto;
import io.mosip.kernel.masterdata.dto.MachineTypeDto;
import io.mosip.kernel.masterdata.dto.PostReasonCategoryDto;
import io.mosip.kernel.masterdata.dto.ReasonListDto;
import io.mosip.kernel.masterdata.dto.RegCenterPostReqDto;
import io.mosip.kernel.masterdata.dto.RegCenterPutReqDto;
import io.mosip.kernel.masterdata.dto.RegistarionCenterReqDto;
import io.mosip.kernel.masterdata.dto.RegisteredDevicePostReqDto;
import io.mosip.kernel.masterdata.dto.RegistrationCenterDeviceDto;
import io.mosip.kernel.masterdata.dto.RegistrationCenterDeviceHistoryDto;
import io.mosip.kernel.masterdata.dto.RegistrationCenterDto;
import io.mosip.kernel.masterdata.dto.RegistrationCenterMachineDeviceDto;
import io.mosip.kernel.masterdata.dto.RegistrationCenterMachineDto;
import io.mosip.kernel.masterdata.dto.RegistrationCenterTypeDto;
import io.mosip.kernel.masterdata.dto.TemplateDto;
import io.mosip.kernel.masterdata.dto.TemplateFileFormatDto;
import io.mosip.kernel.masterdata.dto.TemplateTypeDto;
import io.mosip.kernel.masterdata.dto.TitleDto;
import io.mosip.kernel.masterdata.dto.ValidDocumentDto;
import io.mosip.kernel.masterdata.dto.WorkingNonWorkingDaysDto;
import io.mosip.kernel.masterdata.dto.getresponse.IndividualTypeResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.RegistrationCenterUserMachineMappingHistoryResponseDto;
import io.mosip.kernel.masterdata.dto.registerdevice.DeviceData;
import io.mosip.kernel.masterdata.dto.registerdevice.DeviceInfo;
import io.mosip.kernel.masterdata.dto.registerdevice.DigitalId;
import io.mosip.kernel.masterdata.dto.registerdevice.RegisteredDevicePostDto;
import io.mosip.kernel.masterdata.dto.registerdevice.SignResponseDto;
import io.mosip.kernel.masterdata.entity.BiometricAttribute;
import io.mosip.kernel.masterdata.entity.BlacklistedWords;
import io.mosip.kernel.masterdata.entity.DaysOfWeek;
import io.mosip.kernel.masterdata.entity.Device;
import io.mosip.kernel.masterdata.entity.DeviceHistory;
import io.mosip.kernel.masterdata.entity.DeviceProvider;
import io.mosip.kernel.masterdata.entity.DeviceProviderHistory;
import io.mosip.kernel.masterdata.entity.DeviceSpecification;
import io.mosip.kernel.masterdata.entity.DeviceType;
import io.mosip.kernel.masterdata.entity.DocumentCategory;
import io.mosip.kernel.masterdata.entity.DocumentType;
import io.mosip.kernel.masterdata.entity.FoundationalTrustProvider;
import io.mosip.kernel.masterdata.entity.FoundationalTrustProviderHistory;
import io.mosip.kernel.masterdata.entity.Gender;
import io.mosip.kernel.masterdata.entity.Holiday;
import io.mosip.kernel.masterdata.entity.IdType;
import io.mosip.kernel.masterdata.entity.IndividualType;
import io.mosip.kernel.masterdata.entity.Language;
import io.mosip.kernel.masterdata.entity.Location;
import io.mosip.kernel.masterdata.entity.LocationHierarchy;
import io.mosip.kernel.masterdata.entity.MOSIPDeviceService;
import io.mosip.kernel.masterdata.entity.MOSIPDeviceServiceHistory;
import io.mosip.kernel.masterdata.entity.Machine;
import io.mosip.kernel.masterdata.entity.MachineHistory;
import io.mosip.kernel.masterdata.entity.MachineSpecification;
import io.mosip.kernel.masterdata.entity.MachineType;
import io.mosip.kernel.masterdata.entity.ModuleDetail;
import io.mosip.kernel.masterdata.entity.ReasonCategory;
import io.mosip.kernel.masterdata.entity.ReasonList;
import io.mosip.kernel.masterdata.entity.RegWorkingNonWorking;
import io.mosip.kernel.masterdata.entity.RegisteredDevice;
import io.mosip.kernel.masterdata.entity.RegisteredDeviceHistory;
import io.mosip.kernel.masterdata.entity.RegistrationCenter;
import io.mosip.kernel.masterdata.entity.RegistrationCenterHistory;
import io.mosip.kernel.masterdata.entity.RegistrationCenterType;
import io.mosip.kernel.masterdata.entity.RegistrationDeviceSubType;
import io.mosip.kernel.masterdata.entity.RegistrationDeviceType;
import io.mosip.kernel.masterdata.entity.Template;
import io.mosip.kernel.masterdata.entity.TemplateFileFormat;
import io.mosip.kernel.masterdata.entity.TemplateType;
import io.mosip.kernel.masterdata.entity.Title;
import io.mosip.kernel.masterdata.entity.UserDetails;
import io.mosip.kernel.masterdata.entity.UserDetailsHistory;
import io.mosip.kernel.masterdata.entity.ValidDocument;
import io.mosip.kernel.masterdata.entity.Zone;
import io.mosip.kernel.masterdata.entity.id.CodeAndLanguageCodeID;
import io.mosip.kernel.masterdata.entity.id.CodeLangCodeAndRsnCatCodeID;
import io.mosip.kernel.masterdata.entity.id.GenderID;
import io.mosip.kernel.masterdata.entity.id.RegistrationCenterMachineDeviceHistoryID;
import io.mosip.kernel.masterdata.entity.id.RegistrationCenterMachineDeviceID;
import io.mosip.kernel.masterdata.entity.id.RegistrationCenterMachineUserHistoryID;
import io.mosip.kernel.masterdata.exception.DataNotFoundException;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;
import io.mosip.kernel.masterdata.exception.RequestException;
import io.mosip.kernel.masterdata.repository.ApplicantValidDocumentRepository;
import io.mosip.kernel.masterdata.repository.BiometricAttributeRepository;
import io.mosip.kernel.masterdata.repository.BlacklistedWordsRepository;
import io.mosip.kernel.masterdata.repository.DaysOfWeekListRepo;
import io.mosip.kernel.masterdata.repository.DeviceHistoryRepository;
import io.mosip.kernel.masterdata.repository.DeviceProviderHistoryRepository;
import io.mosip.kernel.masterdata.repository.DeviceProviderRepository;
import io.mosip.kernel.masterdata.repository.DeviceRepository;
import io.mosip.kernel.masterdata.repository.DeviceSpecificationRepository;
import io.mosip.kernel.masterdata.repository.DeviceTypeRepository;
import io.mosip.kernel.masterdata.repository.DocumentCategoryRepository;
import io.mosip.kernel.masterdata.repository.DocumentTypeRepository;
import io.mosip.kernel.masterdata.repository.FoundationalTrustProviderRepository;
import io.mosip.kernel.masterdata.repository.FoundationalTrustProviderRepositoryHistory;
import io.mosip.kernel.masterdata.repository.GenderTypeRepository;
import io.mosip.kernel.masterdata.repository.HolidayRepository;
import io.mosip.kernel.masterdata.repository.IdTypeRepository;
import io.mosip.kernel.masterdata.repository.IndividualTypeRepository;
import io.mosip.kernel.masterdata.repository.LanguageRepository;
import io.mosip.kernel.masterdata.repository.LocationHierarchyRepository;
import io.mosip.kernel.masterdata.repository.LocationRepository;
import io.mosip.kernel.masterdata.repository.MOSIPDeviceServiceHistoryRepository;
import io.mosip.kernel.masterdata.repository.MOSIPDeviceServiceRepository;
import io.mosip.kernel.masterdata.repository.MachineHistoryRepository;
import io.mosip.kernel.masterdata.repository.MachineRepository;
import io.mosip.kernel.masterdata.repository.MachineSpecificationRepository;
import io.mosip.kernel.masterdata.repository.MachineTypeRepository;
import io.mosip.kernel.masterdata.repository.ModuleRepository;
import io.mosip.kernel.masterdata.repository.ReasonCategoryRepository;
import io.mosip.kernel.masterdata.repository.ReasonListRepository;
import io.mosip.kernel.masterdata.repository.RegWorkingNonWorkingRepo;
import io.mosip.kernel.masterdata.repository.RegisteredDeviceHistoryRepository;
import io.mosip.kernel.masterdata.repository.RegisteredDeviceRepository;
import io.mosip.kernel.masterdata.repository.RegistrationCenterHistoryRepository;
import io.mosip.kernel.masterdata.repository.RegistrationCenterRepository;
import io.mosip.kernel.masterdata.repository.RegistrationCenterTypeRepository;
import io.mosip.kernel.masterdata.repository.RegistrationDeviceSubTypeRepository;
import io.mosip.kernel.masterdata.repository.RegistrationDeviceTypeRepository;
import io.mosip.kernel.masterdata.repository.TemplateFileFormatRepository;
import io.mosip.kernel.masterdata.repository.TemplateRepository;
import io.mosip.kernel.masterdata.repository.TemplateTypeRepository;
import io.mosip.kernel.masterdata.repository.TitleRepository;
import io.mosip.kernel.masterdata.repository.UserDetailsHistoryRepository;
import io.mosip.kernel.masterdata.repository.UserDetailsRepository;
import io.mosip.kernel.masterdata.repository.ValidDocumentRepository;
import io.mosip.kernel.masterdata.repository.ZoneUserRepository;
import io.mosip.kernel.masterdata.test.TestBootApplication;
import io.mosip.kernel.masterdata.utils.AuditUtil;
import io.mosip.kernel.masterdata.utils.MapperUtils;
import io.mosip.kernel.masterdata.utils.MasterdataCreationUtil;
import io.mosip.kernel.masterdata.utils.RegistrationCenterValidator;
import io.mosip.kernel.masterdata.utils.ZoneUtils;

/**
 * 
 * @author Sidhant Agarwal
 * @author Urvil Joshi
 * @author Dharmesh Khandelwal
 * @author Sagar Mahapatra
 * @author Ritesh Sinha
 * @author Abhishek Kumar
 * @author Bal Vikash Sharma
 * @author Uday Kumar
 * @author Megha Tanga
 * @author Srinivasan
 * @author Neha Sinha
 * @author Ramadurai Pandian
 * @since 1.0.0
 */
@SpringBootTest(classes = TestBootApplication.class)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc(print = MockMvcPrint.LOG_DEBUG, printOnlyOnFailure = false)
public class MasterdataIntegrationTest {

	// private static final String JSON_STRING_RESPONSE =
	// "{\"uinLength\":24,\"numberOfWrongAttemptsForOtp\":5,\"accountFreezeTimeoutInHours\":10,\"mobilenumberlength\":10,\"archivalPolicy\":\"arc_policy_2\",\"tokenIdLength\":23,\"restrictedNumbers\":[\"8732\",\"321\",\"65\"],\"registrationCenterId\":\"KDUE83CJ3\",\"machineId\":\"MCBD3UI3\",\"supportedLanguages\":[\"eng\",\"hin\",\"ara\",\"deu\",\"fra\"],\"tspIdLength\":24,\"otpTimeOutInMinutes\":2,\"notificationtype\":\"SMS|EMAIL\",\"pridLength\":32,\"vidLength\":32}";

	private static final String JSON_STRING_RESPONSE = "{\r\n" + "\"registrationConfiguration\":\r\n"
			+ "							{\"keyValidityPeriodPreRegPack\":\"3\",\"smsNotificationTemplateRegCorrection\":\"OTP for your request is $otp\",\"defaultDOB\":\"1-Jan\",\"smsNotificationTemplateOtp\":\"OTP for your request is $otp\",\"supervisorVerificationRequiredForExceptions\":\"true\",\"keyValidityPeriodRegPack\":\"3\",\"irisRetryAttempts\":\"10\",\"fingerprintQualityThreshold\":\"120\",\"multifactorauthentication\":\"true\",\"smsNotificationTemplateUpdateUIN\":\"OTP for your request is $otp\",\"supervisorAuthType\":\"password\",\"maxDurationRegPermittedWithoutMasterdataSyncInDays\":\"10\",\"modeOfNotifyingIndividual\":\"mobile\",\"emailNotificationTemplateUpdateUIN\":\"Hello $user the OTP is $otp\",\"maxDocSizeInMB\":\"150\",\"emailNotificationTemplateOtp\":\"Hello $user the OTP is $otp\",\"emailNotificationTemplateRegCorrection\":\"Hello $user the OTP is $otp\",\"faceRetry\":\"12\",\"noOfFingerprintAuthToOnboardUser\":\"10\",\"smsNotificationTemplateLostUIN\":\"OTP for your request is $otp\",\"supervisorAuthMode\":\"IRIS\",\"operatorRegSubmissionMode\":\"fingerprint\",\"officerAuthType\":\"password\",\"faceQualityThreshold\":\"25\",\"gpsDistanceRadiusInMeters\":\"3\",\"automaticSyncFreqServerToClient\":\"25\",\"maxDurationWithoutMasterdataSyncInDays\":\"7\",\"loginMode\":\"bootable dongle\",\"irisQualityThreshold\":\"25\",\"retentionPeriodAudit\":\"3\",\"fingerprintRetryAttempts\":\"234\",\"emailNotificationTemplateNewReg\":\"Hello $user the OTP is $otp\",\"passwordExpiryDurationInDays\":\"3\",\"emailNotificationTemplateLostUIN\":\"Hello $user the OTP is $otp\",\"blockRegistrationIfNotSynced\":\"10\",\"noOfIrisAuthToOnboardUser\":\"10\",\"smsNotificationTemplateNewReg\":\"OTP for your request is $otp\"},\r\n"
			+ "\r\n" + "\"globalConfiguration\":\r\n"
			+ "						{\"mosip.kernel.crypto.symmetric-algorithm-name\":\"AES\",\"mosip.kernel.virus-scanner.port\":\"3310\",\"mosip.kernel.email.max-length\":\"50\",\"mosip.kernel.email.domain.ext-max-lenght\":\"7\",\"mosip.kernel.rid.sequence-length\":\"5\",\"mosip.kernel.uin.uin-generation-cron\":\"0 * * * * *\",\"mosip.kernel.rid.centerid-length\":\"5\",\"mosip.kernel.email.special-char\":\"!#$%&'*+-\\/=?^_`{|}~.\",\"mosip.kernel.rid.requesttime-length\":\"14\",\"mosip.kernel.vid.length.sequence-limit\":\"3\",\"mosip.kernel.keygenerator.asymmetric-key-length\":\"2048\",\"mosip.kernel.uin.min-unused-threshold\":\"100000\",\"mosip.kernel.prid.sequence-limit\":\"3\",\"auth.role.prefix\":\"ROLE_\",\"mosip.kernel.email.domain.ext-min-lenght\":\"2\",\"auth.server.validate.url\":\"http:\\/\\/localhost:8091\\/auth\\/validate_token\",\"mosip.kernel.machineid.length\":\"4\",\"mosip.supported-languages\":\"eng,ara,fra,hin,deu\",\"mosip.kernel.prid.length\":\"14\",\"auth.header.name\":\"Authorization\",\"mosip.kernel.crypto.asymmetric-algorithm-name\":\"RSA\",\"mosip.kernel.phone.min-length\":\"9\",\"mosip.kernel.uin.length\":\"10\",\"mosip.kernel.virus-scanner.host\":\"104.211.209.102\",\"mosip.kernel.email.min-length\":\"7\",\"mosip.kernel.rid.machineid-length\":\"5\",\"mosip.kernel.prid.repeating-block-limit\":\"3\",\"mosip.kernel.vid.length.repeating-block-limit\":\"2\",\"mosip.kernel.rid.length\":\"29\",\"mosip.kernel.phone.max-length\":\"15\",\"mosip.kernel.prid.repeating-limit\":\"2\",\"mosip.kernel.uin.restricted-numbers\":\"786,666\",\"mosip.kernel.email.domain.special-char\":\"-\",\"mosip.kernel.vid.length.repeating-limit\":\"2\",\"mosip.kernel.registrationcenterid.length\":\"4\",\"mosip.kernel.phone.special-char\":\"+ -\",\"mosip.kernel.uin.uins-to-generate\":\"200000\",\"mosip.kernel.vid.length\":\"16\",\"mosip.kernel.tokenid.length\":\"36\",\"mosip.kernel.uin.length.repeating-block-limit\":\"2\",\"mosip.kernel.tspid.length\":\"4\",\"mosip.kernel.tokenid.sequence-limit\":\"3\",\"mosip.kernel.uin.length.repeating-limit\":\"2\",\"mosip.kernel.uin.length.sequence-limit\":\"3\",\"mosip.kernel.keygenerator.symmetric-key-length\":\"256\",\"mosip.kernel.data-key-splitter\":\"#KEY_SPLITTER#\"}\r\n"
			+ "}";

	@MockBean
	ZoneUtils zoneUtils;

	@MockBean
	AuditUtil aditUtil;

	@MockBean
	ZoneUserRepository zoneUserRepository;

	@Autowired
	private MockMvc mockMvc;
	@MockBean
	private RestTemplate restTemplate;
	@MockBean
	private BlacklistedWordsRepository wordsRepository;
	@MockBean
	private LocationRepository locationRepository;
	List<Location> locationHierarchies;

	UserDetailsHistory user;
	List<UserDetailsHistory> users = new ArrayList<>();

	@MockBean
	private UserDetailsHistoryRepository userDetailsRepository;

	@MockBean
	private UserDetailsRepository userRepository;

	@MockBean
	private DeviceRepository deviceRepository;

	@MockBean
	private DocumentTypeRepository documentTypeRepository;

	@MockBean
	private DocumentCategoryRepository documentCategoryRepository;

	@MockBean
	private ValidDocumentRepository validDocumentRepository;

	@MockBean
	private BiometricAttributeRepository biometricAttributeRepository;
	private BiometricAttributeDto biometricAttributeDto;
	private BiometricAttribute biometricAttribute;

	@MockBean
	private TemplateRepository templateRepository;
	private Template template;
	private TemplateDto templateDto;

	private TemplateFileFormatDto templateFileFormatDto;
	private TemplateFileFormat templateFileFormat;

	@MockBean
	private TemplateFileFormatRepository templateFileFormatRepository;

	private RequestWrapper<TemplateFileFormatDto> templateFileFormatRequestDto = new RequestWrapper<TemplateFileFormatDto>();

	@MockBean
	private TemplateTypeRepository templateTypeRepository;
	private TemplateType templateType;
	private TemplateTypeDto templateTypeDto;
	@MockBean
	private DeviceSpecificationRepository deviceSpecificationRepository;

	@MockBean
	DeviceTypeRepository deviceTypeRepository;

	@MockBean
	MachineSpecificationRepository machineSpecificationRepository;

	@MockBean
	MachineRepository machineRepository;

	@MockBean
	MachineTypeRepository machineTypeRepository;

	List<DocumentType> documentTypes;

	DocumentType type;

	List<RegistrationCenterType> regCenterTypes;

	RegistrationCenterType regCenterType;

	List<IdType> idTypes;

	IdType idType;

	List<DocumentCategory> entities;

	DocumentCategory category;

	List<BlacklistedWords> words;

	@MockBean
	private GenderTypeRepository genderTypeRepository;

	private List<Gender> genderTypes;

	private List<Gender> genderTypesNull;

	private GenderID genderId;

	@MockBean
	private HolidayRepository holidayRepository;

	private List<Holiday> holidays;

	@MockBean
	IdTypeRepository idTypeRepository;

	@MockBean
	ReasonCategoryRepository reasonCategoryRepository;

	@MockBean
	ReasonListRepository reasonListRepository;

	@MockBean
	RegistrationCenterTypeRepository registrationCenterTypeRepository;
	
	@MockBean
	private DaysOfWeekListRepo daysOfWeekListRepo;

	private List<ReasonCategory> reasoncategories;

	private List<ReasonList> reasonList;

	private PostReasonCategoryDto postReasonCategoryDto;

	private ReasonListDto reasonListDto;

	private CodeLangCodeAndRsnCatCodeID reasonListId;

	private String reasonListRequest = null;

	private String reasonCategoryRequest = null;

	@MockBean
	RegistrationCenterHistoryRepository repositoryCenterHistoryRepository;

	RegistrationCenterHistory center;
	Device device;
	private DeviceDto deviceDto;

	Title title;
	List<RegistrationCenterHistory> centers = new ArrayList<>();

	@MockBean
	RegistrationCenterRepository registrationCenterRepository;

	RegistrationCenter registrationCenter;
	RegistrationCenter banglore;
	RegistrationCenter chennai;

	List<RegistrationCenter> registrationCenters = new ArrayList<>();

	
	

	RegistrationCenterMachineUserHistoryID registrationCenterUserMachineHistoryId;

	

	RegistrationCenterMachineDeviceHistoryID registrationCenterMachineDeviceHistoryID = null;

	RegistrationCenterMachineDeviceID rcmdIdH = null;
	@MockBean
	private TitleRepository titleRepository;

	private List<Title> titleList;

	private List<Title> titlesNull;

	private CodeAndLanguageCodeID titleId;

	@MockBean
	private LanguageRepository languageRepository;

	private LanguageDto languageDto;

	private Language language;

	private Gender genderType;

	private GenderTypeDto genderDto;

	private ValidDocument validDocument;
	private Holiday holiday;

	
	private RegistrationCenterDeviceDto registrationCenterDeviceDto;
	
	
	private RegistrationCenterMachineDto registrationCenterMachineDto;
	
	private RegistrationCenterMachineDeviceDto registrationCenterMachineDeviceDto;
	
	@MockBean
	private RegWorkingNonWorkingRepo regWorkingNonWorkingRepo;
	private ObjectMapper mapper;

	@Value("${mosip.primary-language}")
	private String primaryLang;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private MachineHistoryRepository machineHistoryRepository;

	@MockBean
	private DeviceHistoryRepository deviceHistoryRepository;

	public static LocalDateTime localDateTimeUTCFormat = LocalDateTime.now();

	public static final DateTimeFormatter UTC_DATE_TIME_FORMAT = DateTimeFormatter
			.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	public static final String UTC_DATE_TIME_FORMAT_DATE_STRING = "2018-12-02T02:50:12.208Z";

	@MockBean
	private IndividualTypeRepository individualTypeRepository;
	private IndividualTypeResponseDto individualTypeResponseDto;
	private List<IndividualType> individualTypes = new ArrayList<>();

	@MockBean
	private FoundationalTrustProviderRepository foundationalTrustProviderRepository;

	@MockBean
	private FoundationalTrustProviderRepositoryHistory foundationalTrustProviderRepositoryHistory;

	private FoundationalTrustProviderDto foundationalTrustProviderDto;

	private FoundationalTrustProvider updateFoundationalTrustProvider;

	private FoundationalTrustProviderPutDto foundationalTrustUpdateProviderDto;

	private FoundationalTrustProviderHistory foundationalTrustProviderHistory;

	@SuppressWarnings("static-access")
	@Before
	public void setUp() {

		Mockito.when(restTemplate.getForObject(Mockito.anyString(), Mockito.any())).thenReturn(JSON_STRING_RESPONSE);

		mapper = new ObjectMapper();

		localDateTimeUTCFormat = localDateTimeUTCFormat.parse(UTC_DATE_TIME_FORMAT_DATE_STRING, UTC_DATE_TIME_FORMAT);
		blacklistedSetup();

		JavaTimeModule timeModule = new JavaTimeModule();
		timeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern("HH:mm:ss")));
		timeModule.addSerializer(LocalDateTime.class,
				new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")));
		mapper.registerModule(new JavaTimeModule());
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		individualTypeSetup();

		genderTypeSetup();

		holidaySetup();

		idTypeSetup();

		packetRejectionSetup();

		registrationCenterHistorySetup();

		registrationCenterSetup();
		updateRegistrationCenterSetup();

		

		titleIntegrationSetup();

		documentCategorySetUp();

		documentTypeSetUp();

		registrationCenterTypeSetUp();

		languageTestSetup();

		addValidDocumentSetUp();

		deviceSetup();

		machineSetUp();
		machinetypeSetUp();
		machineSpecificationSetUp();
		createMachineSetUp();
		updateMachine();
		decommissionMachineSetUp();

		DeviceSpecsetUp();
		DevicetypeSetUp();
		deviceHistorySetUp();
		decommissionDeviceSetUp();
		createdeviceProviderSetUp();

		machineHistorySetUp();
		biometricAttributeTestSetup();
		templateSetup();
		templateTypeTestSetup();
		templateFileFormatSetup();
		registrationCenterDeviceHistorySetup();
		userDetailsHistorySetup();
		MSDcreateSetUp();

		

		decommissionRegCenter();

		foundationProvider();

		newRegCenterSetup();

		setUpRegisteredDevice();

		doNothing().when(aditUtil).auditRequest(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
	}

	private void userDetailsHistorySetup() {
		user = new UserDetailsHistory();
		user.setId("11001");
		user.setRegCenterId("10002");
		user.setEmail("abcd");
		user.setLangCode("eng");
		user.setMobile("124134");
		user.setName("abcd");
		user.setStatusCode("dwd");
		user.setUin("dfwefw");
		users.add(user);
	}

	IndividualType fr;
	IndividualTypeDto frDto;
	private void individualTypeSetup() {
		fr = new IndividualType();
		// fr.setIndividualTypeID(new CodeAndLanguageCodeID("FR", "eng"));
		fr.setCode("FR");
		fr.setLangCode("eng");
		fr.setIsActive(true);
		fr.setName("Foreigner");

		IndividualType nfr = new IndividualType();
		// nfr.setIndividualTypeID(new CodeAndLanguageCodeID("NFR", "eng"));
		nfr.setCode("NFR");
		nfr.setLangCode("eng");
		nfr.setIsActive(true);
		nfr.setName("Non-Foreigner");

		individualTypes.add(fr);
		individualTypes.add(nfr);

		individualTypeResponseDto = new IndividualTypeResponseDto();
		frDto = new IndividualTypeDto();
		MapperUtils.map(fr, frDto);
		IndividualTypeDto nfrDto = new IndividualTypeDto();
		MapperUtils.map(nfr, nfrDto);

		individualTypeResponseDto.getIndividualTypes().add(frDto);
		individualTypeResponseDto.getIndividualTypes().add(nfrDto);

	}

	/* Individual type test */
	@Test
	@WithUserDetails("individual")
	public void getAllIndividualTypeTest() throws Exception {
		when(individualTypeRepository.findAll()).thenReturn(individualTypes);
		mockMvc.perform(get("/individualtypes").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("individual")
	public void getAllIndividualTypeNoTypeFoundTest() throws Exception {
		when(individualTypeRepository.findAll()).thenReturn(null);
		mockMvc.perform(get("/individualtypes").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("individual")
	public void getAllIndividualTypeMasterDataServiceExceptionTest() throws Exception {
		when(individualTypeRepository.findAll()).thenThrow(DataAccessLayerException.class);
		mockMvc.perform(get("/individualtypes").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError());

	}

	@Test
	@WithUserDetails("global-admin")
	public void createIndividualTypeTest() throws Exception {
		RequestWrapper<IndividualTypeDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.match.regcentr.Dcode");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(frDto);

		String individualTypeJson = mapper.writeValueAsString(requestDto);
		when(individualTypeRepository.create(Mockito.any())).thenReturn(fr);
		when(masterdataCreationUtil.createMasterData(IndividualType.class, frDto)).thenReturn(frDto);
		mockMvc.perform(post("/individualtypes").contentType(MediaType.APPLICATION_JSON).content(individualTypeJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void createIndividualTypeExceptionTest() throws Exception {
		RequestWrapper<IndividualTypeDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.match.regcentr.Dcode");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(frDto);

		String individualTypeJson = mapper.writeValueAsString(requestDto);
		when(individualTypeRepository.create(Mockito.any())).thenThrow(DataAccessLayerException.class);
		when(masterdataCreationUtil.createMasterData(IndividualType.class, frDto)).thenReturn(frDto);
		mockMvc.perform(post("/individualtypes").contentType(MediaType.APPLICATION_JSON).content(individualTypeJson))
				.andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("global-admin")
	public void updateIndividualTypeTest() throws Exception {
		RequestWrapper<IndividualTypeDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.match.regcentr.Dcode");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(frDto);

		String individualTypeJson = mapper.writeValueAsString(requestDto);
		when(individualTypeRepository.findIndividualTypeByCodeAndLangCode(Mockito.any(), Mockito.any())).thenReturn(fr);
		when(individualTypeRepository.update(Mockito.any())).thenReturn(fr);
		when(masterdataCreationUtil.updateMasterData(IndividualType.class, frDto)).thenReturn(frDto);
		mockMvc.perform(put("/individualtypes").contentType(MediaType.APPLICATION_JSON).content(individualTypeJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void updateIndividualTypeExceptionTest() throws Exception {
		RequestWrapper<IndividualTypeDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.match.regcentr.Dcode");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(frDto);

		String individualTypeJson = mapper.writeValueAsString(requestDto);
		when(individualTypeRepository.findIndividualTypeByCodeAndLangCode(Mockito.any(), Mockito.any()))
				.thenThrow(DataAccessLayerException.class);
		when(individualTypeRepository.update(Mockito.any())).thenReturn(fr);
		when(masterdataCreationUtil.updateMasterData(IndividualType.class, frDto)).thenReturn(frDto);
		mockMvc.perform(put("/individualtypes").contentType(MediaType.APPLICATION_JSON).content(individualTypeJson))
				.andExpect(status().isInternalServerError());
	}

	/* Applicant type code */
	@MockBean
	private ApplicantValidDocumentRepository applicantValidRepository;

	@Test
	@WithUserDetails("global-admin")
	public void getValidApplicantTypeTest() throws Exception {

		List<Object[]> list = new ArrayList<>();

		Object[] arr = new Object[9];

		for (int i = 0; i < arr.length; i++) {
			arr[i] = "abc";
		}

		list.add(arr);
		when(applicantValidRepository.getDocumentCategoryAndTypesForApplicantCode(Mockito.anyString(),
				Mockito.anyList())).thenReturn(list);
		mockMvc.perform(get("/applicanttype/001/languages?languages=eng&languages=fra&languages=ara"))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void getValidApplicantTypeMasterDataServiceExceptionTest() throws Exception {
		when(applicantValidRepository.getDocumentCategoryAndTypesForApplicantCode(Mockito.anyString(),
				Mockito.anyList())).thenThrow(new DataAccessLayerException("errorCode", "errorMessage", null));
		mockMvc.perform(get("/applicanttype/001/languages?languages=eng&languages=fra&languages=ara"))
				.andExpect(status().isInternalServerError());

	}

	@Test
	@WithUserDetails("global-admin")
	public void getValidApplicantTypeNoTypeFoundTest() throws Exception {
		when(applicantValidRepository.getDocumentCategoryAndTypesForApplicantCode(Mockito.anyString(),
				Mockito.anyList())).thenReturn(null);
		mockMvc.perform(get("/applicanttype/001/languages?languages=eng&languages=fra&languages=ara"))
				.andExpect(status().isOk());
	}

	private DeviceType deviceType;
	private DeviceTypeDto deviceTypeDto;

	private void DevicetypeSetUp() {

		deviceType = new DeviceType();
		deviceType.setCode("1000");
		deviceType.setLangCode("eng");
		deviceType.setName("Laptop");
		deviceType.setIsActive(true);
		deviceType.setDescription("Laptop Description");

		deviceTypeDto = new DeviceTypeDto();
		MapperUtils.map(deviceType, deviceTypeDto);

	}

	private MachineSpecification machineSpecification;
	private MachineSpecificationDto machineSpecificationDto;
	private List<MachineSpecification> machineSpecList;

	private void machineSpecificationSetUp() {

		machineSpecification = new MachineSpecification();
		machineSpecification.setId("1000");
		machineSpecification.setLangCode("eng");
		machineSpecification.setName("laptop");
		machineSpecification.setIsActive(true);
		machineSpecification.setDescription("HP Description");
		machineSpecification.setBrand("HP");
		machineSpecification.setMachineTypeCode("1231");
		machineSpecification.setLangCode("eng");
		machineSpecification.setMinDriverversion("version 0.1");
		machineSpecification.setModel("3168ngw");

		machineSpecList = new ArrayList<>();
		machineSpecList.add(machineSpecification);

		machineSpecificationDto = new MachineSpecificationDto();
		MapperUtils.map(machineSpecification, machineSpecificationDto);

	}

	private MachineType machineType;
	private MachineTypeDto machineTypeDto;



	private void machinetypeSetUp() {
		mapper = new ObjectMapper();

		machineType = new MachineType();
		machineType.setCode("1000");
		machineType.setLangCode("eng");
		machineType.setName("HP");
		machineType.setIsActive(true);
		machineType.setDescription("HP Description");

		machineTypeDto = new MachineTypeDto();
		MapperUtils.map(machineType, machineTypeDto);

	}

	private void biometricAttributeTestSetup() {
		// creating data coming from user
		biometricAttributeDto = new BiometricAttributeDto();
		biometricAttributeDto.setCode("BA222");
		biometricAttributeDto.setLangCode("eng");
		biometricAttributeDto.setName("black_iric");
		biometricAttributeDto.setBiometricTypeCode("iric");
		biometricAttributeDto.setIsActive(Boolean.TRUE);

		biometricAttribute = new BiometricAttribute();
		biometricAttribute.setCode("BA222");
		biometricAttribute.setLangCode("eng");
		biometricAttribute.setName("black_iric");
		biometricAttribute.setBiometricTypeCode("iric");
		biometricAttribute.setIsActive(Boolean.TRUE);

	}

	private void templateSetup() {
		templateDto = new TemplateDto();
		templateDto.setId("T222");
		templateDto.setLangCode("eng");
		templateDto.setName("Email template");
		templateDto.setTemplateTypeCode("EMAIL");
		templateDto.setFileFormatCode("XML");
		templateDto.setModuleId("preregistation");
		templateDto.setIsActive(Boolean.TRUE);

		template = new Template();
		template.setId("T222");
		template.setLangCode("eng");
		template.setName("Email template");
		template.setTemplateTypeCode("EMAIL");
		template.setFileFormatCode("XML");
		template.setModuleId("preregistation");
		template.setIsActive(Boolean.TRUE);

	}

	private void templateTypeTestSetup() {

		templateTypeDto = new TemplateTypeDto();
		templateTypeDto.setCode("TTC222");
		templateTypeDto.setLangCode("eng");
		templateTypeDto.setDescription("Template type desc");
		templateTypeDto.setIsActive(Boolean.TRUE);

		templateType = new TemplateType();
		templateType.setCode("TTC222");
		templateType.setLangCode("eng");
		templateType.setDescription("Template type desc");
		templateType.setIsActive(Boolean.TRUE);

	}

	List<MachineHistory> machineHistoryList;

	private void machineHistorySetUp() {
		LocalDateTime eDate = LocalDateTime.of(2018, Month.JANUARY, 1, 10, 10, 30);
		LocalDateTime vDate = LocalDateTime.of(2022, Month.JANUARY, 1, 10, 10, 30);
		machineHistoryList = new ArrayList<>();
		MachineHistory machineHistory = new MachineHistory();
		machineHistory.setId("1000");
		machineHistory.setName("Laptop");
		machineHistory.setIpAddress("129.0.0.0");
		machineHistory.setMacAddress("129.0.0.0");
		machineHistory.setEffectDateTime(eDate);
		machineHistory.setValidityDateTime(vDate);
		machineHistory.setIsActive(true);
		machineHistory.setLangCode("eng");
		machineHistoryList.add(machineHistory);

	}

	List<DeviceHistory> deviceHistoryList;

	private void deviceHistorySetUp() {
		LocalDateTime eDate = LocalDateTime.of(2018, Month.JANUARY, 1, 10, 10, 30);
		LocalDateTime vDate = LocalDateTime.of(2022, Month.JANUARY, 1, 10, 10, 30);
		deviceHistoryList = new ArrayList<>();
		DeviceHistory deviceHistory = new DeviceHistory();
		deviceHistory.setId("1000");
		deviceHistory.setName("Laptop");
		deviceHistory.setIpAddress("129.0.0.0");
		deviceHistory.setMacAddress("129.0.0.0");
		deviceHistory.setEffectDateTime(eDate);
		deviceHistory.setValidityDateTime(vDate);
		deviceHistory.setIsActive(true);
		deviceHistory.setLangCode("eng");
		deviceHistoryList.add(deviceHistory);

	}

	List<DeviceSpecification> deviceSpecList;
	DeviceSpecification deviceSpecification;
	DeviceSpecificationDto deviceSpecificationDto;

	@Before
	public void DeviceSpecsetUp() {

		deviceSpecList = new ArrayList<>();

		deviceSpecification = new DeviceSpecification();
		deviceSpecification.setId("1000");
		deviceSpecification.setName("Laptop");
		deviceSpecification.setBrand("HP");
		deviceSpecification.setModel("G-Series");
		deviceSpecification.setMinDriverversion("version 7");
		deviceSpecification.setDescription("HP Laptop");
		deviceSpecification.setIsActive(true);

		deviceSpecification = new DeviceSpecification();
		deviceSpecification.setId("1000");
		deviceSpecification.setLangCode("ENG");
		deviceSpecification.setName("laptop");
		deviceSpecification.setIsActive(true);
		deviceSpecification.setDescription("HP Description");
		deviceSpecification.setBrand("HP");
		deviceSpecification.setDeviceTypeCode("1231");
		deviceSpecification.setLangCode("eng");
		deviceSpecification.setMinDriverversion("version 0.1");
		deviceSpecification.setModel("3168ngw");
		deviceSpecList.add(deviceSpecification);

		deviceSpecificationDto = new DeviceSpecificationDto();
		MapperUtils.map(deviceSpecification, deviceSpecificationDto);
	}

	private List<Machine> machineList;
	private Machine machine;
	private MachineHistory machineHistory;
	private MachineDto machineDto;

	LocalDateTime specificDate;
	String machineJson;

	private void machineSetUp() {

		specificDate = LocalDateTime.now(ZoneId.of("UTC"));
		machineList = new ArrayList<>();
		machine = new Machine();
		machine.setId("1000");
		machine.setLangCode("eng");
		machine.setName("HP");
		machine.setIpAddress("129.0.0.0");
		machine.setMacAddress("178.0.0.0");
		machine.setMachineSpecId("1010");
		machine.setSerialNum("123");
		machine.setRegCenterId("10002");
		machine.setIsActive(true);
		// machine.setValidityDateTime(specificDate);
		machineList.add(machine);

		machineHistory = new MachineHistory();

		MapperUtils.mapFieldValues(machine, machineHistory);
		machineDto = new MachineDto();
		MapperUtils.map(machine, machineDto);

	}

	

	List<Device> deviceList;
	List<Object[]> objectList;
	DeviceHistory deviceHistory;
	Page<Device> pageDeviceEntity;
	DevicePutReqDto devicePutDto = null;
	List<Zone> zonesDevice;

	private void deviceSetup() {

		devicePutDto = new DevicePutReqDto();
		devicePutDto.setDeviceSpecId("123");
		devicePutDto.setId("1");
		devicePutDto.setIpAddress("asd");
		devicePutDto.setIsActive(true);
		devicePutDto.setLangCode("eng");
		devicePutDto.setMacAddress("asd");
		devicePutDto.setName("asd");
		devicePutDto.setSerialNum("asd");
		devicePutDto.setZoneCode("MOR");

		LocalDateTime specificDate = LocalDateTime.of(2018, Month.JANUARY, 1, 10, 10, 30);
		Timestamp validDateTime = Timestamp.valueOf(specificDate);
		deviceDto = new DeviceDto();
		deviceDto.setDeviceSpecId("123");
		deviceDto.setId("1");
		deviceDto.setIpAddress("asd");
		deviceDto.setIsActive(true);
		deviceDto.setLangCode("eng");
		deviceDto.setMacAddress("asd");
		deviceDto.setName("asd");
		deviceDto.setSerialNum("asd");
		deviceDto.setZoneCode("MOR");

		deviceList = new ArrayList<>();
		device = new Device();
		device.setId("1000");
		device.setName("Printer");
		device.setLangCode("eng");
		device.setIsActive(true);
		device.setMacAddress("127.0.0.0");
		device.setIpAddress("127.0.0.10");
		device.setSerialNum("234");
		device.setDeviceSpecId("234");
		device.setZoneCode("MOR");
		device.setValidityDateTime(specificDate);
		deviceList.add(device);

		Device device = new Device();
		device.setId("10001");
		device.setName("laptop");
		device.setDeviceSpecId("10001");
		device.setIsActive(true);
		device.setIpAddress("102.0.0.0");
		List<Device> devicelist = new ArrayList<>();
		devicelist.add(device);
		pageDeviceEntity = new PageImpl<>(devicelist);

		objectList = new ArrayList<>();
		Object objects[] = { "1001", "Laptop", "129.0.0.0", "123", "129.0.0.0", "1212", "eng", true, validDateTime,
				"NTH", "LaptopCode" };
		objectList.add(objects);

		deviceHistory = new DeviceHistory();

		zonesDevice = new ArrayList<>();
		Zone zone = new Zone("MOR", "eng", "Berkane", (short) 0, "Province", "MOR", " ");
		zonesDevice.add(zone);

	}

	private void templateFileFormatSetup() {
		templateFileFormatDto = new TemplateFileFormatDto();
		templateFileFormatDto.setCode("xml");
		templateFileFormatDto.setLangCode("eng");
		templateFileFormatDto.setIsActive(true);
		templateFileFormat = new TemplateFileFormat();
		templateFileFormat.setCode("xml");
		templateFileFormat.setLangCode("eng");
		templateFileFormat.setIsActive(true);

		templateFileFormatRequestDto.setRequest(templateFileFormatDto);
	}

	private void addValidDocumentSetUp() {
		validDocument = new ValidDocument();
		validDocument.setDocTypeCode("ttt");
		validDocument.setDocCategoryCode("ddd");
	}

	private void languageTestSetup() {
		// creating data coming from user

		languageDto = new LanguageDto();
		languageDto.setCode("eng");
		languageDto.setName("terman");
		languageDto.setIsActive(Boolean.TRUE);

		language = new Language();
		language.setCode("eng");
		language.setName("terman");
		language.setIsActive(Boolean.TRUE);
	}

	private void documentTypeSetUp() {
		type = new DocumentType();
		type.setCode("DT001");
		// documentTypes = new ArrayList<>();
		// documentTypes.add(type);
	}

	private void registrationCenterTypeSetUp() {
		regCenterType = new RegistrationCenterType();
		regCenterType.setCode("T01");
		regCenterTypes = new ArrayList<>();
		regCenterTypes.add(regCenterType);

	}

	private void documentCategorySetUp() {
		category = new DocumentCategory();
		category.setCode("DC001");
		entities = new ArrayList<>();
		entities.add(category);
	}

	private void titleIntegrationSetup() {
		titleList = new ArrayList<>();
		title = new Title();
		title.setIsActive(true);
		title.setCreatedBy("Ajay");
		title.setCreatedDateTime(null);
		title.setCode("Mr.");
		title.setLangCode("eng");
		title.setTitleDescription("AAAAAAAAAAAA");
		title.setTitleName("HELLO");
		title.setUpdatedBy("XYZ");
		title.setUpdatedDateTime(null);
		titleList.add(title);
	}

	

	RegistarionCenterReqDto<RegCenterPostReqDto> regPostRequest = null;

	RegistrationCenter registrationCenter1 = null;
	RegistrationCenterHistory registrationCenterHistory = null;
	RegistrationCenter registrationCenter2 = null;
	RegistrationCenter registrationCenter3 = null;

	List<RegistrationCenter> registrationCenterEntityList = null;

	private void registrationCenterSetup() {
		registrationCenter = new RegistrationCenter();
		registrationCenter.setId("1");
		registrationCenter.setName("bangalore");
		registrationCenter.setLatitude("12.9180722");
		registrationCenter.setLongitude("77.5028792");
		registrationCenter.setLangCode("eng");
		registrationCenter.setHolidayLocationCode("KAR");
		registrationCenters.add(registrationCenter);
		Location location = new Location();
		location.setCode("BLR");

		banglore = new RegistrationCenter();
		banglore.setId("1");
		banglore.setName("bangalore");
		banglore.setLatitude("12.9180722");
		banglore.setLongitude("77.5028792");
		banglore.setLangCode("eng");
		banglore.setLocationCode("LOC");
		chennai = new RegistrationCenter();
		chennai.setId("2");
		chennai.setName("Bangalore Central");
		chennai.setLangCode("eng");
		chennai.setLocationCode("LOC");
		registrationCenters.add(banglore);
		registrationCenters.add(chennai);

		// ----
		LocalTime centerStartTime = LocalTime.of(1, 10, 10, 30);
		LocalTime centerEndTime = LocalTime.of(1, 10, 10, 30);
		LocalTime lunchStartTime = LocalTime.of(1, 10, 10, 30);
		LocalTime lunchEndTime = LocalTime.of(1, 10, 10, 30);
		LocalTime perKioskProcessTime = LocalTime.of(1, 10, 10, 30);

		regPostRequest = new RegistarionCenterReqDto<>();
		List<RegCenterPostReqDto> regCenterPostReqDtoList = new ArrayList<>();
		regPostRequest.setId("mosip.idtype.create");
		regPostRequest.setVersion("1.0");
		// 1st obj
		RegCenterPostReqDto registrationCenterDto1 = new RegCenterPostReqDto();
		registrationCenterDto1.setName("TEST CENTER");
		registrationCenterDto1.setAddressLine1("Address Line 1");
		registrationCenterDto1.setAddressLine2("Address Line 2");
		registrationCenterDto1.setAddressLine3("Address Line 3");
		registrationCenterDto1.setCenterTypeCode("REG");
		registrationCenterDto1.setContactPerson("TEST");
		registrationCenterDto1.setContactPhone("9999999999");
		registrationCenterDto1.setHolidayLocationCode("HLC01");
		registrationCenterDto1.setLangCode("eng");
		registrationCenterDto1.setLatitude("12.9646818");
		registrationCenterDto1.setLocationCode("10190");
		registrationCenterDto1.setLongitude("77.70168");
		registrationCenterDto1.setPerKioskProcessTime(perKioskProcessTime);
		registrationCenterDto1.setCenterStartTime(centerStartTime);
		registrationCenterDto1.setCenterEndTime(centerEndTime);
		registrationCenterDto1.setLunchStartTime(lunchStartTime);
		registrationCenterDto1.setLunchEndTime(lunchEndTime);
		registrationCenterDto1.setTimeZone("UTC");
		registrationCenterDto1.setWorkingHours("9");
		registrationCenterDto1.setZoneCode("JRD");
		regCenterPostReqDtoList.add(registrationCenterDto1);

		// 2nd obj
		RegCenterPostReqDto registrationCenterDto2 = new RegCenterPostReqDto();
		registrationCenterDto2.setName("TEST CENTER");
		registrationCenterDto2.setAddressLine1("Address Line 1");
		registrationCenterDto2.setAddressLine2("Address Line 2");
		registrationCenterDto2.setAddressLine3("Address Line 3");
		registrationCenterDto2.setCenterTypeCode("REG");
		registrationCenterDto2.setContactPerson("TEST");
		registrationCenterDto2.setContactPhone("9999999999");
		registrationCenterDto2.setHolidayLocationCode("HLC01");
		registrationCenterDto2.setLangCode("ara");
		registrationCenterDto2.setLatitude("12.9646818");
		registrationCenterDto2.setLocationCode("10190");
		registrationCenterDto2.setLongitude("77.70168");
		registrationCenterDto2.setPerKioskProcessTime(perKioskProcessTime);
		registrationCenterDto2.setCenterStartTime(centerStartTime);
		registrationCenterDto2.setCenterEndTime(centerEndTime);
		registrationCenterDto2.setLunchStartTime(lunchStartTime);
		registrationCenterDto2.setLunchEndTime(lunchEndTime);
		registrationCenterDto2.setTimeZone("UTC");
		registrationCenterDto2.setWorkingHours("9");
		registrationCenterDto2.setZoneCode("JRD");
		regCenterPostReqDtoList.add(registrationCenterDto2);

		// 3rd obj
		RegCenterPostReqDto registrationCenterDto3 = new RegCenterPostReqDto();
		registrationCenterDto3.setName("TEST CENTER");
		registrationCenterDto3.setAddressLine1("Address Line 1");
		registrationCenterDto3.setAddressLine2("Address Line 2");
		registrationCenterDto3.setAddressLine3("Address Line 3");
		registrationCenterDto3.setCenterTypeCode("REG");
		registrationCenterDto3.setContactPerson("TEST");
		registrationCenterDto3.setContactPhone("9999999999");
		registrationCenterDto3.setHolidayLocationCode("HLC01");
		registrationCenterDto3.setLangCode("fra");
		registrationCenterDto3.setLatitude("12.9646818");
		registrationCenterDto3.setLocationCode("10190");
		registrationCenterDto3.setLongitude("77.70168");
		registrationCenterDto3.setPerKioskProcessTime(perKioskProcessTime);
		registrationCenterDto3.setCenterStartTime(centerStartTime);
		registrationCenterDto3.setCenterEndTime(centerEndTime);
		registrationCenterDto3.setLunchStartTime(lunchStartTime);
		registrationCenterDto3.setLunchEndTime(lunchEndTime);
		registrationCenterDto3.setTimeZone("UTC");
		registrationCenterDto3.setWorkingHours("9");
		registrationCenterDto3.setZoneCode("JRD");
		regCenterPostReqDtoList.add(registrationCenterDto3);

		regPostRequest.setRequest(regCenterPostReqDtoList);

		registrationCenterEntityList = new ArrayList<>();
		// entity1
		registrationCenter1 = new RegistrationCenter();
		registrationCenter1.setName("TEST CENTER");
		registrationCenter1.setAddressLine1("Address Line 1");
		registrationCenter1.setAddressLine2("Address Line 2");
		registrationCenter1.setAddressLine3("Address Line 3");
		registrationCenter1.setCenterTypeCode("REG");
		registrationCenter1.setContactPerson("TEST");
		registrationCenter1.setContactPhone("9999999999");
		registrationCenter1.setHolidayLocationCode("HLC01");
		registrationCenter1.setId("10000");
		registrationCenter1.setIsActive(false);
		registrationCenter1.setLangCode("eng");
		registrationCenter1.setLatitude("12.9646818");
		registrationCenter1.setLocationCode("10190");
		registrationCenter1.setLongitude("77.70168");
		registrationCenter1.setPerKioskProcessTime(perKioskProcessTime);
		registrationCenter1.setCenterStartTime(centerStartTime);
		registrationCenter1.setCenterEndTime(centerEndTime);
		registrationCenter1.setLunchStartTime(lunchStartTime);
		registrationCenter1.setLunchEndTime(lunchEndTime);
		registrationCenter1.setNumberOfKiosks((short) 0);
		registrationCenter1.setTimeZone("UTC");
		registrationCenter1.setWorkingHours("9");
		registrationCenter1.setZoneCode("JRD");

		registrationCenterEntityList.add(registrationCenter1);

		registrationCenterHistory = new RegistrationCenterHistory();
		registrationCenterHistory.setName("TEST CENTER");
		registrationCenterHistory.setAddressLine1("Address Line 1");
		registrationCenterHistory.setAddressLine2("Address Line 2");
		registrationCenterHistory.setAddressLine3("Address Line 3");
		registrationCenterHistory.setCenterTypeCode("REG");
		registrationCenterHistory.setContactPerson("TEST");
		registrationCenterHistory.setContactPhone("9999999999");
		registrationCenterHistory.setHolidayLocationCode("HLC01");
		registrationCenterHistory.setId("10000");
		registrationCenterHistory.setIsActive(false);
		registrationCenterHistory.setLangCode("fra");
		registrationCenterHistory.setLatitude("12.9646818");
		registrationCenterHistory.setLocationCode("10190");
		registrationCenterHistory.setLongitude("77.70168");
		registrationCenterHistory.setPerKioskProcessTime(perKioskProcessTime);
		registrationCenterHistory.setCenterStartTime(centerStartTime);
		registrationCenterHistory.setCenterEndTime(centerEndTime);
		registrationCenterHistory.setLunchStartTime(lunchStartTime);
		registrationCenterHistory.setLunchEndTime(lunchEndTime);
		registrationCenterHistory.setNumberOfKiosks((short) 0);
		registrationCenterHistory.setTimeZone("UTC");
		registrationCenterHistory.setWorkingHours("9");

	}

	private void registrationCenterHistorySetup() {
		center = new RegistrationCenterHistory();
		center.setId("1");
		center.setName("bangalore");
		center.setLatitude("12.9180722");
		center.setLongitude("77.5028792");
		center.setLangCode("eng");
		center.setLocationCode("BLR");
		centers.add(center);
	}

	private void packetRejectionSetup() {
		ReasonCategory reasonCategory = new ReasonCategory();
		ReasonList reasonListObj = new ReasonList();
		reasonListDto = new ReasonListDto();
		postReasonCategoryDto = new PostReasonCategoryDto();
		postReasonCategoryDto.setCode("RC1");
		postReasonCategoryDto.setDescription("Reason category");
		postReasonCategoryDto.setIsActive(true);
		postReasonCategoryDto.setLangCode("eng");
		postReasonCategoryDto.setName("Reason category");
		reasonListDto.setCode("RL1");
		reasonListDto.setDescription("REASONLIST");
		reasonListDto.setLangCode("eng");
		reasonListDto.setIsActive(true);
		reasonListDto.setName("Reason List 1");
		reasonListDto.setRsnCatCode("RC1");
		reasonList = new ArrayList<>();
		reasonListObj.setCode("RL1");
		reasonListObj.setLangCode("eng");
		reasonListObj.setRsnCatCode("RC1");
		reasonListObj.setDescription("reasonList");
		reasonList.add(reasonListObj);
		reasonCategory.setReasonList(reasonList);
		reasonCategory.setCode("RC1");
		reasonCategory.setLangCode("eng");
		reasoncategories = new ArrayList<>();
		reasoncategories.add(reasonCategory);
		titleId = new CodeAndLanguageCodeID();
		titleId.setCode("RC1");
		titleId.setLangCode("eng");
		reasonListId = new CodeLangCodeAndRsnCatCodeID();
		reasonListId.setCode("RL1");
		reasonListId.setLangCode("eng");
		reasonListId.setRsnCatCode("RC1");
		RequestWrapper<ReasonListDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.create.packetrejection.reason");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(reasonListDto);
		RequestWrapper<PostReasonCategoryDto> requestDto1 = new RequestWrapper<>();
		requestDto1.setId("mosip.create.packetrejection.reason");
		requestDto1.setVersion("1.0.0");
		requestDto1.setRequest(postReasonCategoryDto);
		try {
			reasonListRequest = mapper.writeValueAsString(requestDto);
			reasonCategoryRequest = mapper.writeValueAsString(requestDto1);
		} catch (JsonProcessingException e) {

			e.printStackTrace();
		}
	}

	private void idTypeSetup() {
		idType = new IdType();
		idType.setIsActive(true);
		idType.setCreatedBy("testCreation");
		idType.setLangCode("eng");
		idType.setCode("POA");
		idType.setDescr("Proof Of Address");
		idTypes = new ArrayList<>();
		idTypes.add(idType);
	}

	private void holidaySetup() {
		LocalDateTime specificDate = LocalDateTime.of(2018, Month.JANUARY, 1, 10, 10, 30);
		LocalDate date = LocalDate.of(2018, Month.NOVEMBER, 7);
		holidays = new ArrayList<>();
		holiday = new Holiday();

		holiday = new Holiday();
		// holiday.setHolidayId(new HolidayID("KAR", date, "eng", "Diwali"));
		holiday.setLocationCode("KAR");
		holiday.setHolidayDate(date);
		holiday.setLangCode("eng");
		holiday.setHolidayName("Diwali");
		holiday.setHolidayId(1);
		holiday.setCreatedBy("John");
		holiday.setCreatedDateTime(specificDate);
		holiday.setHolidayDesc("Diwali");
		holiday.setIsActive(true);

		Holiday holiday2 = new Holiday();
		// holiday2.setHolidayId(new HolidayID("KAH", date, "eng", "Durga Puja"));
		holiday2.setLocationCode("KAH");
		holiday2.setHolidayDate(date);
		holiday2.setLangCode("eng");
		holiday2.setHolidayName("Durga Puja");
		holiday2.setHolidayId(1);
		holiday2.setCreatedBy("John");
		holiday2.setCreatedDateTime(specificDate);
		holiday2.setHolidayDesc("Diwali");
		holiday2.setIsActive(true);

		holidays.add(holiday);
		holidays.add(holiday2);
	}

	private void genderTypeSetup() {

		genderDto = new GenderTypeDto();
		genderDto.setCode("GEN01");
		genderDto.setGenderName("Male");
		genderDto.setIsActive(true);
		genderDto.setLangCode("eng");

		genderTypes = new ArrayList<>();
		genderTypesNull = new ArrayList<>();
		genderType = new Gender();
		genderId = new GenderID();
		genderId.setGenderCode("GEN01");
		genderId.setGenderName("Male");
		genderType.setIsActive(true);
		genderType.setCreatedBy("MosipAdmin");
		genderType.setCreatedDateTime(null);
		genderType.setIsDeleted(false);
		genderType.setDeletedDateTime(null);
		genderType.setLangCode("eng");
		genderType.setUpdatedBy("Dom");
		genderType.setUpdatedDateTime(null);
		genderTypes.add(genderType);
	}

	private void blacklistedSetup() {
		words = new ArrayList<>();

		BlacklistedWords blacklistedWords = new BlacklistedWords();
		blacklistedWords.setWord("abc");
		blacklistedWords.setLangCode("e");
		blacklistedWords.setDescription("no description available");

		words.add(blacklistedWords);
		blacklistedWords.setLangCode("TST");
		blacklistedWords.setIsActive(true);
		blacklistedWords.setWord("testword");
	}

	@Before
	public void LocationSetup() {
		locationHierarchies = new ArrayList<>();
		Location locationHierarchy = new Location();
		locationHierarchy.setCode("PAT");
		locationHierarchy.setName("PATANA");
		locationHierarchy.setHierarchyLevel((short) 2);
		locationHierarchy.setHierarchyName("Distic");
		locationHierarchy.setParentLocCode("BHR");
		locationHierarchy.setLangCode("ENG");
		locationHierarchy.setCreatedBy("admin");
		locationHierarchy.setUpdatedBy("admin");
		locationHierarchy.setIsActive(true);
		locationHierarchies.add(locationHierarchy);
		Location locationHierarchy1 = new Location();
		locationHierarchy1.setCode("BX");
		locationHierarchy1.setName("BAXOR");
		locationHierarchy1.setHierarchyLevel((short) 2);
		locationHierarchy1.setHierarchyName("Distic");
		locationHierarchy1.setParentLocCode("BHR");
		locationHierarchy1.setLangCode("ENG");
		locationHierarchy1.setCreatedBy("admin");
		locationHierarchy1.setUpdatedBy("admin");
		locationHierarchy1.setIsActive(true);
		locationHierarchies.add(locationHierarchy1);

	}

	private RegistrationCenterDeviceHistoryDto registrationCenterDeviceHistoryDto;

	private void registrationCenterDeviceHistorySetup() {
		registrationCenterDeviceHistoryDto = new RegistrationCenterDeviceHistoryDto();
		registrationCenterDeviceHistoryDto.setDeviceId("101");
		registrationCenterDeviceHistoryDto.setRegCenterId("1");
		registrationCenterDeviceHistoryDto.setEffectivetimes(localDateTimeUTCFormat);

		DeviceHistory  deviceHistory= new DeviceHistory();
		deviceHistory.setId("101");
		deviceHistory.setEffectDateTime(localDateTimeUTCFormat);
		deviceHistory.setRegCenterId("1");
		deviceHistory.setIsActive(true);
		deviceHistory.setIsDeleted(false);

	}

	



	// -----------------------------LanguageImplementationTest----------------------------------

	@Test
	@WithUserDetails("global-admin")
	public void updateLanguagesTest() throws Exception {
		RequestWrapper<LanguageDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.language.update");
		requestDto.setVersion("1.0.0");

		LanguageDto frenchDto = new LanguageDto();
		frenchDto.setCode("fra");
		frenchDto.setFamily("fra");
		frenchDto.setName("FRENCH");
		frenchDto.setIsActive(true);
		requestDto.setRequest(frenchDto);

		Language french = new Language();
		french.setCode("fra");
		french.setFamily("fra");
		french.setName("french");
		french.setIsActive(true);
		french.setNativeName("french_naiv");
		String content = mapper.writeValueAsString(requestDto);
		when(languageRepository.findLanguageByCode(frenchDto.getCode())).thenReturn(french);
		when(languageRepository.update(Mockito.any())).thenReturn(french);
		mockMvc.perform(put("/languages").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void updateLanguagesDataAccessLayerTest() throws Exception {
		RequestWrapper<LanguageDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.language.update");
		requestDto.setVersion("1.0.0");

		LanguageDto frenchDto = new LanguageDto();
		frenchDto.setCode("fra");
		frenchDto.setFamily("french");
		frenchDto.setName("FRENCH");
		frenchDto.setIsActive(true);
		requestDto.setRequest(frenchDto);

		Language french = new Language();
		french.setCode("fra");
		french.setFamily("fra");
		french.setName("french");
		french.setIsActive(true);
		french.setNativeName("french_naiv");
		String content = mapper.writeValueAsString(requestDto);
		when(languageRepository.findLanguageByCode(frenchDto.getCode())).thenReturn(french);
		when(languageRepository.update(Mockito.any())).thenThrow(DataAccessLayerException.class);
		mockMvc.perform(put("/languages").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().isInternalServerError());

	}

	@Test
	@WithUserDetails("global-admin")
	public void updateLanguagesNotFoundTest() throws Exception {
		RequestWrapper<LanguageDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.language.update");
		requestDto.setVersion("1.0.0");

		LanguageDto frenchDto = new LanguageDto();
		frenchDto.setCode("FRN");
		frenchDto.setFamily("french");
		frenchDto.setName("FRENCH");
		frenchDto.setIsActive(true);
		requestDto.setRequest(frenchDto);

		Language french = new Language();
		french.setCode("FRN");
		french.setFamily("frn");
		french.setName("french");
		french.setIsActive(true);
		french.setNativeName("french_naiv");
		String content = mapper.writeValueAsString(requestDto);
		when(languageRepository.findLanguageByCode(frenchDto.getCode())).thenReturn(null);
		mockMvc.perform(put("/languages").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteLanguagesTest() throws Exception {
		when(languageRepository.findLanguageByCode(languageDto.getCode())).thenReturn(language);
		when(languageRepository.update(Mockito.any())).thenReturn(language);
		mockMvc.perform(delete("/languages/{code}", languageDto.getCode())).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteDataAccessLayerLanguagesTest() throws Exception {
		when(languageRepository.findLanguageByCode(languageDto.getCode())).thenReturn(language);
		when(languageRepository.update(Mockito.any())).thenThrow(DataAccessLayerException.class);
		mockMvc.perform(delete("/languages/{code}", languageDto.getCode())).andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteNotFoundLanguagesTest() throws Exception {
		when(languageRepository.findLanguageByCode(languageDto.getCode())).thenReturn(null);
		mockMvc.perform(delete("/languages/{code}", languageDto.getCode())).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void saveLanguagesTest() throws Exception {
		RequestWrapper<LanguageDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.language.create");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(languageDto);
		String content = mapper.writeValueAsString(requestDto);
		when(languageRepository.create(Mockito.any())).thenReturn(language);
		mockMvc.perform(post("/languages").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void saveLanguagesDataAccessLayerExceptionTest() throws Exception {
		RequestWrapper<LanguageDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.language.create");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(languageDto);
		String content = mapper.writeValueAsString(requestDto);
		when(languageRepository.create(Mockito.any())).thenThrow(DataAccessLayerException.class);
		mockMvc.perform(post("/languages").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().isInternalServerError());

	}

	@Test
	@WithUserDetails("global-admin")
	public void saveLanguagesExceptionTest() throws Exception {
		RequestWrapper<LanguageDto> requestDto = new RequestWrapper<>();
		requestDto.setId("");
		requestDto.setVersion("1.0.0");
		String content = mapper.writeValueAsString(requestDto);
		mockMvc.perform(post("/languages").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().isOk());

	}

	// -----------------------------BlacklistedWordsTest----------------------------------
	@Test
	@WithUserDetails("individual")
	public void getAllWordsBylangCodeSuccessTest() throws Exception {
		when(wordsRepository.findAllByLangCode(anyString())).thenReturn(words);
		mockMvc.perform(get("/blacklistedwords/{langcode}", "ENG")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("individual")
	public void getAllWordsBylangCodeNullResponseTest() throws Exception {
		when(wordsRepository.findAllByLangCode(anyString())).thenReturn(null);
		mockMvc.perform(get("/blacklistedwords/{langcode}", "ENG")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("individual")
	public void getAllWordsBylangCodeEmptyArrayResponseTest() throws Exception {
		when(wordsRepository.findAllByLangCode(anyString())).thenReturn(new ArrayList<>());
		mockMvc.perform(get("/blacklistedwords/{langcode}", "ENG")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("individual")
	public void getAllWordsBylangCodeFetchExceptionTest() throws Exception {
		when(wordsRepository.findAllByLangCode(anyString())).thenThrow(DataRetrievalFailureException.class);
		mockMvc.perform(get("/blacklistedwords/{langcode}", "ENG")).andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("individual")
	public void getAllWordsBylangCodeNullArgExceptionTest() throws Exception {
		mockMvc.perform(get("/blacklistedwords/{langcode}", " ")).andExpect(status().isOk());
	}

	// -----------------------------GenderTypeTest----------------------------------
	@Test
	@WithUserDetails("id-auth")
	public void getGenderByLanguageCodeFetchExceptionTest() throws Exception {

		Mockito.when(genderTypeRepository.findGenderByLangCodeAndIsDeletedFalseOrIsDeletedIsNull("ENG"))
				.thenThrow(DataAccessLayerException.class);

		mockMvc.perform(get("/gendertypes/ENG").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError());

	}

	@Test
	@WithUserDetails("individual")
	public void getGenderByLanguageCodeNotFoundExceptionTest() throws Exception {

		Mockito.when(genderTypeRepository.findGenderByLangCodeAndIsDeletedFalseOrIsDeletedIsNull("ENG"))
				.thenReturn(genderTypesNull);

		mockMvc.perform(get("/gendertypes/ENG").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("id-auth")
	public void getAllGenderFetchExceptionTest() throws Exception {

		Mockito.when(genderTypeRepository.findAllByIsActiveAndIsDeleted()).thenThrow(DataAccessLayerException.class);

		mockMvc.perform(get("/gendertypes").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError());

	}

	@Test
	@WithUserDetails("id-auth")
	public void getAllGenderNotFoundExceptionTest() throws Exception {

		Mockito.when(genderTypeRepository.findAllByIsActiveAndIsDeleted()).thenReturn(genderTypesNull);

		mockMvc.perform(get("/gendertypes").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("individual")
	public void getGenderByLanguageCodeTest() throws Exception {

		Mockito.when(genderTypeRepository.findGenderByLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()))
				.thenReturn(genderTypes);
		mockMvc.perform(get("/gendertypes/{languageCode}", "ENG")).andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("id-auth")
	public void getAllGendersTest() throws Exception {
		Mockito.when(genderTypeRepository.findAllByIsActiveAndIsDeleted()).thenReturn(genderTypes);
		mockMvc.perform(get("/gendertypes")).andExpect(status().isOk());

	}

	// -----------------------------HolidayTest----------------------------------

	@Test
	@WithUserDetails("zonal-admin")
	public void getHolidayAllHolidaysSuccessTest() throws Exception {
		when(holidayRepository.findAllNonDeletedHoliday()).thenReturn(holidays);
		mockMvc.perform(get("/holidays")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void getAllHolidaNoHolidayFoundTest() throws Exception {
		mockMvc.perform(get("/holidays")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void getAllHolidaysHolidayFetchExceptionTest() throws Exception {
		when(holidayRepository.findAllNonDeletedHoliday()).thenThrow(DataRetrievalFailureException.class);
		mockMvc.perform(get("/holidays")).andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void getHolidayByIdSuccessTest() throws Exception {
		when(holidayRepository.findAllById(any(Integer.class))).thenReturn(holidays);
		mockMvc.perform(get("/holidays/{holidayId}", 1)).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void getHolidayByIdHolidayFetchExceptionTest() throws Exception {
		when(holidayRepository.findAllById(any(Integer.class))).thenThrow(DataRetrievalFailureException.class);
		mockMvc.perform(get("/holidays/{holidayId}", 1)).andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("global-admin")
	public void getHolidayByIdNoHolidayFoundTest() throws Exception {
		mockMvc.perform(get("/holidays/{holidayId}", 1)).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void getHolidayByIdAndLangCodeSuccessTest() throws Exception {
		when(holidayRepository.findHolidayByIdAndHolidayIdLangCode(any(Integer.class), anyString()))
				.thenReturn(holidays);
		mockMvc.perform(get("/holidays/{holidayId}/{languagecode}", 1, "ENG")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void getHolidayByIdAndLangCodeHolidayFetchExceptionTest() throws Exception {
		when(holidayRepository.findHolidayByIdAndHolidayIdLangCode(any(Integer.class), anyString()))
				.thenThrow(DataRetrievalFailureException.class);
		mockMvc.perform(get("/holidays/{holidayId}/{languagecode}", 1, "ENG"))
				.andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("global-admin")
	public void getHolidayByIdAndLangCodeHolidayNoDataFoundTest() throws Exception {
		mockMvc.perform(get("/holidays/{holidayId}/{languagecode}", 1, "ENG")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void addHolidayTypeTest() throws Exception {
		String json = "{\n" + "  \"id\": \"string\",\n" + "  \"metadata\": {},\n" + "  \"request\": {\n"
				+ "    \"holidayDate\": \"2019-01-01\",\n" + "    \"holidayDay\": \"Sunday\",\n"
				+ "    \"holidayDesc\": \"New Year\",\n" + "    \"holidayMonth\": \"January\",\n"
				+ "    \"holidayName\": \"New Year\",\n" + "    \"holidayYear\": \"2019\",\n" + "    \"id\": 1,\n"
				+ "    \"isActive\": true,\n" + "    \"langCode\": \"eng\",\n" + "    \"locationCode\": \"BLR\"\n"
				+ "  },\n" + "  \"requesttime\": \"2018-12-06T08:49:32.190Z\",\n" + "  \"version\": \"string\"\n" + "}";
		when(holidayRepository.create(Mockito.any())).thenReturn(holiday);
		mockMvc.perform(post("/holidays").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void addHolidayTypeLanguageValidationTest() throws Exception {
		String json = "{\n" + "  \"id\": \"string\",\n" + "  \"metadata\": {},\n" + "  \"request\": {\n"
				+ "    \"holidayDate\": \"2019-01-01\",\n" + "    \"holidayDay\": \"Sunday\",\n"
				+ "    \"holidayDesc\": \"New Year\",\n" + "    \"holidayMonth\": \"January\",\n"
				+ "    \"holidayName\": \"New Year\",\n" + "    \"holidayYear\": \"2019\",\n" + "    \"id\": 1,\n"
				+ "    \"isActive\": true,\n" + "    \"langCode\": \"asd\",\n" + "    \"locationCode\": \"BLR\"\n"
				+ "  },\n" + "  \"requesttime\": \"2018-12-06T08:49:32.190Z\",\n" + "  \"version\": \"string\"\n" + "}";
		when(holidayRepository.create(Mockito.any())).thenReturn(holiday);
		mockMvc.perform(post("/holidays").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void addHolidayTypeExceptionTest() throws Exception {

		String json = "{\n" + "  \"id\": \"string\",\n" + "  \"metadata\": {},\n" + "  \"request\": {\n"
				+ "    \"holidayDate\": \"2019-01-01\",\n" + "    \"holidayDesc\": \"New Year\",\n" 
				+ "    \"holidayName\": \"New Year\",\n"  
				+ "    \"isActive\": true,\n" + "    \"langCode\": \"eng\",\n" + "    \"locationCode\": \"BLR\"\n"
				+ "  },\n" + "  \"requesttime\": \"2018-12-06T08:49:32.190Z\",\n" + "  \"version\": \"string\"\n" + "}";
		when(holidayRepository.create(Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot execute ", null));
		when(locationRepository.findByCode(anyString())).thenReturn(locationHierarchies);
		when(holidayRepository.findHolidayByHolidayDate(Mockito.any()))
		.thenReturn(holidays);
		when(holidayRepository.findAll()).thenReturn(holidays);
		HolidayDto holidayDto =new HolidayDto();
		holidayDto.setHolidayDate(LocalDate.parse("2019-01-01"));
		holidayDto.setHolidayDesc("New Year");
		holidayDto.setHolidayName("New Year");
		holidayDto.setIsActive(true);
		holidayDto.setLangCode("eng");
		holidayDto.setLocationCode("BLR");
		when(holidayRepository.findHolidayByHolidayDateLocationCodeLangCode(any(), any(),any())).thenReturn(null);
		
		mockMvc.perform(post("/holidays").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isInternalServerError());
	}

	// -----------------------------IdTypeTest----------------------------------
	@Test
	@WithUserDetails("zonal-admin")
	public void getIdTypesByLanguageCodeFetchExceptionTest() throws Exception {
		when(idTypeRepository.findByLangCode("ENG")).thenThrow(DataAccessLayerException.class);
		mockMvc.perform(get("/idtypes/ENG").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void getIdTypesByLanguageCodeNotFoundExceptionTest() throws Exception {
		List<IdType> idTypeList = new ArrayList<>();
		idTypeList.add(idType);
		when(idTypeRepository.findByLangCode("ENG")).thenReturn(idTypeList);
		mockMvc.perform(get("/idtypes/HIN").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void getIdTypesByLanguageCodeTest() throws Exception {
		List<IdType> idTypeList = new ArrayList<>();
		idTypeList.add(idType);
		when(idTypeRepository.findByLangCode("ENG")).thenReturn(idTypeList);

		mockMvc.perform(get("/idtypes/ENG").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.response.idtypes[0].code", is("POA")));
	}

	// -----------------------------PacketRejectionTest----------------------------------
	@Test
	@WithUserDetails("zonal-admin")
	public void getAllRjectionReasonTest() throws Exception {
		Mockito.when(reasonCategoryRepository.findReasonCategoryByIsDeletedFalseOrIsDeletedIsNull())
				.thenReturn(reasoncategories);
		mockMvc.perform(get("/packetrejectionreasons")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void getAllRejectionReasonByCodeAndLangCodeTest() throws Exception {
		Mockito.when(reasonCategoryRepository.findReasonCategoryByCodeAndLangCode(ArgumentMatchers.any(),
				ArgumentMatchers.any())).thenReturn(reasoncategories);
		mockMvc.perform(get("/packetrejectionreasons/{code}/{languageCode}", "RC1", "ENG")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void getAllRjectionReasonFetchExceptionTest() throws Exception {
		Mockito.when(reasonCategoryRepository.findReasonCategoryByIsDeletedFalseOrIsDeletedIsNull())
				.thenThrow(DataRetrievalFailureException.class);
		mockMvc.perform(get("/packetrejectionreasons")).andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("global-admin")
	public void getAllRejectionReasonByCodeAndLangCodeFetchExceptionTest() throws Exception {
		Mockito.when(reasonCategoryRepository.findReasonCategoryByCodeAndLangCode(ArgumentMatchers.any(),
				ArgumentMatchers.any())).thenThrow(DataRetrievalFailureException.class);
		mockMvc.perform(get("/packetrejectionreasons/{code}/{languageCode}", "RC1", "ENG"))
				.andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void getAllRjectionReasonRecordsNotFoundTest() throws Exception {
		Mockito.when(reasonCategoryRepository.findReasonCategoryByIsDeletedFalseOrIsDeletedIsNull()).thenReturn(null);
		mockMvc.perform(get("/packetrejectionreasons")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void getRjectionReasonByCodeAndLangCodeRecordsNotFoundExceptionTest() throws Exception {
		Mockito.when(reasonCategoryRepository.findReasonCategoryByCodeAndLangCode(ArgumentMatchers.any(),
				ArgumentMatchers.any())).thenReturn(null);
		mockMvc.perform(get("/packetrejectionreasons/{code}/{languageCode}", "RC1", "ENG")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void getRjectionReasonByCodeAndLangCodeRecordsEmptyExceptionTest() throws Exception {
		Mockito.when(reasonCategoryRepository.findReasonCategoryByCodeAndLangCode(ArgumentMatchers.any(),
				ArgumentMatchers.any())).thenReturn(new ArrayList<ReasonCategory>());
		mockMvc.perform(get("/packetrejectionreasons/{code}/{languageCode}", "RC1", "ENG")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void getAllRjectionReasonRecordsEmptyExceptionTest() throws Exception {
		Mockito.when(reasonCategoryRepository.findReasonCategoryByIsDeletedFalseOrIsDeletedIsNull())
				.thenReturn(new ArrayList<ReasonCategory>());
		mockMvc.perform(get("/packetrejectionreasons")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void createReasonCateogryTest() throws Exception {
		Mockito.when(reasonCategoryRepository.create(Mockito.any())).thenReturn(reasoncategories.get(0));
		mockMvc.perform(post("/packetrejectionreasons/reasoncategory").contentType(MediaType.APPLICATION_JSON)
				.content(reasonCategoryRequest.getBytes())).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void createReasonCateogryLanguageCodeValidatorFailureTest() throws Exception {
		RequestWrapper<PostReasonCategoryDto> requestDto1 = new RequestWrapper<>();
		requestDto1.setId("mosip.create.packetrejection.reason");
		requestDto1.setVersion("1.0.0");
		postReasonCategoryDto.setLangCode("xxx");
		requestDto1.setRequest(postReasonCategoryDto);
		String content = mapper.writeValueAsString(requestDto1);
		Mockito.when(reasonCategoryRepository.create(Mockito.any())).thenReturn(reasoncategories.get(0));
		mockMvc.perform(post("/packetrejectionreasons/reasoncategory").contentType(MediaType.APPLICATION_JSON)
				.content(content.getBytes())).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void createReasonCateogryLanguageCodeValidatorTest() throws Exception {
		Mockito.when(reasonCategoryRepository.create(Mockito.any())).thenReturn(reasoncategories.get(0));
		mockMvc.perform(post("/packetrejectionreasons/reasoncategory").contentType(MediaType.APPLICATION_JSON)
				.content(reasonCategoryRequest.getBytes())).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void createReasonListTest() throws Exception {
		Mockito.when(reasonListRepository.create(Mockito.any())).thenReturn(reasonList.get(0));
		mockMvc.perform(post("/packetrejectionreasons/reasonlist").contentType(MediaType.APPLICATION_JSON)
				.content(reasonListRequest.getBytes())).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void createReasonListLanguageCodeValidatorTest() throws Exception {
		RequestWrapper<ReasonListDto> requestDto1 = new RequestWrapper<>();
		requestDto1.setId("mosip.create.packetrejection.reason");
		requestDto1.setVersion("1.0.0");
		reasonListDto.setLangCode("xxx");
		requestDto1.setRequest(reasonListDto);
		String content = mapper.writeValueAsString(requestDto1);
		Mockito.when(reasonListRepository.create(Mockito.any())).thenReturn(reasonList.get(0));
		mockMvc.perform(post("/packetrejectionreasons/reasonlist").contentType(MediaType.APPLICATION_JSON)
				.content(content.getBytes())).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void createReasonCateogryFetchExceptionTest() throws Exception {
		Mockito.when(reasonCategoryRepository.create(Mockito.any())).thenThrow(DataAccessLayerException.class);
		mockMvc.perform(post("/packetrejectionreasons/reasoncategory").contentType(MediaType.APPLICATION_JSON)
				.content(reasonCategoryRequest.getBytes())).andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("global-admin")
	public void createReasonListFetchExceptionTest() throws Exception {
		Mockito.when(reasonListRepository.create(Mockito.any())).thenThrow(DataAccessLayerException.class);
		mockMvc.perform(post("/packetrejectionreasons/reasonlist").contentType(MediaType.APPLICATION_JSON)
				.content(reasonListRequest.getBytes())).andExpect(status().isInternalServerError());
	}

	// -----------------------------RegistrationCenterTest----------------------------------

	@Test
	@WithUserDetails("reg-processor")
	public void getSpecificRegistrationCenterByIdTest() throws Exception {

		when(repositoryCenterHistoryRepository
				.findByIdAndLangCodeAndEffectivetimesLessThanEqualAndIsDeletedFalseOrIsDeletedIsNull("1", "ENG",
						localDateTimeUTCFormat)).thenReturn(centers);
		mockMvc.perform(get("/registrationcentershistory/1/ENG/".concat(UTC_DATE_TIME_FORMAT_DATE_STRING))
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.response.registrationCentersHistory[0].name", is("bangalore")));
	}

	@Test
	@WithUserDetails("reg-processor")
	public void getRegistrationCentersHistoryNotFoundExceptionTest() throws Exception {
		when(repositoryCenterHistoryRepository
				.findByIdAndLangCodeAndEffectivetimesLessThanEqualAndIsDeletedFalseOrIsDeletedIsNull("1", "ENG",
						localDateTimeUTCFormat)).thenReturn(null);
		mockMvc.perform(get("/registrationcentershistory/1/ENG/".concat(UTC_DATE_TIME_FORMAT_DATE_STRING))
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
	}

	@Test
	@WithUserDetails("reg-processor")
	public void getRegistrationCentersHistoryEmptyExceptionTest() throws Exception {
		when(repositoryCenterHistoryRepository
				.findByIdAndLangCodeAndEffectivetimesLessThanEqualAndIsDeletedFalseOrIsDeletedIsNull("1", "ENG",
						localDateTimeUTCFormat)).thenReturn(new ArrayList<RegistrationCenterHistory>());
		mockMvc.perform(get("/registrationcentershistory/1/ENG/".concat(UTC_DATE_TIME_FORMAT_DATE_STRING))
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
	}

	@Test
	@WithUserDetails("reg-processor")
	public void getRegistrationCentersHistoryFetchExceptionTest() throws Exception {
		when(repositoryCenterHistoryRepository
				.findByIdAndLangCodeAndEffectivetimesLessThanEqualAndIsDeletedFalseOrIsDeletedIsNull("1", "ENG",
						localDateTimeUTCFormat)).thenThrow(DataAccessLayerException.class);
		mockMvc.perform(get("/registrationcentershistory/1/ENG/".concat(UTC_DATE_TIME_FORMAT_DATE_STRING))
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isInternalServerError()).andReturn();
	}

	@Test
	@WithUserDetails("individual")
	public void getRegistrationCenterByHierarchylevelAndTextAndLanguageCodeTest() throws Exception {
		centers.add(center);
		when(registrationCenterRepository.findRegistrationCenterByListOfLocationCode(Mockito.anySet(),
				Mockito.anyString())).thenReturn(registrationCenters);
		when(locationRepository.getAllLocationsByLangCodeAndLevel(Mockito.anyString(), Mockito.anyShort()))
				.thenReturn(locationHierarchies);
		mockMvc.perform(get("/registrationcenters/ENG/2/PATANA").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.response.registrationCenters[0].name", is("bangalore")));
	}

	@Test
	@WithUserDetails("individual")
	public void getSpecificRegistrationCenterHierarchyLevelFetchExceptionTest() throws Exception {
		Set<String> codes = new HashSet<String>();
		codes.add("global-admin");
		when(registrationCenterRepository.findRegistrationCenterByListOfLocationCode(Mockito.anySet(),
				Mockito.anyString())).thenThrow(DataAccessLayerException.class);
		when(locationRepository.getAllLocationsByLangCodeAndLevel(Mockito.anyString(), Mockito.anyShort()))
				.thenReturn(locationHierarchies);
		mockMvc.perform(get("/registrationcenters/ENG/2/PATANA").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError());

	}

	@Test
	@WithUserDetails("individual")
	public void getRegistrationCenterHierarchyLevelNotFoundExceptionTest() throws Exception {

		List<RegistrationCenter> emptyList = new ArrayList<>();
		when(locationRepository.getAllLocationsByLangCodeAndLevel(Mockito.anyString(), Mockito.anyShort()))
				.thenReturn(locationHierarchies);
		when(registrationCenterRepository.findRegistrationCenterByListOfLocationCode(Mockito.anySet(),
				Mockito.anyString())).thenReturn(emptyList);

		mockMvc.perform(get("/registrationcenters/ENG/2/PATANA").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("individual")
	public void getRegistrationCenterHierarchyLevelNotFoundExceptionTest2() throws Exception {

		List<Location> emptyList = new ArrayList<>();
		when(locationRepository.getAllLocationsByLangCodeAndLevel(Mockito.anyString(), Mockito.anyShort()))
				.thenReturn(emptyList);

		mockMvc.perform(get("/registrationcenters/ENG/2/PATANA").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("individual")
	public void getRegistrationCenterByHierarchylevelAndListTextAndLanguageCodeTest() throws Exception {
		when(locationRepository.getAllLocationsByLangCodeAndLevel(Mockito.anyString(), Mockito.anyShort()))
				.thenReturn(locationHierarchies);
		when(registrationCenterRepository.findRegistrationCenterByListOfLocationCode(Mockito.anySet(),
				Mockito.anyString())).thenReturn(registrationCenters);
		mockMvc.perform(get("/registrationcenters/ENG/2/names").param("name", "PATANA")
				.param("name", "Bangalore Central").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andReturn();

	}

	@Test
	@WithUserDetails("individual")
	public void getRegistrationCenterByHierarchylevelAndListTextAndLanguageCodeFetchExceptionTest() throws Exception {
		when(locationRepository.getAllLocationsByLangCodeAndLevel(Mockito.anyString(), Mockito.anyShort()))
				.thenReturn(locationHierarchies);
		when(registrationCenterRepository.findRegistrationCenterByListOfLocationCode(Mockito.anySet(),
				Mockito.anyString())).thenThrow(DataAccessLayerException.class);

		mockMvc.perform(get("/registrationcenters/ENG/2/names").param("name", "PATANA")
				.param("name", "Bangalore Central").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError());

	}

	@Test
	@WithUserDetails("individual")
	public void getRegistrationCenterByHierarchylevelAndListTextAndLanguageCodeNotFoundExceptionTest()
			throws Exception {

		List<RegistrationCenter> emptyList = new ArrayList<>();
		when(locationRepository.getAllLocationsByLangCodeAndLevel(Mockito.anyString(), Mockito.anyShort()))
				.thenReturn(locationHierarchies);
		when(registrationCenterRepository.findRegistrationCenterByListOfLocationCode(Mockito.anySet(),
				Mockito.anyString())).thenReturn(emptyList);
		mockMvc.perform(get("/registrationcenters/ENG/2/names").param("name", "bangalore").param("name", "BAXOR")
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("individual")
	public void getRegistrationCenterByHierarchylevelAndListTextAndLanguageCodeNotFoundExceptionTest2()
			throws Exception {

		List<RegistrationCenter> emptyList = new ArrayList<>();
		when(locationRepository.getAllLocationsByLangCodeAndLevel(Mockito.anyString(), Mockito.anyShort()))
				.thenReturn(locationHierarchies);
		when(registrationCenterRepository.findRegistrationCenterByListOfLocationCode(Mockito.anySet(),
				Mockito.anyString())).thenReturn(emptyList);
		mockMvc.perform(get("/registrationcenters/ENG/2/names").param("name", "PATANA").param("name", "BAXOR")
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

	}

	// -----------------------------RegistrationCenterIntegrationTest----------------------------------

	@Test
	@WithUserDetails("global-admin")
	public void getSpecificRegistrationCenterByIdAndLangCodeNotFoundExceptionTest() throws Exception {
		when(registrationCenterRepository.findByIdAndLangCode("1", "ENG")).thenReturn(null);

		mockMvc.perform(get("/registrationcenters/1/ENG").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void getSpecificRegistrationCenterByIdAndLangCodeFetchExceptionTest() throws Exception {

		when(registrationCenterRepository.findByIdAndLangCode("1", "ENG")).thenThrow(DataAccessLayerException.class);

		mockMvc.perform(get("/registrationcenters/1/ENG").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError());

	}

	@Test
	@WithUserDetails("individual")
	public void getCoordinateSpecificRegistrationCentersRegistrationCenterNotFoundExceptionTest() throws Exception {
		when(registrationCenterRepository.findRegistrationCentersByLat(12.9180022, 77.5028892, 0.999785939, "ENG"))
				.thenReturn(new ArrayList<>());
		mockMvc.perform(get("/getcoordinatespecificregistrationcenters/ENG/77.5028892/12.9180022/1609")
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
	}

	@Test
	@WithUserDetails("individual")
	public void getCoordinateSpecificRegistrationCentersRegistrationCenterFetchExceptionTest() throws Exception {
		when(registrationCenterRepository.findRegistrationCentersByLat(12.9180022, 77.5028892, 0.999785939, "ENG"))
				.thenThrow(DataAccessLayerException.class);
		mockMvc.perform(get("/getcoordinatespecificregistrationcenters/ENG/77.5028892/12.9180022/1609")
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().is5xxServerError()).andReturn();
	}

	@Test
	@WithUserDetails("global-admin")
	public void getCoordinateSpecificRegistrationCentersNumberFormatExceptionTest() throws Exception {
		mockMvc.perform(get("/getcoordinatespecificregistrationcenters/ENG/77.5028892/12.9180022/ae")
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isInternalServerError()).andReturn();
	}

	@Test
	@WithUserDetails("global-admin")
	public void getSpecificRegistrationCenterByLocationCodeAndLangCodeNotFoundExceptionTest() throws Exception {
		when(registrationCenterRepository.findByLocationCodeAndLangCode("ENG", "BLR")).thenReturn(null);

		mockMvc.perform(get("/getlocspecificregistrationcenters/ENG/BLR").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void getSpecificRegistrationCenterByLocationCodeAndLangCodeFetchExceptionTest() throws Exception {

		when(registrationCenterRepository.findByLocationCodeAndLangCode("BLR", "ENG"))
				.thenThrow(DataAccessLayerException.class);

		mockMvc.perform(get("/getlocspecificregistrationcenters/ENG/BLR").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError());

	}

	@Test
	@WithUserDetails("zonal-admin")
	public void getAllRegistrationCentersNotFoundExceptionTest() throws Exception {
		when(registrationCenterRepository.findAll(RegistrationCenter.class))
				.thenReturn(new ArrayList<RegistrationCenter>());

		mockMvc.perform(get("/registrationcenters").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("zonal-admin")
	public void getAllRegistrationCentersFetchExceptionTest() throws Exception {
		when(registrationCenterRepository.findAllByIsDeletedFalseOrIsDeletedIsNull())
				.thenThrow(DataAccessLayerException.class);

		mockMvc.perform(get("/registrationcenters").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("global-admin")
	public void getSpecificRegistrationCenterByIdTestSuccessTest() throws Exception {
		when(registrationCenterRepository.findByIdAndLangCode("1", "ENG")).thenReturn(banglore);
		mockMvc.perform(get("/registrationcenters/1/ENG").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.response.registrationCenters[0].name", is("bangalore")));
	}

	@Test
	@WithUserDetails("individual")
	public void getCoordinateSpecificRegistrationCentersTest() throws Exception {
		when(registrationCenterRepository.findRegistrationCentersByLat(12.9180022, 77.5028892, 0.999785939, "ENG"))
				.thenReturn(registrationCenters);

		mockMvc.perform(get("/getcoordinatespecificregistrationcenters/ENG/77.5028892/12.9180022/1609")
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.response.registrationCenters[0].name", is("bangalore")));
	}

	@Test
	@WithUserDetails("global-admin")
	public void getLocationSpecificRegistrationCentersTest() throws Exception {
		when(registrationCenterRepository.findByLocationCodeAndLangCode("BLR", "ENG")).thenReturn(registrationCenters);
		mockMvc.perform(get("/getlocspecificregistrationcenters/ENG/BLR").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.response.registrationCenters[0].name", is("bangalore")));
	}

	@Test
	@WithUserDetails("global-admin")
	public void getLocationSpecificMultipleRegistrationCentersTest() throws Exception {
		when(registrationCenterRepository.findByLocationCodeAndLangCode("BLR", "ENG")).thenReturn(registrationCenters);

		mockMvc.perform(get("/getlocspecificregistrationcenters/ENG/BLR").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.response.registrationCenters[0].name", is("bangalore")));
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void getAllRegistrationCenterTest() throws Exception {
		when(registrationCenterRepository.findAllByIsDeletedFalseOrIsDeletedIsNull()).thenReturn(registrationCenters);

		mockMvc.perform(get("/registrationcenters").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.response.registrationCenters[0].name", is("bangalore")));
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void getAllExistingRegistrationCenterTest() throws Exception {
		Page<RegistrationCenter> page = new PageImpl<>(registrationCenters);
		when(registrationCenterRepository
				.findAll(PageRequest.of(0, 10, Sort.by(Direction.fromString("desc"), "createdDateTime"))))
						.thenReturn(page);
		mockMvc.perform(get("/registrationcenters/all")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void getAllExistingRegistrationCenterNotFoundExceptionTest() throws Exception {
		when(registrationCenterRepository
				.findAll(PageRequest.of(0, 10, Sort.by(Direction.fromString("desc"), "createdDateTime"))))
						.thenReturn(null);
		mockMvc.perform(get("/registrationcenters/all")).andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("zonal-admin")
	public void getAllExistingRegistrationCentersFetchExceptionTest() throws Exception {
		when(registrationCenterRepository
				.findAll(PageRequest.of(0, 10, Sort.by(Direction.fromString("desc"), "createdDateTime"))))
						.thenThrow(new DataAccessLayerException("errorCode", "errorMessage", new RuntimeException()));
		mockMvc.perform(get("/registrationcenters/all")).andExpect(status().isInternalServerError());
	}
	// -----------------------------RegistrationCenterIntegrationTest----------------------------------

	@Test
	@WithUserDetails("reg-processor")
	public void getRegistrationCentersMachineUserMappingNotFoundExceptionTest() throws Exception {
		when(machineHistoryRepository
				.findByCntrIdAndMachineIdAndEffectivetimesLessThanEqualAndIsDeletedFalseOrIsDeletedIsNull("1",
						 "1", localDateTimeUTCFormat)).thenReturn(new ArrayList<MachineHistory>());
		when(userDetailsRepository
				.findByCntrIdAndUsrIdAndEffectivetimesLessThanEqualAndIsDeletedFalseOrIsDeletedIsNull("1",
						 "1", localDateTimeUTCFormat)).thenReturn(new ArrayList<UserDetailsHistory>());
		mockMvc.perform(get(
				"/getregistrationmachineusermappinghistory/".concat(UTC_DATE_TIME_FORMAT_DATE_STRING).concat("/1/1/1"))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();
	}

	@Test
	@WithUserDetails("reg-processor")
	public void getRegistrationCentersMachineUserMappingFetchExceptionTest() throws Exception {
		
		when(machineHistoryRepository
				.findByCntrIdAndMachineIdAndEffectivetimesLessThanEqualAndIsDeletedFalseOrIsDeletedIsNull("1",
						 "1", localDateTimeUTCFormat)).thenThrow(DataAccessLayerException.class);
		mockMvc.perform(get(
				"/getregistrationmachineusermappinghistory/".concat(UTC_DATE_TIME_FORMAT_DATE_STRING).concat("/1/1/1"))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError()).andReturn();
	}

	@Test
	@WithUserDetails("reg-processor")
	public void getCoordinateSpecificRegistrationCentersDateTimeParseExceptionTest() throws Exception {
		mockMvc.perform(get("/getregistrationmachineusermappinghistory/2018-10-30T19:20:30.45+5:30/1/1/1")
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
	}

	// @Test
	// @WithUserDetails("reg-processor")
	public void getRegistrationCentersMachineUserMappingTest() throws Exception {
		
		MachineHistory machine=new MachineHistory();
		machine.setId("1");
		machine.setEffectDateTime(localDateTimeUTCFormat);
		machine.setIsActive(true);
		machine.setIsDeleted(false);
		machine.setRegCenterId("1");
		UserDetailsHistory user=new UserDetailsHistory();
		user.setId("1");
		user.setEffDTimes(localDateTimeUTCFormat);
		user.setIsActive(true);
		user.setIsDeleted(false);
		user.setRegCenterId("1");
		
		when(machineHistoryRepository
				.findByCntrIdAndMachineIdAndEffectivetimesLessThanEqualAndIsDeletedFalseOrIsDeletedIsNull("1",
						 "1", localDateTimeUTCFormat)).thenReturn(Arrays.asList(machine));
		when(userDetailsRepository
				.findByCntrIdAndUsrIdAndEffectivetimesLessThanEqualAndIsDeletedFalseOrIsDeletedIsNull("1",
						 "1", localDateTimeUTCFormat)).thenReturn(Arrays.asList(user));
		MvcResult result = mockMvc.perform(get("/getregistrationmachineusermappinghistory/2018-10-30T19:20:30.45/1/1/1")
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();

		RegistrationCenterUserMachineMappingHistoryResponseDto returnResponse = mapper.readValue(
				result.getResponse().getContentAsString(),
				RegistrationCenterUserMachineMappingHistoryResponseDto.class);
		assertThat(returnResponse.getRegistrationCenters().get(0).getCntrId(), is("1"));
		assertThat(returnResponse.getRegistrationCenters().get(0).getUsrId(), is("1"));
		assertThat(returnResponse.getRegistrationCenters().get(0).getMachineId(), is("1"));
	}


	// -----------------------------TitleIntegrationTest----------------------------------
	@Test
	@WithUserDetails("global-admin")
	public void getTitleByLanguageCodeNotFoundExceptionTest() throws Exception {

		titlesNull = new ArrayList<>();

		Mockito.when(titleRepository.getThroughLanguageCode("ENG")).thenReturn(titlesNull);

		mockMvc.perform(get("/title/ENG").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("id-auth")
	public void getAllTitleFetchExceptionTest() throws Exception {

		Mockito.when(titleRepository.findAll(Title.class)).thenThrow(DataAccessLayerException.class);

		mockMvc.perform(get("/title").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError());

	}

	@Test
	@WithUserDetails("id-auth")
	public void getAllTitleNotFoundExceptionTest() throws Exception {

		titlesNull = new ArrayList<>();

		Mockito.when(titleRepository.findAll(Title.class)).thenReturn(titlesNull);

		mockMvc.perform(get("/title").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("id-auth")
	public void getAllTitlesTest() throws Exception {
		Mockito.when(titleRepository.findAll(Title.class)).thenReturn(titleList);
		mockMvc.perform(get("/title")).andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void getTitleByLanguageCodeTest() throws Exception {

		Mockito.when(titleRepository.getThroughLanguageCode(Mockito.anyString())).thenReturn(titleList);
		mockMvc.perform(get("/title/{langcode}", "ENG")).andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void saveTitleTest() throws Exception {
		RequestWrapper<TitleDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.title.update");
		requestDto.setVersion("1.0");
		TitleDto titleDto = new TitleDto();
		titleDto.setCode("001");
		titleDto.setTitleDescription("mosip");
		titleDto.setIsActive(true);
		titleDto.setLangCode("eng");
		titleDto.setTitleName("mosip");
		requestDto.setRequest(titleDto);
		String contentJson = mapper.writeValueAsString(requestDto);
		when(titleRepository.create(Mockito.any())).thenReturn(title);
		when(masterdataCreationUtil.createMasterData(Title.class, titleDto)).thenReturn(titleDto);
		mockMvc.perform(post("/title").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void createTitleLangCodeValidationTest() throws Exception {
		String content = "{ \"id\": \"string\", \"request\": { \"code\": \"43\", \"isActive\": true, \"langCode\": \"dfg\", \"titleDescription\": \"string\", \"titleName\": \"string\" }, \"requesttime\": \"2018-12-17T09:10:25.829Z\", \"version\": \"string\"}";
		mockMvc.perform(post("/title").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void saveTitleExceptionTest() throws Exception {
		RequestWrapper<TitleDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.title.update");
		requestDto.setVersion("1.0");
		TitleDto titleDto = new TitleDto();
		titleDto.setCode("001");
		titleDto.setTitleDescription("mosip");
		titleDto.setIsActive(true);
		titleDto.setLangCode("eng");
		titleDto.setTitleName("mosip");
		requestDto.setRequest(titleDto);
		String contentJson = mapper.writeValueAsString(requestDto);
		when(titleRepository.create(Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot execute ", null));
		when(masterdataCreationUtil.createMasterData(Title.class, titleDto)).thenReturn(titleDto);
		mockMvc.perform(post("/title").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isInternalServerError());

	}

	@Test
	@WithUserDetails("global-admin")
	public void updateTitleTest() throws Exception {
		RequestWrapper<TitleDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.title.update");
		requestDto.setVersion("1.0");
		TitleDto titleDto = new TitleDto();
		titleDto.setCode("001");
		titleDto.setTitleDescription("mosip");
		titleDto.setIsActive(true);
		titleDto.setLangCode("eng");
		titleDto.setTitleName("mosip");
		requestDto.setRequest(titleDto);
		String contentJson = mapper.writeValueAsString(requestDto);
		when(titleRepository.findById(Mockito.any(), Mockito.any())).thenReturn(title);
		when(masterdataCreationUtil.updateMasterData(Title.class, titleDto)).thenReturn(titleDto);
		mockMvc.perform(put("/title").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void updateTitleLangCodeValidationTest() throws Exception {
		String content = "{ \"id\": \"string\", \"request\": { \"code\": \"43\", \"isActive\": true, \"langCode\": \"dfg\", \"titleDescription\": \"string\", \"titleName\": \"string\" }, \"requesttime\": \"2018-12-17T09:10:25.829Z\", \"version\": \"string\"}";
		mockMvc.perform(put("/title").contentType(MediaType.APPLICATION_JSON).content(content))

				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void updateTitleBadRequestTest() throws Exception {
		RequestWrapper<TitleDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.title.update");
		requestDto.setVersion("1.0");
		TitleDto titleDto = new TitleDto();
		titleDto.setCode("001");
		titleDto.setTitleDescription("mosip");
		titleDto.setIsActive(true);
		titleDto.setLangCode("ENG");
		titleDto.setTitleName("mosip");
		requestDto.setRequest(titleDto);
		String contentJson = mapper.writeValueAsString(requestDto);
		when(titleRepository.findById(Mockito.any(), Mockito.any())).thenReturn(null);
		mockMvc.perform(put("/title").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void updateTitleDatabaseConnectionExceptionTest() throws Exception {
		RequestWrapper<TitleDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.title.update");
		requestDto.setVersion("1.0");
		TitleDto titleDto = new TitleDto();
		titleDto.setCode("001");
		titleDto.setTitleDescription("mosip");
		titleDto.setIsActive(true);
		titleDto.setLangCode("eng");
		titleDto.setTitleName("mosip");
		requestDto.setRequest(titleDto);
		String contentJson = mapper.writeValueAsString(requestDto);
		when(titleRepository.findById(Mockito.any(), Mockito.any())).thenReturn(title);
		when(titleRepository.update(Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot execute statement", null));
		mockMvc.perform(put("/title").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteTitleTest() throws Exception {
		when(titleRepository.findByCode(Mockito.any())).thenReturn(titleList);
		when(titleRepository.update(Mockito.any())).thenReturn(title);
		mockMvc.perform(delete("/title/ABC").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteTitleBadRequestTest() throws Exception {
		when(titleRepository.getThroughLanguageCode(Mockito.any())).thenReturn(null);
		mockMvc.perform(delete("/title/ABC").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteTitleDatabaseConnectionExceptionTest() throws Exception {
		when(titleRepository.findByCode(Mockito.any())).thenReturn(titleList);
		when(titleRepository.update(Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot execute statement", null));
		mockMvc.perform(delete("/title/ABC").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError());

	}
	// -----------------------------------gender-type----------------------------------------

	@Test
	@WithUserDetails("global-admin")
	public void addGenderTypeTest() throws Exception {
		RequestWrapper<GenderTypeDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.language.create");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(genderDto);
		String content = mapper.writeValueAsString(requestDto);
		when(genderTypeRepository.create(Mockito.any())).thenReturn(genderType);
		when(masterdataCreationUtil.createMasterData(Gender.class, genderDto)).thenReturn(genderDto);
		mockMvc.perform(post("/gendertypes").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void addGenderTypeLandCodeValidationTest() throws Exception {
		RequestWrapper<GenderTypeDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.language.create");
		requestDto.setVersion("1.0.0");
		genderDto.setLangCode("akk");
		requestDto.setRequest(genderDto);
		String content = mapper.writeValueAsString(requestDto);
		when(masterdataCreationUtil.createMasterData(Gender.class, genderDto)).thenReturn(genderDto);
		mockMvc.perform(post("/gendertypes").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void addGenderTypeExceptionTest() throws Exception {

		RequestWrapper<GenderTypeDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.language.create");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(genderDto);
		String content = mapper.writeValueAsString(requestDto);
		when(genderTypeRepository.create(Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot execute ", null));
		when(masterdataCreationUtil.createMasterData(Gender.class, genderDto)).thenReturn(genderDto);
		mockMvc.perform(post("/gendertypes").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().isInternalServerError());

	}

	@Test
	@WithUserDetails("global-admin")
	public void updateGenderTypeTest() throws Exception {
		RequestWrapper<GenderTypeDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		GenderTypeDto genderTypeDto = new GenderTypeDto("GEN01", "Male", "eng", true);
		requestDto.setRequest(genderTypeDto);
		String contentJson = mapper.writeValueAsString(requestDto);
		when(genderTypeRepository.updateGenderType(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(1);
		when(masterdataCreationUtil.updateMasterData(Gender.class, genderTypeDto)).thenReturn(genderTypeDto);
		mockMvc.perform(put("/gendertypes").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void updateGenderTypeNotFoundExceptionTest() throws Exception {
		RequestWrapper<GenderTypeDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		GenderTypeDto genderTypeDto = new GenderTypeDto("GEN01", "Male", "ENG", true);
		requestDto.setRequest(genderTypeDto);
		String contentJson = mapper.writeValueAsString(requestDto);
		when(genderTypeRepository.updateGenderType(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(0);
		when(masterdataCreationUtil.updateMasterData(Gender.class, genderTypeDto)).thenReturn(genderTypeDto);
		mockMvc.perform(put("/gendertypes").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void updateGenderTypeDatabaseConnectionExceptionTest() throws Exception {
		RequestWrapper<GenderTypeDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		GenderTypeDto genderTypeDto = new GenderTypeDto("GEN01", "Male", "eng", true);
		requestDto.setRequest(genderTypeDto);
		String contentJson = mapper.writeValueAsString(requestDto);
		when(genderTypeRepository.updateGenderType(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any()))
						.thenThrow(new DataAccessLayerException("", "cannot execute statement", null));
		when(masterdataCreationUtil.updateMasterData(Gender.class, genderTypeDto)).thenReturn(genderTypeDto);
		mockMvc.perform(put("/gendertypes").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteGenderTypeTest() throws Exception {
		when(genderTypeRepository.deleteGenderType(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(1);
		mockMvc.perform(delete("/gendertypes/GEN01").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteGenderTypeNotFoundExceptionTest() throws Exception {
		when(genderTypeRepository.deleteGenderType(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(0);
		mockMvc.perform(delete("/gendertypes/GEN01").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteGenderTypeDatabaseConnectionExceptionTest() throws Exception {

		when(genderTypeRepository.deleteGenderType(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot execute statement", null));
		mockMvc.perform(delete("/gendertypes/GEN01").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError());

	}

	// ----------------------------------BiometricAttributeCreateApiTest--------------------------------------------------
	@Test
	@WithUserDetails("global-admin")
	public void createBiometricAttributeTest() throws Exception {

		RequestWrapper<BiometricAttributeDto> requestDto = new RequestWrapper<BiometricAttributeDto>();
		requestDto.setId("mosip.language.create");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(biometricAttributeDto);
		String content = mapper.writeValueAsString(requestDto);
		when(masterdataCreationUtil.createMasterData(BiometricAttribute.class, biometricAttributeDto))
				.thenReturn(biometricAttributeDto);

		Mockito.when(biometricAttributeRepository.create(Mockito.any())).thenReturn(biometricAttribute);
		mockMvc.perform(post("/biometricattributes").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void createBiometricAttributeExceptionTest() throws Exception {
		RequestWrapper<BiometricAttributeDto> requestDto = new RequestWrapper<BiometricAttributeDto>();
		requestDto.setId("mosip.language.create");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(biometricAttributeDto);
		String content = mapper.writeValueAsString(requestDto);
		when(biometricAttributeRepository.create(Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot insert", null));
		when(masterdataCreationUtil.createMasterData(BiometricAttribute.class, biometricAttributeDto))
				.thenReturn(biometricAttributeDto);
		mockMvc.perform(post("/biometricattributes").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("global-admin")
	public void createBiometricAttributeLangCodeValidationTest() throws Exception {
		RequestWrapper<BiometricAttributeDto> requestDto = new RequestWrapper<BiometricAttributeDto>();
		requestDto.setId("mosip.language.create");
		requestDto.setVersion("1.0.0");
		biometricAttributeDto.setLangCode("akk");
		requestDto.setRequest(biometricAttributeDto);
		String content = mapper.writeValueAsString(requestDto);
		when(masterdataCreationUtil.createMasterData(BiometricAttribute.class, biometricAttributeDto))
				.thenReturn(biometricAttributeDto);
		mockMvc.perform(post("/biometricattributes").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().isOk());
	}

	// ----------------------------------TemplateCreateApiTest--------------------------------------------------
	@Test
	@WithUserDetails("global-admin")
	public void createTemplateTest() throws Exception {
		RequestWrapper<TemplateDto> requestDto = new RequestWrapper<TemplateDto>();
		requestDto.setId("mosip.language.create");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(templateDto);
		String content = mapper.writeValueAsString(requestDto);
		Mockito.when(templateRepository
				.findTemplateByIDAndLangCode(Mockito.any(),Mockito.any())).thenReturn(null);
		Mockito.when(templateRepository.create(Mockito.any())).thenReturn(template);
		when(masterdataCreationUtil.createMasterData(Mockito.any(),Mockito.any())).thenReturn(templateDto);
		mockMvc.perform(post("/templates").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void createTemplateLangCodeValidationTest() throws Exception {
		RequestWrapper<TemplateDto> requestDto = new RequestWrapper<TemplateDto>();
		requestDto.setId("mosip.language.create");
		requestDto.setVersion("1.0.0");
		templateDto.setLangCode("akk");
		requestDto.setRequest(templateDto);
		String content = mapper.writeValueAsString(requestDto);
		Mockito.when(templateRepository
				.findTemplateByIDAndLangCode(Mockito.any(),Mockito.any())).thenReturn(null);
		when(masterdataCreationUtil.createMasterData(Mockito.any(),Mockito.any())).thenReturn(templateDto);
		mockMvc.perform(post("/templates").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void createTemplateExceptionTest() throws Exception {
		RequestWrapper<TemplateDto> requestDto = new RequestWrapper<TemplateDto>();
		requestDto.setId("mosip.language.create");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(templateDto);
		String content = mapper.writeValueAsString(requestDto);
		Mockito.when(templateRepository
				.findTemplateByIDAndLangCode(Mockito.any(),Mockito.any())).thenReturn(null);
		when(templateRepository.create(Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot insert", null));
		when(masterdataCreationUtil.createMasterData(Mockito.any(),Mockito.any())).thenReturn(templateDto);
		mockMvc.perform(post("/templates").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().isInternalServerError());
	}

	// ----------------------------------TemplateTypeCreateApiTest--------------------------------------------------
	@Test
	@WithUserDetails("global-admin")
	public void createTemplateTypeTest() throws Exception {
		RequestWrapper<TemplateTypeDto> requestDto = new RequestWrapper<TemplateTypeDto>();
		requestDto.setId("mosip.language.create");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(templateTypeDto);
		String content = mapper.writeValueAsString(requestDto);
		Mockito.when(templateTypeRepository.create(Mockito.any())).thenReturn(templateType);
		when(masterdataCreationUtil.createMasterData(TemplateType.class, templateTypeDto)).thenReturn(templateTypeDto);
		mockMvc.perform(post("/templatetypes").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void createTemplatetypeExceptionTest() throws Exception {
		RequestWrapper<TemplateTypeDto> requestDto = new RequestWrapper<TemplateTypeDto>();
		requestDto.setId("mosip.language.create");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(templateTypeDto);
		String content = mapper.writeValueAsString(requestDto);
		when(templateTypeRepository.create(Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot insert", null));
		when(masterdataCreationUtil.createMasterData(TemplateType.class, templateTypeDto)).thenReturn(templateTypeDto);
		mockMvc.perform(post("/templatetypes").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("global-admin")
	public void createTemplatetypeLangValidationExceptionTest() throws Exception {
		RequestWrapper<TemplateTypeDto> requestDto = new RequestWrapper<TemplateTypeDto>();
		requestDto.setId("mosip.language.create");
		requestDto.setVersion("1.0.0");
		templateTypeDto.setLangCode("akk");
		requestDto.setRequest(templateTypeDto);
		String content = mapper.writeValueAsString(requestDto);
		when(masterdataCreationUtil.createMasterData(TemplateType.class, templateTypeDto)).thenReturn(templateTypeDto);
		mockMvc.perform(post("/templatetypes").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().isOk());
	}

	// -----------------------------------DeviceSpecificationTest---------------------------------
	@Test
	@WithUserDetails("zonal-admin")
	public void findDeviceSpecLangcodeSuccessTest() throws Exception {
		when(deviceSpecificationRepository.findByLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()))
				.thenReturn(deviceSpecList);
		mockMvc.perform(get("/devicespecifications/{langcode}", "ENG")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void findDeviceSpecLangcodeNullResponseTest() throws Exception {
		when(deviceSpecificationRepository.findByLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()))
				.thenReturn(null);
		mockMvc.perform(get("/devicespecifications/{langcode}", "ENG")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void findDeviceSpecLangcodeFetchExceptionTest() throws Exception {
		when(deviceSpecificationRepository.findByLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()))
				.thenThrow(DataRetrievalFailureException.class);
		mockMvc.perform(get("/devicespecifications/{langcode}", "ENG")).andExpect(status().isInternalServerError());
	}

	// --------------------------------------------
	@Test
	@WithUserDetails("global-admin")
	public void findDeviceSpecByLangCodeAndDevTypeCodeSuccessTest() throws Exception {
		when(deviceSpecificationRepository.findByLangCodeAndDeviceTypeCodeAndIsDeletedFalseOrIsDeletedIsNull(
				Mockito.anyString(), Mockito.anyString())).thenReturn(deviceSpecList);
		mockMvc.perform(get("/devicespecifications/{langcode}/{devicetypecode}", "ENG", "laptop"))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void findDeviceSpecByLangCodeAndDevTypeCodeNullResponseTest() throws Exception {
		when(deviceSpecificationRepository.findByLangCodeAndDeviceTypeCodeAndIsDeletedFalseOrIsDeletedIsNull(
				Mockito.anyString(), Mockito.anyString())).thenReturn(null);
		mockMvc.perform(get("/devicespecifications/{langcode}/{devicetypecode}", "ENG", "laptop"))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void findDeviceSpecByLangCodeAndDevTypeCodeFetchExceptionTest() throws Exception {
		when(deviceSpecificationRepository.findByLangCodeAndDeviceTypeCodeAndIsDeletedFalseOrIsDeletedIsNull(
				Mockito.anyString(), Mockito.anyString())).thenThrow(DataRetrievalFailureException.class);
		mockMvc.perform(get("/devicespecifications/{langcode}/{devicetypecode}", "ENG", "laptop"))
				.andExpect(status().isInternalServerError());
	}
	// ----------------------------------------------------------------

	@Test
	@WithUserDetails("global-admin")
	public void createDeviceSpecificationTest() throws Exception {
		RequestWrapper<DeviceSpecificationDto> requestDto;
		requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.match.regcentr.DeviceSpecificationcode");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(deviceSpecificationDto);

		String deviceSpecificationJson = mapper.writeValueAsString(requestDto);
		when(deviceTypeRepository.findDeviceTypeByCodeAndByLangCode(Mockito.any(), Mockito.any()))
				.thenReturn(deviceType);
		when(deviceSpecificationRepository
				.findDeviceSpecificationByIDAndLangCode(Mockito.any(),Mockito.any())).thenReturn(null);
		when(masterdataCreationUtil.createMasterData(Mockito.any(), Mockito.any()))
				.thenReturn(deviceSpecificationDto);
		when(deviceSpecificationRepository.create(Mockito.any())).thenReturn(deviceSpecification);
		mockMvc.perform(
				post("/devicespecifications").contentType(MediaType.APPLICATION_JSON).content(deviceSpecificationJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void createDeviceSpecificationLangCodeValidationTest() throws Exception {
		RequestWrapper<DeviceSpecificationDto> requestDto;
		requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.match.regcentr.DeviceSpecificationcode");
		requestDto.setVersion("1.0.0");
		deviceSpecificationDto.setLangCode("akk");
		requestDto.setRequest(deviceSpecificationDto);
		String deviceSpecificationJson = mapper.writeValueAsString(requestDto);
		mockMvc.perform(
				post("/devicespecifications").contentType(MediaType.APPLICATION_JSON).content(deviceSpecificationJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void createDeviceSpecificationExceptionTest() throws Exception {
		RequestWrapper<DeviceSpecificationDto> requestDto;
		requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.match.regcentr.DeviceSpecificationcode");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(deviceSpecificationDto);
		when(deviceTypeRepository.findDeviceTypeByCodeAndByLangCode(Mockito.any(), Mockito.any()))
				.thenReturn(deviceType);
		String DeviceSpecificationJson = mapper.writeValueAsString(requestDto);
		when(deviceTypeRepository.findDeviceTypeByCodeAndByLangCode(Mockito.any(), Mockito.any()))
				.thenReturn(deviceType);
		when(deviceSpecificationRepository
				.findDeviceSpecificationByIDAndLangCode(Mockito.any(),Mockito.any())).thenReturn(null);
		
		Mockito.when(deviceSpecificationRepository.create(Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot insert", null));
		mockMvc.perform(MockMvcRequestBuilders.post("/devicespecifications").contentType(MediaType.APPLICATION_JSON)
				.content(DeviceSpecificationJson)).andExpect(status().isInternalServerError());
	}

	// ---------------------------------DeviceTypeTest------------------------------------------------

	@Test
	@WithUserDetails("global-admin")
	public void createDeviceTypeTest() throws Exception {

		RequestWrapper<DeviceTypeDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.match.regcentr.Devicetypecode");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(deviceTypeDto);

		String DeviceTypeJson = mapper.writeValueAsString(requestDto);
		when(masterdataCreationUtil.createMasterData(DeviceType.class, deviceTypeDto)).thenReturn(deviceTypeDto);
		when(deviceTypeRepository.create(Mockito.any())).thenReturn(deviceType);
		mockMvc.perform(post("/devicetypes").contentType(MediaType.APPLICATION_JSON).content(DeviceTypeJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void createDeviceTypeExceptionTest() throws Exception {

		RequestWrapper<DeviceTypeDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.match.regcentr.Devicetypecode");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(deviceTypeDto);

		String DeviceTypeJson = mapper.writeValueAsString(requestDto);
		when(masterdataCreationUtil.createMasterData(DeviceType.class, deviceTypeDto)).thenReturn(deviceTypeDto);
		Mockito.when(deviceTypeRepository.create(Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot insert", null));
		mockMvc.perform(MockMvcRequestBuilders.post("/devicetypes").contentType(MediaType.APPLICATION_JSON)
				.content(DeviceTypeJson)).andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("global-admin")
	public void updateDeviceTypeTest() throws Exception {

		RequestWrapper<DeviceTypeDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.match.regcentr.Devicetypecode");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(deviceTypeDto);

		String DeviceTypeJson = mapper.writeValueAsString(requestDto);
		when(masterdataCreationUtil.updateMasterData(DeviceType.class, deviceTypeDto)).thenReturn(deviceTypeDto);
		when(deviceTypeRepository.update(Mockito.any())).thenReturn(deviceType);
		when(deviceTypeRepository.findDeviceTypeByCodeAndByLangCode(Mockito.any(), Mockito.any()))
				.thenReturn(deviceType);
		mockMvc.perform(put("/devicetypes").contentType(MediaType.APPLICATION_JSON).content(DeviceTypeJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void updateDeviceTypeExceptionTest() throws Exception {

		RequestWrapper<DeviceTypeDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.match.regcentr.Devicetypecode");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(deviceTypeDto);

		String DeviceTypeJson = mapper.writeValueAsString(requestDto);
		when(masterdataCreationUtil.updateMasterData(DeviceType.class, deviceTypeDto)).thenReturn(deviceTypeDto);
		when(deviceTypeRepository.findDeviceTypeByCodeAndByLangCode(Mockito.any(), Mockito.any()))
				.thenReturn(deviceType);
		Mockito.when(deviceTypeRepository.update(Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot update", null));
		mockMvc.perform(MockMvcRequestBuilders.put("/devicetypes").contentType(MediaType.APPLICATION_JSON)
				.content(DeviceTypeJson)).andExpect(status().isInternalServerError());
	}

	// -------------------------------MachineSpecificationTest-------------------------------
	@Test
	@WithUserDetails("global-admin")
	public void createMachineSpecificationTest() throws Exception {

		RequestWrapper<MachineSpecificationDto> requestDto;
		requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.match.regcentr.machineSpecificationcode");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(machineSpecificationDto);

		String machineSpecificationJson = mapper.writeValueAsString(requestDto);
		when(machineSpecificationRepository
				.findMachineSpecificationByIDAndLangCode(Mockito.any(),Mockito.any())).thenReturn(null);
		when(masterdataCreationUtil.createMasterData(Mockito.any(), Mockito.any()))
				.thenReturn(machineSpecificationDto);
		when(machineTypeRepository.findMachineTypeByCodeAndByLangCode(Mockito.any(), Mockito.any()))
				.thenReturn(machineType);
		when(machineSpecificationRepository.create(Mockito.any())).thenReturn(machineSpecification);
		mockMvc.perform(post("/machinespecifications").contentType(MediaType.APPLICATION_JSON)
				.content(machineSpecificationJson)).andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void createMachineSpecificationLanguageCodeValidatorTest() throws Exception {

		RequestWrapper<MachineSpecificationDto> requestDto;
		requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.match.regcentr.machineSpecificationcode");
		requestDto.setVersion("1.0.0");
		machineSpecificationDto.setLangCode("xxx");
		requestDto.setRequest(machineSpecificationDto);

		String machineSpecificationJson = mapper.writeValueAsString(requestDto);

		when(machineSpecificationRepository.create(Mockito.any())).thenReturn(machineSpecification);
		mockMvc.perform(post("/machinespecifications").contentType(MediaType.APPLICATION_JSON)
				.content(machineSpecificationJson)).andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void createMachineSpecificationExceptionTest() throws Exception {

		RequestWrapper<MachineSpecificationDto> requestDto;
		requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.match.regcentr.machineSpecificationcode");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(machineSpecificationDto);

		String machineSpecificationJson = mapper.writeValueAsString(requestDto);
		when(machineSpecificationRepository
				.findMachineSpecificationByIDAndLangCode(Mockito.any(),Mockito.any())).thenReturn(null);
		
		when(machineTypeRepository.findMachineTypeByCodeAndByLangCode(Mockito.any(), Mockito.any()))
				.thenReturn(machineType);
		Mockito.when(machineSpecificationRepository.create(Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot insert", null));
		mockMvc.perform(MockMvcRequestBuilders.post("/machinespecifications").contentType(MediaType.APPLICATION_JSON)
				.content(machineSpecificationJson)).andExpect(status().isInternalServerError());

	}

	@Test
	@WithUserDetails("global-admin")
	public void createMachineSpecificationLangCodeValidationTest() throws Exception {
		RequestWrapper<MachineSpecificationDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.match.regcentr.machineSpecificationcode");
		requestDto.setVersion("1.0.0");
		machineSpecificationDto.setLangCode("akk");
		requestDto.setRequest(machineSpecificationDto);
		String machineSpecificationJson = mapper.writeValueAsString(requestDto);
		mockMvc.perform(post("/machinespecifications").contentType(MediaType.APPLICATION_JSON)
				.content(machineSpecificationJson)).andExpect(status().isOk());
	}

	// -------------------------------------------------------------------------
	@Test
	@WithUserDetails("global-admin")
	public void updateMachineSpecificationTest() throws Exception {

		RequestWrapper<MachineSpecificationDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.machineSpecification.update");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(machineSpecificationDto);
		String content = mapper.writeValueAsString(requestDto);
		when(machineTypeRepository.findMachineTypeByCodeAndByLangCode(Mockito.any(), Mockito.any()))
		.thenReturn(machineType);
		when(machineSpecificationRepository.findByIdAndLangCodeIsDeletedFalseorIsDeletedIsNull(Mockito.any(),
				Mockito.any())).thenReturn(machineSpecification);
		Mockito.when(machineSpecificationRepository.update(Mockito.any())).thenReturn(machineSpecification);
		when(masterdataCreationUtil.updateMasterData(MachineSpecification.class, machineSpecificationDto))
				.thenReturn(machineSpecificationDto);
		mockMvc.perform(MockMvcRequestBuilders.put("/machinespecifications").contentType(MediaType.APPLICATION_JSON)
				.content(content)).andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void updateMachineSpecificationLanguageCodeValidatorTest() throws Exception {

		RequestWrapper<MachineSpecificationDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.machineSpecification.update");
		requestDto.setVersion("1.0.0");
		machineSpecificationDto.setLangCode("xxx");
		requestDto.setRequest(machineSpecificationDto);
		String content = mapper.writeValueAsString(requestDto);
		when(machineTypeRepository.findMachineTypeByCodeAndByLangCode(Mockito.any(), Mockito.any()))
		.thenReturn(machineType);
		when(machineSpecificationRepository.findByIdAndLangCodeIsDeletedFalseorIsDeletedIsNull(Mockito.any(),
				Mockito.any())).thenReturn(machineSpecification);
		Mockito.when(machineSpecificationRepository.update(Mockito.any())).thenReturn(machineSpecification);

		mockMvc.perform(MockMvcRequestBuilders.put("/machinespecifications").contentType(MediaType.APPLICATION_JSON)
				.content(content)).andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void updateMachineSpecificationNotFoundExceptionTest() throws Exception {

		RequestWrapper<MachineSpecificationDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.machineSpecification.update");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(machineSpecificationDto);
		String content = mapper.writeValueAsString(requestDto);
		when(machineTypeRepository.findMachineTypeByCodeAndByLangCode(Mockito.any(), Mockito.any()))
		.thenReturn(machineType);
		when(machineSpecificationRepository.findByIdAndLangCodeIsDeletedFalseorIsDeletedIsNull(Mockito.any(),
				Mockito.any())).thenReturn(null);

		mockMvc.perform(MockMvcRequestBuilders.put("/machinespecifications").contentType(MediaType.APPLICATION_JSON)
				.content(content)).andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void updateMachineSpecificationDatabaseConnectionExceptionTest() throws Exception {

		RequestWrapper<MachineSpecificationDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.machineSpecification.update");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(machineSpecificationDto);
		String content = mapper.writeValueAsString(requestDto);
		when(machineTypeRepository.findMachineTypeByCodeAndByLangCode(Mockito.any(), Mockito.any()))
		.thenReturn(machineType);
		when(machineSpecificationRepository.findByIdAndLangCodeIsDeletedFalseorIsDeletedIsNull(Mockito.any(),
				Mockito.any())).thenThrow(DataAccessLayerException.class);

		mockMvc.perform(MockMvcRequestBuilders.put("/machinespecifications").contentType(MediaType.APPLICATION_JSON)
				.content(content)).andExpect(status().isInternalServerError());

	}
	// -----------------------------------------------------------------------------------------------

	@Test
	@WithUserDetails("global-admin")
	public void deleteMachineSpecificationTest() throws Exception {
		List<Machine> emptyList = new ArrayList<>();
		when(machineSpecificationRepository.findByIdAndIsDeletedFalseorIsDeletedIsNull(Mockito.any()))
				.thenReturn(machineSpecList);
		when(machineRepository.findMachineBymachineSpecIdAndIsDeletedFalseorIsDeletedIsNull(Mockito.any()))
				.thenReturn(emptyList);
		when(machineSpecificationRepository.update(Mockito.any())).thenReturn(machineSpecification);
		mockMvc.perform(delete("/machinespecifications/1000").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteMachineSpecificationDataNotFoundExceptionTest() throws Exception {
		List<MachineSpecification> emptyList = new ArrayList<>();
		when(machineSpecificationRepository.findByIdAndIsDeletedFalseorIsDeletedIsNull(Mockito.any()))
				.thenReturn(emptyList);
		mockMvc.perform(delete("/machinespecifications/1000").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteMachineSpecificationDatabaseConnectionExceptionTest() throws Exception {
		List<Machine> emptyList = new ArrayList<>();
		when(machineSpecificationRepository.findByIdAndIsDeletedFalseorIsDeletedIsNull(Mockito.any()))
				.thenReturn(machineSpecList);
		when(machineRepository.findMachineBymachineSpecIdAndIsDeletedFalseorIsDeletedIsNull(Mockito.any()))
				.thenReturn(emptyList);
		when(machineSpecificationRepository.update(Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot execute statement", null));
		mockMvc.perform(delete("/machinespecifications/1000").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError());

	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteMachineSpecificationDependencyExceptionTest() throws Exception {
		List<Machine> machineList = new ArrayList<Machine>();
		Machine machine = new Machine();
		machineList.add(machine);
		when(machineSpecificationRepository.findByIdAndIsDeletedFalseorIsDeletedIsNull(Mockito.any()))
				.thenReturn(machineSpecList);
		when(machineRepository
				.findMachineBymachineSpecIdAndIsDeletedFalseorIsDeletedIsNull(machineSpecification.getId()))
						.thenReturn(machineList);
		mockMvc.perform(delete("/machinespecifications/MS001").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError());

	}

	// -------------------------MachineTest-----------------------------------------

	@Test
	@WithUserDetails("zonal-admin")
	public void getMachineAllSuccessTest() throws Exception {
		when(machineRepository.findAllByIsDeletedFalseOrIsDeletedIsNull()).thenReturn(machineList);
		mockMvc.perform(get("/machines")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void getMachineAllNullResponseTest() throws Exception {
		when(machineRepository.findAllByIsDeletedFalseOrIsDeletedIsNull()).thenReturn(null);
		mockMvc.perform(get("/machines")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void getMachineAllFetchExceptionTest() throws Exception {
		when(machineRepository.findAllByIsDeletedFalseOrIsDeletedIsNull())
				.thenThrow(DataRetrievalFailureException.class);
		mockMvc.perform(get("/machines")).andExpect(status().isInternalServerError());
	}

	// --------------------------------------------
	@Test
	@WithUserDetails("global-admin")
	public void getMachineIdLangcodeSuccessTest() throws Exception {
		List<Machine> machines = new ArrayList<Machine>();
		machines.add(machine);
		when(machineRepository.findAllByIdAndLangCodeAndIsDeletedFalseorIsDeletedIsNull(Mockito.anyString(),
				Mockito.anyString())).thenReturn(machines);
		mockMvc.perform(get("/machines/{id}/{langcode}", "1000", "ENG")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void getMachineIdLangcodeNullResponseTest() throws Exception {
		when(machineRepository.findAllByIdAndLangCodeAndIsDeletedFalseorIsDeletedIsNull(Mockito.anyString(),
				Mockito.anyString())).thenReturn(null);
		mockMvc.perform(get("/machines/{id}/{langcode}", "1000", "ENG")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void getMachineIdLangcodeFetchExceptionTest() throws Exception {
		when(machineRepository.findAllByIdAndLangCodeAndIsDeletedFalseorIsDeletedIsNull(Mockito.anyString(),
				Mockito.anyString())).thenThrow(DataRetrievalFailureException.class);
		mockMvc.perform(get("/machines/{id}/{langcode}", "1000", "ENG")).andExpect(status().isInternalServerError());
	}

	// -----------------------------------
	@Test
	@WithUserDetails("global-admin")
	public void getMachineLangcodeSuccessTest() throws Exception {
		when(machineRepository.findAllByLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()))
				.thenReturn(machineList);
		mockMvc.perform(get("/machines/{langcode}", "ENG")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void getMachineLangcodeNullResponseTest() throws Exception {
		when(machineRepository.findAllByLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()))
				.thenReturn(null);
		mockMvc.perform(get("/machines/{langcode}", "ENG")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void getMachineLangcodeFetchExceptionTest() throws Exception {
		when(machineRepository.findAllByLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()))
				.thenThrow(DataRetrievalFailureException.class);
		mockMvc.perform(get("/machines/{langcode}", "ENG")).andExpect(status().isInternalServerError());
	}

	// ---------------------------------------------------------------------------------------
	@Test
	@WithUserDetails("global-admin")
	public void deleteMachineTest() throws Exception {
		when(machineRepository.findMachineByIdAndIsDeletedFalseorIsDeletedIsNull(Mockito.any()))
				.thenReturn(machineList);
		
		when(machineRepository.update(Mockito.any())).thenReturn(machine);
		when(machineHistoryRepository.create(Mockito.any())).thenReturn(machineHistory);
		mockMvc.perform(delete("/machines/1000").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
	}

	

	@Test
	@WithUserDetails("global-admin")
	public void deleteMachineNotFoundExceptionTest() throws Exception {
		List<Machine> emptList = new ArrayList<>();
		when(machineRepository.findMachineByIdAndIsDeletedFalseorIsDeletedIsNull(Mockito.any())).thenReturn(emptList);

		mockMvc.perform(delete("/machines/1000").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteMachineDatabaseConnectionExceptionTest() throws Exception {
		when(machineRepository.findMachineByIdAndIsDeletedFalseorIsDeletedIsNull(Mockito.any()))
				.thenReturn(machineList);
		when(machineRepository.update(Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot execute statement", null));
		mockMvc.perform(delete("/machines/1000").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError());

	}
	// -----------------------------MachineTypeTest-------------------------------------------

	@Test
	@WithUserDetails("global-admin")
	public void createMachineTypeTest() throws Exception {
		RequestWrapper<MachineTypeDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.match.regcentr.machinetypecode");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(machineTypeDto);

		String machineTypeJson = mapper.writeValueAsString(requestDto);
		when(machineTypeRepository.create(Mockito.any())).thenReturn(machineType);
		when(masterdataCreationUtil.createMasterData(MachineType.class, machineTypeDto)).thenReturn(machineTypeDto);
		mockMvc.perform(post("/machinetypes").contentType(MediaType.APPLICATION_JSON).content(machineTypeJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void createMachineTypeLangCodeValidationTest() throws Exception {
		RequestWrapper<MachineTypeDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.match.regcentr.machinetypecode");
		requestDto.setVersion("1.0.0");
		machineTypeDto.setLangCode("akk");
		requestDto.setRequest(machineTypeDto);

		String machineTypeJson = mapper.writeValueAsString(requestDto);
		when(masterdataCreationUtil.createMasterData(MachineType.class, machineTypeDto)).thenReturn(machineTypeDto);
		mockMvc.perform(post("/machinetypes").contentType(MediaType.APPLICATION_JSON).content(machineTypeJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void createMachineTypeExceptionTest() throws Exception {
		RequestWrapper<MachineTypeDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.match.regcentr.machinetypecode");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(machineTypeDto);

		String machineTypeJson = mapper.writeValueAsString(requestDto);

		Mockito.when(machineTypeRepository.create(Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot insert", null));
		when(masterdataCreationUtil.createMasterData(MachineType.class, machineTypeDto)).thenReturn(machineTypeDto);
		mockMvc.perform(MockMvcRequestBuilders.post("/machinetypes").contentType(MediaType.APPLICATION_JSON)
				.content(machineTypeJson)).andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("global-admin")
	public void updateMachineTypeTest() throws Exception {
		RequestWrapper<MachineTypeDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.match.regcentr.machinetypecode");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(machineTypeDto);

		String machineTypeJson = mapper.writeValueAsString(requestDto);
		when(machineTypeRepository.findtoUpdateMachineTypeByCodeAndByLangCode(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(machineType);
		when(machineTypeRepository.update(Mockito.any())).thenReturn(machineType);
		when(masterdataCreationUtil.updateMasterData(MachineType.class, machineTypeDto)).thenReturn(machineTypeDto);
		mockMvc.perform(put("/machinetypes").contentType(MediaType.APPLICATION_JSON).content(machineTypeJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void updateMachineTypeExceptionTest() throws Exception {
		RequestWrapper<MachineTypeDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.match.regcentr.machinetypecode");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(machineTypeDto);

		String machineTypeJson = mapper.writeValueAsString(requestDto);
		when(machineTypeRepository.findtoUpdateMachineTypeByCodeAndByLangCode(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(machineType);
		Mockito.when(machineTypeRepository.update(Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot update", null));
		when(masterdataCreationUtil.updateMasterData(MachineType.class, machineTypeDto)).thenReturn(machineTypeDto);
		mockMvc.perform(MockMvcRequestBuilders.put("/machinetypes").contentType(MediaType.APPLICATION_JSON)
				.content(machineTypeJson)).andExpect(status().isInternalServerError());
	}
	// --------------------------------DeviceTest-------------------------------------------------
	@Test
	@WithUserDetails("zonal-admin")
	public void getDeviceLangcodeSuccessTest() throws Exception {
		when(deviceRepository.findByLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()))
				.thenReturn(deviceList);
		mockMvc.perform(get("/devices/{langcode}", "ENG")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void getDeviceLangcodeNullResponseTest() throws Exception {
		when(deviceRepository.findByLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString())).thenReturn(null);
		mockMvc.perform(get("/devices/{langcode}", "ENG")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void getDeviceLangcodeFetchExceptionTest() throws Exception {
		when(deviceRepository.findByLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()))
				.thenThrow(DataRetrievalFailureException.class);
		mockMvc.perform(get("/devices/{langcode}", "ENG")).andExpect(status().isInternalServerError());
	}

	// ----------------------------
	@Test
	@WithUserDetails("global-admin")
	public void getDeviceLangCodeAndDeviceTypeSuccessTest() throws Exception {
		when(deviceRepository.findByLangCodeAndDtypeCode(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(objectList);
		mockMvc.perform(get("/devices/{languagecode}/{deviceType}", "ENG", "LaptopCode")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void getDeviceLangCodeAndDeviceTypeNullResponseTest() throws Exception {
		when(deviceRepository.findByLangCodeAndDtypeCode(Mockito.anyString(), Mockito.anyString())).thenReturn(null);
		mockMvc.perform(get("/devices/{languagecode}/{deviceType}", "ENG", "LaptopCode")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void getDeviceLangCodeAndDeviceTypeFetchExceptionTest() throws Exception {
		when(deviceRepository.findByLangCodeAndDtypeCode(Mockito.anyString(), Mockito.anyString()))
				.thenThrow(DataRetrievalFailureException.class);
		mockMvc.perform(get("/devices/{languagecode}/{deviceType}", "ENG", "LaptopCode"))
				.andExpect(status().isInternalServerError());
	}

	// ---------------------------------------------

	@Test
	@WithUserDetails("zonal-admin")
	public void createDeviceSuccessTest() throws Exception {
		RequestWrapper<DeviceDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.device.create");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(deviceDto);
		String content = mapper.writeValueAsString(requestDto);
		when(masterdataCreationUtil.createMasterData(Device.class, deviceDto)).thenReturn(deviceDto);
		Mockito.when(deviceRepository.create(Mockito.any())).thenReturn(device);
		when(deviceHistoryRepository.create(Mockito.any())).thenReturn(deviceHistory);
		mockMvc.perform(
				MockMvcRequestBuilders.post("/devices").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().is2xxSuccessful());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void createDeviceExceptionTest() throws Exception {
		RequestWrapper<DeviceDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.device.create");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(deviceDto);
		String content = mapper.writeValueAsString(requestDto);
		Zone zone = new Zone();
		zone.setCode("MOR");
		List<Zone> zones = new ArrayList<>();
		zones.add(zone);
		when(zoneUtils.getUserZones()).thenReturn(zones);
		when(masterdataCreationUtil.createMasterData(Device.class, deviceDto)).thenReturn(deviceDto);
		Mockito.when(deviceRepository.create(Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot insert", null));
		mockMvc.perform(
				MockMvcRequestBuilders.post("/devices").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().is5xxServerError());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void createDeviceIllegalExceptionTest() throws Exception {
		RequestWrapper<DeviceDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.device.create");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(deviceDto);
		String content = mapper.writeValueAsString(requestDto);
		Zone zone = new Zone();
		zone.setCode("MOR");
		List<Zone> zones = new ArrayList<>();
		zones.add(zone);
		when(zoneUtils.getUserZones()).thenReturn(zones);
		when(masterdataCreationUtil.createMasterData(Device.class, deviceDto)).thenReturn(deviceDto);
		Mockito.when(deviceRepository.create(Mockito.any())).thenThrow(new IllegalArgumentException());
		mockMvc.perform(
				MockMvcRequestBuilders.post("/devices").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void updateDeviceSuccessTest() throws Exception {
		RequestWrapper<DeviceDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.device.update");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(deviceDto);
		String content = mapper.writeValueAsString(requestDto);
		Mockito.when(deviceRepository.findByIdAndLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString(),
				Mockito.anyString())).thenReturn(device);
		when(deviceHistoryRepository.create(Mockito.any())).thenReturn(deviceHistory);
		Mockito.when(deviceRepository.update(Mockito.any())).thenReturn(device);
		mockMvc.perform(MockMvcRequestBuilders.put("/devices").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void updateDeviceDatabaseConnectionExceptionTest() throws Exception {
		when(deviceRepository.findByIdAndLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString(),
				Mockito.anyString())).thenReturn(device);
		when(deviceRepository.update(Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot execute statement", null));
		mockMvc.perform(put("/devices/1000").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError());

	}

	@Test
	@WithUserDetails("zonal-admin")
	public void updateDeviceNotFoundExceptionTest() throws Exception {
		RequestWrapper<DeviceDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.device.create");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(deviceDto);
		String content = mapper.writeValueAsString(requestDto);
		Mockito.when(deviceRepository.findByIdAndLangCodeAndIsDeletedFalseOrIsDeletedIsNullNoIsActive(
				Mockito.anyString(), Mockito.anyString())).thenReturn(null);
		when(deviceHistoryRepository.create(Mockito.any())).thenReturn(deviceHistory);
		Mockito.when(deviceRepository.update(Mockito.any())).thenThrow(new DataNotFoundException("", ""));
		mockMvc.perform(MockMvcRequestBuilders.put("/devices").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteDeviceSuccessTest() throws Exception {
		Mockito.when(deviceRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()))
				.thenReturn(deviceList);
		Mockito.when(deviceRepository.update(Mockito.any())).thenReturn(device);
		
		when(deviceHistoryRepository.create(Mockito.any())).thenReturn(deviceHistory);
		mockMvc.perform(MockMvcRequestBuilders.delete("/devices/123").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}


	@Test
	@WithUserDetails("global-admin")
	public void deleteDeviceExceptionTest() throws Exception {
		List<Device> emptList = new ArrayList<>();
		Mockito.when(deviceRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()))
				.thenReturn(emptList);
		mockMvc.perform(MockMvcRequestBuilders.delete("/devices/1").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteDeviceDatabaseConnectionExceptionTest() throws Exception {
		when(deviceRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.any())).thenReturn(deviceList);
		when(deviceRepository.update(Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot execute statement", null));
		mockMvc.perform(delete("/devices/1000").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError());

	}

	// -----------------------------------------MachineHistory---------------------------------------------
	@Test
	@WithUserDetails("reg-processor")
	public void getMachineHistroyIdLangEffDTimeSuccessTest() throws Exception {
		when(machineHistoryRepository
				.findByFirstByIdAndLangCodeAndEffectDtimesLessThanEqualAndIsDeletedFalseOrIsDeletedIsNull(
						Mockito.anyString(), Mockito.anyString(), Mockito.any())).thenReturn(machineHistoryList);
		mockMvc.perform(
				get("/machineshistories/{id}/{langcode}/{effdatetimes}", "1000", "ENG", "2018-01-01T10:10:30.956Z"))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("reg-processor")
	public void getMachineHistroyIdLangEffDTimeNullResponseTest() throws Exception {
		when(machineHistoryRepository
				.findByFirstByIdAndLangCodeAndEffectDtimesLessThanEqualAndIsDeletedFalseOrIsDeletedIsNull(
						Mockito.anyString(), Mockito.anyString(), Mockito.any())).thenReturn(null);
		mockMvc.perform(
				get("/machineshistories/{id}/{langcode}/{effdatetimes}", "1000", "ENG", "2018-01-01T10:10:30.956Z"))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("reg-processor")
	public void getMachineHistroyIdLangEffDTimeFetchExceptionTest() throws Exception {
		when(machineHistoryRepository
				.findByFirstByIdAndLangCodeAndEffectDtimesLessThanEqualAndIsDeletedFalseOrIsDeletedIsNull(
						Mockito.anyString(), Mockito.anyString(), Mockito.any()))
								.thenThrow(DataRetrievalFailureException.class);
		mockMvc.perform(
				get("/machineshistories/{id}/{langcode}/{effdatetimes}", "1000", "ENG", "2018-01-01T10:10:30.956Z"))
				.andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("global-admin")
	public void addBlackListedWordTest() throws Exception {
		RequestWrapper<BlacklistedWordsDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		BlacklistedWordsDto blacklistedWordsDto = new BlacklistedWordsDto();
		blacklistedWordsDto.setWord("test  word");
		blacklistedWordsDto.setLangCode("eng");
		blacklistedWordsDto.setDescription("test description");
		blacklistedWordsDto.setIsActive(true);
		requestDto.setRequest(blacklistedWordsDto);
		String contentJson = mapper.writeValueAsString(requestDto);
		BlacklistedWords blacklistedWords = new BlacklistedWords();
		blacklistedWords.setLangCode("TST");
		Mockito.when(wordsRepository.create(Mockito.any())).thenReturn(blacklistedWords);
		mockMvc.perform(post("/blacklistedwords").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void createBlacklistedWordsLangValidationExceptionTest() throws Exception {
		RequestWrapper<BlacklistedWordsDto> requestDto = new RequestWrapper<BlacklistedWordsDto>();
		requestDto.setId("mosip.language.create");
		requestDto.setVersion("1.0.0");
		BlacklistedWordsDto blacklistedWordsDto = new BlacklistedWordsDto();
		blacklistedWordsDto.setWord("test  word");
		blacklistedWordsDto.setLangCode("akk");
		blacklistedWordsDto.setDescription("test description");
		blacklistedWordsDto.setIsActive(true);
		requestDto.setRequest(blacklistedWordsDto);
		String content = mapper.writeValueAsString(requestDto);
		mockMvc.perform(post("/blacklistedwords").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void addBlackListedWordExceptionTest() throws Exception {
		RequestWrapper<BlacklistedWordsDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		BlacklistedWordsDto blacklistedWordsDto = new BlacklistedWordsDto();
		blacklistedWordsDto.setWord("test  word");
		blacklistedWordsDto.setLangCode("eng");
		blacklistedWordsDto.setDescription("test description");
		blacklistedWordsDto.setIsActive(true);
		requestDto.setRequest(blacklistedWordsDto);
		String contentJson = mapper.writeValueAsString(requestDto);
		when(wordsRepository.create(Mockito.any())).thenThrow(new DataAccessLayerException("", "cannot insert", null));
		mockMvc.perform(post("/blacklistedwords").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("global-admin")
	public void createRegistrationCenterTypeListTest() throws Exception {
		RequestWrapper<RegistrationCenterTypeDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		RegistrationCenterTypeDto registrationCenterTypeDto = new RegistrationCenterTypeDto();
		registrationCenterTypeDto.setCode("testcode");
		registrationCenterTypeDto.setDescr("testdescription");
		registrationCenterTypeDto.setIsActive(true);
		registrationCenterTypeDto.setLangCode("eng");
		registrationCenterTypeDto.setName("testname");
		requestDto.setRequest(registrationCenterTypeDto);
		String contentJson = mapper.writeValueAsString(requestDto);
		when(masterdataCreationUtil.createMasterData(RegistrationCenterType.class, registrationCenterTypeDto))
				.thenReturn(registrationCenterTypeDto);
		when(registrationCenterTypeRepository.create(Mockito.any())).thenReturn(regCenterType);
		mockMvc.perform(post("/registrationcentertypes").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void createRegistrationCenterTypeListLanguageCodeValidationTest() throws Exception {
		RequestWrapper<RegistrationCenterTypeDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		RegistrationCenterTypeDto registrationCenterTypeDto = new RegistrationCenterTypeDto();
		registrationCenterTypeDto.setCode("testcode");
		registrationCenterTypeDto.setDescr("testdescription");
		registrationCenterTypeDto.setIsActive(true);
		registrationCenterTypeDto.setLangCode("xyz");
		registrationCenterTypeDto.setName("testname");
		requestDto.setRequest(registrationCenterTypeDto);
		String contentJson = mapper.writeValueAsString(requestDto);
		when(registrationCenterTypeRepository.create(Mockito.any())).thenReturn(regCenterType);
		when(masterdataCreationUtil.createMasterData(RegistrationCenterType.class, registrationCenterTypeDto))
				.thenReturn(registrationCenterTypeDto);
		mockMvc.perform(post("/registrationcentertypes").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void createRegistrationCenterTypeListTestExceptionTest() throws Exception {
		RequestWrapper<RegistrationCenterTypeDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		RegistrationCenterTypeDto registrationCenterTypeDto = new RegistrationCenterTypeDto();
		registrationCenterTypeDto.setCode("testcode");
		registrationCenterTypeDto.setDescr("testdescription");
		registrationCenterTypeDto.setIsActive(true);
		registrationCenterTypeDto.setLangCode("eng");
		registrationCenterTypeDto.setName("testname");
		requestDto.setRequest(registrationCenterTypeDto);
		String contentJson = mapper.writeValueAsString(requestDto);
		when(registrationCenterTypeRepository.create(Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot execute statement", null));
		when(masterdataCreationUtil.createMasterData(RegistrationCenterType.class, registrationCenterTypeDto))
				.thenReturn(registrationCenterTypeDto);
		mockMvc.perform(post("/registrationcentertypes").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("global-admin")
	public void createIdTypeTest() throws Exception {
		RequestWrapper<IdTypeDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		IdTypeDto idTypeDto = new IdTypeDto();
		idTypeDto.setCode("testcode");
		idTypeDto.setDescr("testdescription");
		idTypeDto.setIsActive(true);
		idTypeDto.setLangCode("eng");
		idTypeDto.setName("testname");
		requestDto.setRequest(idTypeDto);
		String content = mapper.writeValueAsString(requestDto);
		IdType idType = new IdType();
		idType.setCode("IDT001");
		when(idTypeRepository.create(Mockito.any())).thenReturn(idType);
		mockMvc.perform(post("/idtypes").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void createIdTypeLanguageCodeValidatorTest() throws Exception {
		RequestWrapper<IdTypeDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		IdTypeDto idTypeDto = new IdTypeDto();
		idTypeDto.setCode("testcode");
		idTypeDto.setDescr("testdescription");
		idTypeDto.setIsActive(true);
		idTypeDto.setLangCode("xxx");
		idTypeDto.setName("testname");
		requestDto.setRequest(idTypeDto);
		String content = mapper.writeValueAsString(requestDto);
		IdType idType = new IdType();
		idType.setCode("IDT001");
		when(idTypeRepository.create(Mockito.any())).thenReturn(idType);
		mockMvc.perform(post("/idtypes").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void createIdTypeExceptionTest() throws Exception {
		RequestWrapper<IdTypeDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		IdTypeDto idTypeDto = new IdTypeDto();
		idTypeDto.setCode("testcode");
		idTypeDto.setDescr("testdescription");
		idTypeDto.setIsActive(true);
		idTypeDto.setLangCode("eng");
		idTypeDto.setName("testname");
		requestDto.setRequest(idTypeDto);
		String contentJson = mapper.writeValueAsString(requestDto);
		when(idTypeRepository.create(Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot execute statement", null));
		mockMvc.perform(post("/idtypes").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("global-admin")
	public void addDocumentTypeListTest() throws Exception {
		RequestWrapper<DocumentTypeDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		DocumentTypeDto documentTypeDto = new DocumentTypeDto();
		documentTypeDto.setCode("D001");
		documentTypeDto.setDescription("Proof Of Identity");
		documentTypeDto.setIsActive(true);
		documentTypeDto.setLangCode("eng");
		documentTypeDto.setName("POI");
		requestDto.setRequest(documentTypeDto);
		String contentJson = mapper.writeValueAsString(requestDto);
		when(masterdataCreationUtil.createMasterData(DocumentType.class, documentTypeDto)).thenReturn(documentTypeDto);
		when(documentTypeRepository.create(Mockito.any())).thenReturn(type);
		mockMvc.perform(post("/documenttypes").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void addDocumentTypesDatabaseConnectionExceptionTest() throws Exception {
		RequestWrapper<DocumentTypeDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		DocumentTypeDto documentTypeDto = new DocumentTypeDto();
		documentTypeDto.setCode("D001");
		documentTypeDto.setDescription("Proof Of Identity");
		documentTypeDto.setIsActive(true);
		documentTypeDto.setLangCode("eng");
		documentTypeDto.setName("POI");
		requestDto.setRequest(documentTypeDto);
		String contentJson = mapper.writeValueAsString(requestDto);
		when(masterdataCreationUtil.createMasterData(DocumentType.class, documentTypeDto)).thenReturn(documentTypeDto);
		when(documentTypeRepository.create(Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot execute statement", null));
		mockMvc.perform(post("/documenttypes").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("global-admin")
	public void addDocumentTypesDatabaseExceptionTest() throws Exception {
		RequestWrapper<DocumentTypeDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		DocumentTypeDto documentTypeDto = new DocumentTypeDto();
		documentTypeDto.setCode("D001");
		documentTypeDto.setDescription("Proof Of Identity");
		documentTypeDto.setIsActive(true);
		documentTypeDto.setLangCode("eng");
		documentTypeDto.setName("POI");
		requestDto.setRequest(documentTypeDto);
		String contentJson = mapper.writeValueAsString(requestDto);
		when(masterdataCreationUtil.createMasterData(DocumentType.class, documentTypeDto)).thenReturn(documentTypeDto);
		when(documentTypeRepository.create(Mockito.any())).thenThrow(new IllegalArgumentException());
		mockMvc.perform(post("/documenttypes").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("global-admin")
	public void addDocumentTypesLangCodeValidationTest() throws Exception {
		RequestWrapper<DocumentTypeDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		DocumentTypeDto documentTypeDto = new DocumentTypeDto();
		documentTypeDto.setCode("D001");
		documentTypeDto.setDescription("Proof Of Identity");
		documentTypeDto.setIsActive(true);
		documentTypeDto.setLangCode("akk");
		documentTypeDto.setName("POI");
		requestDto.setRequest(documentTypeDto);
		String contentJson = mapper.writeValueAsString(requestDto);
		when(masterdataCreationUtil.createMasterData(DocumentType.class, documentTypeDto)).thenReturn(documentTypeDto);
		mockMvc.perform(post("/documenttypes").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void updateDocumentTypeTest() throws Exception {
		RequestWrapper<DocumentTypePutReqDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		DocumentTypePutReqDto documentTypeDto = new DocumentTypePutReqDto();
		documentTypeDto.setCode("D001");
		documentTypeDto.setDescription("Proof Of Identity");
		documentTypeDto.setIsActive(true);
		documentTypeDto.setLangCode("eng");
		documentTypeDto.setName("POI");
		requestDto.setRequest(documentTypeDto);
		String contentJson = mapper.writeValueAsString(requestDto);
		when(documentTypeRepository.findByCodeAndLangCode(Mockito.any(), Mockito.any())).thenReturn(type);
		when(masterdataCreationUtil.updateMasterData(DocumentType.class, documentTypeDto)).thenReturn(documentTypeDto);
		when(documentTypeRepository.update(Mockito.any())).thenReturn(type);
		mockMvc.perform(put("/documenttypes").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void updateDocumentTypeLangValidationTest() throws Exception {
		RequestWrapper<DocumentTypePutReqDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		DocumentTypePutReqDto documentTypeDto = new DocumentTypePutReqDto();
		documentTypeDto.setCode("D001");
		documentTypeDto.setDescription("Proof Of Identity");
		documentTypeDto.setIsActive(true);
		documentTypeDto.setLangCode("akk");
		documentTypeDto.setName("POI");
		requestDto.setRequest(documentTypeDto);
		String contentJson = mapper.writeValueAsString(requestDto);
		when(masterdataCreationUtil.updateMasterData(DocumentType.class, documentTypeDto)).thenReturn(documentTypeDto);
		when(documentTypeRepository.update(Mockito.any())).thenReturn(type);
		mockMvc.perform(put("/documenttypes").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void updateDocumentTypeNotFoundExceptionTest() throws Exception {
		RequestWrapper<DocumentTypePutReqDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		DocumentTypePutReqDto documentTypeDto = new DocumentTypePutReqDto();
		documentTypeDto.setCode("D001");
		documentTypeDto.setDescription("Proof Of Identity");
		documentTypeDto.setIsActive(true);
		documentTypeDto.setLangCode("eng");
		documentTypeDto.setName("POI");
		requestDto.setRequest(documentTypeDto);
		String contentJson = mapper.writeValueAsString(requestDto);
		when(masterdataCreationUtil.updateMasterData(DocumentType.class, documentTypeDto)).thenReturn(documentTypeDto);
		when(documentTypeRepository.findByCodeAndLangCode(Mockito.any(), Mockito.any())).thenReturn(null);
		mockMvc.perform(put("/documenttypes").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void updateDocumentTypeDatabaseConnectionExceptionTest() throws Exception {
		RequestWrapper<DocumentTypePutReqDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		DocumentTypePutReqDto documentTypeDto = new DocumentTypePutReqDto();
		documentTypeDto.setCode("D001");
		documentTypeDto.setDescription("Proof Of Identity");
		documentTypeDto.setIsActive(true);
		documentTypeDto.setLangCode("eng");
		documentTypeDto.setName("POI");
		requestDto.setRequest(documentTypeDto);
		String contentJson = mapper.writeValueAsString(requestDto);
		when(documentTypeRepository.findByCodeAndLangCode(Mockito.any(), Mockito.any())).thenReturn(type);
		when(masterdataCreationUtil.updateMasterData(DocumentType.class, documentTypeDto)).thenReturn(documentTypeDto);
		when(documentTypeRepository.update(Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot execute statement", null));
		mockMvc.perform(put("/documenttypes").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isInternalServerError());

	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteDocumentTypeTest() throws Exception {

		when(validDocumentRepository.findByDocTypeCode(Mockito.anyString())).thenReturn(new ArrayList<ValidDocument>());
		when(documentTypeRepository.deleteDocumentType(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(2);
		mockMvc.perform(delete("/documenttypes/DT001").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteDocumentTypeNotFoundExceptionTest() throws Exception {
		when(validDocumentRepository.findByDocTypeCode(Mockito.anyString())).thenReturn(new ArrayList<ValidDocument>());
		when(documentTypeRepository.deleteDocumentType(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(0);

		mockMvc.perform(delete("/documenttypes/DT001").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteDocumentTypeDatabaseConnectionExceptionTest() throws Exception {
		when(validDocumentRepository.findByDocCategoryCode(Mockito.anyString()))
				.thenReturn(new ArrayList<ValidDocument>());
		when(documentTypeRepository.deleteDocumentType(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot execute statement", null));
		mockMvc.perform(delete("/documenttypes/DT001").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError());

	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteDocumentTypeDependencyExceptionTest() throws Exception {
		ValidDocument document = new ValidDocument();
		List<ValidDocument> validDocumentList = new ArrayList<>();
		validDocumentList.add(document);
		when(validDocumentRepository.findByDocTypeCode(Mockito.anyString())).thenReturn(validDocumentList);
		mockMvc.perform(delete("/documenttypes/DT001").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("global-admin")
	public void insertValidDocumentExceptionTest() throws Exception {
		RequestWrapper<ValidDocumentDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		ValidDocumentDto validDocumentDto = new ValidDocumentDto();
		validDocumentDto.setIsActive(true);
		validDocumentDto.setLangCode("eng");
		validDocumentDto.setDocTypeCode("TEST");
		validDocumentDto.setDocCategoryCode("TSC");
		requestDto.setRequest(validDocumentDto);
		String contentJson = mapper.writeValueAsString(requestDto);
		when(validDocumentRepository.create(Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot execute statement", null));
		mockMvc.perform(post("/validdocuments").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("global-admin")
	public void addDocumentCategoryTest() throws Exception {
		RequestWrapper<DocumentCategoryDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		DocumentCategoryDto documentCategoryDto = new DocumentCategoryDto();
		documentCategoryDto.setCode("D001");
		documentCategoryDto.setDescription("Proof Of Identity");
		documentCategoryDto.setIsActive(true);
		documentCategoryDto.setLangCode("eng");
		documentCategoryDto.setName("POI");
		requestDto.setRequest(documentCategoryDto);
		String contentJson = mapper.writeValueAsString(requestDto);
		when(masterdataCreationUtil.createMasterData(DocumentCategory.class, documentCategoryDto))
				.thenReturn(documentCategoryDto);
		when(documentCategoryRepository.create(Mockito.any())).thenReturn(category);
		mockMvc.perform(post("/documentcategories").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void addDocumentCategoryLanguageCodeValidatorTest() throws Exception {
		RequestWrapper<DocumentCategoryDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		DocumentCategoryDto documentCategoryDto = new DocumentCategoryDto();
		documentCategoryDto.setCode("D001");
		documentCategoryDto.setDescription("Proof Of Identity");
		documentCategoryDto.setIsActive(true);
		documentCategoryDto.setLangCode("xxx");
		documentCategoryDto.setName("POI");
		requestDto.setRequest(documentCategoryDto);
		String contentJson = mapper.writeValueAsString(requestDto);

		when(documentCategoryRepository.create(Mockito.any())).thenReturn(category);
		mockMvc.perform(post("/documentcategories").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void addDocumentCategoryDatabaseConnectionExceptionTest() throws Exception {
		RequestWrapper<DocumentCategoryDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		DocumentCategoryDto documentCategoryDto = new DocumentCategoryDto();
		documentCategoryDto.setCode("D001");
		documentCategoryDto.setDescription("Proof Of Identity");
		documentCategoryDto.setIsActive(true);
		documentCategoryDto.setLangCode("eng");
		documentCategoryDto.setName("POI");
		requestDto.setRequest(documentCategoryDto);
		String contentJson = mapper.writeValueAsString(requestDto);

		when(documentCategoryRepository.create(Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot execute statement", null));
		mockMvc.perform(post("/documentcategories").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("global-admin")
	public void updateDocumentCategoryTest() throws Exception {
		RequestWrapper<DocumentCategoryDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		DocumentCategoryDto documentCategoryDto = new DocumentCategoryDto();
		documentCategoryDto.setCode("D001");
		documentCategoryDto.setDescription("Proof Of Identity");
		documentCategoryDto.setIsActive(true);
		documentCategoryDto.setLangCode("eng");
		documentCategoryDto.setName("POI");
		requestDto.setRequest(documentCategoryDto);
		String contentJson = mapper.writeValueAsString(requestDto);
		when(documentCategoryRepository.findByCodeAndLangCode(Mockito.any(),
				Mockito.any())).thenReturn(category);
		when(masterdataCreationUtil.updateMasterData(DocumentCategory.class, documentCategoryDto))
				.thenReturn(documentCategoryDto);
		mockMvc.perform(put("/documentcategories").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void updateDocumentCategoryNotFoundExceptionTest() throws Exception {
		RequestWrapper<DocumentCategoryDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		DocumentCategoryDto documentCategoryDto = new DocumentCategoryDto();
		documentCategoryDto.setCode("D001");
		documentCategoryDto.setDescription("Proof Of Identity");
		documentCategoryDto.setIsActive(true);
		documentCategoryDto.setLangCode("eng");
		documentCategoryDto.setName("POI");
		requestDto.setRequest(documentCategoryDto);
		String contentJson = mapper.writeValueAsString(requestDto);
		when(documentCategoryRepository.findByCodeAndLangCode(Mockito.any(),
				Mockito.any())).thenReturn(null);
		when(masterdataCreationUtil.updateMasterData(DocumentCategory.class, documentCategoryDto))
				.thenReturn(documentCategoryDto);
		mockMvc.perform(put("/documentcategories").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void updateDocumentCategoryDatabaseConnectionExceptionTest() throws Exception {
		RequestWrapper<DocumentCategoryDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		DocumentCategoryDto documentCategoryDto = new DocumentCategoryDto();
		documentCategoryDto.setCode("D001");
		documentCategoryDto.setDescription("Proof Of Identity");
		documentCategoryDto.setIsActive(true);
		documentCategoryDto.setLangCode("eng");
		documentCategoryDto.setName("POI");
		requestDto.setRequest(documentCategoryDto);
		String contentJson = mapper.writeValueAsString(requestDto);
		when(documentCategoryRepository.findByCodeAndLangCode(Mockito.any(),
				Mockito.any())).thenReturn(category);
		when(documentCategoryRepository.update(Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot execute statement", null));
		when(masterdataCreationUtil.updateMasterData(DocumentCategory.class, documentCategoryDto))
				.thenReturn(documentCategoryDto);
		mockMvc.perform(put("/documentcategories").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteDocumentCategoryTest() throws Exception {
		when(validDocumentRepository.findByDocCategoryCode(Mockito.anyString()))
				.thenReturn(new ArrayList<ValidDocument>());
		when(documentCategoryRepository.deleteDocumentCategory(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(2);
		mockMvc.perform(delete("/documentcategories/DC001").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteDocumentCategoryNotFoundExceptionTest() throws Exception {
		when(validDocumentRepository.findByDocCategoryCode(Mockito.anyString()))
				.thenReturn(new ArrayList<ValidDocument>());
		when(documentCategoryRepository.deleteDocumentCategory(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(0);

		mockMvc.perform(delete("/documentcategories/DC001").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteDocumentCategoryDatabaseConnectionExceptionTest() throws Exception {
		when(validDocumentRepository.findByDocCategoryCode(Mockito.anyString()))
				.thenReturn(new ArrayList<ValidDocument>());
		when(documentCategoryRepository.deleteDocumentCategory(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot execute statement", null));
		mockMvc.perform(delete("/documentcategories/DC001").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError());

	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteDocumentCategoryDependencyExceptionTest() throws Exception {
		ValidDocument document = new ValidDocument();
		List<ValidDocument> validDocumentList = new ArrayList<>();
		validDocumentList.add(document);
		when(validDocumentRepository.findByDocCategoryCode(Mockito.anyString())).thenReturn(validDocumentList);
		mockMvc.perform(delete("/documentcategories/DC001").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("global-admin")
	public void insertValidDocumentTest() throws Exception {
		RequestWrapper<ValidDocumentDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		ValidDocumentDto validDocumentDto = new ValidDocumentDto();
		validDocumentDto.setIsActive(true);
		validDocumentDto.setLangCode("eng");
		validDocumentDto.setDocCategoryCode("TSC");
		validDocumentDto.setDocTypeCode("TEST");
		requestDto.setRequest(validDocumentDto);
		String contentJson = mapper.writeValueAsString(requestDto);

		when(validDocumentRepository.create(Mockito.any())).thenReturn(validDocument);
		mockMvc.perform(post("/validdocuments").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteValidDocumentTest() throws Exception {
		when(validDocumentRepository.deleteValidDocument(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(1);
		mockMvc.perform(delete("/validdocuments/DC001/DT001").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteValidDocumentNotFoundExceptionTest() throws Exception {
		when(validDocumentRepository.deleteValidDocument(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(0);
		mockMvc.perform(delete("/validdocuments/DC001/DT001").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteValidDocumentDatabaseConnectionExceptionTest() throws Exception {
		when(validDocumentRepository.deleteValidDocument(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot execute statement", null));
		mockMvc.perform(delete("/validdocuments/DC001/DT001").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void getValidDocumentSuccessTest() throws Exception {
		DocumentCategory documentCategory = new DocumentCategory();
		documentCategory.setCode("POA");
		documentCategory.setName("Proof of Address");
		documentCategory.setDescription("Address Proof");
		documentCategory.setLangCode("eng");
		documentCategory.setIsActive(true);

		List<DocumentCategory> documentCategories = new ArrayList<>();
		documentCategories.add(documentCategory);

		DocumentType documentType = new DocumentType();
		documentType.setCode("RNC");
		documentType.setName("Rental contract");
		documentType.setDescription("Rental Agreement of address");
		documentType.setLangCode("eng");
		documentType.setIsActive(true);

		List<DocumentType> documentTypes = new ArrayList<>();
		documentTypes.add(documentType);

		when(documentCategoryRepository.findAllByLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.any()))
				.thenReturn(documentCategories);
		when(documentTypeRepository.findByCodeAndLangCodeAndIsDeletedFalse(Mockito.any(), Mockito.any()))
				.thenReturn(documentTypes);

		mockMvc.perform(get("/validdocuments/eng")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void getValidDocumentNotFoundExceptionTest() throws Exception {
		when(documentCategoryRepository.findAllByLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.any()))
				.thenReturn(new ArrayList<DocumentCategory>());
		when(documentTypeRepository.findByCodeAndLangCodeAndIsDeletedFalse(Mockito.any(), Mockito.any()))
				.thenReturn(null);

		mockMvc.perform(get("/validdocuments/eng")).andExpect(status().is5xxServerError());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void getValidDocumentFetchExceptionTest() throws Exception {
		when(documentCategoryRepository.findAllByLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.any()))
				.thenThrow(new DataAccessLayerException(null, null, null));

		mockMvc.perform(get("/validdocuments/eng")).andExpect(status().isInternalServerError());
	}

	/*------------------------- deviceSecification update and delete ----------------------------*/
	@Test
	@WithUserDetails("global-admin")
	public void updateDeviceSpecificationTest() throws Exception {
		RequestWrapper<DeviceSpecificationDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		requestDto.setRequest(deviceSpecificationDto);
		String contentJson = mapper.writeValueAsString(requestDto);
		when(deviceTypeRepository.findDeviceTypeByCodeAndByLangCode(Mockito.any(), Mockito.any()))
				.thenReturn(deviceType);
		when(deviceSpecificationRepository.findByIdAndLangCodeAndIsDeletedFalseorIsDeletedIsNull(Mockito.any(),
				Mockito.any())).thenReturn(deviceSpecification);
		when(masterdataCreationUtil.updateMasterData(DeviceSpecification.class, deviceSpecificationDto))
				.thenReturn(deviceSpecificationDto);
		when(deviceSpecificationRepository.update(Mockito.any())).thenReturn(deviceSpecification);
		mockMvc.perform(put("/devicespecifications").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void updateDeviceSpecificationLangCodeValidationTest() throws Exception {
		RequestWrapper<DeviceSpecificationDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		deviceSpecificationDto.setLangCode("akk");
		requestDto.setRequest(deviceSpecificationDto);
		String contentJson = mapper.writeValueAsString(requestDto);
		when(masterdataCreationUtil.updateMasterData(DeviceSpecification.class, deviceSpecificationDto))
				.thenReturn(deviceSpecificationDto);
		mockMvc.perform(put("/devicespecifications").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void updateDeviceSpecificationRequestExceptionTest() throws Exception {
		RequestWrapper<DeviceSpecificationDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");

		requestDto.setRequest(deviceSpecificationDto);
		String contentJson = mapper.writeValueAsString(requestDto);
		when(deviceTypeRepository.findDeviceTypeByCodeAndByLangCode(Mockito.any(), Mockito.any()))
				.thenReturn(deviceType);
		when(deviceSpecificationRepository.findByIdAndLangCodeAndIsDeletedFalseorIsDeletedIsNull(Mockito.any(),
				Mockito.any())).thenReturn(null);
		when(masterdataCreationUtil.updateMasterData(DeviceSpecification.class, deviceSpecificationDto))
				.thenReturn(deviceSpecificationDto);
		mockMvc.perform(put("/devicespecifications").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void updateDeviceSpecificationDatabaseConnectionExceptionTest() throws Exception {
		RequestWrapper<DeviceSpecificationDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		requestDto.setRequest(deviceSpecificationDto);
		String contentJson = mapper.writeValueAsString(requestDto);

		when(deviceTypeRepository.findDeviceTypeByCodeAndByLangCode(Mockito.any(), Mockito.any()))
				.thenReturn(deviceType);
		when(deviceSpecificationRepository.findByIdAndLangCodeAndIsDeletedFalseorIsDeletedIsNull(Mockito.any(),
				Mockito.any())).thenReturn(deviceSpecification);
		when(deviceSpecificationRepository.update(Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot execute statement", null));
		when(masterdataCreationUtil.updateMasterData(DeviceSpecification.class, deviceSpecificationDto))
				.thenReturn(deviceSpecificationDto);
		mockMvc.perform(put("/devicespecifications").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteDeviceSpecificationTest() throws Exception {

		when(deviceSpecificationRepository.findByIdAndIsDeletedFalseorIsDeletedIsNull(Mockito.any()))
				.thenReturn(deviceSpecList);
		when(deviceRepository.findDeviceByDeviceSpecIdAndIsDeletedFalseorIsDeletedIsNull(deviceSpecification.getId()))
				.thenReturn(new ArrayList<Device>());
		when(deviceSpecificationRepository.update(Mockito.any())).thenReturn(deviceSpecification);
		mockMvc.perform(delete("/devicespecifications/DS001").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteDeviceSpecificationNotFoundExceptionTest() throws Exception {
		List<DeviceSpecification> emptList = new ArrayList<>();
		when(deviceSpecificationRepository.findByIdAndIsDeletedFalseorIsDeletedIsNull(Mockito.any()))
				.thenReturn(emptList);
		mockMvc.perform(delete("/devicespecifications/DS001").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteDeviceSpecificationDatabaseConnectionExceptionTest() throws Exception {
		when(deviceSpecificationRepository.findByIdAndIsDeletedFalseorIsDeletedIsNull(Mockito.any()))
				.thenReturn(deviceSpecList);
		when(deviceSpecificationRepository.update(Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot execute statement", null));
		mockMvc.perform(delete("/devicespecifications/DS001").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteDeviceSpecificationdependencyExceptionTest() throws Exception {
		List<Device> deviceList = new ArrayList<Device>();
		Device device = new Device();
		deviceList.add(device);
		when(deviceSpecificationRepository.findByIdAndIsDeletedFalseorIsDeletedIsNull(Mockito.any()))
				.thenReturn(deviceSpecList);
		when(deviceRepository.findDeviceByDeviceSpecIdAndIsDeletedFalseorIsDeletedIsNull(deviceSpecification.getId()))
				.thenReturn(deviceList);
		mockMvc.perform(delete("/devicespecifications/DS001").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError());
	}

	/*------------------------------ template update and delete test-----------------------------*/
	@Test
	@WithUserDetails("global-admin")
	public void updateTemplateTest() throws Exception {
		RequestWrapper<TemplateDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		requestDto.setRequest(templateDto);
		String contentJson = mapper.writeValueAsString(requestDto);
		when(templateRepository.findTemplateByIDAndLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.any(),
				Mockito.any())).thenReturn(template);
		when(templateRepository.update(Mockito.any())).thenReturn(template);
		when(masterdataCreationUtil.updateMasterData(Template.class, templateDto)).thenReturn(templateDto);
		mockMvc.perform(put("/templates").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void updateTemplateLangCodeValidationTest() throws Exception {
		RequestWrapper<TemplateDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		templateDto.setLangCode("akk");
		requestDto.setRequest(templateDto);
		String contentJson = mapper.writeValueAsString(requestDto);
		mockMvc.perform(put("/templates").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void updateTemplateNotRequestExceptionTest() throws Exception {
		RequestWrapper<TemplateDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		requestDto.setRequest(templateDto);
		String contentJson = mapper.writeValueAsString(requestDto);
		when(templateRepository.findTemplateByIDAndLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.any(),
				Mockito.any())).thenReturn(null);
		mockMvc.perform(put("/templates").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void updateTemplateDatabaseConnectionExceptionTest() throws Exception {
		RequestWrapper<TemplateDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		requestDto.setRequest(templateDto);
		String contentJson = mapper.writeValueAsString(requestDto);
		when(templateRepository.findTemplateByIDAndLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.any(),
				Mockito.any())).thenReturn(template);
		when(templateRepository.update(Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot execute statement", null));
		mockMvc.perform(put("/templates").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteTemplateTest() throws Exception {
		when(templateRepository.deleteTemplate(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(1);
		mockMvc.perform(delete("/templates/T001").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteTemplateRequestExceptionTest() throws Exception {
		when(templateRepository.deleteTemplate(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(0);
		mockMvc.perform(delete("/templates/T001").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteTemplateDatabaseConnectionExceptionTest() throws Exception {
		when(templateRepository.deleteTemplate(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot execute statement", null));
		mockMvc.perform(delete("/templates/T001").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError());

	}

	// ------------------------------- TemplateFileFormat Test
	@Test
	@WithUserDetails("global-admin")
	public void updateTemplateFileFormatSuccessTest() throws Exception {
		RequestWrapper<TemplateFileFormatDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.device.update");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(templateFileFormatDto);
		String content = mapper.writeValueAsString(requestDto);
		Mockito.when(templateFileFormatRepository
				.findByCodeAndLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(templateFileFormat);
		Mockito.when(templateFileFormatRepository.update(Mockito.any())).thenReturn(templateFileFormat);
		when(masterdataCreationUtil.updateMasterData(TemplateFileFormat.class, templateFileFormatDto))
				.thenReturn(templateFileFormatDto);
		mockMvc.perform(MockMvcRequestBuilders.put("/templatefileformats").contentType(MediaType.APPLICATION_JSON)
				.content(content)).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void updateTemplateFileFormatLanguageCodeValidationTest() throws Exception {
		RequestWrapper<TemplateFileFormatDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.device.update");
		requestDto.setVersion("1.0.0");
		templateFileFormatDto.setLangCode("xxx");
		requestDto.setRequest(templateFileFormatDto);
		String content = mapper.writeValueAsString(requestDto);
		Mockito.when(templateFileFormatRepository
				.findByCodeAndLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(templateFileFormat);
		Mockito.when(templateFileFormatRepository.update(Mockito.any())).thenReturn(templateFileFormat);
		mockMvc.perform(MockMvcRequestBuilders.put("/templatefileformats").contentType(MediaType.APPLICATION_JSON)
				.content(content)).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void updateTemplateFileFormatExceptionTest() throws Exception {
		RequestWrapper<TemplateFileFormatDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.device.update");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(templateFileFormatDto);
		String content = mapper.writeValueAsString(requestDto);

		Mockito.when(templateFileFormatRepository
				.findByCodeAndLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(null);
		Mockito.when(templateFileFormatRepository.update(Mockito.any())).thenThrow(new RequestException("", ""));
		mockMvc.perform(MockMvcRequestBuilders.put("/templatefileformats").contentType(MediaType.APPLICATION_JSON)
				.content(content)).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteTemplateFileFormatSuccessTest() throws Exception {
		List<Template> templates = new ArrayList<>();
		Mockito.when(templateRepository.findAllByFileFormatCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()))
				.thenReturn(templates);
		Mockito.when(templateFileFormatRepository.deleteTemplateFileFormat(Mockito.anyString(), Mockito.any(),
				Mockito.anyString())).thenReturn(1);
		Mockito.when(templateFileFormatRepository.update(Mockito.any())).thenReturn(templateFileFormat);
		mockMvc.perform(MockMvcRequestBuilders.delete("/templatefileformats/1").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteTemplateFileFormatExceptionTest() throws Exception {
		List<Template> templates = new ArrayList<>();
		Mockito.when(templateRepository.findAllByFileFormatCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()))
				.thenReturn(templates);
		Mockito.when(templateFileFormatRepository.deleteTemplateFileFormat(Mockito.anyString(), Mockito.any(),
				Mockito.anyString())).thenThrow(new RequestException("", ""));
		mockMvc.perform(MockMvcRequestBuilders.delete("/templatefileformats/1").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	/*------------------------------------Holiday Update/delete -------------------------------------*/

	@Test
	@WithUserDetails("global-admin")
	public void deleteHolidaySuccess() throws Exception {
		String input = "{\n" + "  \"id\": \"string\",\n" + "  \"metadata\": {},\n" + "  \"request\": {\n"
				+ "    \"holidayDate\": \"2019-01-01\",\n" + "    \"holidayName\": \"New Year\",\n"
				+ "    \"locationCode\": \"LOC01\"\n" + "  },\n" + "  \"requesttime\": \"2018-12-24T06:15:12.494Z\",\n"
				+ "  \"version\": \"string\"\n" + "}";
		when(holidayRepository.deleteHolidays(any(), anyString(), any(), anyString())).thenReturn(1);
		mockMvc.perform(delete("/holidays").contentType(MediaType.APPLICATION_JSON).content(input))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteHolidayNoHolidayFound() throws Exception {
		String input = "{\n" + "  \"id\": \"string\",\n" + "  \"metadata\": {},\n" + "  \"request\": {\n"
				+ "    \"holidayDate\": \"2019-01-01\",\n" + "    \"holidayName\": \"New Year\",\n"
				+ "    \"locationCode\": \"LOC01\"\n" + "  },\n" + "  \"requesttime\": \"2018-12-24T06:15:12.494Z\",\n"
				+ "  \"version\": \"string\"\n" + "}";
		when(holidayRepository.deleteHolidays(any(), anyString(), any(), anyString())).thenReturn(0);
		mockMvc.perform(delete("/holidays").contentType(MediaType.APPLICATION_JSON).content(input))
				.andExpect(status().isOk());
	}

	@SuppressWarnings("unchecked")
	@Test
	@WithUserDetails("global-admin")
	public void deleteHolidayFailure() throws Exception {
		String input = "{\n" + "  \"id\": \"string\",\n" + "  \"metadata\": {},\n" + "  \"request\": {\n"
				+ "    \"holidayDate\": \"2019-01-01\",\n" + "    \"holidayName\": \"New Year\",\n"
				+ "    \"locationCode\": \"LOC01\"\n" + "  },\n" + "  \"requesttime\": \"2018-12-24T06:15:12.494Z\",\n"
				+ "  \"version\": \"string\"\n" + "}";
		when(holidayRepository.deleteHolidays(any(), anyString(), any(), anyString()))
				.thenThrow(DataRetrievalFailureException.class, DataAccessLayerException.class);
		mockMvc.perform(delete("/holidays").contentType(MediaType.APPLICATION_JSON).content(input))
				.andExpect(status().isInternalServerError());

	}

	@Test
	@WithUserDetails("global-admin")
	public void updateHolidaySuccessTest() throws Exception {
		String input = "{\n" + "  \"id\": \"string\",\n" + "  \"metadata\": {},\n" + "  \"request\": {\n"
				+ "    \"id\": 1,\n" + "    \"locationCode\": \"LOC01\",\n" + "    \"holidayDate\": \"2018-01-01\",\n"
				+ "    \"holidayName\": \"New Year\",\n" + "    \"holidayDesc\": \"New Year\",\n"
				+ "    \"langCode\": \"eng\",\n" + "    \"isActive\": false,\n"
				+ "    \"newHolidayName\": \"string\",\n" + "    \"newHolidayDate\": \"string\",\n"
				+ "    \"newHolidayDesc\": \"string\"\n" + "  },\n"
				+ "  \"requesttime\": \"2018-12-24T06:26:18.807Z\",\n" + "  \"version\": \"string\"\n" + "}";
		when(holidayRepository.createQueryUpdateOrDelete(any(), any())).thenReturn(1);
		mockMvc.perform(put("/holidays").contentType(MediaType.APPLICATION_JSON).content(input))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void updateHolidayLanguageValidationTest() throws Exception {
		String input = "{\n" + "  \"id\": \"string\",\n" + "  \"metadata\": {},\n" + "  \"request\": {\n"
				+ "    \"id\": 1,\n" + "    \"locationCode\": \"LOC01\",\n" + "    \"holidayDate\": \"2018-01-01\",\n"
				+ "    \"holidayName\": \"New Year\",\n" + "    \"holidayDesc\": \"New Year\",\n"
				+ "    \"langCode\": \"asd\",\n" + "    \"isActive\": false,\n"
				+ "    \"newHolidayName\": \"string\",\n" + "    \"newHolidayDate\": \"string\",\n"
				+ "    \"newHolidayDesc\": \"string\"\n" + "  },\n"
				+ "  \"requesttime\": \"2018-12-24T06:26:18.807Z\",\n" + "  \"version\": \"string\"\n" + "}";
		when(holidayRepository.createQueryUpdateOrDelete(any(), any())).thenReturn(1);
		mockMvc.perform(put("/holidays").contentType(MediaType.APPLICATION_JSON).content(input))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void updateHolidaySuccessNewNameAndDateTest() throws Exception {
		String input = "{\n" + "  \"id\": \"string\",\n" + "  \"metadata\": {},\n" + "  \"request\": {\n"
				+ "    \"id\": 1,\n" + "    \"locationCode\": \"LOC01\",\n" + "    \"holidayDate\": \"2018-01-01\",\n"
				+ "    \"holidayName\": \"New Year\",\n" + "    \"holidayDesc\": \"New Year\",\n"
				+ "    \"langCode\": \"eng\",\n" + "    \"isActive\": false,\n"
				+ "    \"newHolidayName\": \"New Year\",\n" + "    \"newHolidayDate\": \"2019-01-01\",\n"
				+ "    \"newHolidayDesc\": \"New Year Desc\"\n" + "  },\n"
				+ "  \"requesttime\": \"2018-12-24T06:26:18.807Z\",\n" + "  \"version\": \"string\"\n" + "}";
		when(holidayRepository.createQueryUpdateOrDelete(any(), any())).thenReturn(1);
		mockMvc.perform(put("/holidays").contentType(MediaType.APPLICATION_JSON).content(input))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void updateHolidaySuccessNewData() throws Exception {
		String input = "{\n" + "  \"id\": \"string\",\n" + "  \"metadata\": {},\n" + "  \"request\": {\n"
				+ "    \"id\": 1,\n" + "    \"locationCode\": \"LOC01\",\n" + "    \"holidayDate\": \"2018-01-01\",\n"
				+ "    \"holidayName\": \"New Year\",\n" + "    \"holidayDesc\": \"New Year\",\n"
				+ "    \"langCode\": \"eng\",\n" + "    \"isActive\": false,\n" + "    \"newHolidayName\": \" \",\n"
				+ "    \"newHolidayDate\": \"null\",\n" + "    \"newHolidayDesc\": \" \"\n" + "  },\n"
				+ "  \"requesttime\": \"2018-12-24T06:26:18.807Z\",\n" + "  \"version\": \"string\"\n" + "}";
		when(holidayRepository.createQueryUpdateOrDelete(any(), any())).thenReturn(1);
		mockMvc.perform(put("/holidays").contentType(MediaType.APPLICATION_JSON).content(input))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void updateHolidayNoHolidayUpdated() throws Exception {
		String input = "{\n" + "  \"id\": \"string\",\n" + "  \"metadata\": {},\n" + "  \"request\": {\n"
				+ "    \"id\": 1,\n" + "    \"locationCode\": \"LOC01\",\n" + "    \"holidayDate\": \"2018-01-01\",\n"
				+ "    \"holidayName\": \"New Year\",\n" + "    \"holidayDesc\": \"New Year\",\n"
				+ "    \"langCode\": \"ENG\",\n" + "    \"isActive\": false,\n"
				+ "    \"newHolidayName\": \"string\",\n" + "    \"newHolidayDate\": \"string\",\n"
				+ "    \"newHolidayDesc\": \"string\"\n" + "  },\n"
				+ "  \"requesttime\": \"2018-12-24T06:26:18.807Z\",\n" + "  \"version\": \"string\"\n" + "}";
		when(holidayRepository.createQueryUpdateOrDelete(any(), any())).thenReturn(0);
		mockMvc.perform(put("/holidays").contentType(MediaType.APPLICATION_JSON).content(input))
				.andExpect(status().isOk());
	}

	@SuppressWarnings("unchecked")
	@Test
	@WithUserDetails("global-admin")
	public void updateHolidayNoHolidayFailure() throws Exception {
		String input = "{\n" + "  \"id\": \"string\",\n" + "  \"metadata\": {},\n" + "  \"request\": {\n"
				+ "    \"id\": 1,\n" + "    \"locationCode\": \"LOC01\",\n" + "    \"holidayDate\": \"2018-01-01\",\n"
				+ "    \"holidayName\": \"New Year\",\n" + "    \"holidayDesc\": \"New Year\",\n"
				+ "    \"langCode\": \"eng\",\n" + "    \"isActive\": false,\n"
				+ "    \"newHolidayName\": \"string\",\n" + "    \"newHolidayDate\": \"string\",\n"
				+ "    \"newHolidayDesc\": \"string\"\n" + "  },\n"
				+ "  \"requesttime\": \"2018-12-24T06:26:18.807Z\",\n" + "  \"version\": \"string\"\n" + "}";
		when(holidayRepository.createQueryUpdateOrDelete(any(), any())).thenThrow(DataRetrievalFailureException.class,
				DataAccessLayerException.class);
		mockMvc.perform(put("/holidays").contentType(MediaType.APPLICATION_JSON).content(input))
				.andExpect(status().isOk());
	}

	/*------------------------------------Blacklisted Word Update/delete -------------------------------------*/

	@Test
	@WithUserDetails("global-admin")
	public void deleteBlacklistedWordSuccess() throws Exception {
		when(wordsRepository.deleteBlackListedWord(anyString(), any())).thenReturn(1);
		mockMvc.perform(delete("/blacklistedwords/{word}", "abc")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteBlacklistedWordNoWordDeleted() throws Exception {
		when(wordsRepository.deleteBlackListedWord(anyString(), any())).thenReturn(0);
		mockMvc.perform(delete("/blacklistedwords/{word}", "abc")).andExpect(status().isOk());
	}

	@SuppressWarnings("unchecked")
	@Test
	@WithUserDetails("global-admin")
	public void deleteBlacklistedWordFailure() throws Exception {
		when(wordsRepository.deleteBlackListedWord(anyString(), any())).thenThrow(DataRetrievalFailureException.class,
				DataAccessLayerException.class);
		mockMvc.perform(delete("/blacklistedwords/{word}", "abc")).andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("global-admin")
	public void updateBadWordSuccess() throws Exception {
		String input = "{\n" + "  \"id\": \"string\",\n" + "  \"metadata\": {},\n" + "  \"request\": {\n"
				+ "    \"description\": \"bad word description\",\n" + "    \"isActive\": false,\n"
				+ "    \"langCode\": \"eng\",\n" + "    \"oldWord\": \"badword\",\n"
				+ "    \"word\": \"badwordUpdate\"\n" + "  },\n" + "  \"requesttime\": \"2018-12-24T07:21:42.232Z\",\n"
				+ "  \"version\": \"string\"\n" + "}";
		when(wordsRepository.createQueryUpdateOrDelete(Mockito.anyString(), Mockito.any())).thenReturn(1);
		mockMvc.perform(put("/blacklistedwords").contentType(MediaType.APPLICATION_JSON).content(input))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void updateBlacklistedWordsLangValidationExceptionTest() throws Exception {
		RequestWrapper<BlacklistedWordsDto> requestDto = new RequestWrapper<BlacklistedWordsDto>();
		requestDto.setId("mosip.language.create");
		requestDto.setVersion("1.0.0");
		BlacklistedWordsDto blacklistedWordsDto = new BlacklistedWordsDto();
		blacklistedWordsDto.setWord("test  word");
		blacklistedWordsDto.setLangCode("akk");
		blacklistedWordsDto.setDescription("test description");
		blacklistedWordsDto.setIsActive(true);
		requestDto.setRequest(blacklistedWordsDto);
		String content = mapper.writeValueAsString(requestDto);
		mockMvc.perform(put("/blacklistedwords").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void updateBadWordNoWordFound() throws Exception {
		String input = "{\"id\": \"string\",\"request\": {\"description\": \"bad word description\",\"isActive\": false,\"langCode\": \"ENG\",\"word\": \"badword\"},\"requesttime\": \"2018-12-24T07:21:42.232Z\",\"version\": \"string\"}";
		mockMvc.perform(put("/blacklistedwords").contentType(MediaType.APPLICATION_JSON).content(input))
				.andExpect(status().isOk());
	}

	@SuppressWarnings("unchecked")
	@Test
	@WithUserDetails("zonal-admin")
	public void updateBadWordFailure() throws Exception {
		String input = "{\n" + "  \"id\": \"string\",\n" + "  \"metadata\": {},\n" + "  \"request\": {\n"
				+ "    \"description\": \"bad word description\",\n" + "    \"isActive\": false,\n"
				+ "    \"langCode\": \"eng\",\n" + "    \"oldWord\": \"badword\",\n"
				+ "    \"word\": \"badwordUpdate\"\n" + "  },\n" + "  \"requesttime\": \"2018-12-24T07:21:42.232Z\",\n"
				+ "  \"version\": \"string\"\n" + "}";
		when(wordsRepository.createQueryUpdateOrDelete(Mockito.anyString(), Mockito.any()))
				.thenThrow(DataRetrievalFailureException.class, DataAccessLayerException.class);
		mockMvc.perform(put("/blacklistedwords").contentType(MediaType.APPLICATION_JSON).content(input))
				.andExpect(status().isInternalServerError());

	}

	@Test
	@WithUserDetails("global-admin")
	public void updateRegistrationCenterTypeTest() throws Exception {
		RequestWrapper<RegistrationCenterTypeDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		RegistrationCenterTypeDto registrationCenterTypeDto = new RegistrationCenterTypeDto();
		registrationCenterTypeDto.setCode("D001");
		registrationCenterTypeDto.setIsActive(true);
		registrationCenterTypeDto.setLangCode("eng");
		registrationCenterTypeDto.setName("POI");
		registrationCenterTypeDto.setDescr("TEST DESCR");
		requestDto.setRequest(registrationCenterTypeDto);
		RegistrationCenterType registrationCenterType = new RegistrationCenterType();
		registrationCenterType.setCode("D001");
		registrationCenterType.setDescr("TEST DESCR");
		registrationCenterType.setName("POI");
		String contentJson = mapper.writeValueAsString(requestDto);

		when(registrationCenterTypeRepository.findByCodeAndLangCode(Mockito.any(),
				Mockito.any())).thenReturn(registrationCenterType);
		when(masterdataCreationUtil.updateMasterData(RegistrationCenterType.class, registrationCenterTypeDto))
				.thenReturn(registrationCenterTypeDto);
		when(registrationCenterTypeRepository.update(Mockito.any())).thenReturn(registrationCenterType);
		mockMvc.perform(put("/registrationcentertypes").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void updateRegistrationCenterTypeLanguageCodeValidatorTest() throws Exception {
		RequestWrapper<RegistrationCenterTypeDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		RegistrationCenterTypeDto registrationCenterTypeDto = new RegistrationCenterTypeDto();
		registrationCenterTypeDto.setCode("D001");
		registrationCenterTypeDto.setIsActive(true);
		registrationCenterTypeDto.setLangCode("ENG");
		registrationCenterTypeDto.setName("POI");
		registrationCenterTypeDto.setDescr("TEST DESCR");
		requestDto.setRequest(registrationCenterTypeDto);
		RegistrationCenterType registrationCenterType = new RegistrationCenterType();
		registrationCenterType.setCode("D001");
		registrationCenterType.setDescr("TEST DESCR");
		registrationCenterType.setName("POI");
		String contentJson = mapper.writeValueAsString(requestDto);
		when(masterdataCreationUtil.updateMasterData(RegistrationCenterType.class, registrationCenterTypeDto))
				.thenReturn(registrationCenterTypeDto);
		when(registrationCenterTypeRepository.findByCodeAndLangCode(Mockito.any(),
				Mockito.any())).thenReturn(registrationCenterType);
		when(registrationCenterTypeRepository.update(Mockito.any())).thenReturn(registrationCenterType);
		mockMvc.perform(put("/registrationcentertypes").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void updateRegistrationCenterTypeNotFoundExceptionTest() throws Exception {
		RequestWrapper<RegistrationCenterTypeDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		RegistrationCenterTypeDto registrationCenterTypeDto = new RegistrationCenterTypeDto();
		registrationCenterTypeDto.setCode("D001");
		registrationCenterTypeDto.setIsActive(true);
		registrationCenterTypeDto.setLangCode("eng");
		registrationCenterTypeDto.setName("POI");
		registrationCenterTypeDto.setDescr("TEST DESCR");
		requestDto.setRequest(registrationCenterTypeDto);
		RegistrationCenterType registrationCenterType = new RegistrationCenterType();
		registrationCenterType.setCode("D001");
		registrationCenterType.setDescr("TEST DESCR");
		registrationCenterType.setName("POI");
		String contentJson = mapper.writeValueAsString(requestDto);
		when(masterdataCreationUtil.updateMasterData(RegistrationCenterType.class, registrationCenterTypeDto))
				.thenReturn(registrationCenterTypeDto);
		when(registrationCenterTypeRepository.findByCodeAndLangCode(Mockito.any(),
				Mockito.any())).thenReturn(null);
		when(registrationCenterTypeRepository.update(Mockito.any())).thenReturn(registrationCenterType);
		mockMvc.perform(put("/registrationcentertypes").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void updateRegistrationCenterTypeDataAccessExceptionTest() throws Exception {
		RequestWrapper<RegistrationCenterTypeDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		RegistrationCenterTypeDto registrationCenterTypeDto = new RegistrationCenterTypeDto();
		registrationCenterTypeDto.setCode("D001");
		registrationCenterTypeDto.setIsActive(true);
		registrationCenterTypeDto.setLangCode("eng");
		registrationCenterTypeDto.setName("POI");
		registrationCenterTypeDto.setDescr("TEST DESCR");
		requestDto.setRequest(registrationCenterTypeDto);
		RegistrationCenterType registrationCenterType = new RegistrationCenterType();
		registrationCenterType.setCode("D001");
		registrationCenterType.setDescr("TEST DESCR");
		registrationCenterType.setName("POI");
		String contentJson = mapper.writeValueAsString(requestDto);
		when(registrationCenterTypeRepository.findByCodeAndLangCode(Mockito.any(),
				Mockito.any())).thenReturn(registrationCenterType);
		when(registrationCenterTypeRepository.update(Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot execute statement", null));
		when(masterdataCreationUtil.updateMasterData(RegistrationCenterType.class, registrationCenterTypeDto))
				.thenReturn(registrationCenterTypeDto);
		mockMvc.perform(put("/registrationcentertypes").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteRegistrationCenterTypeTest() throws Exception {
		RegistrationCenterType registrationCenterType = new RegistrationCenterType();
		registrationCenterType.setCode("RC001");
		registrationCenterType.setName("RGC");
		ArrayList<RegistrationCenterType> list = new ArrayList<>();
		list.add(registrationCenterType);
		when(registrationCenterTypeRepository.findByCode(Mockito.any())).thenReturn(list);
		when(registrationCenterTypeRepository.deleteRegistrationCenterType(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(1);
		mockMvc.perform(delete("/registrationcentertypes/RC001").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteRegistrationCenterTypeNotFoundExceptionTest() throws Exception {
		when(registrationCenterTypeRepository.findByCode(Mockito.any())).thenReturn(new ArrayList<>());
		when(registrationCenterTypeRepository.deleteRegistrationCenterType(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(0);
		mockMvc.perform(delete("/registrationcentertypes/RC001").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteRegistrationCenterTypeDataAccessExceptionTest() throws Exception {
		when(registrationCenterTypeRepository.findByCode(Mockito.any())).thenReturn(new ArrayList<>());
		when(registrationCenterTypeRepository.deleteRegistrationCenterType(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot execute statement", null));

		mockMvc.perform(delete("/registrationcentertypes/RC001").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError());

	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteRegistrationCenterTypeDependencyExceptionTest() throws Exception {
		RegistrationCenter registrationCenter = new RegistrationCenter();
		List<RegistrationCenter> registrationCenterList = new ArrayList<>();
		registrationCenterList.add(registrationCenter);
		when(registrationCenterRepository.findByCenterTypeCode(Mockito.any())).thenReturn(registrationCenterList);
		mockMvc.perform(delete("/registrationcentertypes/RC001").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError());
	}

	// -----------------------------------------DeviceHistory---------------------------------------------
	@Test
	@WithUserDetails("reg-processor")
	public void getDeviceHistroyIdLangEffDTimeSuccessTest() throws Exception {
		when(deviceHistoryRepository
				.findByFirstByIdAndLangCodeAndEffectDtimesLessThanEqualAndIsDeletedFalseOrIsDeletedIsNull(
						Mockito.anyString(), Mockito.anyString(), Mockito.any())).thenReturn(deviceHistoryList);
		mockMvc.perform(
				get("/deviceshistories/{id}/{langcode}/{effdatetimes}", "1000", "ENG", "2018-01-01T10:10:30.956Z"))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("reg-processor")
	public void getDeviceHistroyIdLangEffDTimeNullResponseTest() throws Exception {
		when(deviceHistoryRepository
				.findByFirstByIdAndLangCodeAndEffectDtimesLessThanEqualAndIsDeletedFalseOrIsDeletedIsNull(
						Mockito.anyString(), Mockito.anyString(), Mockito.any())).thenReturn(null);
		mockMvc.perform(
				get("/deviceshistories/{id}/{langcode}/{effdatetimes}", "1000", "ENG", "2018-01-01T10:10:30.956Z"))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("reg-processor")
	public void getDeviceHistroyIdLangEffDTimeFetchExceptionTest() throws Exception {
		when(deviceHistoryRepository
				.findByFirstByIdAndLangCodeAndEffectDtimesLessThanEqualAndIsDeletedFalseOrIsDeletedIsNull(
						Mockito.anyString(), Mockito.anyString(), Mockito.any()))
								.thenThrow(DataRetrievalFailureException.class);
		mockMvc.perform(
				get("/deviceshistories/{id}/{langcode}/{effdatetimes}", "1000", "ENG", "2018-01-01T10:10:30.956Z"))
				.andExpect(status().isInternalServerError());
	}

	// -------------------------------RegistrationCenterControllerTest--------------------------
	@Test
	@WithUserDetails("individual")
	public void testGetRegistraionCenterHolidaysSuccess() throws Exception {
		Mockito.when(registrationCenterRepository.findByIdAndLangCode(anyString(), anyString()))
				.thenReturn(registrationCenter);
		Mockito.when(holidayRepository.findAllByLocationCodeYearAndLangCode(anyString(), anyString(), anyInt()))
				.thenReturn(holidays);
		mockMvc.perform(get("/getregistrationcenterholidays/{languagecode}/{registrationcenterid}/{year}", "ENG",
				"REG_CR_001", 2018)).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("individual")
	public void testGetRegistraionCenterHolidaysNoRegCenterFound() throws Exception {
		mockMvc.perform(get("/getregistrationcenterholidays/{languagecode}/{registrationcenterid}/{year}", "ENG",
				"REG_CR_001", 2017)).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("individual")
	public void testGetRegistraionCenterHolidaysRegistrationCenterFetchException() throws Exception {
		Mockito.when(registrationCenterRepository.findByIdAndLangCode(anyString(), anyString()))
				.thenThrow(DataRetrievalFailureException.class);
		mockMvc.perform(get("/getregistrationcenterholidays/{languagecode}/{registrationcenterid}/{year}", "ENG",
				"REG_CR_001", 2017)).andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("individual")
	public void testGetRegistraionCenterHolidaysHolidayFetchException() throws Exception {
		Mockito.when(registrationCenterRepository.findByIdAndLangCode(anyString(), anyString()))
				.thenReturn(registrationCenter);

		Mockito.when(holidayRepository.findAllByLocationCodeYearAndLangCode(anyString(), anyString(), anyInt()))
				.thenThrow(DataRetrievalFailureException.class);
		mockMvc.perform(get("/getregistrationcenterholidays/{languagecode}/{registrationcenterid}/{year}", "ENG",
				"REG_CR_001", 2018)).andExpect(status().isInternalServerError());
	}

	// -------------------Registration center device history-----------
	@Test
	@WithUserDetails("reg-processor")
	public void getRegCentDevHistByregCentIdDevIdEffTimeTest() throws Exception {
		DeviceHistory device=new DeviceHistory();
		device.setId("DID10");
		device.setRegCenterId("RCI1000");
		device.setEffectDateTime(localDateTimeUTCFormat);
		
		when(deviceHistoryRepository
				.findByFirstByRegCenterIdAndDeviceIdAndEffectDtimesLessThanEqualAndIsDeletedFalseOrIsDeletedIsNull(
						Mockito.anyString(), Mockito.anyString(), Mockito.any()))
								.thenReturn(Arrays.asList(device));
		mockMvc.perform(get("/registrationcenterdevicehistory/{regcenterid}/{deviceid}/{effdatetimes}", "RCI1000",
				"DID10", "2018-01-01T10:10:30.956Z")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("reg-processor")
	public void getRegCentDevHistByregCentIdDevIdEffTimeNullResponseTest() throws Exception {
		when(deviceHistoryRepository
				.findByFirstByRegCenterIdAndDeviceIdAndEffectDtimesLessThanEqualAndIsDeletedFalseOrIsDeletedIsNull(
						Mockito.anyString(), Mockito.anyString(), Mockito.any())).thenReturn(null);
		mockMvc.perform(get("/registrationcenterdevicehistory/{regcenterid}/{deviceid}/{effdatetimes}", "RCI1000",
				"DID10", "2018-01-01T10:10:30.956Z")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("reg-processor")
	public void getRegCentDevHistByregCentIdDevIdEffTimeFetchExceptionTest() throws Exception {
		when(deviceHistoryRepository
				.findByFirstByRegCenterIdAndDeviceIdAndEffectDtimesLessThanEqualAndIsDeletedFalseOrIsDeletedIsNull(
						Mockito.anyString(), Mockito.anyString(), Mockito.any()))
								.thenThrow(DataRetrievalFailureException.class);
		mockMvc.perform(get("/registrationcenterdevicehistory/{regcenterid}/{deviceid}/{effdatetimes}", "RCI1000",
				"DID10", "2018-01-01T10:10:30.956Z")).andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void createRegistrationCenterTest() throws Exception {
		RequestWrapper<RegistrationCenterDto> requestDto = new RequestWrapper<>();
		short numberOfKiosks = 1;
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		RegistrationCenterDto registrationCenterDto = new RegistrationCenterDto();
		registrationCenterDto.setName("TEST CENTER");
		registrationCenterDto.setAddressLine1("Address Line 1");
		registrationCenterDto.setAddressLine2("Address Line 2");
		registrationCenterDto.setAddressLine3("Address Line 3");
		registrationCenterDto.setCenterTypeCode("REG01");
		registrationCenterDto.setContactPerson("test");
		registrationCenterDto.setContactPhone("9999999999");
		registrationCenterDto.setHolidayLocationCode("HLC01");
		registrationCenterDto.setId("676");
		registrationCenterDto.setIsActive(true);
		registrationCenterDto.setLangCode("eng");
		registrationCenterDto.setLatitude("12.9646818");
		registrationCenterDto.setLocationCode("LOC01");
		registrationCenterDto.setLongitude("77.70168");
		registrationCenterDto.setNumberOfKiosks((short) 1);
		registrationCenterDto.setTimeZone("UTC");
		registrationCenterDto.setWorkingHours("9");
		RegistrationCenter registrationCenter = new RegistrationCenter();
		registrationCenter.setName("TEST CENTER");
		registrationCenter.setAddressLine1("Address Line 1");
		registrationCenter.setAddressLine2("Address Line 2");
		registrationCenter.setAddressLine3("Address Line 3");
		registrationCenter.setCenterTypeCode("REG01");
		registrationCenter.setContactPerson("global-admin");
		registrationCenter.setContactPhone("9999999999");
		registrationCenter.setHolidayLocationCode("HLC01");
		registrationCenter.setId("676");
		registrationCenter.setIsActive(true);
		registrationCenter.setLangCode("eng");
		registrationCenter.setLatitude("12.9646818");
		registrationCenter.setLocationCode("LOC01");
		registrationCenter.setLongitude("77.70168");
		registrationCenter.setNumberOfKiosks(numberOfKiosks);
		registrationCenter.setTimeZone("UTC");
		registrationCenter.setWorkingHours("9");
		requestDto.setRequest(registrationCenterDto);
		String contentJson = mapper.writeValueAsString(requestDto);
		when(registrationCenterRepository.create(Mockito.any())).thenReturn(registrationCenter);
		mockMvc.perform(post("/registrationcenters").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void updateRegistrationCenterTest() throws Exception {
		RequestWrapper<RegistrationCenterDto> requestDto = new RequestWrapper<>();
		short numberOfKiosks = 1;
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		RegistrationCenterDto registrationCenterDto = new RegistrationCenterDto();
		registrationCenterDto.setName("UPDATED TEST CENTER");
		registrationCenterDto.setAddressLine1("Address Line 1");
		registrationCenterDto.setAddressLine2("Address Line 2");
		registrationCenterDto.setAddressLine3("Address Line 3");
		registrationCenterDto.setCenterTypeCode("REG01");
		registrationCenterDto.setContactPerson("test");
		registrationCenterDto.setContactPhone("9999999999");
		registrationCenterDto.setHolidayLocationCode("HLC01");
		registrationCenterDto.setId("676");
		registrationCenterDto.setIsActive(true);
		registrationCenterDto.setLangCode("eng");
		registrationCenterDto.setLatitude("12.9646818");
		registrationCenterDto.setLocationCode("LOC01");
		registrationCenterDto.setLongitude("77.70168");
		registrationCenterDto.setNumberOfKiosks((short) 1);
		registrationCenterDto.setTimeZone("UTC");
		registrationCenterDto.setWorkingHours("9");
		RegistrationCenter updatedRegistrationCenter = new RegistrationCenter();
		updatedRegistrationCenter.setName("TEST CENTER");
		updatedRegistrationCenter.setAddressLine1("Address Line 1");
		updatedRegistrationCenter.setAddressLine2("Address Line 2");
		updatedRegistrationCenter.setAddressLine3("Address Line 3");
		updatedRegistrationCenter.setCenterTypeCode("REG01");
		updatedRegistrationCenter.setContactPerson("test");
		updatedRegistrationCenter.setContactPhone("9999999999");
		updatedRegistrationCenter.setHolidayLocationCode("HLC01");
		updatedRegistrationCenter.setId("676");
		updatedRegistrationCenter.setIsActive(true);
		updatedRegistrationCenter.setLangCode("eng");
		updatedRegistrationCenter.setLatitude("12.9646818");
		updatedRegistrationCenter.setLocationCode("LOC01");
		updatedRegistrationCenter.setLongitude("77.70168");
		updatedRegistrationCenter.setNumberOfKiosks(numberOfKiosks);
		updatedRegistrationCenter.setTimeZone("UTC");
		updatedRegistrationCenter.setWorkingHours("9");
		RegistrationCenter registrationCenter = new RegistrationCenter();
		registrationCenter.setName("TEST CENTER");
		registrationCenter.setAddressLine1("Address Line 1");
		registrationCenter.setAddressLine2("Address Line 2");
		registrationCenter.setAddressLine3("Address Line 3");
		registrationCenter.setCenterTypeCode("REG01");
		registrationCenter.setContactPerson("global-admin");
		registrationCenter.setContactPhone("9999999999");
		registrationCenter.setHolidayLocationCode("HLC01");
		registrationCenter.setId("676");
		registrationCenter.setIsActive(true);
		registrationCenter.setLangCode("eng");
		registrationCenter.setLatitude("12.9646818");
		registrationCenter.setLocationCode("LOC01");
		registrationCenter.setLongitude("77.70168");
		registrationCenter.setNumberOfKiosks(numberOfKiosks);
		registrationCenter.setTimeZone("UTC");
		registrationCenter.setWorkingHours("9");
		requestDto.setRequest(registrationCenterDto);
		String contentJson = mapper.writeValueAsString(requestDto);
		when(registrationCenterRepository.findByIdAndIsDeletedFalseOrNull(Mockito.any()))
				.thenReturn(Arrays.asList(registrationCenter));
		when(registrationCenterRepository.update(Mockito.any())).thenReturn(updatedRegistrationCenter);
		mockMvc.perform(put("/registrationcenters").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteRegistrationCenterTest() throws Exception {
		RequestWrapper<RegistrationCenterDto> requestDto = new RequestWrapper<>();
		short numberOfKiosks = 1;
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		RegistrationCenterDto registrationCenterDto = new RegistrationCenterDto();
		registrationCenterDto.setName("TEST CENTER");
		registrationCenterDto.setAddressLine1("Address Line 1");
		registrationCenterDto.setAddressLine2("Address Line 2");
		registrationCenterDto.setAddressLine3("Address Line 3");
		registrationCenterDto.setCenterTypeCode("REG01");
		registrationCenterDto.setContactPerson("test");
		registrationCenterDto.setContactPhone("9999999999");
		registrationCenterDto.setHolidayLocationCode("HLC01");
		registrationCenterDto.setId("676");
		registrationCenterDto.setIsActive(true);
		registrationCenterDto.setLangCode("eng");
		registrationCenterDto.setLatitude("12.9646818");
		registrationCenterDto.setLocationCode("LOC01");
		registrationCenterDto.setLongitude("77.70168");
		registrationCenterDto.setNumberOfKiosks((short) 1);
		registrationCenterDto.setTimeZone("UTC");
		registrationCenterDto.setWorkingHours("9");
		RegistrationCenter updatedRegistrationCenter = new RegistrationCenter();
		updatedRegistrationCenter.setName("TEST CENTER");
		updatedRegistrationCenter.setAddressLine1("Address Line 1");
		updatedRegistrationCenter.setAddressLine2("Address Line 2");
		updatedRegistrationCenter.setAddressLine3("Address Line 3");
		updatedRegistrationCenter.setCenterTypeCode("REG01");
		updatedRegistrationCenter.setContactPerson("test");
		updatedRegistrationCenter.setContactPhone("9999999999");
		updatedRegistrationCenter.setHolidayLocationCode("HLC01");
		updatedRegistrationCenter.setId("676");
		updatedRegistrationCenter.setIsActive(true);
		updatedRegistrationCenter.setLangCode("eng");
		updatedRegistrationCenter.setLatitude("12.9646818");
		updatedRegistrationCenter.setLocationCode("LOC01");
		updatedRegistrationCenter.setLongitude("77.70168");
		updatedRegistrationCenter.setNumberOfKiosks(numberOfKiosks);
		updatedRegistrationCenter.setTimeZone("UTC");
		updatedRegistrationCenter.setWorkingHours("9");
		updatedRegistrationCenter.setIsDeleted(true);
		List<RegistrationCenter> registrationCenterlist = new ArrayList<>();
		RegistrationCenter registrationCenter = new RegistrationCenter();
		registrationCenter.setName("TEST CENTER");
		registrationCenter.setAddressLine1("Address Line 1");
		registrationCenter.setAddressLine2("Address Line 2");
		registrationCenter.setAddressLine3("Address Line 3");
		registrationCenter.setCenterTypeCode("REG01");
		registrationCenter.setContactPerson("test");
		registrationCenter.setContactPhone("9999999999");
		registrationCenter.setHolidayLocationCode("HLC01");
		registrationCenter.setId("676");
		registrationCenter.setIsActive(true);
		registrationCenter.setLangCode("eng");
		registrationCenter.setLatitude("12.9646818");
		registrationCenter.setLocationCode("LOC01");
		registrationCenter.setLongitude("77.70168");
		registrationCenter.setNumberOfKiosks(numberOfKiosks);
		registrationCenter.setTimeZone("UTC");
		registrationCenter.setWorkingHours("9");
		requestDto.setRequest(registrationCenterDto);
		registrationCenterlist.add(registrationCenter);
		String contentJson = mapper.writeValueAsString(requestDto);
		RegistrationCenterHistory registrationCenterHistory = new RegistrationCenterHistory();
		when(registrationCenterRepository.findByRegIdAndIsDeletedFalseOrNull(Mockito.any()))
				.thenReturn(registrationCenterlist);

		when(deviceRepository
				.findByRegIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()))
						.thenReturn(new ArrayList<Device>());

		when(userRepository
				.findByRegIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()))
						.thenReturn(new ArrayList<UserDetails>());

		when(machineRepository.findByRegIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()))
				.thenReturn(new ArrayList<Machine>());

		when(registrationCenterRepository.update(Mockito.any())).thenReturn(registrationCenter);
		// TODO
		when(repositoryCenterHistoryRepository.create(Mockito.any())).thenReturn(registrationCenterHistory);
		mockMvc.perform(delete("/registrationcenters/676").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteRegistrationCenterDependecyTest() throws Exception {
		
		RequestWrapper<RegistrationCenterDto> requestDto = new RequestWrapper<>();
		short numberOfKiosks = 1;
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		RegistrationCenterDto registrationCenterDto = new RegistrationCenterDto();
		registrationCenterDto.setName("TEST CENTER");
		registrationCenterDto.setAddressLine1("Address Line 1");
		registrationCenterDto.setAddressLine2("Address Line 2");
		registrationCenterDto.setAddressLine3("Address Line 3");
		registrationCenterDto.setCenterTypeCode("REG01");
		registrationCenterDto.setContactPerson("global-admin");
		registrationCenterDto.setContactPhone("9999999999");
		registrationCenterDto.setHolidayLocationCode("HLC01");
		registrationCenterDto.setId("676");
		registrationCenterDto.setIsActive(true);
		registrationCenterDto.setLangCode("eng");
		registrationCenterDto.setLatitude("12.9646818");
		registrationCenterDto.setLocationCode("LOC01");
		registrationCenterDto.setLongitude("77.70168");
		registrationCenterDto.setNumberOfKiosks((short) 1);
		registrationCenterDto.setTimeZone("UTC");
		registrationCenterDto.setWorkingHours("9");
		RegistrationCenter updatedRegistrationCenter = new RegistrationCenter();
		updatedRegistrationCenter.setName("TEST CENTER");
		updatedRegistrationCenter.setAddressLine1("Address Line 1");
		updatedRegistrationCenter.setAddressLine2("Address Line 2");
		updatedRegistrationCenter.setAddressLine3("Address Line 3");
		updatedRegistrationCenter.setCenterTypeCode("REG01");
		updatedRegistrationCenter.setContactPerson("global-admin");
		updatedRegistrationCenter.setContactPhone("9999999999");
		updatedRegistrationCenter.setHolidayLocationCode("HLC01");
		updatedRegistrationCenter.setId("676");
		updatedRegistrationCenter.setIsActive(true);
		updatedRegistrationCenter.setLangCode("eng");
		updatedRegistrationCenter.setLatitude("12.9646818");
		updatedRegistrationCenter.setLocationCode("LOC01");
		updatedRegistrationCenter.setLongitude("77.70168");
		updatedRegistrationCenter.setNumberOfKiosks(numberOfKiosks);
		updatedRegistrationCenter.setTimeZone("UTC");
		updatedRegistrationCenter.setWorkingHours("9");
		updatedRegistrationCenter.setIsDeleted(true);
		List<RegistrationCenter> registrationCenterlist = new ArrayList<>();
		RegistrationCenter registrationCenter = new RegistrationCenter();
		registrationCenter.setName("TEST CENTER");
		registrationCenter.setAddressLine1("Address Line 1");
		registrationCenter.setAddressLine2("Address Line 2");
		registrationCenter.setAddressLine3("Address Line 3");
		registrationCenter.setCenterTypeCode("REG01");
		registrationCenter.setContactPerson("global-admin");
		registrationCenter.setContactPhone("9999999999");
		registrationCenter.setHolidayLocationCode("HLC01");
		registrationCenter.setId("676");
		registrationCenter.setIsActive(true);
		registrationCenter.setLangCode("eng");
		registrationCenter.setLatitude("12.9646818");
		registrationCenter.setLocationCode("LOC01");
		registrationCenter.setLongitude("77.70168");
		registrationCenter.setNumberOfKiosks(numberOfKiosks);
		registrationCenter.setTimeZone("UTC");
		registrationCenter.setWorkingHours("9");
		registrationCenterlist.add(registrationCenter);
		requestDto.setRequest(registrationCenterDto);
		String contentJson = mapper.writeValueAsString(requestDto);
		when(registrationCenterRepository.findByRegIdAndIsDeletedFalseOrNull(Mockito.any()))
				.thenReturn(registrationCenterlist);
		when(deviceRepository
				.findByRegIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()))
						.thenReturn(Arrays.asList(new Device()));

		when(userRepository
				.findByRegIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()))
						.thenReturn(new ArrayList<UserDetails>());

		when(machineRepository.findByRegIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()))
				.thenReturn(new ArrayList<Machine>());
		when(registrationCenterRepository.update(Mockito.any())).thenReturn(registrationCenter);
		mockMvc.perform(delete("/registrationcenters/676").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteRegistrationCenterTestForRequestException() throws Exception {
		List<RegistrationCenter> registrationCenterlist = new ArrayList<>();
		when(registrationCenterRepository.findByRegIdAndIsDeletedFalseOrNull(Mockito.any()))
				.thenReturn(registrationCenterlist);
		mockMvc.perform(delete("/registrationcenters/12")).andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void deleteRegistrationCenterDataAccessExceptionTest() throws Exception {
		List<RegistrationCenter> registrationCenterlist = new ArrayList<>();
		RegistrationCenter registrationCenter = new RegistrationCenter();
		registrationCenter.setName("TEST CENTER");
		registrationCenter.setAddressLine1("Address Line 1");
		registrationCenter.setAddressLine2("Address Line 2");
		registrationCenter.setAddressLine3("Address Line 3");
		registrationCenter.setCenterTypeCode("REG01");
		registrationCenter.setContactPerson("global-admin");
		registrationCenter.setContactPhone("9999999999");
		registrationCenter.setHolidayLocationCode("HLC01");
		registrationCenter.setId("676");
		registrationCenter.setIsActive(true);
		registrationCenter.setLangCode("eng");
		registrationCenter.setLatitude("12.9646818");
		registrationCenter.setLocationCode("LOC01");
		registrationCenter.setLongitude("77.70168");
		registrationCenter.setTimeZone("UTC");
		registrationCenter.setWorkingHours("9");
		registrationCenterlist.add(registrationCenter);
		when(registrationCenterRepository.findByRegIdAndIsDeletedFalseOrNull(Mockito.any()))
				.thenReturn(registrationCenterlist);
		when(deviceRepository
				.findByRegIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()))
						.thenReturn(new ArrayList<Device>());

		when(userRepository
				.findByRegIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()))
						.thenReturn(new ArrayList<UserDetails>());

		when(machineRepository.findByRegIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()))
				.thenReturn(new ArrayList<Machine>());

		when(registrationCenterRepository.update(Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot execute statement", null));
		mockMvc.perform(delete("/registrationcenters/12").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError());

	}

	@Test
	@WithUserDetails("zonal-admin")
	public void createRegistrationCenterTestInvalidLatLongFormatTest() throws Exception {
		RequestWrapper<RegistrationCenterDto> requestDto = new RequestWrapper<>();
		short numberOfKiosks = 1;
		requestDto.setId("mosip.idtype.create");
		requestDto.setVersion("1.0");
		RegistrationCenterDto registrationCenterDto = new RegistrationCenterDto();
		registrationCenterDto.setName("TEST CENTER");
		registrationCenterDto.setAddressLine1("Address Line 1");
		registrationCenterDto.setAddressLine2("Address Line 2");
		registrationCenterDto.setAddressLine3("Address Line 3");
		registrationCenterDto.setCenterTypeCode("REG01");
		registrationCenterDto.setContactPerson("global-admin");
		registrationCenterDto.setContactPhone("9999999999");
		registrationCenterDto.setHolidayLocationCode("HLC01");
		registrationCenterDto.setId("676");
		registrationCenterDto.setIsActive(true);
		registrationCenterDto.setLangCode("eng");
		registrationCenterDto.setLatitude("INVALID");
		registrationCenterDto.setLocationCode("INVALID");
		registrationCenterDto.setLongitude("77.70168");
		registrationCenterDto.setNumberOfKiosks((short) 1);
		registrationCenterDto.setTimeZone("UTC");
		registrationCenterDto.setWorkingHours("9");
		RegistrationCenter registrationCenter = new RegistrationCenter();
		registrationCenter.setName("TEST CENTER");
		registrationCenter.setAddressLine1("Address Line 1");
		registrationCenter.setAddressLine2("Address Line 2");
		registrationCenter.setAddressLine3("Address Line 3");
		registrationCenter.setCenterTypeCode("REG01");
		registrationCenter.setContactPerson("global-admin");
		registrationCenter.setContactPhone("9999999999");
		registrationCenter.setHolidayLocationCode("HLC01");
		registrationCenter.setId("676");
		registrationCenter.setIsActive(true);
		registrationCenter.setLangCode("eng");
		registrationCenter.setLatitude("12.9646818");
		registrationCenter.setLocationCode("LOC01");
		registrationCenter.setLongitude("77.70168");
		registrationCenter.setNumberOfKiosks(numberOfKiosks);
		registrationCenter.setTimeZone("UTC");
		registrationCenter.setWorkingHours("9");
		requestDto.setRequest(registrationCenterDto);
		String contentJson = mapper.writeValueAsString(requestDto);
		when(registrationCenterRepository.create(Mockito.any())).thenThrow(new RequestException("", ""));
		mockMvc.perform(post("/registrationcenters").contentType(MediaType.APPLICATION_JSON).content(contentJson))
				.andExpect(status().isOk());
	}

	// -----------------------------------genderNameValidationTest-------------------//

	@WithUserDetails("reg-processor")
	@Test
	public void validateGenderNameInvalidTest() throws Exception {
		Mockito.when(genderTypeRepository.isGenderNamePresent(Mockito.anyString())).thenReturn(false);
		mockMvc.perform(get("/gendertypes/validate/others")).andExpect(status().isOk()).andReturn();

	}

	@WithUserDetails("reg-processor")
	@Test
	public void validateGenderNameValid() throws Exception {
		Mockito.when(genderTypeRepository.isGenderNamePresent(Mockito.anyString())).thenReturn(true);
		mockMvc.perform(get("/gendertypes/validate/male")).andExpect(status().isOk()).andReturn();

	}

	@WithUserDetails("individual")
	@Test()
	public void validateGenderNameException() throws Exception {
		Mockito.when(genderTypeRepository.isGenderNamePresent(Mockito.anyString()))
				.thenThrow(DataRetrievalFailureException.class);
		mockMvc.perform(get("/gendertypes/validate/male")).andExpect(status().is5xxServerError());

	}

	// ----------------------------------------------------location------------------------------//

	@WithUserDetails("reg-processor")
	@Test
	public void validateLocationNameInvalidTest() throws Exception {
		Mockito.when(locationRepository.isLocationNamePresent(Mockito.anyString())).thenReturn(false);
		mockMvc.perform(get("/locations/validate/MyCountry")).andExpect(status().isOk()).andReturn();

	}

	@WithUserDetails("reg-processor")
	@Test
	public void validateLocationNameValidTest() throws Exception {
		Mockito.when(locationRepository.isLocationNamePresent(Mockito.anyString())).thenReturn(true);
		mockMvc.perform(get("/locations/validate/MyCountry")).andExpect(status().isOk()).andReturn();

	}

	@WithUserDetails("reg-processor")
	@Test()
	public void validateLocationNameExceptionTest() throws Exception {
		Mockito.when(locationRepository.isLocationNamePresent(Mockito.anyString()))
				.thenThrow(DataRetrievalFailureException.class);
		mockMvc.perform(get("/locations/validate/MyCountry")).andExpect(status().is5xxServerError());

	}

	@Test
	@WithUserDetails("reg-processor")
	public void getUserDetailHistoryByIdTest() throws Exception {

		when(userDetailsRepository.getByUserIdAndTimestamp(Mockito.anyString(), Mockito.any())).thenReturn(users);
		mockMvc.perform(get("/users/110001/2018-01-01T10:10:30.956Z")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("reg-processor")
	public void getUserDetailHistoryByIdNotFoundExceptionTest() throws Exception {
		when(userDetailsRepository.getByUserIdAndTimestamp("110001", localDateTimeUTCFormat)).thenReturn(null);
		mockMvc.perform(
				get("/users/110001/".concat(UTC_DATE_TIME_FORMAT_DATE_STRING)).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();
	}

	@Test
	@WithUserDetails("reg-processor")
	public void getUserDetailHistoryByIdEmptyExceptionTest() throws Exception {
		when(userDetailsRepository.getByUserIdAndTimestamp("11001", localDateTimeUTCFormat))
				.thenReturn(new ArrayList<UserDetailsHistory>());
		mockMvc.perform(
				get("/users/110001/".concat(UTC_DATE_TIME_FORMAT_DATE_STRING)).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();
	}

	@Test
	@WithUserDetails("reg-admin")
	public void getUserDetailHistoryByIdFetchExceptionTest() throws Exception {
		when(userDetailsRepository.getByUserIdAndTimestamp(Mockito.anyString(), Mockito.any()))
				.thenThrow(DataRetrievalFailureException.class);
		mockMvc.perform(get("/users/110001/2018-01-01T10:10:30.956Z")).andExpect(status().isInternalServerError());
	}

	// -----------------------createRegistrationCenter
	// TestCase------------------------

	@Test
	@WithUserDetails("zonal-admin")
	public void createRegCenterAdminTest() throws Exception {
		String content = objectMapper.writeValueAsString(regPostRequest);
		Zone zone = new Zone();
		zone.setCode("JRD");
		List<Zone> zones = new ArrayList<>();
		zones.add(zone);
		when(zoneUtils.getUserZones()).thenReturn(zones);
		when(registrationCenterRepository.create(Mockito.any())).thenReturn(registrationCenter1);
		when(repositoryCenterHistoryRepository.create(Mockito.any())).thenReturn(registrationCenterHistory);
		mockMvc.perform(post("/registrationcenters").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void createRegCenterAdminDataExcpTest() throws Exception {
		String content = objectMapper.writeValueAsString(regPostRequest);
		Zone zone = new Zone();
		zone.setCode("JRD");
		List<Zone> zones = new ArrayList<>();
		zones.add(zone);
		when(zoneUtils.getUserZones()).thenReturn(zones);
		when(registrationCenterRepository.create(Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot execute statement", null));
		mockMvc.perform(post("/registrationcenters").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().is2xxSuccessful());
	}

	// ------------------------------------------------RegistrationMachine Get
	// ------------------------------------------------
	@Test
	@WithUserDetails("zonal-admin")
	public void getMachineRegistrationCenterMappingSuccessTest() throws Exception {
		String page = "0";
		String size = "2";
		String orderBy = "id";
		String direction = "ASC";
		Machine machine = new Machine();
		machine.setId("10001");
		machine.setName("laptop");
		machine.setMachineSpecId("10001");
		machine.setIsActive(true);
		machine.setIpAddress("102.0.0.0");
		List<Machine> machinelist = new ArrayList<>();
		machinelist.add(machine);
		Page<Machine> pageEntity = new PageImpl<>(machinelist);

		when(machineRepository.findMachineByRegCenterId(Mockito.anyString(), Mockito.any())).thenReturn(pageEntity);
		mockMvc.perform(get("/machines/mappedmachines/{regCenterId}", "10001").param("page", page).param("size", size)
				.param("orderBy", orderBy).param("direction", direction)).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void getMachineRegistrationCenterMappingNullResponseTest() throws Exception {
		String page = "0";
		String size = "2";
		String orderBy = "id";
		String direction = "ASC";
		when(machineRepository.findMachineByRegCenterId(Mockito.anyString(), Mockito.any())).thenReturn(null);
		mockMvc.perform(get("/machines/mappedmachines/{regCenterId}", "10001").param("page", page).param("size", size)
				.param("orderBy", orderBy).param("direction", direction)).andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("zonal-admin")
	public void getMachineRegistrationCenterMappingFetchExceptionTest() throws Exception {
		String page = "0";
		String size = "2";
		String orderBy = "id";
		String direction = "ASC";
		when(machineRepository.findMachineByRegCenterId(Mockito.anyString(), Mockito.any()))
				.thenThrow(DataRetrievalFailureException.class);
		mockMvc.perform(get("/machines/mappedmachines/{regCenterId}", "10001").param("page", page).param("size", size)
				.param("orderBy", orderBy).param("direction", direction)).andExpect(status().isInternalServerError());

	}

	// ------------------------------------------------Get Devices mapped with given
	// Registration center ------------------------------------------------
	@Test
	@WithUserDetails("zonal-admin")
	public void getDeviceRegistrationCenterMappingSuccessTest() throws Exception {
		String page = "0";
		String size = "2";
		String orderBy = "id";
		String direction = "ASC";
		when(deviceRepository.findDeviceByRegCenterId(Mockito.anyString(), Mockito.any())).thenReturn(pageDeviceEntity);
		mockMvc.perform(get("/devices/mappeddevices/{regCenterId}", "10001").param("page", page).param("size", size)
				.param("orderBy", orderBy).param("direction", direction)).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void getDeviceRegistrationCenterMappingNullResponseTest() throws Exception {
		String page = "0";
		String size = "2";
		String orderBy = "id";
		String direction = "ASC";
		when(deviceRepository.findDeviceByRegCenterId(Mockito.anyString(), Mockito.any())).thenReturn(null);
		mockMvc.perform(get("/devices/mappeddevices/{regCenterId}", "10001").param("page", page).param("size", size)
				.param("orderBy", orderBy).param("direction", direction)).andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("zonal-admin")
	public void getDeviceRegistrationCenterMappingFetchExceptionTest() throws Exception {
		String page = "0";
		String size = "2";
		String orderBy = "id";
		String direction = "ASC";
		when(deviceRepository.findDeviceByRegCenterId(Mockito.anyString(), Mockito.any()))
				.thenThrow(DataRetrievalFailureException.class);
		mockMvc.perform(get("/devices/mappeddevices/{regCenterId}", "10001").param("page", page).param("size", size)
				.param("orderBy", orderBy).param("direction", direction)).andExpect(status().isInternalServerError());

	}

	/* get all document type by lang code test cases */
	@Test
	@WithUserDetails("zonal-admin")
	public void getAllDocumetTypeByLangCode() throws Exception {
		DocumentType documentType = new DocumentType();
		documentType.setCode("RNC");
		documentType.setName("Rental contract");
		documentType.setDescription("Rental Agreement of address");
		documentType.setLangCode("eng");
		documentType.setIsActive(true);

		List<DocumentType> documentTypes = new ArrayList<>();
		documentTypes.add(documentType);
		when(documentTypeRepository.findAllByLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()))
				.thenReturn(documentTypes);
		mockMvc.perform(MockMvcRequestBuilders.get("/documenttypes/{langcode}", "eng"))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void getAllDocumetTypeByLangCodeNotFoundException() throws Exception {
		List<DocumentType> docuemtnsReturns = new ArrayList<DocumentType>();
		when(documentTypeRepository.findAllByLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()))
				.thenReturn(docuemtnsReturns);
		mockMvc.perform(MockMvcRequestBuilders.get("/documenttypes/{langcode}", "eng"))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void testDocumentTypeFetchException() throws Exception {
		when(documentTypeRepository.findAllByLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()))
				.thenThrow(DataRetrievalFailureException.class);
		mockMvc.perform(MockMvcRequestBuilders.get("/documenttypes/{langcode}", "eng"))
				.andExpect(MockMvcResultMatchers.status().isInternalServerError());
	}
	/*----------------------------------end-----------------------------------*/
	// ------ decommission regCenter---------------------------------------

	private List<Zone> userZones;
	private RegistrationCenter regCenterZoneDecom;

	private void decommissionRegCenter() {
		regCenterZoneDecom = new RegistrationCenter();
		regCenterZoneDecom.setId("10001");
		regCenterZoneDecom.setZoneCode("MOR");
		userZones = new ArrayList<>();
		Zone zone = new Zone("MOR", "eng", "Berkane", (short) 0, "Province", "MOR", " ");
		userZones.add(zone);
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void decommissionRegCenterInvalidLength() throws Exception {
		mockMvc.perform(put("/registrationcenters/decommission/100000101").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$.errors.[0].errorCode", isA(String.class)));
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void decommissionRegCenterSuccessTest() throws Exception {
		
		when(zoneUtils.getUserZones()).thenReturn(userZones);
		when(registrationCenterRepository.findByLangCodeAndId(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(regCenterZoneDecom);
		when(machineRepository.findByRegIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()))
				.thenReturn(new ArrayList<Machine>());
		when(deviceRepository.findByRegIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()))
				.thenReturn(new ArrayList<Device>());
		when(userRepository.findByRegIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()))
				.thenReturn(new ArrayList<UserDetails>());
		when(registrationCenterRepository.findByRegIdAndIsDeletedFalseOrNull(Mockito.anyString()))
				.thenReturn(registrationCenters);
		when(registrationCenterRepository.decommissionRegCenter(Mockito.anyString(), Mockito.anyString(),
				Mockito.any())).thenReturn(1);
		mockMvc.perform(put("/registrationcenters/decommission/10001").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void decommissionRegCenterNotFoundTest() throws Exception {
		
		List<RegistrationCenter> regCenterList = new ArrayList<>();
		when(zoneUtils.getUserZones()).thenReturn(userZones);
		when(registrationCenterRepository.findByLangCodeAndId(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(regCenterZoneDecom);
		when(machineRepository.findByRegIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()))
			.thenReturn(new ArrayList<Machine>());
		when(deviceRepository.findByRegIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()))
			.thenReturn(new ArrayList<Device>());
		when(userRepository.findByRegIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()))
			.thenReturn(new ArrayList<UserDetails>());
		when(registrationCenterRepository.findByRegIdAndIsDeletedFalseOrNull(Mockito.anyString()))
				.thenReturn(regCenterList);
		mockMvc.perform(put("/registrationcenters/decommission/10001").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void decommissionRegCenterInternalServerErrorTest() throws Exception {
		when(zoneUtils.getUserZones()).thenReturn(userZones);
		when(registrationCenterRepository.findByLangCodeAndId(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(regCenterZoneDecom);
		when(userRepository.findByRegIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()))
				.thenThrow(new DataAccessLayerException("KER-MSD-354", "Internal Server Error", null));
		mockMvc.perform(put("/registrationcenters/decommission/10001").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError());
	}
	
	@Test
	@WithUserDetails("zonal-admin")
	public void decommissionRegCenterNotFoundTest2() throws Exception {
		when(zoneUtils.getUserZones()).thenReturn(userZones);
		when(registrationCenterRepository.findByLangCodeAndId(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(null);
		mockMvc.perform(put("/registrationcenters/decommission/10001").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}
	@Test
	@WithUserDetails("zonal-admin")
	public void decommissionRegCenterZoneNotSame() throws Exception {
		regCenterZoneDecom.setZoneCode("BDR");
		when(zoneUtils.getUserZones()).thenReturn(userZones);
		when(registrationCenterRepository.findByLangCodeAndId(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(regCenterZoneDecom);
		mockMvc.perform(put("/registrationcenters/decommission/10001").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void decommissionRegCenterMappedMachineTest() throws Exception {
		List<Machine> regCenterMachineMappings = new ArrayList<>();
		regCenterMachineMappings.add(new Machine());
		when(zoneUtils.getUserZones()).thenReturn(userZones);
		when(registrationCenterRepository.findByLangCodeAndId(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(regCenterZoneDecom);
		when(machineRepository.findByRegIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()))
			.thenReturn(regCenterMachineMappings);
		when(deviceRepository.findByRegIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()))
			.thenReturn(new ArrayList<Device>());
		when(userRepository.findByRegIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()))
			.thenReturn(new ArrayList<UserDetails>());
		mockMvc.perform(put("/registrationcenters/decommission/10001").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void decommissionRegCenterMappedDeviceTest() throws Exception {
		

		List<Device> devices = new ArrayList<>();
		devices.add(new Device());
		when(zoneUtils.getUserZones()).thenReturn(userZones);
		when(registrationCenterRepository.findByLangCodeAndId(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(regCenterZoneDecom);
		when(machineRepository.findByRegIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()))
			.thenReturn(new ArrayList<Machine>());
		when(deviceRepository.findByRegIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()))
			.thenReturn(devices);
		when(userRepository.findByRegIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()))
			.thenReturn(new ArrayList<UserDetails>());
		mockMvc.perform(put("/registrationcenters/decommission/10001").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void decommissionRegCenterMappedUserTest() throws Exception {
		
		UserDetails registrationCenterUser = new UserDetails();
		registrationCenterUser.setId("1001");
		
		when(zoneUtils.getUserZones()).thenReturn(userZones);
		when(registrationCenterRepository.findByLangCodeAndId(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(regCenterZoneDecom);
		when(userRepository.findByRegIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()))
				.thenReturn(Arrays.asList(registrationCenterUser));
		mockMvc.perform(put("/registrationcenters/decommission/10001").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void testMapDocCategoryAndDocType() throws Exception {
		Mockito.when(
				validDocumentRepository.findByDocCategoryCodeAndDocTypeCode(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(new ValidDocument());
		mockMvc.perform(put("/validdocuments/map/POE/CIN")).andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void testUnmapDocCategoryAndDocType() throws Exception {
		Mockito.when(
				validDocumentRepository.findByDocCategoryCodeAndDocTypeCode(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(new ValidDocument());
		mockMvc.perform(MockMvcRequestBuilders.put("/validdocuments/unmap/POE/CIN"))
				.andExpect(MockMvcResultMatchers.status().isInternalServerError());
	}

	private List<Zone> getZones() {
		Zone zone = new Zone();
		zone.setCode("JRD");
		zone.setLangCode(primaryLang);
		zone.setHierarchyPath("MOR/NTH/ORT/JRD");
		Zone zone1 = new Zone();
		zone1.setCode("TZT");
		zone1.setLangCode(primaryLang);
		zone1.setHierarchyPath("MOR/STH/SOS/TZT");
		Zone zone2 = new Zone();
		zone2.setCode("TTA");
		zone2.setLangCode(primaryLang);
		zone2.setHierarchyPath("MOR/STH/SOS/TTA");
		Zone zone3 = new Zone();
		zone3.setCode("BRT");
		zone3.setLangCode(primaryLang);
		zone3.setHierarchyPath("MOR/NTH/ORT/BRK");
		Zone zone4 = new Zone();
		zone4.setCode("CST");
		zone4.setLangCode(primaryLang);
		zone4.setHierarchyPath("MOR/NTH/ORT/CST");
		Zone zone6 = new Zone();
		zone6.setCode("NTH");
		zone6.setLangCode(primaryLang);
		zone6.setHierarchyPath("MOR/NTH");
		Zone zone5 = new Zone();
		zone5.setCode("NTH");
		zone5.setLangCode(primaryLang);
		zone5.setHierarchyPath("MOR/STH");

		return Arrays.asList(zone, zone1, zone2, zone3, zone4, zone5, zone6);
	}

	// ---------------------------create
	// Machine-----------------------------------------------------

	private MachinePostReqDto reqPostMachine = null;
	private Machine machineEntity = null;
	private List<Zone> zonesMachines;
	private List<Zone> zonesInvalide;
	private MachinePostReqDto inValideMID = null;
	private MachinePostReqDto inValideLang = null;

	private void createMachineSetUp() {

		specificDate = LocalDateTime.now(ZoneId.of("UTC"));
		reqPostMachine = new MachinePostReqDto();
		reqPostMachine.setLangCode("eng");
		reqPostMachine.setName("HP");
		reqPostMachine.setIpAddress("129.0.0.0");
		reqPostMachine.setMacAddress("178.0.0.0");
		reqPostMachine.setMachineSpecId("1010");
		reqPostMachine.setSerialNum("123");
		reqPostMachine.setIsActive(true);
		reqPostMachine.setZoneCode("MOR");
		reqPostMachine.setPublicKey("testpublic");
		reqPostMachine.setRegCenterId("10001");
		// machine.setValidityDateTime(specificDate);
		// machineList.add(machine);

		machineEntity = new Machine();
		machineEntity.setId("10001");

		machineHistory = new MachineHistory();

		/*
		 * MapperUtils.mapFieldValues(machine, machineHistory); machineDto = new
		 * MachineDto(); MapperUtils.map(machine, machineDto);
		 */

		zonesMachines = new ArrayList<>();
		Zone zone = new Zone("MOR", "eng", "Berkane", (short) 0, "Province", "MOR", " ");
		zonesMachines.add(zone);

		zonesInvalide = new ArrayList<>();
		Zone zoneInv = new Zone("NTR", "eng", "Berkane", (short) 0, "Province", "NTR", " ");
		zonesInvalide.add(zoneInv);
	}

	@MockBean
	RegistrationCenterValidator registrationCenterValidator;

	@MockBean
	MachineIdGenerator<String> machineIdGenerator;

	@MockBean
	MasterdataCreationUtil masterdataCreationUtil;

	@Test
	@WithUserDetails("zonal-admin")
	public void createMachineTest() throws Exception {
		RequestWrapper<MachinePostReqDto> requestDto;
		requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.match.regcentr.machineid");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(reqPostMachine);

		machineJson = mapper.writeValueAsString(requestDto);
		when(registrationCenterRepository.findByIdAndIsDeletedFalseOrNull(Mockito.any())).thenReturn(Arrays.asList(new RegistrationCenter() ));
		RegistrationCenter center=new RegistrationCenter();
		center.setZoneCode("MOR");
		when(registrationCenterRepository.findByRegIdAndLangCode(Mockito.any(),Mockito.any())).thenReturn(Arrays.asList(new RegistrationCenter() ));
		when(zoneUtils.getUserZones()).thenReturn(zonesMachines);
		when(masterdataCreationUtil.createMasterData(Machine.class, reqPostMachine)).thenReturn(reqPostMachine);
		when(registrationCenterValidator.generateMachineIdOrvalidateWithDB(Mockito.any())).thenReturn("10001");
		when(machineRepository.create(Mockito.any())).thenReturn(machineEntity);
		when(machineHistoryRepository.create(Mockito.any())).thenReturn(machineHistory);
		mockMvc.perform(post("/machines").contentType(MediaType.APPLICATION_JSON).content(machineJson))
				.andExpect(status().isOk());
	}
	
	@Test
	@WithUserDetails("zonal-admin")
	public void createMachineinvalidCenterTest() throws Exception {
		RequestWrapper<MachinePostReqDto> requestDto;
		requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.match.regcentr.machineid");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(reqPostMachine);

		machineJson = mapper.writeValueAsString(requestDto);
		when(registrationCenterRepository.findByIdAndIsDeletedFalseOrNull(Mockito.any())).thenReturn(Arrays.asList());
		when(registrationCenterRepository.findByRegIdAndZone(Mockito.any(),Mockito.any())).thenReturn(Arrays.asList(new RegistrationCenter() ));
		when(zoneUtils.getUserZones()).thenReturn(zonesMachines);
		when(masterdataCreationUtil.createMasterData(Machine.class, reqPostMachine)).thenReturn(reqPostMachine);
		when(registrationCenterValidator.generateMachineIdOrvalidateWithDB(Mockito.any())).thenReturn("10001");
		when(machineRepository.create(Mockito.any())).thenReturn(machineEntity);
		when(machineHistoryRepository.create(Mockito.any())).thenReturn(machineHistory);
		mockMvc.perform(post("/machines").contentType(MediaType.APPLICATION_JSON).content(machineJson))
				.andExpect(status().isOk());
	}
	
	@Test
	@WithUserDetails("zonal-admin")
	public void createMachineInavalidCenterZoneTest() throws Exception {
		RequestWrapper<MachinePostReqDto> requestDto;
		requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.match.regcentr.machineid");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(reqPostMachine);

		machineJson = mapper.writeValueAsString(requestDto);
		when(registrationCenterRepository.findByIdAndIsDeletedFalseOrNull(Mockito.any())).thenReturn(Arrays.asList(new RegistrationCenter() ));
		RegistrationCenter center=new RegistrationCenter();
		center.setZoneCode("MDR");
		when(registrationCenterRepository.findByRegIdAndLangCode(Mockito.any(),Mockito.any())).thenReturn(Arrays.asList(center));
		when(zoneUtils.getUserZones()).thenReturn(zonesMachines);
		when(masterdataCreationUtil.createMasterData(Machine.class, reqPostMachine)).thenReturn(reqPostMachine);
		when(registrationCenterValidator.generateMachineIdOrvalidateWithDB(Mockito.any())).thenReturn("10001");
		when(machineRepository.create(Mockito.any())).thenReturn(machineEntity);
		when(machineHistoryRepository.create(Mockito.any())).thenReturn(machineHistory);
		mockMvc.perform(post("/machines").contentType(MediaType.APPLICATION_JSON).content(machineJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void createMachineExceptionTest() throws Exception {
		RequestWrapper<MachinePostReqDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.Machine.create");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(reqPostMachine);
		String content = mapper.writeValueAsString(requestDto);
		when(registrationCenterRepository.findByIdAndIsDeletedFalseOrNull(Mockito.any())).thenReturn(Arrays.asList(new RegistrationCenter() ));
		when(registrationCenterRepository.findByRegIdAndLangCode(Mockito.any(),Mockito.any())).thenReturn(Arrays.asList(new RegistrationCenter() ));
		
		when(zoneUtils.getUserZones()).thenReturn(zonesMachines);
		when(masterdataCreationUtil.createMasterData(Machine.class, reqPostMachine)).thenReturn(reqPostMachine);
		when(registrationCenterValidator.generateMachineIdOrvalidateWithDB(Mockito.any())).thenReturn("10001");
		Mockito.when(machineRepository.create(Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot insert", null));
		
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.post("/machines").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().isOk()).andReturn();
		
		ResponseWrapper<?> responseWrapper = objectMapper.readValue(result.getResponse().getContentAsString(),
				ResponseWrapper.class);

		assertNotNull(responseWrapper.getErrors());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void createMachineTestZoneValidation() throws Exception {
		RequestWrapper<MachinePostReqDto> requestDto;
		requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.match.regcentr.machineid");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(reqPostMachine);

		machineJson = mapper.writeValueAsString(requestDto);

		when(zoneUtils.getUserZones()).thenReturn(zonesInvalide);
		mockMvc.perform(post("/machines").contentType(MediaType.APPLICATION_JSON).content(machineJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void createMachineLanguageCodeValidatorTest() throws Exception {
		RequestWrapper<MachinePostReqDto> requestDto;
		requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.match.regcentr.machineid");
		requestDto.setVersion("1.0.0");

		inValideLang = new MachinePostReqDto();
		inValideLang.setId("10001");
		inValideLang.setLangCode("xxx");
		inValideLang.setName("HP");
		inValideLang.setIpAddress("129.0.0.0");
		inValideLang.setMacAddress("178.0.0.0");
		inValideLang.setMachineSpecId("1010");
		inValideLang.setSerialNum("123");
		inValideLang.setIsActive(true);
		requestDto.setRequest(inValideLang);

		machineJson = mapper.writeValueAsString(requestDto);
		when(registrationCenterRepository.findByIdAndIsDeletedFalseOrNull(Mockito.any())).thenReturn(Arrays.asList(new RegistrationCenter() ));
		when(registrationCenterRepository.findByRegIdAndZone(Mockito.any(),Mockito.any())).thenReturn(Arrays.asList(new RegistrationCenter() ));
		
		when(zoneUtils.getUserZones()).thenReturn(zonesMachines);
		when(masterdataCreationUtil.createMasterData(Machine.class, reqPostMachine)).thenReturn(reqPostMachine);
		when(registrationCenterValidator.generateMachineIdOrvalidateWithDB(Mockito.any())).thenReturn("10001");
		when(machineRepository.create(Mockito.any())).thenReturn(machineEntity);
		when(machineHistoryRepository.create(Mockito.any())).thenReturn(machineHistory);
		mockMvc.perform(post("/machines").contentType(MediaType.APPLICATION_JSON).content(machineJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void createMachineTestInvalidID() throws Exception {
		RequestWrapper<MachinePostReqDto> requestDto;
		requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.match.regcentr.machineid");
		requestDto.setVersion("1.0.0");
		inValideMID = new MachinePostReqDto();
		inValideMID.setId("1000ddfagsdgfadsfdgdsagdsagdsagdagagagdsgagadgagdf");
		inValideMID.setLangCode("eng");
		inValideMID.setName("HP");
		inValideMID.setIpAddress("129.0.0.0");
		inValideMID.setMacAddress("178.0.0.0");
		inValideMID.setMachineSpecId("1010");
		inValideMID.setSerialNum("123");
		inValideMID.setIsActive(true);

		requestDto.setRequest(inValideMID);
		machineJson = mapper.writeValueAsString(requestDto);

		mockMvc.perform(post("/machines").contentType(MediaType.APPLICATION_JSON).content(machineJson))
				.andExpect(status().isOk());
	}

	// ---------------------------- update
	// Machine-----------------------------------------------------
	private MachinePutReqDto machinePutReqDto = null;
	private Machine updMachine = null;
	
	private List<RegistrationCenter> renRegistrationCenters = new ArrayList<>();
	
	private RegistrationCenter renRegCenter;
	private String updateMachinecontent;
	private String updateMachineInValideLang;
	private MachinePutReqDto inValideMacLang;

	public void updateMachine() {
		machinePutReqDto = new MachinePutReqDto();
		machinePutReqDto.setId("10001");
		machinePutReqDto.setName("Laptop");
		machinePutReqDto.setLangCode("eng");
		machinePutReqDto.setZoneCode("MOR");
		machinePutReqDto.setName("HP");
		machinePutReqDto.setIpAddress("129.0.0.0");
		machinePutReqDto.setMacAddress("178.0.0.0");
		machinePutReqDto.setMachineSpecId("1010");
		machinePutReqDto.setSerialNum("123");
		machinePutReqDto.setRegCenterId("10001");
		machinePutReqDto.setPublicKey("testPublic");
		machinePutReqDto.setIsActive(true);

		updMachine = new Machine();
		updMachine.setId("10001");
		updMachine.setIsActive(false);
		updMachine.setRegCenterId("10001");
		

		renRegCenter = new RegistrationCenter();
		renRegCenter.setId("10001");
		renRegCenter.setNumberOfKiosks((short) 1);
		renRegistrationCenters.add(renRegCenter);

		registrationCenterHistory = new RegistrationCenterHistory();

		inValideMacLang = new MachinePutReqDto();
	}

	@Test
	@WithUserDetails("global-admin")
	public void updateMachineTest() throws Exception {

		RequestWrapper<MachinePutReqDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.machine.update");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(machinePutReqDto);
		updateMachinecontent = mapper.writeValueAsString(requestDto);
		when(registrationCenterRepository.findByIdAndIsDeletedFalseOrNull(Mockito.any())).thenReturn(Arrays.asList(new RegistrationCenter() ));
		when(registrationCenterRepository.findByRegIdAndLangCode(Mockito.any(),Mockito.any())).thenReturn(Arrays.asList(new RegistrationCenter() ));
		
		when(zoneUtils.getUserZones()).thenReturn(zonesMachines);
		when(machineRepository.findMachineByIdAndLangCodeAndIsDeletedFalseorIsDeletedIsNullWithoutActiveStatusCheck(
				Mockito.any(), Mockito.anyString())).thenReturn(updMachine);
		
		when(registrationCenterRepository.findByRegIdAndIsDeletedFalseOrNull(Mockito.any()))
				.thenReturn(renRegistrationCenters);
		when(registrationCenterRepository.update(Mockito.any())).thenReturn(renRegCenter);
		when(repositoryCenterHistoryRepository.create(Mockito.any())).thenReturn(registrationCenterHistory);
		when(masterdataCreationUtil.updateMasterData(Machine.class, machinePutReqDto)).thenReturn(machinePutReqDto);
		Mockito.when(machineRepository.update(Mockito.any())).thenReturn(updMachine);
		when(machineHistoryRepository.create(Mockito.any())).thenReturn(machineHistory);
		mockMvc.perform(MockMvcRequestBuilders.put("/machines").contentType(MediaType.APPLICATION_JSON)
				.content(updateMachinecontent)).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void updateMachineNotFoundTest() throws Exception {

		RequestWrapper<MachinePutReqDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.machine.update");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(machinePutReqDto);
		updateMachinecontent = mapper.writeValueAsString(requestDto);
		when(registrationCenterRepository.findByIdAndIsDeletedFalseOrNull(Mockito.any())).thenReturn(Arrays.asList(new RegistrationCenter() ));
		when(registrationCenterRepository.findByRegIdAndLangCode(Mockito.any(),Mockito.any())).thenReturn(Arrays.asList(new RegistrationCenter() ));
		
		when(zoneUtils.getUserZones()).thenReturn(zonesMachines);
		when(machineRepository.findMachineByIdAndLangCodeAndIsDeletedFalseorIsDeletedIsNullWithoutActiveStatusCheck(
				Mockito.any(), Mockito.anyString())).thenReturn(null);
		mockMvc.perform(MockMvcRequestBuilders.put("/machines").contentType(MediaType.APPLICATION_JSON)
				.content(updateMachinecontent)).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void updateMachineDataAccessExpTest() throws Exception {

		RequestWrapper<MachinePutReqDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.machine.update");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(machinePutReqDto);
		updateMachinecontent = mapper.writeValueAsString(requestDto);
		updMachine.setRegCenterId("10001");
		when(registrationCenterRepository.findByIdAndIsDeletedFalseOrNull(Mockito.any())).thenReturn(Arrays.asList(new RegistrationCenter() ));
		when(registrationCenterRepository.findByRegIdAndZone(Mockito.any(),Mockito.any())).thenReturn(Arrays.asList(new RegistrationCenter() ));
		
		when(zoneUtils.getUserZones()).thenReturn(zonesMachines);
		when(machineRepository.findMachineByIdAndLangCodeAndIsDeletedFalseorIsDeletedIsNullWithoutActiveStatusCheck(
				Mockito.any(), Mockito.anyString())).thenReturn(updMachine);
		
		when(registrationCenterRepository.findByRegIdAndIsDeletedFalseOrNull(Mockito.any()))
				.thenReturn(Arrays.asList(renRegCenter));
		when(registrationCenterRepository.update(Mockito.any())).thenReturn(renRegCenter);
		when(repositoryCenterHistoryRepository.create(Mockito.any())).thenReturn(registrationCenterHistory);
		when(masterdataCreationUtil.updateMasterData(Machine.class, machinePutReqDto)).thenReturn(machinePutReqDto);
		Mockito.when(machineRepository.update(Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot insert", null));
		mockMvc.perform(MockMvcRequestBuilders.put("/machines").contentType(MediaType.APPLICATION_JSON)
				.content(updateMachinecontent)).andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("global-admin")
	public void updateMachineLanguageCodeValidatorTest() throws Exception {

		RequestWrapper<MachinePutReqDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.machine.update");
		requestDto.setVersion("1.0.0");
		inValideMacLang.setLangCode("xxx");
		requestDto.setRequest(inValideMacLang);
		updateMachineInValideLang = mapper.writeValueAsString(requestDto);
		mockMvc.perform(MockMvcRequestBuilders.put("/machines").contentType(MediaType.APPLICATION_JSON)
				.content(updateMachineInValideLang)).andExpect(status().isOk());
	}

	// --------------------decommission
	// Machine------------------------------------------
	private MachineHistory decMachineHistory = null;
	private List<Machine> machines = null;
	

	public void decommissionMachineSetUp() {
		decMachineHistory = new MachineHistory();
		
		machines = new ArrayList<>();
		Machine machine = new Machine();
		machine.setId("10001");
		machine.setZoneCode("MOR");
		machines.add(machine);

	}

	@Test
	@WithUserDetails("global-admin")
	public void decommissionMachineTest() throws Exception {

		when(zoneUtils.getUserZones()).thenReturn(zonesMachines);
		when(machineRepository.findMachineByIdAndIsDeletedFalseorIsDeletedIsNullNoIsActive(Mockito.any()))
				.thenReturn(machines);
		
		when(machineRepository.decommissionMachine(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(1);
		when(machineHistoryRepository.create(Mockito.any())).thenReturn(decMachineHistory);
		mockMvc.perform(put("/machines/decommission/10001").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void decommissionMachineExceptionTest() throws Exception {
		when(zoneUtils.getUserZones()).thenReturn(zonesMachines);
		when(machineRepository.findMachineByIdAndIsDeletedFalseorIsDeletedIsNullNoIsActive(Mockito.any()))
				.thenReturn(machines);
		
		when(machineRepository.decommissionMachine(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenThrow(DataAccessLayerException.class);
		mockMvc.perform(put("/machines/decommission/10001").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError());

	}

	@Test
	@WithUserDetails("global-admin")
	public void decommissionMachineNotFoundTest() throws Exception {
		List<Machine> machines = new ArrayList<>();
		when(zoneUtils.getUserZones()).thenReturn(zonesMachines);
		when(machineRepository.findMachineByIdAndIsDeletedFalseorIsDeletedIsNullNoIsActive(Mockito.any()))
				.thenReturn(machines);
		mockMvc.perform(put("/machines/decommission/10001").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void decommissionMachineInvalideZoneTest() throws Exception {
		List<Machine> machines = new ArrayList<>();
		Machine machine = new Machine();
		machine.setId("10001");
		machine.setZoneCode("NTR");
		machines.add(machine);
		when(zoneUtils.getUserZones()).thenReturn(zonesMachines);
		when(machineRepository.findMachineByIdAndIsDeletedFalseorIsDeletedIsNullNoIsActive(Mockito.any()))
				.thenReturn(machines);
		mockMvc.perform(put("/machines/decommission/10001").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("global-admin")
	public void decommissionMachineRegCenterTest() throws Exception {
		Machine machine = new Machine();
		machine.setId("10001");
		machine.setZoneCode("MOR");
		machines.add(machine);
		when(zoneUtils.getUserZones()).thenReturn(zonesMachines);
		when(machineRepository.findMachineByIdAndIsDeletedFalseorIsDeletedIsNullNoIsActive(Mockito.any()))
				.thenReturn(machines);
		
		mockMvc.perform(put("/machines/decommission/10001").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

	}

	// --------------------------decommission device test-----------------
	List<Zone> zonesDevices = null;
	
	List<Device> decDevices = null;
	Device decDevice = null;
	DeviceHistory devHistory = new DeviceHistory();

	public void decommissionDeviceSetUp() {
		zonesDevices = new ArrayList<>();
		Zone zone = new Zone("MOR", "eng", "Berkane", (short) 0, "Province", "MOR", " ");
		zonesDevices.add(zone);

		
		decDevice = new Device();
		decDevice.setId("10001");
		decDevice.setZoneCode("JRD");
		
		decDevices = new ArrayList<>();
		decDevice.setZoneCode("MOR");
		decDevices.add(decDevice);
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void decommissionDeviceTest() throws Exception {
		when(zoneUtils.getUserZones()).thenReturn(zonesDevices);
		when(deviceRepository.findDeviceByIdAndIsDeletedFalseorIsDeletedIsNullNoIsActive(Mockito.any()))
				.thenReturn(decDevices);
		
		when(deviceRepository.decommissionDevice(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(1);
		when(deviceHistoryRepository.create(Mockito.any())).thenReturn(devHistory);
		mockMvc.perform(put("/devices/decommission/10001").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("zonal-admin")
	public void decommissionDeviceExceptionTest() throws Exception {
		when(zoneUtils.getUserZones()).thenReturn(zonesDevices);
		when(deviceRepository.findDeviceByIdAndIsDeletedFalseorIsDeletedIsNullNoIsActive(Mockito.any()))
				.thenReturn(decDevices);
		
		when(deviceRepository.decommissionDevice(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenThrow(DataAccessLayerException.class);
		mockMvc.perform(put("/devices/decommission/10001").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError());

	}

	/*
	 * @Test
	 * 
	 * @WithUserDetails("zonal-admin") public void decommissionDeviceNotFoundTest()
	 * throws Exception { List<Device> devices = new ArrayList<>();
	 * when(zoneUtils.getUserZones()).thenReturn(zonesDevices);
	 * when(deviceRepository.
	 * findDeviceByIdAndIsDeletedFalseorIsDeletedIsNullNoIsActive(Mockito.any()))
	 * .thenReturn(devices);
	 * mockMvc.perform(put("/devices/decommission/10001").contentType(MediaType.
	 * APPLICATION_JSON)) .andExpect(status().isOk());
	 * 
	 * }
	 */

	@Test
	@WithUserDetails("zonal-admin")
	public void decommissionDeviceInvalideZoneTest() throws Exception {
		List<Device> decDevices = new ArrayList<>();
		Device device = new Device();
		device.setId("10001");
		device.setZoneCode("NTR");
		decDevices.add(device);
		when(zoneUtils.getUserZones()).thenReturn(zonesDevices);
		when(deviceRepository.findDeviceByIdAndIsDeletedFalseorIsDeletedIsNullNoIsActive(Mockito.any()))
				.thenReturn(decDevices);
		;
		mockMvc.perform(put("/devices/decommission/10001").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("zonal-admin")
	public void decommissionDeviceRegCenterTest() throws Exception {
		List<Device> decDevices = new ArrayList<>();
		Device device = new Device();
		device.setId("10001");
		device.setRegCenterId("10001");
		device.setZoneCode("JRD");
		decDevices.add(device);
		when(zoneUtils.getUserZones()).thenReturn(zonesDevices);
		when(deviceRepository.findDeviceByIdAndIsDeletedFalseorIsDeletedIsNullNoIsActive(Mockito.any()))
				.thenReturn(decDevices);
		
		mockMvc.perform(put("/devices/decommission/10001").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

	}

	// ---------------------------create MDS--------------------------------------

	private MOSIPDeviceServiceDto mosipDeviceServiceDto = null;
	private MOSIPDeviceServiceHistory msdHistory = null;
	private MOSIPDeviceService mosipDeviceService = null;
	private RegistrationDeviceType regDeviceType = null;
	private RegistrationDeviceSubType regDeviceSubType = null;
	private DeviceProvider deviceProvider = null;
	private String mdsJson = null;

	private void MSDcreateSetUp() {

		byte[] binary = { 1 };
		specificDate = LocalDateTime.now(ZoneId.of("UTC"));
		mosipDeviceServiceDto = new MOSIPDeviceServiceDto();
		/* mosipDeviceServiceDto.setId("10002"); */
		mosipDeviceServiceDto.setSwVersion("0.1v");
		mosipDeviceServiceDto.setMake("make");
		mosipDeviceServiceDto.setModel("model");
		mosipDeviceServiceDto.setRegDeviceSubCode("10001");
		mosipDeviceServiceDto.setRegDeviceTypeCode("10003");
		mosipDeviceServiceDto.setDeviceProviderId("10003");
		mosipDeviceServiceDto.setSwBinaryHash("test");
		mosipDeviceServiceDto.setIsActive(true);

		mosipDeviceService = new MOSIPDeviceService();
		mosipDeviceService.setId("10002");
		msdHistory = new MOSIPDeviceServiceHistory();

		regDeviceType = new RegistrationDeviceType();
		regDeviceType.setCode("10003");

		regDeviceSubType = new RegistrationDeviceSubType();
		regDeviceSubType.setCode("10003");

		deviceProvider = new DeviceProvider();
		deviceProvider.setId("10003");
	}

	@MockBean
	MOSIPDeviceServiceRepository mosipDeviceServiceRepository;

	@MockBean
	MOSIPDeviceServiceHistoryRepository mosipDeviceServiceHistoryRepository;

	@MockBean
	RegistrationDeviceTypeRepository registrationDeviceTypeRepository;

	@MockBean
	RegistrationDeviceSubTypeRepository registrationDeviceSubTypeRepository;

	@MockBean
	DeviceProviderRepository deviceProviderRepository;

	@Test
	@WithUserDetails("zonal-admin")
	public void createMOSIPDeviceServiceTest() throws Exception {

		RequestWrapper<MOSIPDeviceServiceDto> requestMSDDto = null;
		requestMSDDto = new RequestWrapper<>();
		requestMSDDto.setId("mosip.match.regcentr.machineid");
		requestMSDDto.setVersion("1.0.0");
		requestMSDDto.setRequest(mosipDeviceServiceDto);

		mdsJson = mapper.writeValueAsString(requestMSDDto);
		when(registrationDeviceTypeRepository
				.findByCodeAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.any())).thenReturn(regDeviceType);
		when(registrationDeviceSubTypeRepository
				.findByCodeAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.any()))
						.thenReturn(regDeviceSubType);
		when(deviceProviderRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.any()))
				.thenReturn(deviceProvider);
		when(mosipDeviceServiceRepository.create(Mockito.any())).thenReturn(mosipDeviceService);
		when(mosipDeviceServiceHistoryRepository.create(Mockito.any())).thenReturn(msdHistory);
		mockMvc.perform(post("/mosipdeviceservice").contentType(MediaType.APPLICATION_JSON).content(mdsJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void createMSDregDeviceTypeNotFoundTest() throws Exception {
		RequestWrapper<MOSIPDeviceServiceDto> requestMSDDto = null;
		requestMSDDto = new RequestWrapper<>();
		requestMSDDto.setId("mosip.match.regcentr.machineid");
		requestMSDDto.setVersion("1.0.0");
		requestMSDDto.setRequest(mosipDeviceServiceDto);

		mdsJson = mapper.writeValueAsString(requestMSDDto);
		// when(mosipDeviceServiceRepository.findById(Mockito.any(),
		// Mockito.any())).thenReturn(null);
		when(registrationDeviceTypeRepository
				.findByCodeAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.any())).thenReturn(null);
		mockMvc.perform(post("/mosipdeviceservice").contentType(MediaType.APPLICATION_JSON).content(mdsJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void createMSDregDeviceSubTypeNotFoundTest() throws Exception {
		RequestWrapper<MOSIPDeviceServiceDto> requestMSDDto = null;
		requestMSDDto = new RequestWrapper<>();
		requestMSDDto.setId("mosip.match.regcentr.machineid");
		requestMSDDto.setVersion("1.0.0");
		requestMSDDto.setRequest(mosipDeviceServiceDto);

		mdsJson = mapper.writeValueAsString(requestMSDDto);
		when(registrationDeviceTypeRepository
				.findByCodeAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.any())).thenReturn(regDeviceType);
		when(registrationDeviceSubTypeRepository
				.findByCodeAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.any())).thenReturn(null);
		mockMvc.perform(post("/mosipdeviceservice").contentType(MediaType.APPLICATION_JSON).content(mdsJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void createMSDDeviceProviderTest() throws Exception {
		RequestWrapper<MOSIPDeviceServiceDto> requestMSDDto = null;
		requestMSDDto = new RequestWrapper<>();
		requestMSDDto.setId("mosip.match.regcentr.machineid");
		requestMSDDto.setVersion("1.0.0");
		requestMSDDto.setRequest(mosipDeviceServiceDto);

		mdsJson = mapper.writeValueAsString(requestMSDDto);
		when(registrationDeviceTypeRepository
				.findByCodeAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.any())).thenReturn(regDeviceType);
		when(registrationDeviceSubTypeRepository
				.findByCodeAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.any()))
						.thenReturn(regDeviceSubType);
		when(deviceProviderRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.any()))
				.thenReturn(null);
		mockMvc.perform(post("/mosipdeviceservice").contentType(MediaType.APPLICATION_JSON).content(mdsJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void createMDSInternaleExpTest() throws Exception {
		RequestWrapper<MOSIPDeviceServiceDto> requestMSDDto = null;
		requestMSDDto = new RequestWrapper<>();
		requestMSDDto.setId("mosip.match.regcentr.machineid");
		requestMSDDto.setVersion("1.0.0");
		requestMSDDto.setRequest(mosipDeviceServiceDto);
		mdsJson = objectMapper.writeValueAsString(requestMSDDto);
		when(registrationDeviceTypeRepository
				.findByCodeAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.any())).thenReturn(regDeviceType);
		when(registrationDeviceSubTypeRepository
				.findByCodeAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.any()))
						.thenReturn(regDeviceSubType);
		when(deviceProviderRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.any()))
				.thenReturn(deviceProvider);
		when(mosipDeviceServiceRepository.create(Mockito.any())).thenThrow(DataAccessLayerException.class);
		mockMvc.perform(post("/mosipdeviceservice").contentType(MediaType.APPLICATION_JSON).content(mdsJson))
				.andExpect(status().isInternalServerError());
	}

	private void foundationProvider() {
		foundationalTrustProviderDto = new FoundationalTrustProviderDto();
		foundationalTrustProviderDto.setIsActive(true);
		foundationalTrustProviderDto.setAddress("test address");
		foundationalTrustProviderDto.setCertAlias("141d3962380139742ac9a1e4b23e0221");
		foundationalTrustProviderDto.setContactNo("9876378945");
		foundationalTrustProviderDto.setEmail("test@mosip.io");
		foundationalTrustProviderDto.setName("Test Name");

		foundationalTrustUpdateProviderDto = new FoundationalTrustProviderPutDto();
		foundationalTrustUpdateProviderDto.setId("24233443444");
		foundationalTrustUpdateProviderDto.setIsActive(true);
		foundationalTrustUpdateProviderDto.setAddress("test address");
		foundationalTrustUpdateProviderDto.setCertAlias("141d3962380139742ac9a1e4b23e0221");
		foundationalTrustUpdateProviderDto.setContactNo("9876378945");
		foundationalTrustUpdateProviderDto.setEmail("test@mosip.io");
		foundationalTrustUpdateProviderDto.setName("Test Name");

		updateFoundationalTrustProvider = new FoundationalTrustProvider();
		updateFoundationalTrustProvider.setId("1234545");

		foundationalTrustProviderHistory = new FoundationalTrustProviderHistory();
		foundationalTrustProviderHistory.setId("24233443444");
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void createFoundationalProviderTest() throws Exception {
		RequestWrapper<FoundationalTrustProviderDto> requestMSDDto = null;
		requestMSDDto = new RequestWrapper<>();
		requestMSDDto.setId("mosip.foundationalprovider.test");
		requestMSDDto.setVersion("1.0.0");
		requestMSDDto.setRequest(foundationalTrustProviderDto);

		mdsJson = mapper.writeValueAsString(requestMSDDto);
		when(foundationalTrustProviderRepository.findByDetails(Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean())).thenReturn(null);
		when(foundationalTrustProviderRepository.create(Mockito.any())).thenReturn(updateFoundationalTrustProvider);
		mockMvc.perform(post("/foundationaltrustprovider").contentType(MediaType.APPLICATION_JSON).content(mdsJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void createFoundationalProviderExcepTest() throws Exception {
		RequestWrapper<FoundationalTrustProviderDto> requestMSDDto = null;
		requestMSDDto = new RequestWrapper<>();
		requestMSDDto.setId("mosip.foundationalprovider.test");
		requestMSDDto.setVersion("1.0.0");
		requestMSDDto.setRequest(foundationalTrustProviderDto);

		mdsJson = mapper.writeValueAsString(requestMSDDto);
		when(foundationalTrustProviderRepository.findByDetails(Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean()))
						.thenReturn(updateFoundationalTrustProvider);
		mockMvc.perform(post("/foundationaltrustprovider").contentType(MediaType.APPLICATION_JSON).content(mdsJson))
				.andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void updateFoundationalProviderTest() throws Exception {
		RequestWrapper<FoundationalTrustProviderPutDto> requestMSDDto = null;
		requestMSDDto = new RequestWrapper<>();
		requestMSDDto.setId("mosip.foundationalprovider.test");
		requestMSDDto.setVersion("1.0.0");
		requestMSDDto.setRequest(foundationalTrustUpdateProviderDto);

		mdsJson = mapper.writeValueAsString(requestMSDDto);
		when(foundationalTrustProviderRepository.findById(Mockito.any(), Mockito.any()))
				.thenReturn(updateFoundationalTrustProvider);
		when(foundationalTrustProviderRepository.update(Mockito.any())).thenReturn(updateFoundationalTrustProvider);
		when(foundationalTrustProviderRepositoryHistory.create(Mockito.any()))
				.thenReturn(foundationalTrustProviderHistory);
		mockMvc.perform(put("/foundationaltrustprovider").contentType(MediaType.APPLICATION_JSON).content(mdsJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void updateFoundationalProviderExcepTest() throws Exception {
		RequestWrapper<FoundationalTrustProviderPutDto> requestMSDDto = null;
		requestMSDDto = new RequestWrapper<>();
		requestMSDDto.setId("mosip.foundationalprovider.test");
		requestMSDDto.setVersion("1.0.0");
		requestMSDDto.setRequest(foundationalTrustUpdateProviderDto);

		mdsJson = mapper.writeValueAsString(requestMSDDto);
		when(foundationalTrustProviderRepository.findById(Mockito.any(), Mockito.any())).thenReturn(null);
		mockMvc.perform(put("/foundationaltrustprovider").contentType(MediaType.APPLICATION_JSON).content(mdsJson))
				.andExpect(status().isInternalServerError());
	}

	// --------------------- create Device provider----------------------
	private DeviceProviderDto deviceProviderDto = null;
	private DeviceProviderPutDto deviceProviderPutDto = null;
	private DeviceProvider deviceProviderEnt = null;
	private DeviceProviderHistory deviceProviderHistory = null;

	private void createdeviceProviderSetUp() {
		deviceProviderDto = new DeviceProviderDto();
		deviceProviderDto.setVendorName("name");
		deviceProviderDto.setAddress("address1");
		deviceProviderDto.setContactNumber("123456789");
		deviceProviderDto.setEmail("device@gmail.com");
		deviceProviderDto.setCertificateAlias("device");
		deviceProviderDto.setIsActive(true);

		deviceProviderEnt = new DeviceProvider();
		deviceProviderEnt.setId("10001");

		deviceProviderHistory = new DeviceProviderHistory();
		deviceProviderHistory.setId("10001");

		deviceProviderPutDto = new DeviceProviderPutDto();

		deviceProviderPutDto.setVendorName("name");
		deviceProviderPutDto.setId("1000");
		deviceProviderPutDto.setAddress("address1");
		deviceProviderPutDto.setContactNumber("123456789");
		deviceProviderPutDto.setEmail("device@gmail.com");
		deviceProviderPutDto.setCertificateAlias("device");
		deviceProviderPutDto.setIsActive(true);
	}

	@MockBean
	DeviceProviderHistoryRepository deviceProviderHistoryRepository;

	@Test
	@WithUserDetails("zonal-admin")
	public void createDeviceProviderTest() throws Exception {
		RequestWrapper<DeviceProviderDto> requestMSDDto = null;
		requestMSDDto = new RequestWrapper<>();
		requestMSDDto.setId("mosip.match.regcentr.machineid");
		requestMSDDto.setVersion("1.0.0");
		requestMSDDto.setRequest(deviceProviderDto);

		String deviceProviderJson = mapper.writeValueAsString(requestMSDDto);
		when(deviceProviderRepository.create(Mockito.any())).thenReturn(deviceProviderEnt);
		when(deviceProviderHistoryRepository.create(Mockito.any())).thenReturn(deviceProviderHistory);
		mockMvc.perform(post("/deviceprovider").contentType(MediaType.APPLICATION_JSON).content(deviceProviderJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void createDeviceProvideInternaleExpTest() throws Exception {
		RequestWrapper<DeviceProviderDto> requestMSDDto = null;
		requestMSDDto = new RequestWrapper<>();
		requestMSDDto.setId("mosip.match.regcentr.machineid");
		requestMSDDto.setVersion("1.0.0");
		requestMSDDto.setRequest(deviceProviderDto);

		String deviceProviderJson = mapper.writeValueAsString(requestMSDDto);
		when(deviceProviderRepository.findById(Mockito.any(), Mockito.any())).thenReturn(null);
		when(deviceProviderRepository.create(Mockito.any())).thenThrow(DataAccessLayerException.class);
		mockMvc.perform(post("/deviceprovider").contentType(MediaType.APPLICATION_JSON).content(deviceProviderJson))
				.andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void createDeviceProviderPKExistTest() throws Exception {
		RequestWrapper<DeviceProviderDto> requestMSDDto = null;
		requestMSDDto = new RequestWrapper<>();
		requestMSDDto.setId("mosip.match.regcentr.machineid");
		requestMSDDto.setVersion("1.0.0");
		requestMSDDto.setRequest(deviceProviderDto);

		String deviceProviderJson = mapper.writeValueAsString(requestMSDDto);
		when(deviceProviderRepository.findById(Mockito.any(), Mockito.any())).thenReturn(deviceProviderEnt);
		when(deviceProviderRepository.create(Mockito.any())).thenReturn(deviceProviderEnt);
		mockMvc.perform(post("/deviceprovider").contentType(MediaType.APPLICATION_JSON).content(deviceProviderJson))
				.andExpect(status().isOk());
	}

	// ---------------------------update Device Provider--------------------
	@Test
	@WithUserDetails("zonal-admin")
	public void updateDeviceProviderTest() throws Exception {
		RequestWrapper<DeviceProviderPutDto> requestMSDDto = null;
		requestMSDDto = new RequestWrapper<>();
		requestMSDDto.setId("mosip.match.regcentr.machineid");
		requestMSDDto.setVersion("1.0.0");
		requestMSDDto.setRequest(deviceProviderPutDto);

		String deviceProviderJson = mapper.writeValueAsString(requestMSDDto);
		when(deviceProviderRepository.findByNameAndAddressAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.any(), Mockito.any())).thenReturn(null);
		when(deviceProviderRepository.findById(Mockito.any(), Mockito.any())).thenReturn(deviceProviderEnt);
		when(deviceProviderRepository.update(Mockito.any())).thenReturn(deviceProviderEnt);
		when(deviceProviderHistoryRepository.create(Mockito.any())).thenReturn(deviceProviderHistory);
		mockMvc.perform(put("/deviceprovider").contentType(MediaType.APPLICATION_JSON).content(deviceProviderJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void updateDeviceProvideInternaleExpTest() throws Exception {
		RequestWrapper<DeviceProviderPutDto> requestMSDDto = null;
		requestMSDDto = new RequestWrapper<>();
		requestMSDDto.setId("mosip.match.regcentr.machineid");
		requestMSDDto.setVersion("1.0.0");
		requestMSDDto.setRequest(deviceProviderPutDto);

		String deviceProviderJson = mapper.writeValueAsString(requestMSDDto);
		when(deviceProviderRepository.findById(Mockito.any(), Mockito.any())).thenReturn(deviceProviderEnt);
		when(deviceProviderRepository.update(Mockito.any())).thenThrow(DataAccessLayerException.class);
		mockMvc.perform(put("/deviceprovider").contentType(MediaType.APPLICATION_JSON).content(deviceProviderJson))
				.andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void updateDeviceProviderNotFoundTest() throws Exception {
		RequestWrapper<DeviceProviderPutDto> requestMSDDto = null;
		requestMSDDto = new RequestWrapper<>();
		requestMSDDto.setId("mosip.match.regcentr.machineid");
		requestMSDDto.setVersion("1.0.0");
		requestMSDDto.setRequest(deviceProviderPutDto);

		String deviceProviderJson = mapper.writeValueAsString(requestMSDDto);
		when(deviceProviderRepository.findById(Mockito.any(), Mockito.any())).thenReturn(null);
		when(deviceProviderRepository.update(Mockito.any())).thenReturn(deviceProviderEnt);
		mockMvc.perform(put("/deviceprovider").contentType(MediaType.APPLICATION_JSON).content(deviceProviderJson))
				.andExpect(status().isOk());
	}

	RegCenterPostReqDto regCenterPostReqDto = null;

	private void newRegCenterSetup() {
		LocalTime centerStartTime = LocalTime.of(1, 10, 10, 30);
		LocalTime centerEndTime = LocalTime.of(1, 10, 10, 30);
		LocalTime lunchStartTime = LocalTime.of(1, 10, 10, 30);
		LocalTime lunchEndTime = LocalTime.of(1, 10, 10, 30);
		LocalTime perKioskProcessTime = LocalTime.parse("09:00:00");
		// LocalTime perKioskProcessTime = LocalTime.of(1, 10, 10, 30);
		regCenterPostReqDto = new RegCenterPostReqDto();
		regCenterPostReqDto.setName("TEST CENTER");
		regCenterPostReqDto.setAddressLine1("Address Line 1");
		regCenterPostReqDto.setAddressLine2("Address Line 2");
		regCenterPostReqDto.setAddressLine3("Address Line 3");
		regCenterPostReqDto.setCenterTypeCode("REG");
		regCenterPostReqDto.setContactPerson("Test");
		regCenterPostReqDto.setContactPhone("9999999999");
		regCenterPostReqDto.setHolidayLocationCode("HLC01");
		regCenterPostReqDto.setLangCode("eng");
		regCenterPostReqDto.setLatitude("1.9643");
		regCenterPostReqDto.setLocationCode("RBR");
		regCenterPostReqDto.setLongitude("7.7016");
		regCenterPostReqDto.setIsActive(true);
		regCenterPostReqDto.setPerKioskProcessTime(perKioskProcessTime);
		regCenterPostReqDto.setCenterStartTime(centerStartTime);
		regCenterPostReqDto.setCenterEndTime(centerEndTime);
		regCenterPostReqDto.setLunchStartTime(lunchStartTime);
		regCenterPostReqDto.setLunchEndTime(lunchEndTime);
		regCenterPostReqDto.setTimeZone("UTC");
		regCenterPostReqDto.setWorkingHours("9");
		regCenterPostReqDto.setZoneCode("JRD");

	}

	
	  @Test 
	  @WithUserDetails("zonal-admin") 
	  public void createRegCenterTest() throws Exception { 
		  RequestWrapper<RegCenterPostReqDto> requestMSDDto = null;
		  requestMSDDto = new RequestWrapper<>();
		  requestMSDDto.setId("mosip.match.regcentr.machineid");
		  requestMSDDto.setVersion("1.0.0");
		  requestMSDDto.setRequest(regCenterPostReqDto);
	  
		  String regcenterJson = objectMapper.writeValueAsString(requestMSDDto);
		  when(masterdataCreationUtil.createMasterData(Mockito.any(), Mockito.any())).thenReturn(regCenterPostReqDto);
		  when(registrationCenterTypeRepository.findByCodeAndLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.any(),
				  Mockito.any())).thenReturn(regCenterType);
		  when(locationRepository.findLocationHierarchyByCodeAndLanguageCode(Mockito.any(), Mockito.any())).
		  thenReturn(locationHierarchies);
		  when(registrationCenterRepository.findById(Mockito.any(),Mockito.any())).thenReturn(null);
		  when(registrationCenterRepository.create(Mockito.any())).thenReturn(registrationCenter);
		  when(repositoryCenterHistoryRepository.create(Mockito.any())).thenReturn(registrationCenterHistory);
		  mockMvc.perform(post("/registrationcenters").contentType(MediaType.APPLICATION_JSON).content(regcenterJson)) 
		  .andExpect(status().isOk()); 
	  }
	  @Test 
	  @WithUserDetails("zonal-admin")
	  public void createRegCenterLocationNotFoundTest() throws Exception { 
		  RequestWrapper<RegCenterPostReqDto> requestMSDDto = null;
		  requestMSDDto = new RequestWrapper<>();
		  requestMSDDto.setId("mosip.match.regcentr.machineid");
		  requestMSDDto.setVersion("1.0.0");
		  requestMSDDto.setRequest(regCenterPostReqDto);
	  
		  String regcenterJson = objectMapper.writeValueAsString(requestMSDDto);
		  when(masterdataCreationUtil.createMasterData(Mockito.any(), Mockito.any())).thenReturn(regCenterPostReqDto);
		  when(registrationCenterTypeRepository.findByCodeAndLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.any(),
				  Mockito.any())).thenReturn(regCenterType);
		  when(locationRepository.findLocationHierarchyByCodeAndLanguageCode(Mockito.any(), Mockito.any())).
		  thenReturn(null);
		  mockMvc.perform(post("/registrationcenters").contentType(MediaType.APPLICATION_JSON).content(regcenterJson)) 
		  .andExpect(status().is5xxServerError()); 
	  }
	  @Test 
	  @WithUserDetails("zonal-admin")
	  public void createRegCenterFailureTest() throws Exception { 
		  RequestWrapper<RegCenterPostReqDto> requestMSDDto = null;
		  requestMSDDto = new RequestWrapper<>();
		  requestMSDDto.setId("mosip.match.regcentr.machineid");
		  requestMSDDto.setVersion("1.0.0");
		  requestMSDDto.setRequest(regCenterPostReqDto);
	  
		  String regcenterJson = objectMapper.writeValueAsString(requestMSDDto);
		  when(masterdataCreationUtil.createMasterData(Mockito.any(), Mockito.any())).thenReturn(regCenterPostReqDto);
		  when(registrationCenterTypeRepository.findByCodeAndLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.any(),
				  Mockito.any())).thenReturn(regCenterType);
		  when(locationRepository.findLocationHierarchyByCodeAndLanguageCode(Mockito.any(), Mockito.any())).
		  thenReturn(locationHierarchies);
		  when(registrationCenterRepository.findById(Mockito.any(),Mockito.any())).thenReturn(null);
		  when(registrationCenterRepository.create(Mockito.any())).thenReturn(null);
		  mockMvc.perform(post("/registrationcenters").contentType(MediaType.APPLICATION_JSON).content(regcenterJson)) 
		  .andExpect(status().is5xxServerError()); 
	  }
	
	  @Test
	 
	  @WithUserDetails("zonal-admin") public void createRegCenterExpTest() throws Exception { 
		  RequestWrapper<RegCenterPostReqDto> requestMSDDto = null;
		  requestMSDDto = new RequestWrapper<>();
		  requestMSDDto.setId("mosip.match.regcentr.machineid");
		  requestMSDDto.setVersion("1.0.0");
		  requestMSDDto.setRequest(regCenterPostReqDto);
	  
		  String regcenterJson = objectMapper.writeValueAsString(requestMSDDto);
		  when(masterdataCreationUtil.createMasterData(Mockito.any(), Mockito.any())).thenReturn(regCenterPostReqDto);
		  when(registrationCenterTypeRepository.findByCodeAndLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.any(),
				  Mockito.any())).thenReturn(regCenterType);
		  when(locationRepository.findLocationHierarchyByCodeAndLanguageCode(Mockito.any(), Mockito.any())).
	  		thenReturn(locationHierarchies);
		  when(registrationCenterRepository.findById(Mockito.any(),Mockito.any())).thenReturn(null);
		  when(registrationCenterRepository.create(Mockito.any())).thenReturn(registrationCenter);
		  when(repositoryCenterHistoryRepository.create(Mockito.any())).thenReturn(registrationCenterHistory);
		  when(registrationCenterRepository.create(Mockito.any())).thenThrow(DataAccessLayerException.class);
		  mockMvc.perform(post("/registrationcenters").contentType(MediaType.APPLICATION_JSON).content(regcenterJson)) 
		  .andExpect(status().is5xxServerError()); 
	  }
	 

	@WithUserDetails("zonal-admin")
	@Test
	public void createRegCenterTypeExpTest() throws Exception {
		RequestWrapper<RegCenterPostReqDto> requestMSDDto = null;
		requestMSDDto = new RequestWrapper<>();
		requestMSDDto.setId("mosip.match.regcentr.machineid");
		requestMSDDto.setVersion("1.0.0");
		requestMSDDto.setRequest(regCenterPostReqDto);

		String regcenterJson = objectMapper.writeValueAsString(requestMSDDto);
		when(registrationCenterTypeRepository.findByCodeAndLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.any(),
				Mockito.any())).thenReturn(null);
		mockMvc.perform(post("/registrationcenters").contentType(MediaType.APPLICATION_JSON).content(regcenterJson))
				.andExpect(status().is5xxServerError());
	}

	@WithUserDetails("zonal-admin")
	@Test
	public void createRegCenterLocationExpTest() throws Exception {
		RequestWrapper<RegCenterPostReqDto> requestMSDDto = null;
		requestMSDDto = new RequestWrapper<>();
		requestMSDDto.setId("mosip.match.regcentr.machineid");
		requestMSDDto.setVersion("1.0.0");
		requestMSDDto.setRequest(regCenterPostReqDto);

		String regcenterJson = objectMapper.writeValueAsString(requestMSDDto);
		when(locationRepository.findLocationHierarchyByCodeAndLanguageCode(Mockito.any(), Mockito.any()))
				.thenReturn(null);
		mockMvc.perform(post("/registrationcenters").contentType(MediaType.APPLICATION_JSON).content(regcenterJson))
				.andExpect(status().is5xxServerError());
	}

	
	  @Test
	  
	  @WithUserDetails("zonal-admin") 
	  public void createRegCenterValidationExpTest() throws Exception {
	  RequestWrapper<RegCenterPostReqDto> requestMSDDto = null;
	  regCenterPostReqDto.setCenterEndTime(LocalTime.of(17, 0)); requestMSDDto = new RequestWrapper<>(); 
	  requestMSDDto.setId("mosip.match.regcentr.machineid");
	  requestMSDDto.setVersion("1.0.0");
	  requestMSDDto.setRequest(regCenterPostReqDto);
	  
	  String regcenterJson = objectMapper.writeValueAsString(requestMSDDto);
	  when(registrationCenterTypeRepository.
	  findByCodeAndLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.any(),
	  Mockito.any())).thenReturn(regCenterType);
	  when(locationRepository.findLocationHierarchyByCodeAndLanguageCode(Mockito.
	  any(), Mockito.any())).thenReturn(locationHierarchies);
	  when(registrationCenterRepository.findById(Mockito.any(),
	  Mockito.any())).thenReturn(null);
	  when(registrationCenterRepository.create(Mockito.any())).thenReturn(
	  registrationCenter);
	  when(repositoryCenterHistoryRepository.create(Mockito.any())).thenReturn(
	  registrationCenterHistory);
	  when(registrationCenterRepository.create(Mockito.any())).thenThrow(
	  DataAccessLayerException.class);
	  mockMvc.perform(post("/registrationcenters").contentType(MediaType.
	  APPLICATION_JSON).content(regcenterJson)) .andExpect(status().is5xxServerError()); 
	  }
	 

	// update registartion Center
	RegistarionCenterReqDto<RegCenterPutReqDto> updRegRequest = null;
	RegistrationCenter registrationCenter11 = null;
	RegistrationCenterHistory registrationCenterHistory1 = null;
	RequestWrapper<RegCenterPutReqDto> requestPutDto = null;
	RegCenterPutReqDto registrationCenterPutReqAdmDto1=null;
	private void updateRegistrationCenterSetup() {
		
		LocalTime centerStartTime = LocalTime.of(1, 10, 10, 30);
		LocalTime centerEndTime = LocalTime.of(1, 10, 10, 30);
		LocalTime lunchStartTime = LocalTime.of(1, 10, 10, 30);
		LocalTime lunchEndTime = LocalTime.of(1, 10, 10, 30);
		LocalTime perKioskProcessTime = LocalTime.of(1, 10, 10, 30);

		requestPutDto = new RequestWrapper<RegCenterPutReqDto>();
		// List<RegCenterPutReqDto> updRequestList = new ArrayList<>();
		requestPutDto.setId("mosip.idtype.create");
		requestPutDto.setVersion("1.0");
		// 1st obj
		registrationCenterPutReqAdmDto1 = new RegCenterPutReqDto();
		registrationCenterPutReqAdmDto1.setName("TEST CENTER");
		registrationCenterPutReqAdmDto1.setAddressLine1("Address Line 1");
		registrationCenterPutReqAdmDto1.setAddressLine2("Address Line 2");
		registrationCenterPutReqAdmDto1.setAddressLine3("Address Line 3");
		registrationCenterPutReqAdmDto1.setCenterTypeCode("REG");
		registrationCenterPutReqAdmDto1.setContactPerson("Test");
		registrationCenterPutReqAdmDto1.setContactPhone("9999999999");
		registrationCenterPutReqAdmDto1.setHolidayLocationCode("HLC01");
		registrationCenterPutReqAdmDto1.setId("676");
		registrationCenterPutReqAdmDto1.setLangCode("eng");
		registrationCenterPutReqAdmDto1.setLatitude("12.9646818");
		registrationCenterPutReqAdmDto1.setLocationCode("10190");
		registrationCenterPutReqAdmDto1.setLongitude("77.70168");
		registrationCenterPutReqAdmDto1.setPerKioskProcessTime(perKioskProcessTime);
		registrationCenterPutReqAdmDto1.setCenterStartTime(centerStartTime);
		registrationCenterPutReqAdmDto1.setCenterEndTime(centerEndTime);
		registrationCenterPutReqAdmDto1.setLunchStartTime(lunchStartTime);
		registrationCenterPutReqAdmDto1.setLunchEndTime(lunchEndTime);
		registrationCenterPutReqAdmDto1.setTimeZone("UTC");
		registrationCenterPutReqAdmDto1.setWorkingHours("9");
		registrationCenterPutReqAdmDto1.setIsActive(false);
		registrationCenterPutReqAdmDto1.setZoneCode("JRD");
		WorkingNonWorkingDaysDto workingNonWorkingDaysDto=new WorkingNonWorkingDaysDto();
		workingNonWorkingDaysDto.setMon(true);
		workingNonWorkingDaysDto.setTue(true);
		workingNonWorkingDaysDto.setWed(true);
		workingNonWorkingDaysDto.setThu(true);
		workingNonWorkingDaysDto.setFri(true);
		workingNonWorkingDaysDto.setSat(false);
		workingNonWorkingDaysDto.setSun(false);
		registrationCenterPutReqAdmDto1.setWorkingNonWorkingDays(workingNonWorkingDaysDto);
		ExceptionalHolidayPutPostDto hol=new ExceptionalHolidayPutPostDto();
		hol.setExceptionHolidayDate(LocalDate.of(2020, 3, 1).toString());
		hol.setExceptionHolidayName("name");
		hol.setExceptionHolidayReson("reason");
		registrationCenterPutReqAdmDto1.setExceptionalHolidayPutPostDto(Arrays.asList(hol));
		// updRequestList.add(registrationCenterPutReqAdmDto1);

		requestPutDto.setRequest(registrationCenterPutReqAdmDto1);

		// entity1
		registrationCenter11 = new RegistrationCenter();
		registrationCenter11.setName("TEST CENTER");
		registrationCenter11.setAddressLine1("Address Line 1");
		registrationCenter11.setAddressLine2("Address Line 2");
		registrationCenter11.setAddressLine3("Address Line 3");
		registrationCenter11.setCenterTypeCode("REG");
		registrationCenter11.setContactPerson("Test");
		registrationCenter11.setContactPhone("9999999999");
		registrationCenter11.setHolidayLocationCode("HLC01");
		registrationCenter11.setId("676");
		registrationCenter11.setIsActive(false);
		registrationCenter11.setLangCode("eng");
		registrationCenter11.setLatitude("12.9646818");
		registrationCenter11.setLocationCode("10190");
		registrationCenter11.setLongitude("77.70168");
		registrationCenter11.setPerKioskProcessTime(perKioskProcessTime);
		registrationCenter11.setCenterStartTime(centerStartTime);
		registrationCenter11.setCenterEndTime(centerEndTime);
		registrationCenter11.setLunchStartTime(lunchStartTime);
		registrationCenter11.setLunchEndTime(lunchEndTime);
		registrationCenter11.setNumberOfKiosks((short) 0);
		registrationCenter11.setTimeZone("UTC");
		registrationCenter11.setZoneCode("JRD");
		registrationCenter11.setWorkingHours("9");

	}

	@Test
	@WithUserDetails("zonal-admin")
	public void updateRegCenterNewCreateAdminCenterTypeNotFoundTest() throws Exception {
		String content = objectMapper.writeValueAsString(requestPutDto);
		Zone zone = new Zone();
		zone.setCode("JRD");
		List<Zone> zones = new ArrayList<>();
		zones.add(zone);
		when(registrationCenterTypeRepository.findByCodeAndLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.any(),
				Mockito.any())).thenReturn(null);
		mockMvc.perform(put("/registrationcenters").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().is5xxServerError());
	}
	@Test
	@WithUserDetails("zonal-admin")
	public void updateRegCenterNewCreateAdminLocationNotFoundTest() throws Exception {
		String content = objectMapper.writeValueAsString(requestPutDto);
		Zone zone = new Zone();
		zone.setCode("JRD");
		List<Zone> zones = new ArrayList<>();
		zones.add(zone);
		when(registrationCenterTypeRepository.findByCodeAndLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.any(),
				Mockito.any())).thenReturn(regCenterType);
		when(locationRepository.findLocationHierarchyByCodeAndLanguageCode(Mockito.any(), Mockito.any()))
				.thenReturn(null);
		mockMvc.perform(put("/registrationcenters").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().is5xxServerError());
	}
	@Test
	@WithUserDetails("zonal-admin")
	public void updateRegCenterNewCreateAdminDataAccessExceptionTest() throws Exception {
		String content = objectMapper.writeValueAsString(requestPutDto);
		Zone zone = new Zone();
		zone.setCode("JRD");
		List<Zone> zones = new ArrayList<>();
		zones.add(zone);
		when(registrationCenterTypeRepository.findByCodeAndLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.any(),
				Mockito.any())).thenThrow(DataAccessLayerException.class);
		
		mockMvc.perform(put("/registrationcenters").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().is5xxServerError());
	}
	private List<DaysOfWeek> getDaysOfWeek(){
		DaysOfWeek sun=new DaysOfWeek();
		sun.setCode("1");
		DaysOfWeek mon=new DaysOfWeek();
		mon.setCode("2");
		DaysOfWeek tue=new DaysOfWeek();
		tue.setCode("3");
		DaysOfWeek wed=new DaysOfWeek();
		wed.setCode("4");
		DaysOfWeek thu=new DaysOfWeek();
		thu.setCode("5");
		DaysOfWeek fri=new DaysOfWeek();
		fri.setCode("6");
		DaysOfWeek sat=new DaysOfWeek();
		sat.setCode("7");
		return Arrays.asList(mon,tue,wed,thu,fri,sat,sun);
	}
	
	@Test
	@WithUserDetails("zonal-admin")
	public void updateRegCenterNewCreateAdminTest() throws Exception {
		
		String content = objectMapper.writeValueAsString(requestPutDto);
		Zone zone = new Zone();
		zone.setCode("JRD");
		Device device=new Device();
		device.setId("10001");
		device.setZoneCode("JRD");
		device.setRegCenterId("676");
		List<Zone> zones = new ArrayList<>();
		zones.add(zone);
		
		when(registrationCenterTypeRepository.findByCodeAndLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.any(),
				Mockito.any())).thenReturn(regCenterType);
		when(locationRepository.findLocationHierarchyByCodeAndLanguageCode(Mockito.any(), Mockito.any()))
				.thenReturn(locationHierarchies);
		when(masterdataCreationUtil.updateMasterData(Mockito.any(), Mockito.any())).thenReturn(registrationCenterPutReqAdmDto1);
		when(zoneUtils.getUserZones()).thenReturn(zones);
		when(registrationCenterRepository.findByIdAndLangCodeAndIsDeletedTrue(Mockito.any(), Mockito.any()))
				.thenReturn(registrationCenter11);
		when(deviceRepository.findByRegIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.any()))
			.thenReturn(Arrays.asList());
		when(daysOfWeekListRepo.findBylangCode(Mockito.any())).thenReturn(getDaysOfWeek());
		when(regWorkingNonWorkingRepo
				.findByRegCenterIdAndlanguagecode(Mockito.any(), Mockito.any())).thenReturn(getWorkingNonWorkingDays());
		when(zoneUtils.getChildZoneList(Mockito.any(), Mockito.any(),Mockito.any())).thenReturn(zones);
		when(registrationCenterRepository.update(Mockito.any())).thenReturn(registrationCenter11);
		when(registrationCenterRepository.findByRegCenterIdAndIsDeletedFalseOrNull(Mockito.any()))
				.thenReturn(registrationCenterEntityList);
		when(registrationCenterRepository.create(Mockito.any())).thenReturn(registrationCenter11);
		when(repositoryCenterHistoryRepository.create(Mockito.any())).thenReturn(registrationCenterHistory);
		mockMvc.perform(put("/registrationcenters").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().isOk());
	}
	
	@Test
	@WithUserDetails("zonal-admin")
	public void updateRegCenterNewCreateAdminWorkingsNonWorkingDaysUnavailableTest() throws Exception {
		
		String content = objectMapper.writeValueAsString(requestPutDto);
		Zone zone = new Zone();
		zone.setCode("JRD");
		List<Zone> zones = new ArrayList<>();
		device.setZoneCode("JRD");
		device.setRegCenterId("676");
		zones.add(zone);
		System.out.println(primaryLang);
		when(registrationCenterRepository.findByIdAndIsDeletedFalseOrNull(Mockito.any())).thenReturn(Arrays.asList(new RegistrationCenter() ));
		when(registrationCenterRepository.findByRegIdAndZone(Mockito.any(),Mockito.any())).thenReturn(Arrays.asList(new RegistrationCenter() ));
		
		when(registrationCenterTypeRepository.findByCodeAndLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.any(),
				Mockito.any())).thenReturn(regCenterType);
		when(locationRepository.findLocationHierarchyByCodeAndLanguageCode(Mockito.any(), Mockito.any()))
				.thenReturn(locationHierarchies);
		when(masterdataCreationUtil.updateMasterData(Mockito.any(), Mockito.any())).thenReturn(registrationCenterPutReqAdmDto1);
		when(zoneUtils.getUserZones()).thenReturn(zones);
		when(registrationCenterRepository.findByIdAndLangCodeAndIsDeletedTrue(Mockito.any(), Mockito.any()))
				.thenReturn(registrationCenter11);
		when(deviceRepository.findByRegIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.any()))
		.thenReturn(Arrays.asList());
		when(daysOfWeekListRepo.findBylangCode(Mockito.any())).thenReturn(getDaysOfWeek());
		when(zoneUtils.getChildZoneList(Mockito.any(), Mockito.any(),Mockito.any())).thenReturn(zones);
		when(registrationCenterRepository.update(Mockito.any())).thenReturn(registrationCenter11);
		when(registrationCenterRepository.findByRegCenterIdAndIsDeletedFalseOrNull(Mockito.any()))
				.thenReturn(registrationCenterEntityList);
		when(registrationCenterRepository.create(Mockito.any())).thenReturn(registrationCenter11);
		when(repositoryCenterHistoryRepository.create(Mockito.any())).thenReturn(registrationCenterHistory);
		mockMvc.perform(put("/registrationcenters").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().isOk());
	}
	
	private List<RegWorkingNonWorking> getWorkingNonWorkingDays() {
		RegWorkingNonWorking sunDay=new RegWorkingNonWorking();
		sunDay.setDayCode("101");
		RegWorkingNonWorking monDay=new RegWorkingNonWorking();
		monDay.setDayCode("102");
		RegWorkingNonWorking tuesDay=new RegWorkingNonWorking();
		tuesDay.setDayCode("103");
		RegWorkingNonWorking wednesDay=new RegWorkingNonWorking();
		wednesDay.setDayCode("104");
		RegWorkingNonWorking thursDay=new RegWorkingNonWorking();
		thursDay.setDayCode("105");
		RegWorkingNonWorking friDay=new RegWorkingNonWorking();
		friDay.setDayCode("106");
		RegWorkingNonWorking saturDay=new RegWorkingNonWorking();
		saturDay.setDayCode("107");
		return Arrays.asList(sunDay,monDay,tuesDay,wednesDay,thursDay,friDay,saturDay);
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void updateRegCenterNewCreateAdminSecondaryLangCenterEntityNotFoundTest() throws Exception {
		registrationCenterPutReqAdmDto1.setLangCode("ara");
		requestPutDto.setRequest(registrationCenterPutReqAdmDto1);
		String content = objectMapper.writeValueAsString(requestPutDto);
		Zone zone = new Zone();
		zone.setCode("JRD");
		List<Zone> zones = new ArrayList<>();
		zones.add(zone);
		System.out.println(primaryLang);
		when(registrationCenterTypeRepository.findByCodeAndLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.any(),
				Mockito.any())).thenReturn(regCenterType);
		when(locationRepository.findLocationHierarchyByCodeAndLanguageCode(Mockito.any(), Mockito.any()))
				.thenReturn(locationHierarchies);
		when(masterdataCreationUtil.updateMasterData(Mockito.any(), Mockito.any())).thenReturn(registrationCenterPutReqAdmDto1);
		when(zoneUtils.getUserZones()).thenReturn(zones);
		when(registrationCenterRepository.findByIdAndLangCodeAndIsDeletedTrue(Mockito.any(), Mockito.any()))
				.thenReturn(null);
		// when(registrationCenterRepository.update(Mockito.any())).thenReturn(registrationCenter1);
		when(registrationCenterRepository.findByRegCenterIdAndIsDeletedFalseOrNull(Mockito.any()))
				.thenReturn(registrationCenterEntityList);
		when(registrationCenterRepository.create(Mockito.any())).thenReturn(registrationCenter11);
		when(repositoryCenterHistoryRepository.create(Mockito.any())).thenReturn(registrationCenterHistory);
		mockMvc.perform(put("/registrationcenters").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().isOk());
	}
	
	@Test
	@WithUserDetails("zonal-admin")
	public void updateRegCenterNewCreateAdminDecommissionedTest() throws Exception {
		String content = objectMapper.writeValueAsString(requestPutDto);
		Zone zone = new Zone();
		zone.setCode("JRD");
		List<Zone> zones = new ArrayList<>();
		zones.add(zone);
		System.out.println(primaryLang);
		when(registrationCenterTypeRepository.findByCodeAndLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.any(),
				Mockito.any())).thenReturn(regCenterType);
		when(locationRepository.findLocationHierarchyByCodeAndLanguageCode(Mockito.any(), Mockito.any()))
				.thenReturn(locationHierarchies);
		when(masterdataCreationUtil.updateMasterData(Mockito.any(), Mockito.any())).thenReturn(registrationCenterPutReqAdmDto1);
		when(zoneUtils.getUserZones()).thenReturn(zones);
		when(registrationCenterRepository.findByIdAndLangCodeAndIsDeletedTrue(Mockito.any(), Mockito.any()))
				.thenReturn(null);
		mockMvc.perform(put("/registrationcenters").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().is5xxServerError());
	}
	
	@Test
	@WithUserDetails("zonal-admin")
	public void updateRegCenterNewCreateAdminUpdateFailureTest() throws Exception {
		String content = objectMapper.writeValueAsString(requestPutDto);
		Zone zone = new Zone();
		zone.setCode("JRD");
		List<Zone> zones = new ArrayList<>();
		zones.add(zone);
		registrationCenter1.setLangCode("${mosip.primary-language}");
		when(registrationCenterTypeRepository.findByCodeAndLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.any(),
				Mockito.any())).thenReturn(regCenterType);
		when(locationRepository.findLocationHierarchyByCodeAndLanguageCode(Mockito.any(), Mockito.any()))
				.thenReturn(locationHierarchies);
		when(zoneUtils.getUserZones()).thenReturn(zones);
		when(registrationCenterRepository.findByIdAndLangCodeAndIsDeletedTrue(Mockito.any(), Mockito.any()))
				.thenReturn(null);
		 when(registrationCenterRepository.update(Mockito.any())).thenReturn(registrationCenter1);
		when(registrationCenterRepository.findByIdAndLangCodeAndIsDeletedTrue(Mockito.any(),Mockito.any()))
				.thenReturn(null);
		when(registrationCenterRepository.create(Mockito.any())).thenReturn(registrationCenter11);
		when(repositoryCenterHistoryRepository.create(Mockito.any())).thenReturn(registrationCenterHistory);
		mockMvc.perform(put("/registrationcenters").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().isOk());
	}
	
	@Test
	@WithUserDetails("zonal-admin")
	public void updateRegCenterNewCreateAdminUpdateFailureTest2() throws Exception {
		String content = objectMapper.writeValueAsString(requestPutDto);
		Zone zone = new Zone();
		zone.setCode("JRD");
		List<Zone> zones = new ArrayList<>();
		zones.add(zone);
		registrationCenter1.setLangCode("${mosip.secondary-language}");
		when(registrationCenterTypeRepository.findByCodeAndLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.any(),
				Mockito.any())).thenReturn(regCenterType);
		when(locationRepository.findLocationHierarchyByCodeAndLanguageCode(Mockito.any(), Mockito.any()))
				.thenReturn(locationHierarchies);
		when(zoneUtils.getUserZones()).thenReturn(zones);
		when(registrationCenterRepository.findByIdAndLangCodeAndIsDeletedTrue(Mockito.any(), Mockito.any()))
				.thenReturn(null);
		 when(registrationCenterRepository.update(Mockito.any())).thenReturn(registrationCenter1);
		when(registrationCenterRepository.findByIdAndLangCodeAndIsDeletedTrue(Mockito.any(),Mockito.any()))
				.thenReturn(null);
		when(registrationCenterRepository.create(Mockito.any())).thenReturn(registrationCenter11);
		when(repositoryCenterHistoryRepository.create(Mockito.any())).thenReturn(registrationCenterHistory);
		mockMvc.perform(put("/registrationcenters").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().isOk());
	}

	// -----------------------------------Create RegisteredDevice
	// Testcase-------------

	@MockBean
	RegisteredDeviceRepository registeredDeviceRepository;

	@MockBean
	RegisteredDeviceHistoryRepository registeredDeviceHistoryRepo;

	private RegisteredDevicePostReqDto registeredDeviceDto = null;
	private DeviceProvider devProvider = null;
	private RegistrationDeviceType registrationDeviceType = null;
	private RegistrationDeviceSubType registrationDeviceSubType = null;
	private RegisteredDevice registeredDevice = null;
	private RegisteredDeviceHistory registeredDeviceHistory = null;
	private List<Device> dList = new ArrayList<>();
	List<Device> list = new ArrayList<>();

	public void setUpRegisteredDevice() {
		registeredDeviceDto = new RegisteredDevicePostReqDto();
		registeredDeviceDto.setDeviceId("10001");
		registeredDeviceDto.setStatusCode("Registered");

		registeredDeviceDto.setStatusCode("registered");
		registeredDeviceDto.setDeviceSubId("1234");
		registeredDeviceDto.setPurpose("REGISTRATION");
		registeredDeviceDto.setFirmware("firmware");
		registeredDeviceDto.setCertificationLevel("L0");
		registeredDeviceDto.setFoundationalTPId("foundationalTPId");
		/*
		 * registeredDeviceDto.setFoundationalTrustSignature(
		 * "foundationalTrustSignature");
		 * registeredDeviceDto.setDeviceProviderSignature("sign");
		 */

		DigitalIdDeviceRegisterDto digitalIdDto = new DigitalIdDeviceRegisterDto();
		digitalIdDto.setDpId("1111");
		digitalIdDto.setDp("INTEL");
		digitalIdDto.setMake("make-updated");
		digitalIdDto.setModel("model-updated");
		digitalIdDto.setSerialNo("BS563Q2230890");
		digitalIdDto.setDeviceTypeCode("Face");
		digitalIdDto.setDeviceSTypeCode("Slab");
		// digitalIdDto.setType("face");
		registeredDeviceDto.setDigitalIdDto(digitalIdDto);

		registeredDevice = new RegisteredDevice();
		registeredDevice.setCode("10001");

		registeredDeviceHistory = new RegisteredDeviceHistory();
		registeredDeviceHistory.setCode("10001");

		devProvider = new DeviceProvider();
		devProvider.setId("1111");

		registrationDeviceType = new RegistrationDeviceType();
		registrationDeviceType.setCode("10001");

		registrationDeviceSubType = new RegistrationDeviceSubType();
		registrationDeviceSubType.setCode("10001");

		Device device = new Device();
		device.setId("10001");
		device.setSerialNum("123456789");
		dList.add(device);
	}

	RequestWrapper<RegisteredDevicePostDto> requestDto = null;
	RegisteredDevicePostDto registeredDevicePostDto = null;

	private RegisteredDevicePostDto getDeviceDataForRegisterDevice() throws Exception {
		registeredDevicePostDto = new RegisteredDevicePostDto();
		DigitalId dig = new DigitalId();
		dig.setDateTime(LocalDateTime.now(ZoneOffset.UTC));
		dig.setDeviceProvider("SYNCBYTE");
		dig.setDeviceProviderId("SYNCBYTE.MC01A");
		dig.setMake("MC01A");
		dig.setModel("SMIDCL");
		dig.setSerialNo("1801160991");
		dig.setDeviceSubType("Single");
		dig.setType("Fingerprint");
		DeviceData device = new DeviceData();
		device.setDeviceId("70959dd5-e45f-438a-9ff8-9b263908e572");
		device.setFoundationalTrustProviderId("");
		device.setPurpose("AUTH");
		DeviceInfo deviceInfo = new DeviceInfo();
		deviceInfo.setCertification("L0");
		deviceInfo.setDeviceSubId("1");
		deviceInfo.setDeviceExpiry(LocalDateTime.now(ZoneOffset.UTC));
		deviceInfo.setFirmware("firmware");
		deviceInfo.setDigitalId(CryptoUtil.encodeBase64String(objectMapper.writeValueAsBytes(dig)));
		deviceInfo.setTimeStamp(LocalDateTime.now(ZoneOffset.UTC));
		device.setDeviceInfo(deviceInfo);
		registeredDevicePostDto.setDeviceData(CryptoUtil.encodeBase64String(objectMapper.writeValueAsBytes(device)));
		return registeredDevicePostDto;
	}

	@SuppressWarnings("unchecked")
	@Test
	@WithUserDetails("zonal-admin")
	public void createRegisteredDevice() throws Exception {
		requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.match.regcentr.machineid");
		requestDto.setVersion("1.0.0");
		requestDto.setRequesttime(LocalDateTime.now());
		requestDto.setRequest(getDeviceDataForRegisterDevice());
		String regcenterJson = objectMapper.writeValueAsString(requestDto);
		when(deviceProviderRepository.findByIdAndNameAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.any(),
				Mockito.any())).thenReturn(devProvider);
		when(registrationDeviceTypeRepository
				.findByCodeAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.any()))
						.thenReturn(registrationDeviceType);
		when(registrationDeviceSubTypeRepository
				.findByCodeAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.any()))
						.thenReturn(registrationDeviceSubType);
		when(deviceRepository.findDeviceBySerialNumberAndIsDeletedFalseorIsDeletedIsNullNoIsActive(Mockito.anyString()))
				.thenReturn(dList);
		when(registeredDeviceRepository.create(Mockito.any())).thenReturn(registeredDevice);
		when(registeredDeviceHistoryRepo.create(Mockito.any())).thenReturn(registeredDeviceHistory);
		SignResponseDto signResponseDto = new SignResponseDto();
		signResponseDto.setTimestamp(LocalDateTime.now());
		signResponseDto.setSignature(
				".TGlqZ0lPaUU0MTVHTHEwekxlSkZMb2I4MktTeHdnazc0YkgzZUdwTE9tdm4xVFNYUS8rZHFuemZoM2x2cjZhOVRHb1ZzYjFIeEJqRFdpOStWNlV5THBJVm82VlVwVnppaCtVRno4c0xDSjJsUWJWajhKdm5ybDdPWlpTQWZwVHZnYkxsZ3pNV3FDR0JrVzdITnFTRHVVZFRPblE3azc5RHlQam5sSjlHQkdFaWpMRERUSVNDKzUyT2JpdjdZemUxWVBjbkl4MGNtYVI4bWF2bmYvN09qdmk5VFZQQlppYkx3eVlFZDgvQnJ4OVpReWlXUmJ5bVNIUGo2L1dqVFBsSnJQZGdXTEVONVhrdWFLQldWN1BrR1R2d3Fydit4RjRtc3FvdElGTGs0cnZ3R0JYTTJ3K2pCeUhNT3c1SmpTMXUxNFh1ejhTK3N0eTMrNGNXcVZ0bVZRPT0=");

		ResponseWrapper<SignResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(signResponseDto);
		responseWrapper.setResponsetime(LocalDateTime.now());
		String response = objectMapper.writeValueAsString(responseWrapper);
		when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class),
				Mockito.any(Class.class))).thenReturn(new ResponseEntity<String>(response, HttpStatus.OK));
		mockMvc.perform(post("/registereddevices").contentType(MediaType.APPLICATION_JSON).content(regcenterJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void createRegisteredDeviceDevProviderNull() throws Exception {
		requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.match.regcentr.machineid");
		requestDto.setVersion("1.0.0");
		requestDto.setRequesttime(LocalDateTime.now());
		requestDto.setRequest(getDeviceDataForRegisterDevice());
		String regcenterJson = objectMapper.writeValueAsString(requestDto);
		when(deviceProviderRepository.findByIdAndNameAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.any(),
				Mockito.any())).thenReturn(null);
		mockMvc.perform(post("/registereddevices").contentType(MediaType.APPLICATION_JSON).content(regcenterJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void createRegisteredDeviceRegDeviceTypeNull() throws Exception {
		requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.match.regcentr.machineid");
		requestDto.setVersion("1.0.0");
		requestDto.setRequesttime(LocalDateTime.now());
		requestDto.setRequest(getDeviceDataForRegisterDevice());
		String regcenterJson = objectMapper.writeValueAsString(requestDto);
		when(deviceProviderRepository.findByIdAndNameAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.any(),
				Mockito.any())).thenReturn(devProvider);
		when(registrationDeviceTypeRepository
				.findByCodeAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.any())).thenReturn(null);
		mockMvc.perform(post("/registereddevices").contentType(MediaType.APPLICATION_JSON).content(regcenterJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void createRegisteredDeviceRegDeviceSubTypeNull() throws Exception {
		requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.match.regcentr.machineid");
		requestDto.setVersion("1.0.0");
		requestDto.setRequesttime(LocalDateTime.now());
		RegisteredDevicePostDto postDto = getDeviceDataForRegisterDevice();
		String regcenterJson = objectMapper.writeValueAsString(requestDto);
		when(deviceProviderRepository.findByIdAndNameAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.any(),
				Mockito.any())).thenReturn(devProvider);
		when(registrationDeviceTypeRepository
				.findByCodeAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.any()))
						.thenReturn(registrationDeviceType);
		when(registrationDeviceSubTypeRepository
				.findByCodeAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.any())).thenReturn(null);
		mockMvc.perform(post("/registereddevices").contentType(MediaType.APPLICATION_JSON).content(regcenterJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void createRegisteredDeviceSerialNumberNotFound() throws Exception {
		requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.match.regcentr.machineid");
		requestDto.setVersion("1.0.0");
		requestDto.setRequesttime(LocalDateTime.now());
		requestDto.setRequest(getDeviceDataForRegisterDevice());
		String regcenterJson = objectMapper.writeValueAsString(requestDto);
		when(deviceProviderRepository.findByIdAndNameAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.any(),
				Mockito.any())).thenReturn(devProvider);
		when(registrationDeviceTypeRepository
				.findByCodeAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.any()))
						.thenReturn(registrationDeviceType);
		when(registrationDeviceSubTypeRepository
				.findByCodeAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.any()))
						.thenReturn(registrationDeviceSubType);
		when(deviceRepository.findDeviceBySerialNumberAndIsDeletedFalseorIsDeletedIsNullNoIsActive(Mockito.anyString()))
				.thenReturn(list);
		mockMvc.perform(post("/registereddevices").contentType(MediaType.APPLICATION_JSON).content(regcenterJson))
				.andExpect(status().is5xxServerError());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void createRegisteredDeviceDataAccessExcp() throws Exception {
		requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.match.regcentr.machineid");
		requestDto.setVersion("1.0.0");
		requestDto.setRequesttime(LocalDateTime.now());
		requestDto.setRequest(getDeviceDataForRegisterDevice());
		String regcenterJson = objectMapper.writeValueAsString(requestDto);
		when(deviceProviderRepository.findByIdAndNameAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.any(),
				Mockito.any())).thenReturn(devProvider);
		when(registrationDeviceTypeRepository
				.findByCodeAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.any()))
						.thenReturn(registrationDeviceType);
		when(registrationDeviceSubTypeRepository
				.findByCodeAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.any()))
						.thenReturn(registrationDeviceSubType);
		when(deviceRepository.findDeviceBySerialNumberAndIsDeletedFalseorIsDeletedIsNullNoIsActive(Mockito.anyString()))
				.thenReturn(dList);
		when(registeredDeviceRepository.create(Mockito.any())).thenThrow(new MasterDataServiceException("ADM-DPM-035",
				"Error occurred while storing Registered Device Details"));
		mockMvc.perform(post("/registereddevices").contentType(MediaType.APPLICATION_JSON).content(regcenterJson))
				.andExpect(status().is5xxServerError());
	}

	// -------------------------update Device-------------------------

	@Test
	@WithUserDetails("zonal-admin")
	public void updateDeviceSuccessTest1() throws Exception {
		RequestWrapper<DevicePutReqDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.device.update");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(devicePutDto);
		String content = mapper.writeValueAsString(requestDto);
		when(registrationCenterRepository.findByIdAndIsDeletedFalseOrNull(Mockito.any())).thenReturn(Arrays.asList(new RegistrationCenter() ));
		when(registrationCenterRepository.findByRegIdAndZone(Mockito.any(),Mockito.any())).thenReturn(Arrays.asList(new RegistrationCenter() ));
		Mockito.when(deviceRepository.findByIdAndLangCodeAndIsDeletedFalseOrIsDeletedIsNullNoIsActive(
				Mockito.anyString(), Mockito.anyString())).thenReturn(device);
		when(zoneUtils.getUserZones()).thenReturn(zonesDevice);
		when(masterdataCreationUtil.updateMasterData(Device.class, devicePutDto)).thenReturn(devicePutDto);
		Mockito.when(deviceRepository.update(Mockito.any())).thenReturn(device);
		when(deviceHistoryRepository.create(Mockito.any())).thenReturn(deviceHistory);
		mockMvc.perform(put("/devices").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void updateDeviceValidateZoneTest() throws Exception {
		RequestWrapper<DevicePutReqDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.device.update");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(devicePutDto);
		String content = mapper.writeValueAsString(requestDto);
		List<Zone> zonesDev = new ArrayList<>();

		when(zoneUtils.getUserZones()).thenReturn(zonesDev);
		Mockito.when(deviceRepository.findByIdAndLangCodeAndIsDeletedFalseOrIsDeletedIsNullNoIsActive(
				Mockito.anyString(), Mockito.anyString())).thenReturn(device);
		mockMvc.perform(put("/devices").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void updateDeviceDataAccessExcp() throws Exception {
		RequestWrapper<DevicePutReqDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.device.update");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(devicePutDto);
		String content = mapper.writeValueAsString(requestDto);

		Mockito.when(deviceRepository.findByIdAndLangCodeAndIsDeletedFalseOrIsDeletedIsNullNoIsActive(
				Mockito.anyString(), Mockito.anyString())).thenReturn(device);
		when(zoneUtils.getUserZones()).thenReturn(zonesDevice);
		when(masterdataCreationUtil.updateMasterData(Device.class, devicePutDto)).thenReturn(devicePutDto);
		Mockito.when(deviceRepository.update(Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot insert", null));
		mockMvc.perform(put("/devices").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void updateDeviceSecodarySuccessTest() throws Exception {
		RequestWrapper<DevicePutReqDto> requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.device.update");
		requestDto.setVersion("1.0.0");
		devicePutDto.setLangCode("ara");
		devicePutDto.setRegCenterId("10001");
		requestDto.setRequest(devicePutDto);
		String content = mapper.writeValueAsString(requestDto);
		when(registrationCenterRepository.findByIdAndIsDeletedFalseOrNull(Mockito.any())).thenReturn(Arrays.asList(new RegistrationCenter() ));
		when(registrationCenterRepository.findByRegIdAndLangCode(Mockito.any(),Mockito.any())).thenReturn(Arrays.asList(new RegistrationCenter() ));
		
		Mockito.when(deviceRepository.findByIdAndLangCodeAndIsDeletedFalseOrIsDeletedIsNullNoIsActive(
				Mockito.anyString(), Mockito.anyString())).thenReturn(null);
		when(zoneUtils.getUserZones()).thenReturn(zonesDevice);
		when(masterdataCreationUtil.updateMasterData(Device.class, devicePutDto)).thenReturn(devicePutDto);
		Mockito.when(deviceRepository.create(Mockito.any())).thenReturn(device);
		when(deviceHistoryRepository.create(Mockito.any())).thenReturn(deviceHistory);
		mockMvc.perform(put("/devices").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().isOk());
	}

	@Test
	@Ignore
	@WithUserDetails("global-admin")
	public void updateRegistrationCenterAdminDataExcpTest() throws Exception {
		String content = objectMapper.writeValueAsString(updRegRequest);
		Zone zone = new Zone();
		zone.setCode("JRD");
		List<Zone> zones = new ArrayList<>();
		zones.add(zone);
		when(zoneUtils.getUserZones()).thenReturn(zones);
		when(registrationCenterRepository.findByIdAndLangCodeAndIsDeletedTrue(Mockito.any(), Mockito.any()))
				.thenReturn(registrationCenter1);
		when(registrationCenterRepository.update(Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot execute statement", null));
		mockMvc.perform(put("/registrationcenters").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().is5xxServerError());
	}

	// ------------get Holidaylist for the given langauge code and level---
	@Test
	@WithUserDetails("zonal-admin")
	public void getLocationCodeByLangCodeSuccessTest() throws Exception {
		Set<Location> locations = new HashSet<>();
		Location location = new Location();
		location.setCode("1000");
		locations.add(location);
		when(locationRepository.findLocationByLangCodeLevel(Mockito.anyString(), Mockito.anyShort()))
				.thenReturn(locations);
		mockMvc.perform(get("/locations/level/eng")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void getLocationCodeByLangCodeNullResponseTest() throws Exception {
		when(locationRepository.findLocationByLangCodeLevel(Mockito.anyString(), Mockito.anyShort())).thenReturn(null);
		mockMvc.perform(get("/locations/level/eng")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("zonal-admin")
	public void getLocationCodeByLangCodeFetchExceptionTest() throws Exception {
		when(locationRepository.findLocationByLangCodeLevel(Mockito.anyString(), Mockito.anyShort()))
				.thenThrow(DataRetrievalFailureException.class);
		mockMvc.perform(get("/locations/level/eng")).andExpect(status().isInternalServerError());
	}
	
	
	@Test
	@WithUserDetails("zonal-admin")
	@Ignore
	public void createTestWithNoPublicKey() {
		MachinePostReqDto req = new MachinePostReqDto();
		req.setLangCode("eng");
		req.setName("HP");
		req.setIpAddress("129.0.0.0");
		req.setMacAddress("178.0.0.0");
		req.setMachineSpecId("1010");
		req.setSerialNum("123");
		req.setIsActive(true);
		req.setZoneCode("MOR");
		req.setPublicKey(null);
		
		RequestWrapper<MachinePostReqDto> requestDto;
		requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.match.regcentr.machineid");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(req);
		
		try {
			when(zoneUtils.getUserZones()).thenReturn(zonesMachines);
			//when(masterdataCreationUtil.createMasterData(Machine.class, req)).thenReturn(req);
			when(registrationCenterValidator.generateMachineIdOrvalidateWithDB(Mockito.any())).thenReturn("10001");
			when(machineRepository.create(Mockito.any())).thenReturn(machineEntity);
			when(machineHistoryRepository.create(Mockito.any())).thenReturn(machineHistory);
			
			MvcResult result = mockMvc.perform(post("/machines").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(requestDto)))
			.andExpect(status().isOk()).andReturn();

			ResponseWrapper<?> responseWrapper = objectMapper.readValue(result.getResponse().getContentAsString(),
					ResponseWrapper.class);

		} catch(Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	@WithUserDetails("zonal-admin")
	public void createTestWithInvalidPublicKey() {
		MachinePostReqDto req = new MachinePostReqDto();
		req.setLangCode("eng");
		req.setName("HP");
		req.setIpAddress("129.0.0.0");
		req.setMacAddress("178.0.0.0");
		req.setMachineSpecId("1010");
		req.setSerialNum("123");
		req.setIsActive(true);
		req.setZoneCode("MOR");
		req.setPublicKey("test-public-key");
		
		RequestWrapper<MachinePostReqDto> requestDto;
		requestDto = new RequestWrapper<>();
		requestDto.setId("mosip.match.regcentr.machineid");
		requestDto.setVersion("1.0.0");
		requestDto.setRequest(req);
		
		try {
			when(registrationCenterRepository.findByIdAndIsDeletedFalseOrNull(Mockito.any())).thenReturn(Arrays.asList(new RegistrationCenter() ));
			when(registrationCenterRepository.findByRegIdAndZone(Mockito.any(),Mockito.any())).thenReturn(Arrays.asList(new RegistrationCenter() ));
			
			when(zoneUtils.getUserZones()).thenReturn(zonesMachines);
			when(masterdataCreationUtil.createMasterData(Machine.class, req)).thenReturn(req);
			when(registrationCenterValidator.generateMachineIdOrvalidateWithDB(Mockito.any())).thenReturn("10001");
			when(machineRepository.create(Mockito.any())).thenReturn(machineEntity);
			when(machineHistoryRepository.create(Mockito.any())).thenReturn(machineHistory);
			
			MvcResult result = mockMvc.perform(post("/machines").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(requestDto)))
					.andExpect(status().isOk()).andReturn();
			
			ResponseWrapper<?> responseWrapper = objectMapper.readValue(result.getResponse().getContentAsString(),
					ResponseWrapper.class);

			assertThat(responseWrapper.getErrors().get(0).getErrorCode(), is("KER-MSD-353"));
			
		} catch(Exception e) {
			Assert.fail(e.getMessage());
		}
	}
//----------------------------	TemplateFileFormat --------------------------------------
	
	@Test
	@WithUserDetails("global-admin")
	public void getTemplateFileFormatLangCodeSuccessTest() throws Exception {
		List<TemplateFileFormat> templateFileFormats = new ArrayList<TemplateFileFormat>();
		templateFileFormats.add(templateFileFormat);
		when(templateFileFormatRepository.findAllByLangCodeAndIsDeletedFalseorIsDeletedIsNull(Mockito.anyString()
				)).thenReturn(templateFileFormats);
		mockMvc.perform(get("/templatefileformats/{langcode}", "ENG")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void getTemplateFileFormatLangCodeNullResponseTest() throws Exception {
		when(templateFileFormatRepository.findAllByLangCodeAndIsDeletedFalseorIsDeletedIsNull(Mockito.anyString()
				)).thenReturn(null);
		mockMvc.perform(get("/templatefileformats/{langcode}", "ENG")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void getTemplateFileFormatLangCodeFetchExceptionTest() throws Exception {
		when(templateFileFormatRepository.findAllByLangCodeAndIsDeletedFalseorIsDeletedIsNull(Mockito.anyString()
				)).thenThrow(DataRetrievalFailureException.class);
		mockMvc.perform(get("/templatefileformats/{langcode}", "ENG")).andExpect(status().isInternalServerError());
	}
	
	@Test
	@WithUserDetails("global-admin")
	public void getTemplateFileFormatCodeLangcodeSuccessTest() throws Exception {
		List<TemplateFileFormat> templateFileFormats = new ArrayList<TemplateFileFormat>();
		templateFileFormats.add(templateFileFormat);
		when(templateFileFormatRepository.findAllByCodeAndLangCodeAndIsDeletedFalseorIsDeletedIsNull(Mockito.anyString(),
				Mockito.anyString())).thenReturn(templateFileFormats);
		mockMvc.perform(get("/templatefileformats/{code}/{langcode}", "1000", "ENG")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void getTemplateFileFormatCodeLangcodeNullResponseTest() throws Exception {
		when(templateFileFormatRepository.findAllByCodeAndLangCodeAndIsDeletedFalseorIsDeletedIsNull(Mockito.anyString(),
				Mockito.anyString())).thenReturn(null);
		mockMvc.perform(get("/templatefileformats/{code}/{langcode}", "1000", "ENG")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void getTemplateFileFormatCodeLangcodeFetchExceptionTest() throws Exception {
		when(templateFileFormatRepository.findAllByCodeAndLangCodeAndIsDeletedFalseorIsDeletedIsNull(Mockito.anyString(),
				Mockito.anyString())).thenThrow(DataRetrievalFailureException.class);
		mockMvc.perform(get("/templatefileformats/{code}/{langcode}", "1000", "ENG")).andExpect(status().isInternalServerError());
	}
//----------------------------------------Template Type ------------------------------------------
	@Test
	@WithUserDetails("global-admin")
	public void getTemplateTypeCodeLangcodeSuccessTest() throws Exception {
		List<TemplateType> templateTypes = new ArrayList<TemplateType>();
		templateTypes.add(templateType);
		when(templateTypeRepository.findAllByCodeAndLangCodeAndIsDeletedFalseorIsDeletedIsNull(Mockito.anyString(),
				Mockito.anyString())).thenReturn(templateTypes);
		mockMvc.perform(get("/templatetypes/{code}/{langcode}", "1000", "ENG")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void getTemplateTypeCodeLangcodeNullResponseTest() throws Exception {
		when(templateTypeRepository.findAllByCodeAndLangCodeAndIsDeletedFalseorIsDeletedIsNull(Mockito.anyString(),
				Mockito.anyString())).thenReturn(null);
		mockMvc.perform(get("/templatetypes/{code}/{langcode}", "1000", "ENG")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void getTemplateTypeCodeLangcodeFetchExceptionTest() throws Exception {
		when(templateTypeRepository.findAllByCodeAndLangCodeAndIsDeletedFalseorIsDeletedIsNull(Mockito.anyString(),
				Mockito.anyString())).thenThrow(DataRetrievalFailureException.class);
		mockMvc.perform(get("/templatetypes/{code}/{langcode}", "1000", "ENG")).andExpect(status().isInternalServerError());
	}
	
	//---------------

	@Test
	@WithUserDetails("global-admin")
	public void getTemplateTypeLangCodeSuccessTest() throws Exception {
		List<TemplateType> templateTypes = new ArrayList<TemplateType>();
		templateTypes.add(templateType);
		when(templateTypeRepository.findAllByLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()
				)).thenReturn(templateTypes);
		mockMvc.perform(get("/templatetypes/{langcode}", "ENG")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void getTemplateTypeLangCodeNullResponseTest() throws Exception {
		when(templateTypeRepository.findAllByLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()
				)).thenReturn(null);
		mockMvc.perform(get("/templatetypes/{langcode}", "ENG")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void getTemplateTypeLangCodeFetchExceptionTest() throws Exception {
		when(templateTypeRepository.findAllByLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()
				)).thenThrow(DataRetrievalFailureException.class);
		mockMvc.perform(get("/templatetypes/{langcode}", "ENG")).andExpect(status().isInternalServerError());
	}
//-----------------------------------module----------------------------
	@MockBean
	ModuleRepository moduleRepository;
	
	@Test
	@WithUserDetails("global-admin")
	public void getModuleLangCodeSuccessTest() throws Exception {
		ModuleDetail module = new ModuleDetail();
		module.setId("1001");
		module.setLangCode("eng");
		module.setIsActive(true);
		module.setName("module");
		module.setDescription("description");
		List<ModuleDetail> modules = new ArrayList<ModuleDetail>();
		modules.add(module);
		when(moduleRepository.findAllByLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()
				)).thenReturn(modules);
		mockMvc.perform(get("/modules/{langcode}", "ENG")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void getModuleLangCodeNullResponseTest() throws Exception {
		when(moduleRepository.findAllByLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()
				)).thenReturn(null);
		mockMvc.perform(get("/modules/{langcode}", "ENG")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void getModuleLangCodeFetchExceptionTest() throws Exception {
		when(moduleRepository.findAllByLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString()
				)).thenThrow(DataRetrievalFailureException.class);
		mockMvc.perform(get("/modules/{langcode}", "ENG")).andExpect(status().isInternalServerError());
	}
	
	//-------------------------
	@Test
	@WithUserDetails("global-admin")
	public void getModuleIdLangcodeSuccessTest() throws Exception {
		ModuleDetail module = new ModuleDetail();
		module.setId("1001");
		module.setLangCode("eng");
		module.setIsActive(true);
		module.setName("module");
		module.setDescription("description");
		List<ModuleDetail> modules = new ArrayList<ModuleDetail>();
		modules.add(module);
		when(moduleRepository.findAllByIdAndLangCodeAndIsDeletedFalseorIsDeletedIsNull(Mockito.anyString(),
				Mockito.anyString())).thenReturn(modules);
		mockMvc.perform(get("/modules/{id}/{langcode}", "1000", "ENG")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void getModuleIdLangcodeNullResponseTest() throws Exception {
		when(moduleRepository.findAllByIdAndLangCodeAndIsDeletedFalseorIsDeletedIsNull(Mockito.anyString(),
				Mockito.anyString())).thenReturn(null);
		mockMvc.perform(get("/modules/{id}/{langcode}", "1000", "ENG")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void getModuleIdLangcodeFetchExceptionTest() throws Exception {
		when(moduleRepository.findAllByIdAndLangCodeAndIsDeletedFalseorIsDeletedIsNull(Mockito.anyString(),
				Mockito.anyString())).thenThrow(DataRetrievalFailureException.class);
		mockMvc.perform(get("/modules/{id}/{langcode}", "1000", "ENG")).andExpect(status().isInternalServerError());
	}
	//----------------------------Location hierarchy-------------------------
	@MockBean
	LocationHierarchyRepository locationHierarchyRepository;
	short level = 0;
	
	@Test
	@WithUserDetails("global-admin")
	public void getLocationHierarchyLevelAndLangCodeSuccessTest() throws Exception {
		LocationHierarchy locationHierarchy = new LocationHierarchy();
		locationHierarchy.setHierarchyLevel((short)0);
		locationHierarchy.setLangCode("eng");
		locationHierarchy.setIsActive(true);
		locationHierarchy.setHierarchyLevelName("name");
		List<LocationHierarchy> locationHierarchys = new ArrayList<LocationHierarchy>();
		locationHierarchys.add(locationHierarchy);
		when(locationHierarchyRepository
				.findAllByLevelAndLangCodeAndIsDeletedFalseorIsDeletedIsNull(Mockito.anyShort(),Mockito.anyString())).thenReturn(locationHierarchys);
		mockMvc.perform(get("/locationHierarchyLevels/{level}/{langcode}", level, "ENG")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void getLocationHierarchyLevelAndLangCodeResponseTest() throws Exception {
		when(locationHierarchyRepository.findAllByLevelAndLangCodeAndIsDeletedFalseorIsDeletedIsNull(Mockito.anyShort(),
				Mockito.anyString())).thenReturn(null);
		mockMvc.perform(get("/locationHierarchyLevels/{level}/{langcode}", level, "ENG")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void getLocationHierarchyLevelAndLangCodeFetchExceptionTest() throws Exception {
		when(locationHierarchyRepository.findAllByLevelAndLangCodeAndIsDeletedFalseorIsDeletedIsNull(Mockito.anyShort(),
				Mockito.anyString())).thenThrow(DataRetrievalFailureException.class);
		mockMvc.perform(get("/locationHierarchyLevels/{level}/{langcode}", level, "ENG")).andExpect(status().isInternalServerError());
	}
	
	//--------------------------------
	@Test
	@WithUserDetails("global-admin")
	public void getLocationHierarchyLangCodeSuccessTest() throws Exception {
		LocationHierarchy locationHierarchy = new LocationHierarchy();
		locationHierarchy.setHierarchyLevel((short)0);
		locationHierarchy.setLangCode("eng");
		locationHierarchy.setIsActive(true);
		locationHierarchy.setHierarchyLevelName("name");
		List<LocationHierarchy> locationHierarchys = new ArrayList<LocationHierarchy>();
		locationHierarchys.add(locationHierarchy);
		when(locationHierarchyRepository
				.findAllByLangCodeAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString())).thenReturn(locationHierarchys);
		mockMvc.perform(get("/locationHierarchyLevels/{langcode}", "ENG")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void getLocationHierarchyLangCodeResponseTest() throws Exception {
		
		when(locationHierarchyRepository.findAllByLangCodeAndIsDeletedFalseOrIsDeletedIsNull(
				Mockito.anyString())).thenReturn(null);
		mockMvc.perform(get("/locationHierarchyLevels/{langcode}","ENG")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void getLocationHierarchyLangCodeFetchExceptionTest() throws Exception {
		
		when(locationHierarchyRepository.findAllByLangCodeAndIsDeletedFalseOrIsDeletedIsNull(
				Mockito.anyString())).thenThrow(DataRetrievalFailureException.class);
		mockMvc.perform(get("/locationHierarchyLevels/{langcode}","ENG")).andExpect(status().isInternalServerError());
	}
}
