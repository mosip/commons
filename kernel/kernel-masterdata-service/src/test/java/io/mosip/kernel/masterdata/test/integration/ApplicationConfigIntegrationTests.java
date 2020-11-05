package io.mosip.kernel.masterdata.test.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class ApplicationConfigIntegrationTests {
	@Autowired
	private MockMvc mockMvc;
	
	private ObjectMapper mapper;
	@MockBean
	private RestTemplate restTemplate;
	
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws JSONException {
		mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		JSONObject json = new JSONObject();
		json.put("version", "value");
		ResponseEntity<String> response =new ResponseEntity<String>(json.toString(), HttpStatus.OK);
		Mockito.when(restTemplate.exchange(Mockito.anyString(),Mockito.any(HttpMethod.class), Mockito.any(), Mockito.any(Class.class))).thenReturn(response);
		
	}
	@Test
	@WithUserDetails("zonal-admin")
	public void  getLanguageConfigDetailsSuccessTest() throws Exception {
		mockMvc.perform(get("/applicationconfigs")).andExpect(status().isOk());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@WithUserDetails("zonal-admin")
	public void  getLanguageConfigDetailsExceptionTest() throws Exception {
		RestClientException ex=new RestClientException("exception");
		Mockito.when(restTemplate.exchange(Mockito.anyString(),Mockito.any(HttpMethod.class), Mockito.any(), Mockito.any(Class.class))).thenThrow(ex);
		
		mockMvc.perform(get("/applicationconfigs")).andExpect(status().isInternalServerError());
	}
}
