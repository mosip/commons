package test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.utils.IOUtils;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.web.client.HttpServerErrorException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.exception.FileNotFoundException;
import io.mosip.kernel.core.exception.IOException;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.packetmanager.dto.CryptomanagerResponseDto;
import io.mosip.kernel.packetmanager.dto.DecryptResponseDto;
import io.mosip.kernel.packetmanager.exception.ApiNotAccessibleException;
import io.mosip.kernel.packetmanager.exception.PacketDecryptionFailureException;
import io.mosip.kernel.packetmanager.impl.PacketDecryptorImpl;
import io.mosip.kernel.packetmanager.spi.PacketDecryptor;
import io.mosip.kernel.packetmanager.util.IdSchemaUtils;
import io.mosip.kernel.packetmanager.util.RestUtil;
import io.mosip.kernel.packetmanager.util.ZipUtils;

@RunWith(PowerMockRunner.class)
public class PacketDecryptorTest {

	
	@Mock
	RestUtil restApiClient;

	@Mock
	private Environment env;
	
	private String data;
	private File encrypted;
	
	@Mock
	private CryptomanagerResponseDto cryptomanagerResponseDto;
	
	
	private InputStream inputStream;

	@Mock
	ObjectMapper objectMapper;
	
	@InjectMocks
	private PacketDecryptor decryptor=new PacketDecryptorImpl();
	
	private String schemaJson = "{\"$schema\":\"http:\\/\\/json-schema.org\\/draft-07\\/schema#\",\"description\":\"test mosip id schema\",\"additionalProperties\":false,\"title\":\"mosip id schema\",\"type\":\"object\",\"definitions\":{\"simpleType\":{\"uniqueItems\":true,\"additionalItems\":false,\"type\":\"array\",\"items\":{\"additionalProperties\":false,\"type\":\"object\",\"required\":[\"language\",\"value\"],\"properties\":{\"language\":{\"type\":\"string\"},\"value\":{\"type\":\"string\"}}}},\"documentType\":{\"additionalProperties\":false,\"type\":\"object\",\"properties\":{\"format\":{\"type\":\"string\"},\"type\":{\"type\":\"string\"},\"value\":{\"type\":\"string\"}}},\"biometricsType\":{\"additionalProperties\":false,\"type\":\"object\",\"properties\":{\"format\":{\"type\":\"string\"},\"version\":{\"type\":\"number\",\"minimum\":0},\"value\":{\"type\":\"string\"}}}},\"properties\":{\"identity\":{\"additionalProperties\":false,\"type\":\"object\",\"required\":[\"IDSchemaVersion\",\"UIN\",\"fullName\",\"dateOfBirth\",\"gender\",\"addressLine1\",\"addressLine2\",\"addressLine3\",\"region\",\"province\",\"city\",\"postalCode\",\"phone\",\"email\",\"zone\",\"proofOfIdentity\",\"proofOfRelationship\",\"proofOfDateOfBirth\",\"proofOfException\",\"proofOfException\",\"individualBiometrics\"],\"properties\":{\"proofOfAddress\":{\"fieldCategory\":\"optional\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/documentType\"},\"gender\":{\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"city\":{\"validators\":[{\"validator\":\"^(?=.{0,50}$).*\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"postalCode\":{\"validators\":[{\"validator\":\"^[(?i)A-Z0-9]{5}$|^NA$\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"type\":\"string\",\"fieldType\":\"default\"},\"individualBiometrics\":{\"bioAttributes\":\"[leftIris, face]\",\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/biometricsType\"},\"province\":{\"validators\":[{\"validator\":\"^(?=.{0,50}$).*\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"zone\":{\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"proofOfDateOfBirth\":{\"fieldCategory\":\"evidence\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/documentType\"},\"addressLine1\":{\"validators\":[{\"validator\":\"^(?=.{0,50}$).*\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"addressLine2\":{\"validators\":[{\"validator\":\"^(?=.{0,50}$).*\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"residenceStatus\":{\"fieldCategory\":\"kyc\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"referenceIdentityNumber\":{\"fieldCategory\":\"kyc\",\"format\":\"none\",\"fieldType\":\"default\",\"type\":\"string\"},\"addressLine3\":{\"validators\":[{\"validator\":\"^(?=.{0,50}$).*\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"email\":{\"validators\":[{\"validator\":\"^[a-zA-Z-\\\\+]+(\\\\.[a-zA-Z]+)*@[a-zA-Z-]+(\\\\.[a-zA-Z]+)*(\\\\.[a-zA-Z]{2,})$\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"type\":\"string\",\"fieldType\":\"default\"},\"parentOrGuardianRID\":{\"fieldCategory\":\"evidence\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"parentOrGuardianBiometrics\":{\"bioAttributes\":\"[face]\",\"fieldCategory\":\"evidence\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/biometricsType\"},\"fullName\":{\"validators\":[{\"validator\":\"^(?=.{0,50}$).*\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"dateOfBirth\":{\"validators\":[{\"validator\":\"^(1869|18[7-9][0-9]|19[0-9][0-9]|20[0-9][0-9])\\/([0][1-9]|1[0-2])\\/([0][1-9]|[1-2][0-9]|3[01])$\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"type\":\"string\",\"fieldType\":\"default\"},\"parentOrGuardianUIN\":{\"fieldCategory\":\"evidence\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"proofOfIdentity\":{\"fieldCategory\":\"evidence\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/documentType\"},\"IDSchemaVersion\":{\"fieldCategory\":\"none\",\"format\":\"none\",\"type\":\"number\",\"fieldType\":\"default\",\"minimum\":0},\"proofOfException\":{\"fieldCategory\":\"evidence\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/documentType\"},\"phone\":{\"validators\":[{\"validator\":\"^([6-9]{1})([0-9]{9})$\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"type\":\"string\",\"fieldType\":\"default\"},\"parentOrGuardianName\":{\"fieldCategory\":\"evidence\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"proofOfRelationship\":{\"fieldCategory\":\"evidence\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/documentType\"},\"UIN\":{\"fieldCategory\":\"none\",\"format\":\"none\",\"type\":\"integer\",\"fieldType\":\"default\",\"minimum\":0},\"region\":{\"validators\":[{\"validator\":\"^(?=.{0,50}$).*\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"}}}}}";

	private String str="{\"id\":\"84071493960000320190110145452\",\"version\":\"1.0\",\"metadata\":null,\"data\":\"asdf\",\"response\":{\"id\":\"IDSchemaVersion\",\"description\":\"ID Schema Version\",\"labelName\":\"IDSchemaVersion\",\"type\":\"number\",\"data\":\"asdf\"},\"responseTime\":\"2020-05-02T02:50:12.208Z\",\"errors\":null}";
	
	@Before
	public void setup() throws PacketDecryptionFailureException, ApiNotAccessibleException, java.io.IOException {
	
		data = "bW9zaXA";
		cryptomanagerResponseDto = new CryptomanagerResponseDto();
		cryptomanagerResponseDto.setResponse(new DecryptResponseDto(data));

		when(env.getProperty("mosip.registration.processor.crypto.decrypt.id"))
		.thenReturn("mosip.cryptomanager.decrypt");
		when(env.getProperty("mosip.registration.processor.application.version")).thenReturn("1.0");
		when(env.getProperty("mosip.registration.processor.datetime.pattern"))
		.thenReturn("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		when(env.getProperty("CRYPTOMANAGERDECRYPT"))
		.thenReturn("http://localhost:8080/");
		ClassLoader classLoader = getClass().getClassLoader();
		encrypted = new File(classLoader.getResource("84071493960000320190110145452.zip").getFile());
		inputStream = new FileInputStream(encrypted);		
	}
	
	@Test
	public void decryptTest() throws ApiNotAccessibleException, PacketDecryptionFailureException, JsonParseException, JsonMappingException, java.io.IOException {

		Mockito.when(objectMapper.readValue(str, CryptomanagerResponseDto.class)).thenReturn(cryptomanagerResponseDto);
		Mockito.when(restApiClient.postApi(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
		.thenReturn(str);
		InputStream result=decryptor.decrypt(inputStream, "84071493960000320190110145452");			
		assertNotNull(result);
	}

	@Test(expected = PacketDecryptionFailureException.class)
	public void HttpClientErrorExceptionTest() throws ApiNotAccessibleException, PacketDecryptionFailureException{
		
		ApiNotAccessibleException apisNotAccessibleException = Mockito.mock(ApiNotAccessibleException.class);
		Mockito.when(restApiClient.postApi(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
		.thenReturn(str);
		 InputStream result=decryptor.decrypt(inputStream, "84071493960000320190110145452");	
		 assertNull(result);
	}

	@Test(expected = ApiNotAccessibleException.class)
	public void HttpServerErrorExceptionTest()
			throws FileNotFoundException, PacketDecryptionFailureException, ApiNotAccessibleException {

		ApiNotAccessibleException apisNotAccessibleException = Mockito.mock(ApiNotAccessibleException.class);
		HttpServerErrorException httpServerErrorException = new HttpServerErrorException(
				HttpStatus.INTERNAL_SERVER_ERROR, "KER-FSE-004:encrypted data is corrupted or not base64 encoded");
		Mockito.when(apisNotAccessibleException.getCause()).thenReturn(httpServerErrorException);
		Mockito.when(restApiClient.postApi(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
		.thenThrow(apisNotAccessibleException);

		InputStream result=decryptor.decrypt(inputStream, "84071493960000320190110145452");
		assertNull(result);
	}

	@Test(expected = PacketDecryptionFailureException.class)
	public void PacketDecryptionFailureExceptionTest()
			throws FileNotFoundException, ApiNotAccessibleException, PacketDecryptionFailureException {

		ApiNotAccessibleException apisNotAccessibleException = new ApiNotAccessibleException(
				"Packet Decryption failure");
		Mockito.when(restApiClient.postApi(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
		.thenThrow(apisNotAccessibleException);
		InputStream result=decryptor.decrypt(inputStream, "84071493960000320190110145452");
		assertNull(inputStream);
	}

	@Test(expected = PacketDecryptionFailureException.class)
	public void invalidPacketFormatTest() throws PacketDecryptionFailureException, ApiNotAccessibleException {
		decryptor.decrypt(inputStream, "01901101456");

	}

	@Test(expected = PacketDecryptionFailureException.class)
	public void invalidPacketFormatParsingDateTimeTest()
			throws PacketDecryptionFailureException, ApiNotAccessibleException {
		InputStream result=decryptor.decrypt(inputStream, "8407149396000032019T110145452");
		assertNull(result);
	}

	@Test(expected = PacketDecryptionFailureException.class)
	public void decryptErrorCryptoManagerTest()
			throws PacketDecryptionFailureException, ApiNotAccessibleException {
		CryptomanagerResponseDto cryptomanagerResponseDto = new CryptomanagerResponseDto();
		cryptomanagerResponseDto.setErrors(Arrays.asList(new ServiceError("Error-001", "Error-Message-001")));
		Mockito.when(restApiClient.postApi(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
		.thenReturn(cryptomanagerResponseDto);
		InputStream result=decryptor.decrypt(inputStream, "84071493960000320190110145452");
		assertNotNull(result);
	}

}
