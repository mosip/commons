package io.mosip.kernel.masterdata.test.integration;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.masterdata.dto.LocationCreateDto;
import io.mosip.kernel.masterdata.dto.LocationDto;
import io.mosip.kernel.masterdata.entity.Location;
import io.mosip.kernel.masterdata.entity.LocationHierarchy;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;
import io.mosip.kernel.masterdata.repository.LocationHierarchyRepository;
import io.mosip.kernel.masterdata.repository.LocationRepository;
import io.mosip.kernel.masterdata.utils.AuditUtil;
import io.mosip.kernel.masterdata.utils.MasterdataCreationUtil;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class LocationControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AuditUtil auditUtil;

	@MockBean
	private LocationRepository repo;
	private Location parentLoc;
	private Location location1;
	private Location location2;
	private Location location3;
	private LocationDto dto1;
	private LocationCreateDto locationCreateDto;
	private LocationDto dto2;
	private List<Location> parentLocList;
	private List<Object[]> locationObjects;

	@MockBean
	MasterdataCreationUtil masterdataCreationUtil;

	@Autowired
	private ObjectMapper mapper;
	
	@MockBean
	private LocationHierarchyRepository locationHierarchyRepository;

	private RequestWrapper<LocationDto> request;
	private RequestWrapper<LocationCreateDto> createRequest;

	@Before
	public void setup() {
		parentLoc = new Location("XYZ", "LOCATION NAME", (short) 3, "City", "test", "eng", null);
		parentLoc.setIsActive(true);
		location1 = new Location("MDDR", "LOCATION NAME", (short) 3, "City", "XYZ", "eng", null);
		location2 = new Location("", "LOCATION NAME", (short) 3, "City", "XYZ", "ara", null);
		location3 = new Location("", "LOCATION NAME", (short) 3, "City", "XYZ", "eng", null);
		dto1 = new LocationDto("MMDR", "Location Name", (short) 3, "City", "XYZ", "eng", false);
		dto2 = new LocationDto("", "Location Name", (short) 3, "City", "XYZ", "ara", false);
		locationCreateDto = new LocationCreateDto("", "Location Name", (short) 3, "City", "XYZ", "ara", false);
		request = new RequestWrapper<>();
		request.setId("1.0");
		request.setRequesttime(LocalDateTime.now());
		request.setMetadata("masterdata.location.create");
		createRequest = new RequestWrapper<>();
		createRequest.setId("1.0");
		createRequest.setRequesttime(LocalDateTime.now());
		createRequest.setMetadata("masterdata.location.create");
		when(repo.save(Mockito.any())).thenReturn(location1);
		when(repo.save(Mockito.any())).thenReturn(parentLoc);
		LocationHierarchy hierarchy=new LocationHierarchy((short) 3, "City", "eng");
		when(locationHierarchyRepository.findByLangCodeAndLevelAndName(Mockito.anyString(), Mockito.anyShort(),
				Mockito.anyString())).thenReturn(hierarchy);
		parentLocList = new ArrayList<>();
		parentLocList.add(parentLoc);
		doNothing().when(auditUtil).auditRequest(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		Object[] object1= {(short) 1,"hierarchy_level_name",true};
		Object[] object2= {(short) 2,"hierarchy_level_name",true};
		locationObjects=Arrays.asList(object1,object2);
		
	}

	@Test
	@WithUserDetails("global-admin")
	public void locationCreateSuccess() throws Exception {
		createRequest.setRequest(locationCreateDto);
		String requestJson = mapper.writeValueAsString(createRequest);
		when(masterdataCreationUtil.createMasterData(Location.class, locationCreateDto)).thenReturn(locationCreateDto);
		when(repo.findLocationHierarchyByCodeAndLanguageCode(Mockito.any(), Mockito.any())).thenReturn(parentLocList);
		// when(repo.findByNameAndLevelLangCode(Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(parentLocList);
		when(repo.create(Mockito.any())).thenReturn(location1);
		mockMvc.perform(post("/locations").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void locationParentNotFoundSuccess() throws Exception {
		createRequest.setRequest(locationCreateDto);
		String requestJson = mapper.writeValueAsString(createRequest);
		// when(repo.findLocationHierarchyByCodeAndLanguageCode(Mockito.any(),Mockito.any())).thenThrow(new
		// MasterDataServiceException("","Parent location not found"));
		// when(repo.findByNameAndLevelLangCode(Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(parentLocList);
		// when(repo.create(Mockito.any())).thenReturn(location1);
		mockMvc.perform(post("/locations").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andExpect(status().isOk());
	}
	
	@Test
	@WithUserDetails("global-admin")
	public void locationParentNotFoundException() throws Exception {
		createRequest.setRequest(locationCreateDto);
		String requestJson = mapper.writeValueAsString(createRequest);
		when(masterdataCreationUtil.createMasterData(Location.class, locationCreateDto)).thenReturn(locationCreateDto);
		when(repo.findLocationHierarchyByCodeAndLanguageCode(Mockito.any(), Mockito.any())).thenReturn(null);
		// when(repo.create(Mockito.any())).thenReturn(location1);
		mockMvc.perform(post("/locations").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void createActiveLocationSuccess() throws Exception {
		location1.setIsActive(true);
		when(repo.save(Mockito.any())).thenReturn(Arrays.asList(location1));
		dto1.setIsActive(true);
		createRequest.setRequest(locationCreateDto);
		String requestJson = mapper.writeValueAsString(createRequest);
		when(masterdataCreationUtil.createMasterData(Location.class, locationCreateDto)).thenReturn(locationCreateDto);
		when(repo.findLocationHierarchyByCodeAndLanguageCode(Mockito.any(), Mockito.any())).thenReturn(parentLocList);
		when(repo.findByNameAndLevelLangCode(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(parentLocList);
		when(repo.create(Mockito.any())).thenReturn(location1);
		mockMvc.perform(post("/locations").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void updateActiveLocationSuccess() throws Exception {
		location1.setIsActive(true);
		dto1.setIsActive(true);
		request.setRequest(dto1);
		String requestJson = mapper.writeValueAsString(request);
		when(repo.findLocationHierarchyByCodeAndLanguageCode(Mockito.any(), Mockito.any()))
				.thenReturn(Arrays.asList(location1));
		when(masterdataCreationUtil.updateMasterData(Location.class, dto1)).thenReturn(dto1);
		when(repo.findLocationByCodeAndLanguageCode(Mockito.any(), Mockito.any())).thenReturn(location1);
		when(repo.update(Mockito.any())).thenReturn(location1);
		mockMvc.perform(put("/locations").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void updateParentLocationNotFoundException() throws Exception {
		location1.setIsActive(true);
		dto1.setIsActive(true);
		request.setRequest(dto1);
		String requestJson = mapper.writeValueAsString(request);
		when(repo.findLocationHierarchyByCodeAndLanguageCode(Mockito.any(), Mockito.any())).thenReturn(null);
		when(masterdataCreationUtil.updateMasterData(Location.class, dto1)).thenReturn(dto1);
		when(repo.findLocationByCodeAndLanguageCode(Mockito.any(), Mockito.any())).thenReturn(location1);
		when(repo.update(Mockito.any())).thenReturn(location1);
		mockMvc.perform(put("/locations").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void updateIllegalExceptionTest() throws Exception {
		location1.setIsActive(true);
		dto1.setIsActive(true);
		request.setRequest(dto1);
		String requestJson = mapper.writeValueAsString(request);
		when(repo.findLocationHierarchyByCodeAndLanguageCode(Mockito.any(), Mockito.any())).thenReturn(Arrays.asList(location1));
		when(masterdataCreationUtil.updateMasterData(Location.class, dto1)).thenReturn(dto1);
		when(repo.findLocationByCodeAndLanguageCode(Mockito.any(), Mockito.any())).thenReturn(location1);
		when(repo.update(Mockito.any())).thenThrow(new IllegalArgumentException());
		mockMvc.perform(put("/locations").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andExpect(status().is5xxServerError());
	}
	
	@Test
	@WithUserDetails("global-admin")
	public void updateLocationNotFoundTest() throws Exception {
		location1.setIsActive(true);
		dto1.setIsActive(true);
		request.setRequest(dto1);
		String requestJson = mapper.writeValueAsString(request);
		when(repo.findLocationHierarchyByCodeAndLanguageCode(Mockito.any(), Mockito.any())).thenReturn(Arrays.asList(location1));
		when(masterdataCreationUtil.updateMasterData(Location.class, dto1)).thenReturn(dto1);
		when(repo.findLocationByCodeAndLanguageCode(Mockito.any(), Mockito.any())).thenReturn(null);
		mockMvc.perform(put("/locations").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andExpect(status().is5xxServerError());
	}
	
	@Test
	@WithUserDetails("global-admin")
	public void updateChildStatusExceptionTest() throws Exception {
		location1.setIsActive(true);
		dto1.setIsActive(false);
		request.setRequest(dto1);
		String requestJson = mapper.writeValueAsString(request);
		when(repo.findLocationHierarchyByCodeAndLanguageCode(Mockito.any(), Mockito.any())).thenReturn(Arrays.asList(location1));
		
		when(repo.findLocationHierarchyByParentLocCodeAndLanguageCode(Mockito.any(), Mockito.any())).thenReturn(Arrays.asList(location1));
		mockMvc.perform(put("/locations").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andExpect(status().is5xxServerError());
	}
	
	@Test
	@WithUserDetails("global-admin")
	public void updateLocationAlreadyExistsUnderHeirarchyExceptionTest() throws Exception {
		location1.setIsActive(true);
		dto1.setIsActive(true);
		request.setRequest(dto1);
		String requestJson = mapper.writeValueAsString(request);
		when(repo.findLocationHierarchyByCodeAndLanguageCode(Mockito.any(), Mockito.any()))
				.thenReturn(Arrays.asList(location1));
		when(repo.findByNameAndLevelLangCodeNotCode(Mockito.any(),Mockito.any(),Mockito.any(), Mockito.any())).thenReturn(Arrays.asList(location1));
		mockMvc.perform(put("/locations").contentType(MediaType.APPLICATION_JSON).content(requestJson))
		.andExpect(status().isOk());
	}
	
	

	@Test
	@WithUserDetails("global-admin")
	public void createDefaultlangMissing() throws Exception {
		createRequest.setRequest(locationCreateDto);
		String requestJson = mapper.writeValueAsString(createRequest);
		when(masterdataCreationUtil.createMasterData(Location.class, locationCreateDto)).thenReturn(locationCreateDto);
		when(repo.findLocationHierarchyByCodeAndLanguageCode(Mockito.any(), Mockito.any())).thenReturn(parentLocList);
		when(repo.findByNameAndLevelLangCode(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(parentLocList);
		when(repo.create(Mockito.any())).thenReturn(location1);
		mockMvc.perform(post("/locations").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void createActiveLocationFailure() throws Exception {
		// dto1.setIsActive(true);
		createRequest.setRequest(locationCreateDto);
		String requestJson = mapper.writeValueAsString(createRequest);
		when(masterdataCreationUtil.createMasterData(Location.class, locationCreateDto)).thenReturn(locationCreateDto);
		when(repo.findLocationHierarchyByCodeAndLanguageCode(Mockito.any(), Mockito.any())).thenReturn(parentLocList);
		when(repo.findByNameAndLevelLangCode(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(parentLocList);
		when(repo.create(Mockito.any())).thenReturn(location1);
		mockMvc.perform(post("/locations").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void createLocationHierarchyLevelAlreadtExist() throws Exception {
		when(repo.findByNameAndLevel(Mockito.anyString(), Mockito.anyShort())).thenReturn(Arrays.asList(location1));
		createRequest.setRequest(locationCreateDto);
		String requestJson = mapper.writeValueAsString(createRequest);
		when(masterdataCreationUtil.createMasterData(Location.class, locationCreateDto)).thenReturn(locationCreateDto);
		when(repo.findLocationHierarchyByCodeAndLanguageCode(Mockito.any(), Mockito.any())).thenReturn(parentLocList);
		when(repo.findByNameAndLevelLangCode(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(parentLocList);
		when(repo.create(Mockito.any())).thenReturn(location1);
		mockMvc.perform(post("/locations").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void createLocatioParentLocationnoExist() throws Exception {
		createRequest.setRequest(locationCreateDto);
		String requestJson = mapper.writeValueAsString(createRequest);
		when(masterdataCreationUtil.createMasterData(Location.class, locationCreateDto)).thenReturn(locationCreateDto);
		when(repo.findLocationHierarchyByCodeAndLanguageCode(Mockito.any(), Mockito.any()))
				.thenThrow(new MasterDataServiceException("", "Location not Exist"));
		mockMvc.perform(post("/locations").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void createInvalidLangCode() throws Exception {
		dto2.setLangCode("ABC");
		createRequest.setRequest(locationCreateDto);
		String requestJson = mapper.writeValueAsString(createRequest);
		when(masterdataCreationUtil.createMasterData(Location.class, locationCreateDto)).thenReturn(locationCreateDto);
		when(repo.findLocationHierarchyByCodeAndLanguageCode(Mockito.any(), Mockito.any())).thenReturn(parentLocList);
		when(repo.findByNameAndLevelLangCode(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(parentLocList);
		when(repo.create(Mockito.any())).thenReturn(location1);
		mockMvc.perform(post("/locations").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void createEmptyLangCode() throws Exception {
		dto2.setLangCode("");
		createRequest.setRequest(locationCreateDto);
		String requestJson = mapper.writeValueAsString(createRequest);
		when(masterdataCreationUtil.createMasterData(Location.class, locationCreateDto)).thenReturn(locationCreateDto);
		when(repo.findLocationHierarchyByCodeAndLanguageCode(Mockito.any(), Mockito.any())).thenReturn(parentLocList);
		when(repo.findByNameAndLevelLangCode(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(parentLocList);
		when(repo.create(Mockito.any())).thenReturn(location1);
		mockMvc.perform(post("/locations").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void createInvalidHierarachyLevel() throws Exception {
		dto2.setHierarchyLevel((short) 1);
		createRequest.setRequest(locationCreateDto);
		String requestJson = mapper.writeValueAsString(createRequest);
		when(masterdataCreationUtil.createMasterData(Location.class, locationCreateDto)).thenReturn(locationCreateDto);
		when(repo.findLocationHierarchyByCodeAndLanguageCode(Mockito.any(), Mockito.any())).thenReturn(parentLocList);
		when(repo.findByNameAndLevelLangCode(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(parentLocList);
		when(repo.create(Mockito.any())).thenReturn(location1);
		mockMvc.perform(post("/locations").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void createInvalidCode() throws Exception {
		dto2.setCode("MNB");
		createRequest.setRequest(locationCreateDto);
		String requestJson = mapper.writeValueAsString(createRequest);
		when(masterdataCreationUtil.createMasterData(Location.class, locationCreateDto)).thenReturn(locationCreateDto);
		when(repo.findLocationHierarchyByCodeAndLanguageCode(Mockito.any(), Mockito.any())).thenReturn(parentLocList);
		when(repo.findByNameAndLevelLangCode(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(parentLocList);
		when(repo.create(Mockito.any())).thenReturn(location1);
		mockMvc.perform(post("/locations").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void createEmptyLocationCode() throws Exception {
		dto2.setCode("");
		createRequest.setRequest(locationCreateDto);
		String requestJson = mapper.writeValueAsString(createRequest);
		when(masterdataCreationUtil.createMasterData(Location.class, locationCreateDto)).thenReturn(locationCreateDto);
		when(repo.findLocationHierarchyByCodeAndLanguageCode(Mockito.any(), Mockito.any())).thenReturn(parentLocList);
		when(repo.findByNameAndLevelLangCode(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(parentLocList);
		when(repo.create(Mockito.any())).thenReturn(location1);
		mockMvc.perform(post("/locations").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void createEmptyLocationCodePrimary() throws Exception {
		dto1.setCode("");
		createRequest.setRequest(locationCreateDto);
		String requestJson = mapper.writeValueAsString(createRequest);
		when(masterdataCreationUtil.createMasterData(Location.class, locationCreateDto)).thenReturn(locationCreateDto);
		when(repo.findLocationHierarchyByCodeAndLanguageCode(Mockito.any(), Mockito.any())).thenReturn(parentLocList);
		when(repo.findByNameAndLevelLangCode(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(parentLocList);
		when(repo.create(Mockito.any())).thenReturn(location1);
		mockMvc.perform(post("/locations").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void createSaveFailure() throws Exception {
		when(repo.save(Mockito.any())).thenThrow(DataIntegrityViolationException.class);
		createRequest.setRequest(locationCreateDto);
		String requestJson = mapper.writeValueAsString(createRequest);
		when(masterdataCreationUtil.createMasterData(Location.class, locationCreateDto)).thenReturn(locationCreateDto);
		when(repo.findLocationHierarchyByCodeAndLanguageCode(Mockito.any(), Mockito.any())).thenReturn(parentLocList);
		when(repo.findByNameAndLevelLangCode(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(parentLocList);
		when(repo.create(Mockito.any())).thenReturn(location1);
		mockMvc.perform(post("/locations").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void createLocationException() throws Exception {
		when(repo.save(Mockito.any())).thenThrow(DataIntegrityViolationException.class);
		createRequest.setRequest(locationCreateDto);
		String requestJson = mapper.writeValueAsString(createRequest);
		when(masterdataCreationUtil.createMasterData(Location.class, locationCreateDto)).thenReturn(locationCreateDto);
		when(repo.findLocationHierarchyByCodeAndLanguageCode(Mockito.any(), Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot insert", null));
		mockMvc.perform(post("/locations").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void createLocationIllegalException() throws Exception {
		when(repo.save(Mockito.any())).thenThrow(DataIntegrityViolationException.class);
		createRequest.setRequest(locationCreateDto);
		String requestJson = mapper.writeValueAsString(createRequest);
		when(masterdataCreationUtil.createMasterData(Location.class, locationCreateDto)).thenReturn(locationCreateDto);
		when(repo.findLocationHierarchyByCodeAndLanguageCode(Mockito.any(), Mockito.any()))
				.thenThrow(new IllegalArgumentException());
		mockMvc.perform(post("/locations").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andExpect(status().isOk());
	}
	
	@Test
	@WithUserDetails("zonal-admin")
	public void getLocationDetailsSuccess() throws Exception {
		when(repo.findDistinctLocationHierarchyByIsDeletedFalse(Mockito.any())).thenReturn(locationObjects);
		mockMvc.perform(get("/locations/eng").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}
	
	@Test
	@WithUserDetails("zonal-admin")
	public void getLocationDetailsDataAccessException() throws Exception {
		when(repo.findDistinctLocationHierarchyByIsDeletedFalse(Mockito.any())).thenThrow(new DataAccessLayerException("","",null));
		mockMvc.perform(get("/locations/eng").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().is5xxServerError());
	}
	
	@Test
	@WithUserDetails("zonal-admin")
	public void getLocationDetailsLocationsNotFound() throws Exception {
		when(repo.findDistinctLocationHierarchyByIsDeletedFalse(Mockito.any())).thenReturn(new ArrayList<Object[]>());
		mockMvc.perform(get("/locations/eng").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}
	
//	@Test
//	@WithUserDetails("zonal-admin")
//	public void getLocationHierarchyByLangCodeSuccess() throws Exception {
//		Location location4 = new Location("BDR", "LOCATION NAME", (short) 3, "City", "MDDR", "eng", null);
//		when(repo.findLocationHierarchyByParentLocCodeAndLanguageCode(Mockito.any(),Mockito.any())).thenReturn(Arrays.asList(location4)).thenReturn(Arrays.asList());
//		when(repo.findLocationHierarchyByCodeAndLanguageCode(Mockito.any(),Mockito.any())).thenReturn(Arrays.asList(location1)).thenReturn(Arrays.asList(parentLoc)).thenReturn(Arrays.asList());
//		mockMvc.perform(get("/locations/MDDR/eng").contentType(MediaType.APPLICATION_JSON))
//				.andExpect(status().isOk());
//	}
	
	@Test
	@WithUserDetails("individual")
	public void getLocationDataByHierarchyNameSuccess() throws Exception {
		Location location4 = new Location("BDR", "LOCATION NAME", (short) 3, "City", "MDDR", "eng", null);
		when(repo.findAllByHierarchyNameIgnoreCase(Mockito.any())).thenReturn(Arrays.asList(location4));
		mockMvc.perform(get("/locations/locationhierarchy/City").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}
	
	@Test
	@WithUserDetails("individual")
	public void getLocationDataByHierarchyNameDataAccessLayerException() throws Exception {
		when(repo.findAllByHierarchyNameIgnoreCase(Mockito.any())).thenThrow(new DataAccessLayerException("","",null));
		mockMvc.perform(get("/locations/locationhierarchy/City").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().is5xxServerError());
	}
	
	@Test
	@WithUserDetails("individual")
	public void getLocationDataByHierarchyNameLocationNotFound() throws Exception {
		when(repo.findAllByHierarchyNameIgnoreCase(Mockito.any())).thenReturn(Arrays.asList());
		mockMvc.perform(get("/locations/locationhierarchy/City").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}
	
	@Test
	@WithUserDetails("global-admin")
	public void deleteLocationDetialsSuccess() throws Exception {
		Location location4 = new Location("BDR", "LOCATION NAME", (short) 3, "City", "MDDR", "eng", null);
		when(repo.findByCode(Mockito.any())).thenReturn(Arrays.asList(location4));
		when(repo.update(Mockito.any(Location.class))).thenReturn(location4);
		mockMvc.perform(delete("/locations/MDDR").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}
	
	@Test
	@WithUserDetails("global-admin")
	public void deleteLocationDetialsLocationNotFound() throws Exception {
		
		when(repo.findByCode(Mockito.any())).thenReturn(Arrays.asList());
		
		mockMvc.perform(delete("/locations/MDDR").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}
	
	@Test
	@WithUserDetails("individual")
	public void deleteLocationDetialsDataAccessLayerException() throws Exception {
		
		when(repo.findByCode(Mockito.any())).thenThrow(new DataAccessLayerException("","",null));
		
		mockMvc.perform(delete("/locations/MDDR").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().is5xxServerError());
	}
	
	@Test
	@WithUserDetails("individual")
	public void getImmediateChildrenByLocCodeAndLangCodeSuccess() throws Exception {
		Location location4 = new Location("BDR", "LOCATION NAME", (short) 3, "City", "MDDR", "eng", null);
		when(repo.findLocationHierarchyByParentLocCodeAndLanguageCode(Mockito.any(),Mockito.any())).thenReturn(Arrays.asList(location4));
		
		mockMvc.perform(get("/locations/immediatechildren/MDDR/eng").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}
	
	@Test
	@WithUserDetails("individual")
	public void getImmediateChildrenByLocCodeAndLangCodeDataAcessException() throws Exception {
		when(repo.findLocationHierarchyByParentLocCodeAndLanguageCode(Mockito.any(),Mockito.any())).thenThrow(DataAccessLayerException.class);
		
		mockMvc.perform(get("/locations/immediatechildren/MDDR/eng").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().is5xxServerError());
	}
	
	@Test
	@WithUserDetails("individual")
	public void getImmediateChildrenByLocCodeAndLangCodeNotfound() throws Exception {
		when(repo.findLocationHierarchyByParentLocCodeAndLanguageCode(Mockito.any(),Mockito.any())).thenReturn(Arrays.asList());
		
		mockMvc.perform(get("/locations/immediatechildren/MDDR/eng").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}
	
}