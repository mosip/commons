package io.mosip.kernel.masterdata.test.service;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.masterdata.constant.SchemaErrorCode;
import io.mosip.kernel.masterdata.dto.DynamicFieldDto;
import io.mosip.kernel.masterdata.dto.DynamicFieldValueDto;
import io.mosip.kernel.masterdata.dto.IdSchemaPublishDto;
import io.mosip.kernel.masterdata.dto.IdentitySchemaDto;
import io.mosip.kernel.masterdata.dto.SchemaDto;
import io.mosip.kernel.masterdata.dto.getresponse.IdSchemaResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.PageDto;
import io.mosip.kernel.masterdata.entity.DynamicField;
import io.mosip.kernel.masterdata.entity.IdentitySchema;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;
import io.mosip.kernel.masterdata.exception.RequestException;
import io.mosip.kernel.masterdata.repository.DynamicFieldRepository;
import io.mosip.kernel.masterdata.repository.IdentitySchemaRepository;
import io.mosip.kernel.masterdata.service.DynamicFieldService;
import io.mosip.kernel.masterdata.service.IdentitySchemaService;
import io.mosip.kernel.masterdata.test.TestBootApplication;

/**
 * 
 * @author anusha
 *
 */

@SpringBootTest(classes = TestBootApplication.class)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class SchemaServiceTest {
	
	@MockBean
	private IdentitySchemaRepository identitySchemaRepository;

	@MockBean
	private DynamicFieldRepository dynamicFieldRepository;
	
	@Autowired
	private IdentitySchemaService identitySchemaService;	
	
	@Autowired
	private DynamicFieldService dynamicFieldService;
	
	
	private Page<DynamicField> fieldPagedResult;
	private Page<IdentitySchema> schemaPagedResult;
	
	private IdentitySchema draftSchema;
	private IdentitySchema publishedSchema;
	
	private DynamicField bloodTypeField;
	private DynamicField mstatusField;	
	
	PageRequest pageRequest = null;
	
	@Before
	public void setup() {
		List<DynamicField> list = new ArrayList<DynamicField>();		
		bloodTypeField = new DynamicField();
		bloodTypeField.setDataType("simpleType");
		bloodTypeField.setDescription("test");
		bloodTypeField.setId("1233");
		bloodTypeField.setIsActive(true);
		bloodTypeField.setLangCode("eng");
		bloodTypeField.setName("bloodType");
		bloodTypeField.setValueJson("[{\"value\":\"Optv\",\"code\":\"BT1\",\"active\":true},{\"value\":\"Ontv\",\"code\":\"BT2\",\"active\":true}]");
		
		mstatusField = new DynamicField();
		mstatusField.setDataType("string");
		mstatusField.setDescription("test desc");
		mstatusField.setId("1234");
		mstatusField.setIsActive(true);
		mstatusField.setLangCode("eng");
		mstatusField.setName("martialStatus");
		mstatusField.setValueJson("[{\"value\":\"married\",\"code\":\"MS1\",\"active\":true},{\"value\":\"single\",\"code\":\"MS2\",\"active\":true}]");
		list.add(mstatusField);
		
		fieldPagedResult = new PageImpl<DynamicField>(list);		
		
		List<IdentitySchema> schemaList = new ArrayList<IdentitySchema>();
		draftSchema = new IdentitySchema();
		draftSchema.setId("11");
		draftSchema.setAdditionalProperties(false);
		draftSchema.setEffectiveFrom(LocalDateTime.now());
		draftSchema.setIdVersion(0);
		draftSchema.setIdAttributeJson("[]");
		draftSchema.setIsActive(true);
		draftSchema.setIsDeleted(false);
		draftSchema.setSchemaJson("{}");
		draftSchema.setStatus(IdentitySchemaService.STATUS_DRAFT);
		schemaList.add(draftSchema);
		
		publishedSchema = new IdentitySchema();
		publishedSchema.setId("12");
		publishedSchema.setAdditionalProperties(false);
		publishedSchema.setEffectiveFrom(LocalDateTime.now());
		publishedSchema.setIdVersion(0.1);
		publishedSchema.setIdAttributeJson("[]");
		publishedSchema.setIsActive(true);
		publishedSchema.setIsDeleted(false);
		publishedSchema.setSchemaJson("{}");
		publishedSchema.setStatus(IdentitySchemaService.STATUS_PUBLISHED);
		schemaList.add(publishedSchema);
		
		schemaPagedResult = new PageImpl<IdentitySchema>(schemaList);
		pageRequest = PageRequest.of(0, 10, Sort.by(Direction.fromString("desc"), "cr_dtimes"));
	}
	
	@Test
	@WithUserDetails("reg-officer")
	public void testFetchAllDynamicFields() throws Exception {		
		Mockito.when(dynamicFieldRepository.findAllDynamicFields(pageRequest)).thenReturn(fieldPagedResult);		
		dynamicFieldService.getAllDynamicField(0, 10, "cr_dtimes", "desc", null);
	}
	
	@Test
	@WithUserDetails("reg-officer")
	public void testFetchAllDynamicFieldsByLangCode() throws Exception {		
		Mockito.when(dynamicFieldRepository.findAllDynamicFieldsByLangCode("eng", pageRequest)).thenReturn(fieldPagedResult);		
		dynamicFieldService.getAllDynamicField(0, 10, "cr_dtimes", "desc", "eng");
	}
	
	@Test
	@WithUserDetails("global-admin")
	public void testCreateDynamicField() throws Exception {		
		Mockito.when(dynamicFieldRepository.create(Mockito.any(DynamicField.class))).thenReturn(bloodTypeField);
		
		DynamicFieldDto dto = new DynamicFieldDto();
		dto.setName("bloodType");
		dto.setActive(true);
		dto.setDataType("simpleType");
		dto.setLangCode("eng");
		
		dynamicFieldService.createDynamicField(dto);
	}
	
	@Test(expected = RequestException.class)
	@WithUserDetails("global-admin")
	public void testUpdateDynamicField() throws Exception {		
		Mockito.when(dynamicFieldRepository.updateDynamicField(Mockito.anyString(), Mockito.anyString(),  Mockito.anyString(), 
				 Mockito.anyString(),  Mockito.anyBoolean(), Mockito.any(LocalDateTime.class),  Mockito.anyString())).thenReturn(0);
		
		Mockito.when(dynamicFieldRepository.findDynamicFieldById(Mockito.anyString())).thenReturn(bloodTypeField);
		
		DynamicFieldDto dto = new DynamicFieldDto();
		dto.setName("bloodType");
		dto.setActive(false);
		dto.setDataType("simpleType");
		dto.setLangCode("eng");
		
		dynamicFieldService.updateDynamicField("1233", dto);
	}
	
	@Test
	@WithUserDetails("global-admin")
	public void testUpdateDynamicFieldValue() throws Exception {		
		Mockito.when(dynamicFieldRepository.updateDynamicFieldValue(Mockito.anyString(), Mockito.anyString(),  Mockito.anyString(), 
				 Mockito.any(LocalDateTime.class),  Mockito.anyString())).thenReturn(1);
		
		Mockito.when(dynamicFieldRepository.findDynamicFieldById(Mockito.anyString())).thenReturn(bloodTypeField);
		
		DynamicFieldValueDto dto = new DynamicFieldValueDto();
		dto.setActive(true);
		dto.setCode("BT1");
		dto.setValue("Ove");
		dto.setLangCode("eng");
		
		assertEquals("1233", dynamicFieldService.updateFieldValue("1233", dto));
	}
	
	@Test(expected = RequestException.class)
	@WithUserDetails("global-admin")
	public void testUpdateDynamicFieldValueWithFieldNotFound() throws Exception {		
		Mockito.when(dynamicFieldRepository.updateDynamicFieldValue(Mockito.anyString(), Mockito.anyString(),  Mockito.anyString(), 
				 Mockito.any(LocalDateTime.class),  Mockito.anyString())).thenReturn(1);
	
		DynamicFieldValueDto dto = new DynamicFieldValueDto();
		dto.setActive(true);
		dto.setCode("BT1");
		dto.setValue("Ove");
		dto.setLangCode("eng");
		
		assertEquals("1233", dynamicFieldService.updateFieldValue("1233", dto));
	}
	
	@Test
	@WithUserDetails("reg-officer")
	public void testFetchAllIdentitySchema() throws Exception {		
		Mockito.when(identitySchemaRepository.findAllIdentitySchema(true, pageRequest)).thenReturn(schemaPagedResult);		
		PageDto<IdSchemaResponseDto> resp = identitySchemaService.getAllSchema(0, 10, "cr_dtimes", "desc");
		assertEquals(2, resp.getTotalItems());
	}
	
	@Test
	@WithUserDetails("reg-officer")
	public void testFetchLatestIdentitySchema() throws Exception {		
		Mockito.when(identitySchemaRepository.findLatestPublishedIdentitySchema()).thenReturn(publishedSchema);		
		IdSchemaResponseDto resp = identitySchemaService.getLatestSchema();
		assertEquals("PUBLISHED", resp.getStatus());
	}
	
	@Test
	@WithUserDetails("global-admin")
	public void testCreateIdentitySchema() throws Exception {		
		Mockito.when(identitySchemaRepository.create(Mockito.any(IdentitySchema.class))).thenReturn(draftSchema);
		IdentitySchemaDto dto = new IdentitySchemaDto();
		dto.setTitle("test");
		dto.setSchema(new ArrayList<SchemaDto>());
		dto.setDescription("test");
		
		IdSchemaResponseDto resp = identitySchemaService.createSchema(dto);		
		assertEquals("DRAFT", resp.getStatus());
	}
	
		
	@Test
	@WithUserDetails("global-admin")
	public void testUpdateIdentitySchema() throws Exception {		
		Mockito.when(identitySchemaRepository.updateIdentitySchema(Mockito.anyString(), Mockito.anyString(), 
				Mockito.anyBoolean(), Mockito.any(LocalDateTime.class), Mockito.anyString())).thenReturn(1);
		Mockito.when(identitySchemaRepository.findIdentitySchemaById(Mockito.anyString())).thenReturn(draftSchema);
		
		IdentitySchemaDto dto = new IdentitySchemaDto();
		dto.setTitle("test");
		dto.setSchema(new ArrayList<SchemaDto>());
		
		IdSchemaResponseDto resp = identitySchemaService.updateSchema(draftSchema.getId(), dto);
		assertEquals("DRAFT", resp.getStatus());
	}
	
	@Test
	@WithUserDetails("global-admin")
	public void testPublishIdentitySchema() throws Exception {		
		Mockito.when(identitySchemaRepository.publishIdentitySchema(Mockito.anyString(), Mockito.anyString(), 
				Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class), Mockito.anyString(), Mockito.anyDouble())).thenReturn(1);
		
		Mockito.when(identitySchemaRepository.findIdentitySchemaById(Mockito.anyString())).thenReturn(draftSchema);
		
		IdSchemaPublishDto dto = new IdSchemaPublishDto();
		dto.setEffectiveFrom(LocalDateTime.now().plusDays(1));
		dto.setId("test-test-test-test");
		assertEquals(dto.getId(), identitySchemaService.publishSchema(dto));
	}
	
	@Test
	@WithUserDetails("global-admin")
	public void testPublishAlreadyPublishedIdentitySchema() throws Exception {		
		Mockito.when(identitySchemaRepository.publishIdentitySchema(Mockito.anyString(), Mockito.anyString(), 
				Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class), Mockito.anyString(), Mockito.anyDouble())).thenReturn(1);
		
		Mockito.when(identitySchemaRepository.findIdentitySchemaById(Mockito.anyString())).thenReturn(publishedSchema);
		
		IdSchemaPublishDto dto = new IdSchemaPublishDto();
		dto.setEffectiveFrom(LocalDateTime.now().plusDays(1));
		dto.setId("test-test-test-test");
		
		try {
			identitySchemaService.publishSchema(dto);
		} catch(MasterDataServiceException e) {
			assertEquals(SchemaErrorCode.SCHEMA_ALREADY_PUBLISHED.getErrorCode(), e.getErrorCode());
		}
	}
	
	@Test
	@WithUserDetails("global-admin")
	public void testPublishIdentitySchemaWithException() throws Exception {		
		Mockito.when(identitySchemaRepository.publishIdentitySchema(Mockito.anyString(), Mockito.anyString(), 
				Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class), Mockito.anyString(),Mockito.anyDouble())).thenReturn(1);
		
		Mockito.when(identitySchemaRepository.findIdentitySchemaById(Mockito.anyString())).thenReturn(draftSchema);
		
		IdSchemaPublishDto dto = new IdSchemaPublishDto();
		dto.setEffectiveFrom(LocalDateTime.now());
		dto.setId("test-test-test-test");
		try {
			identitySchemaService.publishSchema(dto);
		} catch(MasterDataServiceException e) {
			assertEquals(SchemaErrorCode.SCHEMA_EFFECTIVE_FROM_IS_OLDER.getErrorCode(), e.getErrorCode());
		}		
	}
	
	
	@Test
	@WithUserDetails("global-admin")
	public void testDeleteIdentitySchema() throws Exception {		
		Mockito.when(identitySchemaRepository.deleteIdentitySchema(Mockito.anyString(),  Mockito.any(LocalDateTime.class), 
				Mockito.anyString())).thenReturn(1);		
		identitySchemaService.deleteSchema("test-test");
	}
	
	@Test(expected = RequestException.class)
	@WithUserDetails("global-admin")
	public void testDeleteIdentitySchemaFailed() throws Exception {		
		Mockito.when(identitySchemaRepository.deleteIdentitySchema(Mockito.anyString(),  Mockito.any(LocalDateTime.class), 
				Mockito.anyString())).thenReturn(0);		
		identitySchemaService.deleteSchema("test-test");
	}
	
	@Test(expected = MasterDataServiceException.class)
	@WithUserDetails("global-admin")
	public void testDeleteIdentitySchemaFailedUpdate() throws Exception {		
		Mockito.when(identitySchemaRepository.deleteIdentitySchema(Mockito.anyString(),  Mockito.any(LocalDateTime.class), 
				Mockito.anyString())).thenThrow(DataAccessLayerException.class);		
		identitySchemaService.deleteSchema("test-test");
	}

}
