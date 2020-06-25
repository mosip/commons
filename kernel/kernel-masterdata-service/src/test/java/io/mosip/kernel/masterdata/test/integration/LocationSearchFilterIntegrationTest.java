package io.mosip.kernel.masterdata.test.integration;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
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
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.masterdata.util.model.Node;
import io.mosip.kernel.core.masterdata.util.spi.UBtree;
import io.mosip.kernel.masterdata.dto.request.FilterDto;
import io.mosip.kernel.masterdata.dto.request.FilterValueDto;
import io.mosip.kernel.masterdata.dto.request.Pagination;
import io.mosip.kernel.masterdata.dto.request.SearchDto;
import io.mosip.kernel.masterdata.dto.request.SearchFilter;
import io.mosip.kernel.masterdata.dto.request.SearchSort;
import io.mosip.kernel.masterdata.dto.response.LocationSearchDto;
import io.mosip.kernel.masterdata.entity.Location;
import io.mosip.kernel.masterdata.repository.LocationHierarchyRepository;
import io.mosip.kernel.masterdata.repository.LocationRepository;
import io.mosip.kernel.masterdata.utils.AuditUtil;
import io.mosip.kernel.masterdata.utils.MasterDataFilterHelper;
import io.mosip.kernel.masterdata.utils.PageUtils;

/**
 * @author Sidhant Agarwal
 * @author Ritesh Sinha
 * @since 1.0.0
 *
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class LocationSearchFilterIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private LocationRepository locationRepository;

	@MockBean
	private UBtree<Location> locationTree;

	@MockBean
	private PageUtils pageUtils;

	@MockBean
	private MasterDataFilterHelper masterDataFilterHelper;
	
	@MockBean
	private LocationHierarchyRepository locationHierarchyRepository;

	@Autowired
	private ObjectMapper objectMapper;

	private SearchSort sort;

	private SearchDto searchDto;

	private SearchFilter filter;
	private SearchFilter filter1;

	private RequestWrapper<SearchDto> request;

	@MockBean
	private AuditUtil auditUtil;

	@Before
	public void setup() throws JsonProcessingException {
		sort = new SearchSort();
		sort.setSortType("ASC");
		sort.setSortField("postalCode");
		request = new RequestWrapper<>();
		searchDto = new SearchDto();
		Pagination pagination = new Pagination(0, 10);
		filter = new SearchFilter();
		filter.setColumnName("city");
		filter.setType("equals");
		filter.setValue("Rabta");
		filter1 = new SearchFilter();
		filter1.setColumnName("isActive");
		filter1.setType("equals");
		filter1.setValue("true");
		searchDto.setFilters(Arrays.asList(filter,filter1));
		searchDto.setLanguageCode("eng");
		searchDto.setPagination(pagination);
		searchDto.setSort(Arrays.asList(sort));
		request.setRequest(searchDto);
		doNothing().when(auditUtil).auditRequest(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

	}

	@Test
	@WithUserDetails("global-admin")
	public void searchLocationTest() throws Exception {
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
		when(locationRepository.findAllByLangCode(Mockito.anyString())).thenReturn(locations);
		when(locationHierarchyRepository.findByheirarchyLevalNameAndLangCode(Mockito.anyString(), Mockito.anyString())).thenReturn(1);
		when(locationRepository.findLocationByHierarchyLevel(Mockito.anyShort(), Mockito.anyString(),
				Mockito.anyString(), Mockito.anyBoolean())).thenReturn(location);
		mockMvc.perform(post("/locations/search").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk());
	}
	
	@Test
	@WithUserDetails("global-admin")
	public void searchLocationemptyFiltersTest() throws Exception {
		List<Location> locations = new ArrayList<>();
		List<Node<Location>> tree=new ArrayList<>();
		Location location1 = new Location("1001","10045",(short)1,"region","1000","eng",null);
		Location location2 = new Location("1002","10045",(short)2,"province","1001","eng",null);
		Location location3 = new Location("1003","10045",(short)3,"city","1002","eng",null);
		Location location4 = new Location("1004","10045",(short)4,"zone","1003","eng",null);
		Location location5 = new Location("1005","10045",(short)5,"postalcode","1004","eng",null);
		
		locations.add(location1);
		locations.add(location2);
		locations.add(location3);
		locations.add(location4);
		locations.add(location5);
		tree.add(new Node<>("1001",location1,"1000"));
		tree.add(new Node<>("1002",location2,"1001"));
		tree.add(new Node<>("1003",location3,"1002"));
		tree.add(new Node<>("1004",location4,"1003"));
		tree.add(new Node<>("1005",location5,"1004"));
		Pagination pagination = new Pagination(0, 10);
		searchDto = new SearchDto();
		searchDto.setFilters(Arrays.asList());
		searchDto.setLanguageCode("eng");
		searchDto.setPagination(pagination);
		searchDto.setSort(Arrays.asList(sort));
		request.setRequest(searchDto);
		String json = objectMapper.writeValueAsString(request);
		when(locationRepository.findAllByLangCode(Mockito.anyString(),Mockito.anyBoolean())).thenReturn(locations);
		when(locationTree.createTree(Mockito.any())).thenReturn(tree);
		when(locationTree.findRootNode(Mockito.any())).thenReturn(tree.get(0));
		when(locationTree.findLeafs(Mockito.any())).thenReturn(tree);
		when(locationTree.getParentHierarchy(Mockito.any())).thenReturn(locations);
		mockMvc.perform(post("/locations/search").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void searchLocationContainsTest() throws Exception {
		List<Location> locations = new ArrayList<>();
		Location location = new Location();
		location.setCode("1001");
		location.setHierarchyName("postalCode");
		location.setLangCode("eng");
		location.setParentLocCode("PAR");
		location.setHierarchyLevel((short) 2);
		location.setName("10045");
		locations.add(location);
		filter.setType("contains");
		searchDto.setFilters(Arrays.asList(filter));
		String json = objectMapper.writeValueAsString(request);
		when(locationRepository.findAllByLangCode(Mockito.anyString())).thenReturn(locations);
		when(locationRepository.findLocationByHierarchyLevelContains(Mockito.anyShort(), Mockito.anyString(),
				Mockito.anyString(), Mockito.anyBoolean())).thenReturn(Arrays.asList(location));
		mockMvc.perform(post("/locations/search").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void searchLocationStartsWithTest() throws Exception {
		List<Location> locations = new ArrayList<>();
		List<Node<Location>> tree=new ArrayList<>();
		Location location1 = new Location("1001","10045",(short)1,"region","1000","eng",null);
		Location location2 = new Location("1002","10045",(short)2,"province","1001","eng",null);
		Location location3 = new Location("1003","10045",(short)3,"city","1002","eng",null);
		Location location4 = new Location("1004","10045",(short)4,"zone","1003","eng",null);
		Location location5 = new Location("1005","10045",(short)5,"postalcode","1004","eng",null);
		
		locations.add(location1);
		locations.add(location2);
		locations.add(location3);
		locations.add(location4);
		locations.add(location5);
		tree.add(new Node<>("1001",location1,"1000"));
		tree.add(new Node<>("1002",location2,"1001"));
		tree.add(new Node<>("1003",location3,"1002"));
		tree.add(new Node<>("1004",location4,"1003"));
		tree.add(new Node<>("1005",location5,"1004"));
		filter.setType("startSWith");
		searchDto.setFilters(Arrays.asList(filter));
		String json = objectMapper.writeValueAsString(request);
		when(locationRepository.findAllByLangCode(Mockito.anyString())).thenReturn(locations);
		when(locationRepository.findLocationByHierarchyLevelStartsWith(Mockito.anyShort(), Mockito.anyString(),
				Mockito.anyString(), Mockito.anyBoolean())).thenReturn(locations);
		when(locationTree.createTree(Mockito.any())).thenReturn(tree);
		when(locationTree.findNode(Mockito.any(),Mockito.anyString())).thenReturn(tree.get(0));
		when(locationTree.findLeafs(Mockito.any())).thenReturn(tree);
		when(locationTree.getParentHierarchy(Mockito.any())).thenReturn(locations);
		mockMvc.perform(post("/locations/search").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void searchLocationExceptionTest() throws Exception {
		List<Location> locations = new ArrayList<>();
		Location location = new Location();
		location.setCode("1001");
		location.setHierarchyName("postalCode");
		location.setLangCode("eng");
		location.setParentLocCode("PAR");
		location.setHierarchyLevel((short) 2);
		location.setName("10045");
		locations.add(location);
		filter.setType("error-type");
		searchDto.setFilters(Arrays.asList(filter));
		String json = objectMapper.writeValueAsString(request);
		when(locationRepository.findAllByLangCode(Mockito.anyString())).thenReturn(locations);
		when(locationRepository.findLocationByHierarchyLevelContains(Mockito.anyShort(), Mockito.anyString(),
				Mockito.anyString(), Mockito.anyBoolean())).thenReturn(Arrays.asList(location));
		MvcResult response = mockMvc
				.perform(post("/locations/search").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk()).andReturn();
		String errorResponse = response.getResponse().getContentAsString();
		ResponseWrapper<LocationSearchDto> responseWrapper = objectMapper.readValue(errorResponse,
				ResponseWrapper.class);

		assertThat(responseWrapper.getErrors().get(0).getMessage(),
				is("Column city doesn't support filter type error-type"));
	}

	@Test
	@WithUserDetails("global-admin")
	public void filterAllEmptyTextLocationTest() throws Exception {
		FilterDto filterDto = new FilterDto();
		filterDto.setColumnName("Zone");
		filterDto.setType("all");
		filterDto.setText("");
		FilterValueDto filterValueDto = new FilterValueDto();
		filterValueDto.setFilters(Arrays.asList(filterDto));
		filterValueDto.setLanguageCode("eng");
		RequestWrapper<FilterValueDto> requestDto = new RequestWrapper<>();
		requestDto.setRequest(filterValueDto);
		String json = objectMapper.writeValueAsString(requestDto);
		List<String> hierarchyNames = new ArrayList<>();
		hierarchyNames.add("Zone");
		List<Location> locations = new ArrayList<>();
		Location location = new Location();
		location.setCode("1001");
		location.setHierarchyName("postalCode");
		location.setLangCode("eng");
		location.setParentLocCode("PAR");
		location.setHierarchyLevel((short) 2);
		location.setName("10045");
		locations.add(location);
		when(locationRepository.findLocationAllHierarchyNames()).thenReturn(hierarchyNames);
		when(locationRepository.findAllHierarchyNameAndNameValueForEmptyTextFilter(Mockito.anyString(),
				Mockito.anyString())).thenReturn(locations);

		mockMvc.perform(post("/locations/filtervalues").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void filterAllWithTextLocationTest() throws Exception {
		FilterDto filterDto = new FilterDto();
		filterDto.setColumnName("Zone");
		filterDto.setType("all");
		filterDto.setText("abc");
		FilterValueDto filterValueDto = new FilterValueDto();
		filterValueDto.setFilters(Arrays.asList(filterDto));
		filterValueDto.setLanguageCode("eng");
		RequestWrapper<FilterValueDto> requestDto = new RequestWrapper<>();
		requestDto.setRequest(filterValueDto);
		String json = objectMapper.writeValueAsString(requestDto);
		List<String> hierarchyNames = new ArrayList<>();
		hierarchyNames.add("Zone");
		List<Location> locations = new ArrayList<>();
		Location location = new Location();
		location.setCode("1001");
		location.setHierarchyName("postalCode");
		location.setLangCode("eng");
		location.setParentLocCode("PAR");
		location.setHierarchyLevel((short) 2);
		location.setName("10045");
		locations.add(location);
		when(locationRepository.findLocationAllHierarchyNames()).thenReturn(hierarchyNames);
		when(locationRepository.findAllHierarchyNameAndNameValueForTextFilter(Mockito.anyString(),Mockito.anyString(),
				Mockito.anyString())).thenReturn(locations);

		mockMvc.perform(post("/locations/filtervalues").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk());
	}
	
	@Test
	@WithUserDetails("global-admin")
	public void filterEmptyTypeTest() throws Exception {
		FilterDto filterDto = new FilterDto();
		filterDto.setColumnName("Zone");
		filterDto.setType("");
		filterDto.setText("abc");
		FilterValueDto filterValueDto = new FilterValueDto();
		filterValueDto.setFilters(Arrays.asList(filterDto));
		filterValueDto.setLanguageCode("eng");
		RequestWrapper<FilterValueDto> requestDto = new RequestWrapper<>();
		requestDto.setRequest(filterValueDto);
		String json = objectMapper.writeValueAsString(requestDto);
		List<String> hierarchyNames = new ArrayList<>();
		hierarchyNames.add("Zone");
		
		when(locationRepository.findLocationAllHierarchyNames()).thenReturn(hierarchyNames);
		

		mockMvc.perform(post("/locations/filtervalues").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk());
	}
	
	@Test
	@WithUserDetails("global-admin")
	public void filterDataAccessexceptionTest() throws Exception {
		FilterDto filterDto = new FilterDto();
		filterDto.setColumnName("Zone");
		filterDto.setType("");
		filterDto.setText("abc");
		FilterValueDto filterValueDto = new FilterValueDto();
		filterValueDto.setFilters(Arrays.asList(filterDto));
		filterValueDto.setLanguageCode("eng");
		RequestWrapper<FilterValueDto> requestDto = new RequestWrapper<>();
		requestDto.setRequest(filterValueDto);
		String json = objectMapper.writeValueAsString(requestDto);
		when(locationRepository.findLocationAllHierarchyNames()).thenThrow(DataAccessLayerException.class);
		mockMvc.perform(post("/locations/filtervalues").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().is5xxServerError());
	}

	@Test
	@WithUserDetails("global-admin")
	public void filterUniqueEmptyTextLocationTest() throws Exception {
		FilterDto filterDto = new FilterDto();
		filterDto.setColumnName("Zone");
		filterDto.setType("unique");
		filterDto.setText("");
		FilterValueDto filterValueDto = new FilterValueDto();
		filterValueDto.setFilters(Arrays.asList(filterDto));
		filterValueDto.setLanguageCode("eng");
		RequestWrapper<FilterValueDto> requestDto = new RequestWrapper<>();
		requestDto.setRequest(filterValueDto);
		String json = objectMapper.writeValueAsString(requestDto);
		List<String> hierarchyNames = new ArrayList<>();
		List<Location> location =new ArrayList<Location>();
		hierarchyNames.add("Zone");

		when(locationRepository.findLocationAllHierarchyNames()).thenReturn(hierarchyNames);
		when(locationRepository.findDistinctHierarchyNameAndNameValueForEmptyTextFilter(Mockito.anyString(),
				Mockito.anyString())).thenReturn(location);

		mockMvc.perform(post("/locations/filtervalues").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk());
	}
	
	@Test
	@WithUserDetails("global-admin")
	public void filterUniqueIsActiveColumnTest() throws Exception {
		FilterDto filterDto = new FilterDto();
		filterDto.setColumnName("isActive");
		filterDto.setType("unique");
		filterDto.setText("true");
		FilterValueDto filterValueDto = new FilterValueDto();
		filterValueDto.setFilters(Arrays.asList(filterDto));
		filterValueDto.setLanguageCode("eng");
		RequestWrapper<FilterValueDto> requestDto = new RequestWrapper<>();
		requestDto.setRequest(filterValueDto);
		String json = objectMapper.writeValueAsString(requestDto);
		List<String> hierarchyNames = new ArrayList<>();
		hierarchyNames.add("Zone");

		when(locationRepository.findLocationAllHierarchyNames()).thenReturn(hierarchyNames);
		when(masterDataFilterHelper.filterValues(Mockito.any(), Mockito.any(),Mockito.any())).thenReturn(Arrays.asList("a","b"));

		mockMvc.perform(post("/locations/filtervalues").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk());
	}

	
	@Test
	@WithUserDetails("global-admin")
	public void filterInvalidTypeExceptionLocationTest() throws Exception {
		FilterDto filterDto = new FilterDto();
		filterDto.setColumnName("Zone");
		filterDto.setType("invalidType");
		filterDto.setText("abc");
		FilterValueDto filterValueDto = new FilterValueDto();
		filterValueDto.setFilters(Arrays.asList(filterDto));
		filterValueDto.setLanguageCode("eng");
		RequestWrapper<FilterValueDto> requestDto = new RequestWrapper<>();
		requestDto.setRequest(filterValueDto);
		String json = objectMapper.writeValueAsString(requestDto);

		mockMvc.perform(post("/locations/filtervalues").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("global-admin")
	public void filterInvalidColumnNameExceptionLocationTest() throws Exception {
		FilterDto filterDto = new FilterDto();
		filterDto.setColumnName("InvalidColumn");
		filterDto.setType("all");
		filterDto.setText("abc");
		FilterValueDto filterValueDto = new FilterValueDto();
		filterValueDto.setFilters(Arrays.asList(filterDto));
		filterValueDto.setLanguageCode("eng");
		RequestWrapper<FilterValueDto> requestDto = new RequestWrapper<>();
		requestDto.setRequest(filterValueDto);
		String json = objectMapper.writeValueAsString(requestDto);

		mockMvc.perform(post("/locations/filtervalues").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk());
	}
}
