package io.mosip.kernel.ridgenerator.test.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import io.mosip.kernel.ridgenerator.dto.RidGeneratorResponseDto;
import io.mosip.kernel.ridgenerator.service.RidGeneratorService;
import io.mosip.kernel.ridgenerator.test.TestBootApplication;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = TestBootApplication.class)
public class RidGeneratorControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private RidGeneratorService<RidGeneratorResponseDto> ridGeneratorService;

	@Test
	@WithUserDetails("reg-processor")
	public void generateRidTest() throws Exception {
		String rid = "21347867870000120190329060437";
		RidGeneratorResponseDto response = new RidGeneratorResponseDto();
		response.setRid(rid);
		when(ridGeneratorService.generateRid(Mockito.any(), Mockito.any())).thenReturn(response);
		mockMvc.perform(get("/generate/rid/21347/86787").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}
}
