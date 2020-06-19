package io.mosip.kernel.tokenidgenerator.test.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import io.mosip.kernel.core.keymanager.spi.KeyStore;
import io.mosip.kernel.keymanagerservice.test.KeymanagerTestBootApplication;
import io.mosip.kernel.tokenidgenerator.dto.TokenIDResponseDto;

@SpringBootTest(classes = KeymanagerTestBootApplication.class)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class TokenIdGeneratorIntegrationTest {
	
	@MockBean
	private KeyStore keyStore;

	@Autowired
	private MockMvc mockMvc;

	@Test
	@WithUserDetails("id-auth")
	public void generateTokenIDTest() throws Exception {
		TokenIDResponseDto response = new TokenIDResponseDto();
		response.setTokenID("123456");
		mockMvc.perform(get("/1234/1234").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

	}

	@Test
	@WithUserDetails("id-auth")
	public void generateTokenIdExceptionTest() throws Exception {
		mockMvc.perform(get("/    /  ").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
	}
}
