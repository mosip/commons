package test;

import static org.hamcrest.CoreMatchers.any;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.packetmanager.constants.IDschemaConstants;
import io.mosip.kernel.packetmanager.dto.SchemaResponseDto;
import io.mosip.kernel.packetmanager.exception.ApiNotAccessibleException;
import io.mosip.kernel.packetmanager.exception.PacketDecryptionFailureException;
import io.mosip.kernel.packetmanager.spi.PacketReaderService;
import io.mosip.kernel.packetmanager.util.IdSchemaUtils;
import io.mosip.kernel.packetmanager.util.RestUtil;
import io.mosip.kernel.packetmanager.util.ZipUtils;

@RunWith(PowerMockRunner.class)
@ContextConfiguration(classes = AppConfig.class, loader = AnnotationConfigContextLoader.class)
@PrepareForTest({ZipUtils.class,System.class})
public class IdSchemaUtilsTest {
	
	@InjectMocks
	private IdSchemaUtils idSchemaUtils=new IdSchemaUtils();
	
	@Mock
	private ObjectMapper objectMapper;
	
	@Mock
	private RestUtil restApiClient;
	
	@Mock
	Environment env;
	
	private String value;
	
	@Mock
	RestTemplate restTemplate;
	
	@Mock
	private SchemaResponseDto schemaResponseDto;
		
	private String jsonString="{\"response\":{\"schemaJson\":{\"$schema\":\"http://json-schema.org/draft-07/schema#\",\"properties\":{\"identity\":{\"properties\":{\"36e60a08-7ff4-4bbd-b45c-d0116cb5716a\":{\"fieldCategory\":\"test1\"}}}},\"additionalProperties\":false,\"title\":\"mosip id schema\",\"type\":\"object\"}}}";
	
	private String schemaJson = "{\"id\":\"36e60a08-7ff4-4bbd-b45c-d0116cb5716a\",\"idVersion\":0.1,\"schema\":[{\"id\":\"IDSchemaVersion\",\"properties\":\"ID Schema Version\",\"labelName\":\"IDSchemaVersion\",\"type\":\"number\",\"minimum\":0,\"maximum\":0,\"controlType\":null,\"fieldType\":\"default\",\"format\":\"none\",\"fieldCategory\":\"pvt\",\"inputRequired\":false,\"validators\":[],\"bioAttributes\":null,\"requiredOn\":null,\"required\":true}],\"response\":{\"id\":\"IDSchemaVersion\",\"description\":\"ID Schema Version\",\"labelName\":\"IDSchemaVersion\",\"type\":\"number\",\"minimum\":0,\"maximum\":0,\"controlType\":null,\"fieldType\":\"default\",\"format\":\"none\",\"fieldCategory\":\"pvt\",\"schemaJson\":\"{\\\"$schema\\\":\\\"http:\\\\/\\\\/json-schema.org\\\\/draft-07\\\\/schema#\\\",\\\"properties\\\":\\\"test mosip id schema\\\",\\\"additionalProperties\\\":false,\\\"title\\\":\\\"mosip id schema\\\",\\\"type\\\":\\\"object\\\",\\\"definitions\\\":{\\\"simpleType\\\":{\\\"uniqueItems\\\":true,\\\"additionalItems\\\":false,\\\"type\\\":\\\"array\\\",\\\"items\\\":{\\\"additionalProperties\\\":false,\\\"type\\\":\\\"object\\\",\\\"required\\\":[\\\"language\\\",\\\"value\\\"],\\\"properties\\\":{\\\"language\\\":{\\\"type\\\":\\\"string\\\"},\\\"value\\\":{\\\"type\\\":\\\"string\\\"}}}}\",\"inputRequired\":false},\"effectiveFrom\":\"2021-04-25T20:05:55.171\"}";
	
	private String jsonString1="{\"response\":{\"schemaJson\":{\"$schema\":\"http://json-schema.org/draft-07/schema#\",\"properties\":{\"identity\":{\"properties\":{\"36e60a08-7ff4-4bbd-b45c-d0116cb5716a\":{}}}},\"additionalProperties\":false,\"title\":\"mosip id schema\",\"type\":\"object\"}}}";
	
	private String jsonString2="{\"response\":{\"schemaJson\":{\"$schema\":\"http://json-schema.org/draft-07/schema#\",\"properties\":{\"identity\":{}},\"additionalProperties\":false,\"title\":\"mosip id schema\",\"type\":\"object\"}}}";
	@Mock
	PacketReaderService packetReaderService;
	
	@Before
	public void setUp() {
		schemaResponseDto=new SchemaResponseDto();
		when(env.getProperty("schema.default.fieldCategory")).thenReturn("testFieled1,testfield2");
		when(env.getProperty("IDSCHEMA"))
		.thenReturn("http://localhost:9001");

       
	}
	@Test
	public void testGetSourceWithInvalidJson() throws ApiNotAccessibleException, IOException, PacketDecryptionFailureException, io.mosip.kernel.core.exception.IOException {
		
		Mockito.when(packetReaderService.getFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
		.thenReturn(new ByteArrayInputStream(new String("uingeneratorstage").getBytes()));
		PowerMockito.when(restApiClient.getApi(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(schemaJson);
		String result=idSchemaUtils.getSource(Mockito.anyString(), 1.0);
		assertNull(result);
	}
	@Test
	public void testApiNotAccsibleExeption() throws IOException, PacketDecryptionFailureException, io.mosip.kernel.core.exception.IOException, ApiNotAccessibleException {
		
		ApiNotAccessibleException exp=new ApiNotAccessibleException();
		Mockito.when(packetReaderService.getFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
		.thenReturn(new ByteArrayInputStream(new String("uingeneratorstage").getBytes()));
		PowerMockito.when(restApiClient.getApi(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(schemaJson);
		PowerMockito.when(idSchemaUtils.getIdSchema(Mockito.anyDouble())).thenThrow(exp);
		idSchemaUtils.getSource(Mockito.anyString(), 1.0);
	}
	
	@Test(expected = IOException.class)
	public void testIOException() throws ApiNotAccessibleException, IOException, PacketDecryptionFailureException, io.mosip.kernel.core.exception.IOException {
		String str = "response:";
		Mockito.when(packetReaderService.getFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
		.thenReturn(new ByteArrayInputStream(new String("uingeneratorstage").getBytes()));
		PowerMockito.when(restApiClient.getApi(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn("test");
		idSchemaUtils.getSource(Mockito.anyString(), 1.0);
	}
	
	@Test(expected = ClassCastException.class)
	public void testClassCastException() throws ApiNotAccessibleException, IOException, PacketDecryptionFailureException, io.mosip.kernel.core.exception.IOException {
		Mockito.when(packetReaderService.getFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
		.thenReturn(new ByteArrayInputStream(new String("uingeneratorstage").getBytes()));
		PowerMockito.when(restApiClient.getApi(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(schemaResponseDto);
		idSchemaUtils.getSource(Mockito.anyString(), 1.0);
	}
	@Test(expected = IOException.class)
	public void testIOAndJsonException() throws ApiNotAccessibleException, IOException, PacketDecryptionFailureException, io.mosip.kernel.core.exception.IOException {
		String str="{\n" + 
				"	\"id\": \"36e60a08-7ff4-4bbd-b45c-d0116cb5716a\",\n" + 
				"	\"idVersion\": 0.1\n" + 
				"}";
		Mockito.when(packetReaderService.getFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
		.thenReturn(new ByteArrayInputStream(new String("uingeneratorstage").getBytes()));
		PowerMockito.when(restApiClient.getApi(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(str);
		idSchemaUtils.getSource(Mockito.anyString(), 1.0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetJson() throws ApiNotAccessibleException, IOException, PacketDecryptionFailureException, io.mosip.kernel.core.exception.IOException {
		String str="{\n" + 
				"	\"id\": \"36e60a08-7ff4-4bbd-b45c-d0116cb5716a\",\n" + 
				"	\"idVersion\": 0.1\n" + 
				"}";
		when(restTemplate.getForObject(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(str);
		IdSchemaUtils.getJson("localhost:8080", "/test");
	}
	@Test
	public void testGetSourceForValidJson() throws IOException, PacketDecryptionFailureException, io.mosip.kernel.core.exception.IOException, ApiNotAccessibleException {
		
		ApiNotAccessibleException exp=new ApiNotAccessibleException();
		Mockito.when(packetReaderService.getFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
		.thenReturn(new ByteArrayInputStream(new String("uingeneratorstage").getBytes()));
		PowerMockito.when(restApiClient.getApi(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(jsonString);
		String result=idSchemaUtils.getSource("36e60a08-7ff4-4bbd-b45c-d0116cb5716a", 1.0);
		assertNotNull(result);
	}

	@Test
	public void testGetSourceWithoutFieldCategory() throws IOException, PacketDecryptionFailureException, io.mosip.kernel.core.exception.IOException, ApiNotAccessibleException {
		
		ApiNotAccessibleException exp=new ApiNotAccessibleException();
		Mockito.when(packetReaderService.getFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
		.thenReturn(new ByteArrayInputStream(new String("uingeneratorstage").getBytes()));
		PowerMockito.when(restApiClient.getApi(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(jsonString1);
		String result=idSchemaUtils.getSource("36e60a08-7ff4-4bbd-b45c-d0116cb5716a", 1.0);
		assertNull(result);
	}
	@Test
	public void testGetSourceWithoutProperties() throws IOException, PacketDecryptionFailureException, io.mosip.kernel.core.exception.IOException, ApiNotAccessibleException {
		
		ApiNotAccessibleException exp=new ApiNotAccessibleException();
		Mockito.when(packetReaderService.getFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
		.thenReturn(new ByteArrayInputStream(new String("uingeneratorstage").getBytes()));
		PowerMockito.when(restApiClient.getApi(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(jsonString2);
		String result=idSchemaUtils.getSource("36e60a08-7ff4-4bbd-b45c-d0116cb5716a", 1.0);
		assertNull(result);
	}

}
