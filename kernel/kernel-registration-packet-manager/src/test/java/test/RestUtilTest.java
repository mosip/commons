package test;

import static org.hamcrest.CoreMatchers.any;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.assertj.core.internal.bytebuddy.jar.asm.commons.Method;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.packetmanager.dto.AuditDto;
import io.mosip.kernel.packetmanager.exception.ApiNotAccessibleException;
import io.mosip.kernel.packetmanager.util.RestUtil;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.models.HttpMethod;


@ContextConfiguration(classes = { AppConfig.class })
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@PrepareForTest({ SSLContextBuilder.class,HttpClientBuilder.class,TrustManagerFactory.class,CloseableHttpResponse.class,SSLContexts.class,SSLContext.class,TrustStrategy.class})
public class RestUtilTest {
	
	@InjectMocks
	private RestUtil restApiClient=new RestUtil();

	/** The env. */
	@Mock
	private Environment env;
	
	
	private ObjectMapper objectMapper=new ObjectMapper();
	
	private AuditDto auditDto;
	
	//@Mock
	//private SSLContext sslContext;
	@Mock
	private SSLContext sslContext;

	@Mock
	private SSLContexts sslContexts;
	
	@Mock
	private SSLContextBuilder sslContextBuilder;

	@Autowired
	private RestTemplate restTemplate;
	
	@Mock
	private HttpClientBuilder httpClientBuilder;
	

	
	@Before
	public void setUp() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, ClientProtocolException, IOException {
		

		org.apache.http.HttpEntity httpEntity=mock(org.apache.http.HttpEntity.class);
		Header[] header=new Header[] {mock(Header.class)};
	    CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
	    
        HttpGet httpGet = mock(HttpGet.class);
        HttpPost httpPost = mock(HttpPost.class);
        PowerMockito.mockStatic(HttpClientBuilder.class); 
        PowerMockito.mockStatic(CloseableHttpResponse.class);
        CloseableHttpResponse httpResponse = mock(CloseableHttpResponse.class);
        httpResponse.setEntity(httpEntity);
        PowerMockito.when(HttpClientBuilder.create()).thenReturn(httpClientBuilder);
        PowerMockito.when(httpClientBuilder.build()).thenReturn(httpClient);
		Mockito.when(env.getProperty("token.request.issuerUrl")).thenReturn("http://localhost:8080");
		when(httpResponse.getHeaders(Mockito.anyString())).thenReturn(header);
	    when(httpClient.execute(Mockito.any())).thenReturn(httpResponse);
	    when(httpResponse.getHeaders("Set-Cookie")[0].getValue()).thenReturn("as45dfgh78er9f;");
	    PowerMockito.when(httpResponse.getEntity()).thenReturn(httpEntity);
		Mockito.when(env.getProperty("token.request.clientId")).thenReturn("c123");
		Mockito.when(env.getProperty("token.request.id")).thenReturn("r456");
		Mockito.when(env.getProperty("token.request.version")).thenReturn("1.0");
		Mockito.when(env.getProperty("KEYBASEDTOKENAPI")).thenReturn("test");
		auditDto = new AuditDto();
	}

	@Test(expected = ApiNotAccessibleException.class)
	public void testApiNotAccessibleExceptionForGetApi() throws Exception {
		AuditDto objectA = null;
		URI uri = null;
		PowerMockito.when(env.getProperty(ArgumentMatchers.any())).thenReturn("AUDIT");
		restApiClient.getApi(uri,AuditDto.class);
	}

	@Test
	public void testGetToken() throws IOException {
		String str= restApiClient.getToken();
		assertNotNull(str);
	}

	@Test(expected = ApiNotAccessibleException.class)
	public void testApiNotAccessibleExceptionForPostApi() throws Exception {
		
		Mockito.when(env.getProperty(ArgumentMatchers.any())).thenReturn("AUDIT");
		restApiClient.postApi("localhost:8080/", MediaType.APPLICATION_JSON, auditDto, AuditDto.class);
	}


}
