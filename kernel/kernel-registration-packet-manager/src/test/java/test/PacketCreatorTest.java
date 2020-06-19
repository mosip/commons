package test;

import io.mosip.kernel.core.exception.IOException;
import io.mosip.kernel.packetmanager.constants.ErrorCode;
import io.mosip.kernel.packetmanager.dto.AuditDto;
import io.mosip.kernel.packetmanager.dto.BiometricsDto;
import io.mosip.kernel.packetmanager.dto.DocumentDto;
import io.mosip.kernel.packetmanager.dto.SimpleDto;
import io.mosip.kernel.packetmanager.dto.metadata.BiometricsException;
import io.mosip.kernel.packetmanager.exception.PacketCreatorException;
import io.mosip.kernel.packetmanager.spi.PacketCreator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppConfig.class, loader = AnnotationConfigContextLoader.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PacketCreatorTest {
	
	@Autowired
	private PacketCreator packetCreatorImpl;
	
	
	private String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsIN13fjuTASPK1pvjOv8m3_06_dPhvwcIO-WvtG6MsbXmhpzwuo6W4d9eycYNNvwpNqLvC-8S1bhDdWcLZ_4zgXC9YF8maVUXNJ-y4ldQf5TzrofRx-1hexEIdABwuVUwohzQWBcpiQpZCMJYAscpsscNhTwxZhv_FLyiLmbaw-AyEjVXjNKeXfrhAhlkpA4g35pJzWoKCb75efJAWEi-3OGPYDaYtc-9VL8HmMBeHAo_BTsYfNWth-fr46k3SIV8X5LiXzvm0wtiIgt9WXDdsM5rQRofyiRf3B6frKYVWc4Jh7cjo4Q7YvKtNJx8kWC-04hxPi_XkvcLUIwLdY6jwIDAQAB";
	//private String schemaJson = "{\"$schema\":\"http:\\/\\/json-schema.org\\/draft-07\\/schema#\",\"description\":\"test mosip id schema\",\"additionalProperties\":false,\"title\":\"mosip id schema\",\"type\":\"object\",\"definitions\":{\"simpleType\":{\"uniqueItems\":true,\"additionalItems\":false,\"type\":\"array\",\"items\":{\"additionalProperties\":false,\"type\":\"object\",\"required\":[\"language\",\"value\"],\"properties\":{\"language\":{\"type\":\"string\"},\"value\":{\"type\":\"string\"}}}},\"documentType\":{\"additionalProperties\":false,\"type\":\"object\",\"properties\":{\"format\":{\"type\":\"string\"},\"type\":{\"type\":\"string\"},\"value\":{\"type\":\"string\"}}},\"biometricsType\":{\"additionalProperties\":false,\"type\":\"object\",\"properties\":{\"format\":{\"type\":\"string\"},\"version\":{\"type\":\"number\",\"minimum\":0},\"value\":{\"type\":\"string\"}}}},\"properties\":{\"identity\":{\"additionalProperties\":false,\"type\":\"object\",\"required\":[\"IDSchemaVersion\",\"UIN\",\"fullName\",\"dateOfBirth\",\"gender\",\"addressLine1\",\"addressLine2\",\"addressLine3\",\"region\",\"province\",\"city\",\"postalCode\",\"phone\",\"email\",\"zone\",\"proofOfIdentity\",\"proofOfRelationship\",\"proofOfDateOfBirth\",\"proofOfException\",\"proofOfException\",\"individualBiometrics\"],\"properties\":{\"proofOfAddress\":{\"fieldCategory\":\"optional\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/documentType\"},\"gender\":{\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"city\":{\"validators\":[{\"validator\":\"^(?=.{0,50}$).*\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"postalCode\":{\"validators\":[{\"validator\":\"^[(?i)A-Z0-9]{5}$|^NA$\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"type\":\"string\",\"fieldType\":\"default\"},\"individualBiometrics\":{\"bioAttributes\":[\"leftEye\", \"face\"],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/biometricsType\"},\"province\":{\"validators\":[{\"validator\":\"^(?=.{0,50}$).*\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"zone\":{\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"proofOfDateOfBirth\":{\"fieldCategory\":\"evidence\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/documentType\"},\"addressLine1\":{\"validators\":[{\"validator\":\"^(?=.{0,50}$).*\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"addressLine2\":{\"validators\":[{\"validator\":\"^(?=.{0,50}$).*\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"residenceStatus\":{\"fieldCategory\":\"optional\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"addressLine3\":{\"validators\":[{\"validator\":\"^(?=.{0,50}$).*\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"email\":{\"validators\":[{\"validator\":\"^[a-zA-Z-\\\\+]+(\\\\.[a-zA-Z]+)*@[a-zA-Z-]+(\\\\.[a-zA-Z]+)*(\\\\.[a-zA-Z]{2,})$\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"type\":\"string\",\"fieldType\":\"default\"},\"parentOrGuardianRID\":{\"fieldCategory\":\"evidence\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"parentOrGuardianBiometrics\":{\"bioAttributes\":\"[face]\",\"fieldCategory\":\"evidence\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/biometricsType\"},\"fullName\":{\"validators\":[{\"validator\":\"^(?=.{0,50}$).*\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"dateOfBirth\":{\"validators\":[{\"validator\":\"^(1869|18[7-9][0-9]|19[0-9][0-9]|20[0-9][0-9])\\/([0][1-9]|1[0-2])\\/([0][1-9]|[1-2][0-9]|3[01])$\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"type\":\"string\",\"fieldType\":\"default\"},\"parentOrGuardianUIN\":{\"fieldCategory\":\"evidence\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"proofOfIdentity\":{\"fieldCategory\":\"none\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/documentType\"},\"IDSchemaVersion\":{\"fieldCategory\":\"pvt\",\"format\":\"none\",\"type\":\"number\",\"fieldType\":\"default\",\"minimum\":0},\"proofOfException\":{\"fieldCategory\":\"evidence\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/documentType\"},\"phone\":{\"validators\":[{\"validator\":\"^([6-9]{1})([0-9]{9})$\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"type\":\"string\",\"fieldType\":\"default\"},\"parentOrGuardianName\":{\"fieldCategory\":\"evidence\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"proofOfRelationship\":{\"fieldCategory\":\"evidence\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/documentType\"},\"UIN\":{\"fieldCategory\":\"pvt\",\"format\":\"none\",\"type\":\"integer\",\"fieldType\":\"default\",\"minimum\":0},\"region\":{\"validators\":[{\"validator\":\"^(?=.{0,50}$).*\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"}}}}}";
	private String schemaJson = "{\"$schema\":\"http:\\/\\/json-schema.org\\/draft-07\\/schema#\",\"description\":\"test mosip id schema\",\"additionalProperties\":false,\"title\":\"mosip id schema\",\"type\":\"object\",\"definitions\":{\"simpleType\":{\"uniqueItems\":true,\"additionalItems\":false,\"type\":\"array\",\"items\":{\"additionalProperties\":false,\"type\":\"object\",\"required\":[\"language\",\"value\"],\"properties\":{\"language\":{\"type\":\"string\"},\"value\":{\"type\":\"string\"}}}},\"documentType\":{\"additionalProperties\":false,\"type\":\"object\",\"properties\":{\"format\":{\"type\":\"string\"},\"type\":{\"type\":\"string\"},\"value\":{\"type\":\"string\"}}},\"biometricsType\":{\"additionalProperties\":false,\"type\":\"object\",\"properties\":{\"format\":{\"type\":\"string\"},\"version\":{\"type\":\"number\",\"minimum\":0},\"value\":{\"type\":\"string\"}}}},\"properties\":{\"identity\":{\"additionalProperties\":false,\"type\":\"object\",\"required\":[\"IDSchemaVersion\",\"UIN\",\"fullName\",\"dateOfBirth\",\"gender\",\"addressLine1\",\"addressLine2\",\"addressLine3\",\"region\",\"province\",\"city\",\"postalCode\",\"phone\",\"email\",\"zone\",\"proofOfIdentity\",\"proofOfRelationship\",\"proofOfDateOfBirth\",\"proofOfException\",\"proofOfException\",\"individualBiometrics\"],\"properties\":{\"proofOfAddress\":{\"fieldCategory\":\"optional\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/documentType\"},\"gender\":{\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"city\":{\"validators\":[{\"validator\":\"^(?=.{0,50}$).*\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"postalCode\":{\"validators\":[{\"validator\":\"^[(?i)A-Z0-9]{5}$|^NA$\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"type\":\"string\",\"fieldType\":\"default\"},\"individualBiometrics\":{\"bioAttributes\":\"[leftIris, face]\",\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/biometricsType\"},\"province\":{\"validators\":[{\"validator\":\"^(?=.{0,50}$).*\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"zone\":{\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"proofOfDateOfBirth\":{\"fieldCategory\":\"evidence\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/documentType\"},\"addressLine1\":{\"validators\":[{\"validator\":\"^(?=.{0,50}$).*\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"addressLine2\":{\"validators\":[{\"validator\":\"^(?=.{0,50}$).*\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"residenceStatus\":{\"fieldCategory\":\"kyc\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"referenceIdentityNumber\":{\"fieldCategory\":\"kyc\",\"format\":\"none\",\"fieldType\":\"default\",\"type\":\"string\"},\"addressLine3\":{\"validators\":[{\"validator\":\"^(?=.{0,50}$).*\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"email\":{\"validators\":[{\"validator\":\"^[a-zA-Z-\\\\+]+(\\\\.[a-zA-Z]+)*@[a-zA-Z-]+(\\\\.[a-zA-Z]+)*(\\\\.[a-zA-Z]{2,})$\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"type\":\"string\",\"fieldType\":\"default\"},\"parentOrGuardianRID\":{\"fieldCategory\":\"evidence\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"parentOrGuardianBiometrics\":{\"bioAttributes\":\"[face]\",\"fieldCategory\":\"evidence\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/biometricsType\"},\"fullName\":{\"validators\":[{\"validator\":\"^(?=.{0,50}$).*\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"dateOfBirth\":{\"validators\":[{\"validator\":\"^(1869|18[7-9][0-9]|19[0-9][0-9]|20[0-9][0-9])\\/([0][1-9]|1[0-2])\\/([0][1-9]|[1-2][0-9]|3[01])$\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"type\":\"string\",\"fieldType\":\"default\"},\"parentOrGuardianUIN\":{\"fieldCategory\":\"evidence\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"proofOfIdentity\":{\"fieldCategory\":\"evidence\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/documentType\"},\"IDSchemaVersion\":{\"fieldCategory\":\"none\",\"format\":\"none\",\"type\":\"number\",\"fieldType\":\"default\",\"minimum\":0},\"proofOfException\":{\"fieldCategory\":\"evidence\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/documentType\"},\"phone\":{\"validators\":[{\"validator\":\"^([6-9]{1})([0-9]{9})$\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"type\":\"string\",\"fieldType\":\"default\"},\"parentOrGuardianName\":{\"fieldCategory\":\"evidence\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"proofOfRelationship\":{\"fieldCategory\":\"evidence\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/documentType\"},\"UIN\":{\"fieldCategory\":\"none\",\"format\":\"none\",\"type\":\"integer\",\"fieldType\":\"default\",\"minimum\":0},\"region\":{\"validators\":[{\"validator\":\"^(?=.{0,50}$).*\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"}}}}}";
	
	@Test
	//@Ignore
	public void createPacketStep00Test() {
		try {
			packetCreatorImpl.createPacket("test", 1.0, schemaJson, null, publicKey.getBytes(), null);
		} catch (PacketCreatorException e) {
			assertEquals(ErrorCode.INITIALIZATION_ERROR.getErrorCode(), e.getErrorCode());
		}
	}
	
	@Test
	//@Ignore
	public void createPacketStep01Test() {
		packetCreatorImpl.initialize();
	}
	
	@Test
	//@Ignore
	public void createPacketStep2Test() {		
		List<SimpleDto> nameList = new ArrayList<>();
		SimpleDto fullName = new SimpleDto("eng", "testUser");
		nameList.add(fullName);		
		packetCreatorImpl.setField("fullName", nameList);
		List<SimpleDto> generList = new ArrayList<>();
		SimpleDto gender = new SimpleDto("eng", "female");
		generList.add(gender);		
		packetCreatorImpl.setField("gender", generList);
		List<SimpleDto> indiviaulStatusList = new ArrayList<>();
		SimpleDto indiviaulStatus = new SimpleDto("eng", "Non-Forienger");
		indiviaulStatusList.add(indiviaulStatus);		
		packetCreatorImpl.setField("residenceStatus", indiviaulStatusList);
		packetCreatorImpl.setField("referenceIdentityNumber", "1234567890");
	}
	
	@Test
	//@Ignore
	public void createPacketStep02Test() {
		packetCreatorImpl.setField("dateOfBirth", "23/05/2000");
		packetCreatorImpl.setField("postalCode", "NA");
		packetCreatorImpl.setField("phone", "9090909090");
		packetCreatorImpl.setField("email", "test@Test.com");
		packetCreatorImpl.setField("UIN", "2013021947");
	}
	
	@Test
	//@Ignore
	public void createPacketStep021Test() {
		DocumentDto poa = new DocumentDto();
		poa.setCategory("POA");
		poa.setFormat("pdf");
		poa.setOwner("user");
		poa.setValue("POA_Driving licence of the applicant");
		poa.setType("Driving licence of the applicant");
		poa.setDocument("test agreement content".getBytes());
		packetCreatorImpl.setDocument("proofOfAddress", poa);
		
		DocumentDto poi = new DocumentDto();
		poi.setCategory("POI");
		poi.setFormat("pdf");
		poi.setOwner("user");
		poi.setValue("POI_Service photo ID cards that is issued by a PSU");
		poi.setType("Service photo ID cards that is issued by a PSU");
		poi.setDocument("test passport content".getBytes());
		packetCreatorImpl.setDocument("proofOfIdentity", poi);
		
		DocumentDto poe = new DocumentDto();
		poe.setCategory("POE");
		poe.setFormat("jpg");
		poe.setOwner("user");
		poe.setValue("POE_Certification of Exception");
		poe.setType("Certification of Exception");
		poe.setDocument("test exception photograph".getBytes());
		packetCreatorImpl.setDocument("proofOfException", poe);		
	}
	
	@Test
	//@Ignore
	public void createPacketStep023Test() {
		List<AuditDto> audits = new ArrayList<AuditDto>();
		AuditDto dto = new AuditDto();
		dto.setEventId("001");
		audits.add(dto);
		AuditDto dto2 = new AuditDto();
		dto2.setEventId("002");
		audits.add(dto2);
		packetCreatorImpl.setAudits(audits);
	}
	
	@Test
	//@Ignore
	public void createPacketStep024Test() {
		packetCreatorImpl.setAcknowledgement("test-receipt", "test ack content".getBytes());
	}
	
	@Test
	//@Ignore
	public void createPacketStep025Test() throws java.io.IOException {
		List<BiometricsDto> list = new ArrayList<BiometricsDto>();
		BiometricsDto dto = new BiometricsDto("leftEye", IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("leftEye.iso")),
				90.0);
		dto.setNumOfRetries(2);
		dto.setForceCaptured(false);
		dto.setSubType("applicant");
		list.add(dto);
		packetCreatorImpl.setBiometric("individualBiometrics", list);
	}
	
	@Test
	//@Ignore
	public void createPacketStep026Test() {
		List<BiometricsException> list = new ArrayList<>();
		list.add(new BiometricsException("FIRS", "LeftIndex", "missing finger", "Permanent", 
				"applicant"));
		packetCreatorImpl.setBiometricException("individualBiometrics", list);
	}
	
	@Test
	//@Ignore
	public void createPacketStep027Test() {
		packetCreatorImpl.setMetaInfo("consentOfApplicant", "Yes");
		packetCreatorImpl.setMetaInfo("creationDate", 
				LocalDateTime.now(ZoneId.of("UTC")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")));
		packetCreatorImpl.setOperationsInfo("officerId", "10007");
		packetCreatorImpl.setOperationsInfo("supervisorId", null);
		packetCreatorImpl.setOperationsInfo("supervisorPassword", "false");
		packetCreatorImpl.setOperationsInfo("supervisorPIN", "false");
		packetCreatorImpl.setOperationsInfo("supervisorOTPAuthentication", "false");
		packetCreatorImpl.setOperationsInfo("officerPassword", "true");
		packetCreatorImpl.setOperationsInfo("officerPIN", "false");
		packetCreatorImpl.setOperationsInfo("officerOTPAuthentication", "false");
		packetCreatorImpl.setOperationsInfo("officerBiometricFileName", null);
		packetCreatorImpl.setOperationsInfo("supervisorBiometricFileName",  null);
	}
	
	
	@Test
	//@Ignore
	public void createPacketStep999999Test() throws PacketCreatorException, IOException, java.io.IOException {
		String registrationId = "10007103600000420200510150916";
		byte[] packet = packetCreatorImpl.createPacket(registrationId, 1.0, schemaJson, null, publicKey.getBytes(), null);
		assertNotNull(packet);
		
		//FileUtils.writeByteArrayToFile(new File("/opt/mosip/packets/"+registrationId+".zip"), packet);
	}

}
