package io.mosip.kernel.pridgenerator.test.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.pridgenerator.test.config.HibernateDaoConfig;
import io.mosip.kernel.pridgenerator.constant.PRIDGeneratorConstant;
import io.mosip.kernel.pridgenerator.constant.PridLifecycleStatus;
import io.mosip.kernel.pridgenerator.entity.PridEntity;
import io.mosip.kernel.pridgenerator.exception.PridGeneratorServiceException;
import io.mosip.kernel.pridgenerator.generator.PridWriter;
import io.mosip.kernel.pridgenerator.repository.PridRepository;
import io.mosip.kernel.pridgenerator.service.PridService;

@SpringBootTest
@TestPropertySource({ "classpath:application-test.properties", "classpath:bootstrap.properties" })
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = HibernateDaoConfig.class, loader = AnnotationConfigContextLoader.class)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class PridGeneratorTest {

	private static final String TEST_USER = "TestUser";
	
	
	@Autowired
	private PridWriter pridWriter;

	@Autowired
	private PridService pridService;

	@MockBean
	private PridRepository pridRepository;

	@MockBean
	private RestTemplate restTemplate;

	
	private PridEntity pridEntity;
	
	@Before
	public void init() {
		pridEntity = new PridEntity();
		pridEntity.setPrid("8292938292");
		pridEntity.setStatus(PridLifecycleStatus.AVAILABLE);
	}

	

	@Test
	public void fetchPridTest() {
		Mockito.when(pridRepository.findFirstByStatus(PridLifecycleStatus.AVAILABLE)).thenReturn(pridEntity);
		Mockito.doNothing().when(pridRepository).updatePrid(PridLifecycleStatus.ASSIGNED, PRIDGeneratorConstant.DEFAULTADMIN_MOSIP_IO,
				DateUtils.getUTCCurrentDateTime(), pridEntity.getPrid());
		assertThat(pridService.fetchPrid().getPrid(), is(pridEntity.getPrid()));
	}
	
	@Test(expected = PridGeneratorServiceException.class)
	public void fetchPridNullPridTest() {
		Mockito.when(pridRepository.findFirstByStatus(PridLifecycleStatus.AVAILABLE)).thenReturn(null);
		pridService.fetchPrid().getPrid();
	}
	
	
	@Test(expected = PridGeneratorServiceException.class)
	public void fetchPridDataAccessExceptionTest() {
		Mockito.when(pridRepository.findFirstByStatus(PridLifecycleStatus.AVAILABLE)).thenThrow(new DataRetrievalFailureException("DataBase error occur"));
		pridService.fetchPrid().getPrid();
	}
	
	@Test(expected = PridGeneratorServiceException.class)
	public void fetchPridExceptionTest() {
		Mockito.when(pridRepository.findFirstByStatus(PridLifecycleStatus.AVAILABLE)).thenThrow(new RuntimeException("Runtime error occur"));
		pridService.fetchPrid().getPrid();
	}
	
	
	@Test
	public void fetchPridCountTest() {
		Mockito.when(pridRepository.countByStatusAndIsDeletedFalse(PridLifecycleStatus.AVAILABLE)).thenReturn(10L);
		assertThat(pridService.fetchPridCount(PridLifecycleStatus.AVAILABLE), is(10L));
	}
	
	@Test
	public void fetchPridCountDataAccessExceptionTest() {
		Mockito.when(pridRepository.countByStatusAndIsDeletedFalse(PridLifecycleStatus.AVAILABLE)).thenThrow(new DataRetrievalFailureException("DataBase error occur"));
		assertThat(pridService.fetchPridCount(PridLifecycleStatus.AVAILABLE), is(0L));
	}
	
	@Test
	public void fetchPridCountExceptionTest() {
		Mockito.when(pridRepository.countByStatusAndIsDeletedFalse(PridLifecycleStatus.AVAILABLE)).thenThrow(new RuntimeException("Runtime error occur"));
		assertThat(pridService.fetchPridCount(PridLifecycleStatus.AVAILABLE), is(0L));
	}
	
	
	@Test
	public void savePRIDTest() {
		Mockito.when(pridRepository.existsById(pridEntity.getPrid())).thenReturn(false);
		Mockito.when(pridRepository.saveAndFlush(pridEntity)).thenReturn(pridEntity);
		assertThat(pridService.savePRID(pridEntity), is(true));
	}
	
	@Test
	public void savePRIDAlreadyExistTest() {
		Mockito.when(pridRepository.existsById(pridEntity.getPrid())).thenReturn(true);
		assertThat(pridService.savePRID(pridEntity), is(false));
	}
	
	@Test
	public void savePRIDDataAccessExceptionTest() {
		Mockito.when(pridRepository.existsById(pridEntity.getPrid())).thenReturn(false);
		Mockito.when(pridRepository.saveAndFlush(pridEntity)).thenThrow(new DataRetrievalFailureException("DataBase error occur"));
		assertThat(pridService.savePRID(pridEntity), is(false));
	}
	
	@Test
	public void savePRIDExceptionTest() {
		Mockito.when(pridRepository.existsById(pridEntity.getPrid())).thenReturn(false);
		Mockito.when(pridRepository.saveAndFlush(pridEntity)).thenThrow(new RuntimeException("Runtime error occur"));
		assertThat(pridService.savePRID(pridEntity), is(false));
	}
	
	
	@Test
	public void savePRIDWriterTest() {
		Mockito.when(pridRepository.existsById(pridEntity.getPrid())).thenReturn(false);
		Mockito.when(pridRepository.saveAndFlush(pridEntity)).thenReturn(pridEntity);
		assertThat(pridWriter.persistPrids(pridEntity), is(true));
	}
}