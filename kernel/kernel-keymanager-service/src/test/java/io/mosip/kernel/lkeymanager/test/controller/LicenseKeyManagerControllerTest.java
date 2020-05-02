package io.mosip.kernel.lkeymanager.test.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.keymanager.spi.KeyStore;
import io.mosip.kernel.core.licensekeymanager.spi.LicenseKeyManagerService;
import io.mosip.kernel.keymanagerservice.test.KeymanagerTestBootApplication;
import io.mosip.kernel.lkeymanager.dto.LicenseKeyGenerationDto;
import io.mosip.kernel.lkeymanager.dto.LicenseKeyMappingDto;

@SpringBootTest(classes = KeymanagerTestBootApplication.class)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class LicenseKeyManagerControllerTest {
	
	@MockBean
	private KeyStore keyStore;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private LicenseKeyManagerService<String, LicenseKeyGenerationDto, LicenseKeyMappingDto> service;

	/**
	 * TEST SCENARIO : Testing generation end point.
	 */
	@Test
	@WithUserDetails("reg-processor")
	public void licenseKeyGenerationTest() throws Exception {
		LicenseKeyGenerationDto licenseKeyGenerationDto = new LicenseKeyGenerationDto();
		licenseKeyGenerationDto.setLicenseExpiryTime(LocalDateTime.of(2019, Month.FEBRUARY, 9, 10, 23, 0));
		licenseKeyGenerationDto.setTspId("TESTID");
		given(service.generateLicenseKey(Mockito.any())).willReturn("asdfghkngyrthgfyt");
		String json = objectMapper.writeValueAsString(licenseKeyGenerationDto);
		mockMvc.perform(post("/license/generate").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk());
	}

	/**
	 * TEST SCENARIO : Testing mapping end point.
	 */
	@Test
	@WithUserDetails("reg-processor")
	public void licenseKeyMappingTest() throws Exception {
		List<String> permissions = new ArrayList<>();
		permissions.add("permission1");
		permissions.add("permission2");
		LicenseKeyMappingDto licenseKeyMappingDto = new LicenseKeyMappingDto();
		licenseKeyMappingDto.setLicenseKey("fqELcNGoaEeuuJAs");
		licenseKeyMappingDto.setTspId("TESTID");
		licenseKeyMappingDto.setPermissions(permissions);
		given(service.mapLicenseKey(Mockito.any())).willReturn("Mapped License with the permissions");
		String json = objectMapper.writeValueAsString(licenseKeyMappingDto);
		mockMvc.perform(post("/license/permission").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk());
	}

	/**
	 * TEST SCENARIO : Testing fetch end point.
	 */
	@Test
	@WithUserDetails("reg-processor")
	public void licenseKeyFetchTest() throws Exception {
		List<String> listPermissions = new ArrayList<>();
		listPermissions.add("PERMISSION1");
		listPermissions.add("PERMISSION2");
		given(service.fetchLicenseKeyPermissions(Mockito.any(), Mockito.any())).willReturn(listPermissions);
		mockMvc.perform(get("/license/permission?licenseKey=fqELcNGoaEeuuJAs&tspId=TSPID")
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.response.permissions[0]", is("PERMISSION1")));
	}
}
