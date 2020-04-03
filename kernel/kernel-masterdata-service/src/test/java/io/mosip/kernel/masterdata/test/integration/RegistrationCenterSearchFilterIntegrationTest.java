package io.mosip.kernel.masterdata.test.integration;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.masterdata.dto.request.FilterDto;
import io.mosip.kernel.masterdata.dto.request.FilterValueDto;
import io.mosip.kernel.masterdata.dto.request.Pagination;
import io.mosip.kernel.masterdata.dto.request.SearchDto;
import io.mosip.kernel.masterdata.dto.request.SearchFilter;
import io.mosip.kernel.masterdata.dto.request.SearchSort;
import io.mosip.kernel.masterdata.dto.response.PageResponseDto;
import io.mosip.kernel.masterdata.dto.response.RegistrationCenterSearchDto;
import io.mosip.kernel.masterdata.entity.Location;
import io.mosip.kernel.masterdata.entity.Zone;
import io.mosip.kernel.masterdata.repository.RegistrationCenterRepository;
import io.mosip.kernel.masterdata.test.TestBootApplication;
import io.mosip.kernel.masterdata.utils.AuditUtil;
import io.mosip.kernel.masterdata.utils.LocationUtils;
import io.mosip.kernel.masterdata.utils.MasterDataFilterHelper;
import io.mosip.kernel.masterdata.utils.PageUtils;
import io.mosip.kernel.masterdata.utils.RegistrationCenterServiceHelper;
import io.mosip.kernel.masterdata.utils.ZoneUtils;
import io.mosip.kernel.masterdata.validator.FilterColumnValidator;
import io.mosip.kernel.masterdata.validator.FilterTypeValidator;

@SpringBootTest(classes = TestBootApplication.class)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class RegistrationCenterSearchFilterIntegrationTest {
	@Autowired
	private MockMvc mockMvc;
	@MockBean
	private PageUtils pageUtils;

	@MockBean
	private MasterDataFilterHelper masterDataFilterHelper;

	@MockBean
	private RegistrationCenterRepository registrationCenterRepository;
	@MockBean
	private RegistrationCenterServiceHelper serviceHelper;
	@MockBean
	private LocationUtils locationUtils;
	@MockBean
	private FilterTypeValidator filterTypeValidator;
	@MockBean
	private ZoneUtils zoneUtils;
	@MockBean
	private FilterColumnValidator filterColumnValidator;
	@Autowired
	private ObjectMapper objectMapper;

	private SearchSort sort;

	private SearchDto searchDto;

	private SearchFilter filter;
	private SearchFilter filter1;

	private RequestWrapper<SearchDto> request;
	private RequestWrapper<FilterValueDto> requestDto;
	private List<Zone> zones;

	@MockBean
	private AuditUtil auditUtil;
	
	@Before
	public void setup() throws JsonProcessingException {
		sort = new SearchSort();
		sort.setSortType("ASC");
		sort.setSortField("name");
		request = new RequestWrapper<>();
		searchDto = new SearchDto();
		Pagination pagination = new Pagination(0, 10);
		filter = new SearchFilter();
		filter.setColumnName("zone");
		filter.setType("equals");
		filter.setValue("Rabta");
		filter1 = new SearchFilter();
		filter1.setColumnName("centertypename");
		filter1.setType("equals");
		filter1.setValue("regular");
		searchDto.setFilters(Arrays.asList(filter,filter1));
		searchDto.setLanguageCode("eng");
		searchDto.setPagination(pagination);
		searchDto.setSort(Arrays.asList(sort));
		request.setRequest(searchDto);
		Zone zone = new Zone();
		zone.setCode("JRD");
		zone.setLangCode("eng");
		zone.setHierarchyPath("MOR/NTH/ORT/JRD");
		Zone zone1 = new Zone();
		zone1.setCode("TZT");
		zone1.setLangCode("eng");
		zone1.setHierarchyPath("MOR/STH/SOS/TZT");
		Zone zone2 = new Zone();
		zone2.setCode("TTA");
		zone2.setLangCode("eng");
		zone2.setHierarchyPath("MOR/STH/SOS/TTA");
		Zone zone3 = new Zone();
		zone3.setCode("BRT");
		zone3.setLangCode("eng");
		zone3.setHierarchyPath("MOR/NTH/ORT/BRK");
		Zone zone4 = new Zone();
		zone4.setCode("CST");
		zone4.setLangCode("eng");
		zone4.setHierarchyPath("MOR/NTH/ORT/CST");
		Zone zone6 = new Zone();
		zone6.setCode("NTH");
		zone6.setLangCode("eng");
		zone6.setHierarchyPath("MOR/NTH");
		Zone zone5 = new Zone();
		zone5.setCode("NTH");
		zone5.setLangCode("eng");
		zone5.setHierarchyPath("MOR/STH");
		zones=new ArrayList<>();
		zones.addAll(Arrays.asList(zone1,zone2,zone3,zone4,zone5));
		FilterDto filterDto = new FilterDto();
		filterDto.setColumnName("Zone");
		filterDto.setType("invalidType");
		filterDto.setText("abc");
		FilterValueDto filterValueDto = new FilterValueDto();
		filterValueDto.setFilters(Arrays.asList(filterDto));
		filterValueDto.setLanguageCode("eng");
		requestDto = new RequestWrapper<>();
		requestDto.setRequest(filterValueDto);
		doNothing().when(auditUtil).auditRequest(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

	}
	@Test
	@WithUserDetails("global-admin")
	public void searchRegistrationCenterTest() throws Exception {
		List<Location> locations = new ArrayList<>();
		Location location = new Location();
		location.setCode("1001");
		location.setHierarchyName("postalCode");
		location.setLangCode("eng");
		location.setParentLocCode("PAR");
		location.setHierarchyLevel((short) 2);
		location.setName("10045");
		location.setIsActive(Boolean.TRUE);
		locations.add(location);
		String json = objectMapper.writeValueAsString(request);
		when(serviceHelper.fetchLocations(Mockito.anyString())).thenReturn(locations);
		doNothing().when(serviceHelper).centerTypeSearch(Mockito.any(), Mockito.any(), Mockito.any());
		when(serviceHelper.locationSearch(Mockito.any())).thenReturn(location);
		when(locationUtils.getDescedants(Mockito.any(), Mockito.any())).thenReturn(locations);
		when(serviceHelper.buildLocationSearchFilter(Mockito.any())).thenReturn(Arrays.asList(filter,filter1));
		when(serviceHelper.fetchUserZone(Mockito.any(),Mockito.any())).thenReturn(zones);
		when(filterTypeValidator.validate(Mockito.any(),Mockito.any())).thenReturn(true);
		when(serviceHelper.searchCenter(Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any())).
		thenReturn(new PageResponseDto<>(1, 20, 30, null));
		mockMvc.perform(post("/registrationcenters/search").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk());
	}
	
	@Test
	@WithUserDetails("global-admin")
	public void searchRegistrationCenterNotfoundTest() throws Exception {
		List<Location> locations = new ArrayList<>();
		Location location = new Location();
		location.setCode("1001");
		location.setHierarchyName("postalCode");
		location.setLangCode("eng");
		location.setParentLocCode("PAR");
		location.setHierarchyLevel((short) 2);
		location.setName("10045");
		location.setIsActive(Boolean.TRUE);
		locations.add(location);
		String json = objectMapper.writeValueAsString(request);
		when(serviceHelper.fetchLocations(Mockito.anyString())).thenReturn(locations);
		doNothing().when(serviceHelper).centerTypeSearch(Mockito.any(), Mockito.any(), Mockito.any());
		when(serviceHelper.locationSearch(Mockito.any())).thenReturn(null);
		when(filterTypeValidator.validate(Mockito.any(),Mockito.any())).thenReturn(false);
		mockMvc.perform(post("/registrationcenters/search").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk());
	}
	
	@Test
	@WithUserDetails("global-admin")
	public void registrationCenterFilterValuesTest() throws Exception {
		List<Location> locations = new ArrayList<>();
		Location location = new Location();
		location.setCode("1001");
		location.setHierarchyName("postalCode");
		location.setLangCode("eng");
		location.setParentLocCode("PAR");
		location.setHierarchyLevel((short) 2);
		location.setName("10045");
		location.setIsActive(Boolean.TRUE);
		locations.add(location);
		String json = objectMapper.writeValueAsString(requestDto);
		when(zoneUtils.getUserZones()).thenReturn(zones);
		when(filterColumnValidator.validate(Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(true);
		when(masterDataFilterHelper.filterValues(Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(Arrays.asList("a"));
		
		mockMvc.perform(post("/registrationcenters/filtervalues").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk());
	}
	
	@Test
	@WithUserDetails("global-admin")
	public void registrationCenterFilterValuesZoneNotFoundTest() throws Exception {
		List<Location> locations = new ArrayList<>();
		Location location = new Location();
		location.setCode("1001");
		location.setHierarchyName("postalCode");
		location.setLangCode("eng");
		location.setParentLocCode("PAR");
		location.setHierarchyLevel((short) 2);
		location.setName("10045");
		location.setIsActive(Boolean.TRUE);
		locations.add(location);
		String json = objectMapper.writeValueAsString(requestDto);
		when(zoneUtils.getUserZones()).thenReturn(null);
		
		mockMvc.perform(post("/registrationcenters/filtervalues").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk());
	}
}
