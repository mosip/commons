package io.mosip.idrepository.core.test.helper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.idrepository.core.builder.AuditRequestBuilder;
import io.mosip.idrepository.core.builder.RestRequestBuilder;
import io.mosip.idrepository.core.constant.AuditEvents;
import io.mosip.idrepository.core.constant.AuditModules;
import io.mosip.idrepository.core.constant.IdRepoErrorConstants;
import io.mosip.idrepository.core.constant.IdType;
import io.mosip.idrepository.core.exception.IdRepoAppException;
import io.mosip.idrepository.core.exception.IdRepoDataValidationException;
import io.mosip.idrepository.core.helper.AuditHelper;
import io.mosip.idrepository.core.helper.RestHelper;
import io.mosip.idrepository.core.security.IdRepoSecurityManager;

/**
 * @author Manoj SP
 *
 */
@ContextConfiguration(classes = { TestContext.class, WebApplicationContext.class })
@RunWith(SpringRunner.class)
@WebMvcTest
public class AuditHelperTest {

	@Mock
	RestHelper restHelper;

	@InjectMocks
	AuditHelper auditHelper;

	@Mock
	IdRepoSecurityManager securityManager;

	@Autowired
	MockMvc mockMvc;

	@Mock
	AuditRequestBuilder auditBuilder;

	@Mock
	RestRequestBuilder restBuilder;

	@Autowired
	Environment env;

	@Before
	public void before() {
		ReflectionTestUtils.setField(auditBuilder, "env", env);
		ReflectionTestUtils.setField(restBuilder, "env", env);
		ReflectionTestUtils.setField(auditHelper, "mapper", new ObjectMapper());
		when(securityManager.hash(Mockito.any())).thenReturn("mock");
	}

	@Test
	public void testAudit() throws IdRepoDataValidationException {
		auditHelper.audit(AuditModules.ID_REPO_CORE_SERVICE, AuditEvents.CREATE_IDENTITY_REQUEST_RESPONSE, "id",
				IdType.REG_ID, "desc");
	}

	@Test
	public void testAuditFailure() throws IdRepoDataValidationException {
		when(restBuilder.buildRequest(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenThrow(new IdRepoDataValidationException());
		auditHelper.audit(AuditModules.ID_REPO_CORE_SERVICE, AuditEvents.CREATE_IDENTITY_REQUEST_RESPONSE, "id",
				IdType.REG_ID, "desc");
	}

	@Test
	public void testAuditError() throws IdRepoDataValidationException {
		auditHelper.auditError(AuditModules.ID_REPO_CORE_SERVICE, AuditEvents.CREATE_IDENTITY_REQUEST_RESPONSE, "id",
				IdType.REG_ID, new IdRepoAppException(IdRepoErrorConstants.AUTHORIZATION_FAILED));
	}

	@SuppressWarnings("serial")
	@Test
	public void testAuditErrorFailure() throws IdRepoDataValidationException, JsonProcessingException {
		ObjectMapper mapperMock = mock(ObjectMapper.class);
		when(mapperMock.writeValueAsString(Mockito.any())).thenThrow(new JsonProcessingException("") {
		});
		ReflectionTestUtils.setField(auditHelper, "mapper", mapperMock);
		auditHelper.auditError(AuditModules.ID_REPO_CORE_SERVICE, AuditEvents.CREATE_IDENTITY_REQUEST_RESPONSE, "id",
				IdType.REG_ID, new IdRepoAppException(IdRepoErrorConstants.AUTHORIZATION_FAILED));
		ReflectionTestUtils.setField(auditHelper, "mapper", new ObjectMapper());
	}

}
