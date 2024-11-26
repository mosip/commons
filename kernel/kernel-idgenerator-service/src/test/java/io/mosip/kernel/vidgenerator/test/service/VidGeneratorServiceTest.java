package io.mosip.kernel.vidgenerator.test.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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

import io.mosip.kernel.core.authmanager.authadapter.spi.VertxAuthenticationProvider;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.idgenerator.config.HibernateDaoConfig;
import io.mosip.kernel.vidgenerator.constant.VidLifecycleStatus;
import io.mosip.kernel.vidgenerator.entity.VidAssignedEntity;
import io.mosip.kernel.vidgenerator.entity.VidEntity;
import io.mosip.kernel.vidgenerator.exception.VidGeneratorServiceException;
import io.mosip.kernel.vidgenerator.repository.VidAssignedRepository;
import io.mosip.kernel.vidgenerator.repository.VidRepository;
import io.mosip.kernel.vidgenerator.service.VidService;
import io.vertx.ext.web.RoutingContext;

@SpringBootTest
@TestPropertySource({ "classpath:application-test.properties", "classpath:bootstrap.properties" })
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = HibernateDaoConfig.class, loader = AnnotationConfigContextLoader.class)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class VidGeneratorServiceTest {

	private static final String TEST_USER = "TestUser";

	@Autowired
	private VidService vidService;

	@MockBean
	private VidRepository vidRepository;

	@MockBean
	private VidAssignedRepository vidAssignedRepository;

	@MockBean
	private RoutingContext routingContext;

	@MockBean
	private VertxAuthenticationProvider authHandler;

	@MockBean
	private RestTemplate restTemplate;

	private List<VidAssignedEntity> expiredButAssignedStatusVAEntities;

	private List<VidAssignedEntity> notExpiredButAssignedStatusVAEntities;

	private List<VidAssignedEntity> releasableButExpiredStatusVAEntities;

	private List<VidAssignedEntity> nonReleasableButExpiredStatusVAEntities;

	private List<VidEntity> nonExpiredButAssignedStatusEntities;

	private List<VidEntity> expiredButAssignedStatusEntities;

	@Captor
	ArgumentCaptor<List<VidEntity>> vidEntityListCaptor;

	@Captor
	ArgumentCaptor<List<VidAssignedEntity>> vidAssignedEntityListCaptor;

	@Captor
	ArgumentCaptor<List<VidAssignedEntity>> vidAssignedEntityListCaptor2;


	private VidEntity availableEntity;

	private VidEntity availableEntityWithExpiry;

	@Before
	public void init() {
		availableEntityWithExpiry = new VidEntity("3650694284580734", VidLifecycleStatus.AVAILABLE, null);
		availableEntity = new VidEntity("3650694284580734", VidLifecycleStatus.AVAILABLE, null);
		
		VidAssignedEntity expiredButAssignedStatusVAEntity = new VidAssignedEntity("3690694284580731", 
			VidLifecycleStatus.ASSIGNED, DateUtils.getUTCCurrentDateTime().minusMinutes(30));
		expiredButAssignedStatusVAEntity.setCreatedBy("MOSIP_ADMIN");
		expiredButAssignedStatusVAEntity.setCreatedtimes(DateUtils.getUTCCurrentDateTime().minusMonths(10));
		expiredButAssignedStatusVAEntity.setUpdatedBy("MOSIP_ADMIN");
		expiredButAssignedStatusVAEntity.setUpdatedtimes(DateUtils.getUTCCurrentDateTime().minusDays(10));
		expiredButAssignedStatusVAEntities = new ArrayList<>();
		expiredButAssignedStatusVAEntities.add(expiredButAssignedStatusVAEntity);

		VidAssignedEntity notExpiredButAssignedStatusVAEntity = new VidAssignedEntity("3690694284580732", 
			VidLifecycleStatus.ASSIGNED, DateUtils.getUTCCurrentDateTime().plusMinutes(30));
		notExpiredButAssignedStatusVAEntity.setCreatedBy("MOSIP_ADMIN");
		notExpiredButAssignedStatusVAEntity.setCreatedtimes(DateUtils.getUTCCurrentDateTime().minusMonths(10));
		notExpiredButAssignedStatusVAEntity.setUpdatedBy("MOSIP_ADMIN");
		notExpiredButAssignedStatusVAEntity.setUpdatedtimes(DateUtils.getUTCCurrentDateTime().minusDays(10));
		notExpiredButAssignedStatusVAEntities = new ArrayList<>();
		notExpiredButAssignedStatusVAEntities.add(notExpiredButAssignedStatusVAEntity);

		VidAssignedEntity releasableButExpiredStatusVAEntity = new VidAssignedEntity("3690694284580733", 
			VidLifecycleStatus.EXPIRED, DateUtils.getUTCCurrentDateTime().minusMonths(7));
		releasableButExpiredStatusVAEntity.setCreatedBy("MOSIP_ADMIN");
		releasableButExpiredStatusVAEntity.setCreatedtimes(DateUtils.getUTCCurrentDateTime().minusMonths(10));
		releasableButExpiredStatusVAEntity.setUpdatedBy("MOSIP_ADMIN");
		releasableButExpiredStatusVAEntity.setUpdatedtimes(DateUtils.getUTCCurrentDateTime().minusDays(10));
		releasableButExpiredStatusVAEntities = new ArrayList<>();
		releasableButExpiredStatusVAEntities.add(releasableButExpiredStatusVAEntity);

		VidAssignedEntity nonReleasableButExpiredStatusVAEntity = new VidAssignedEntity("3690694284580734", 
			VidLifecycleStatus.EXPIRED, DateUtils.getUTCCurrentDateTime().minusMinutes(30));
		nonReleasableButExpiredStatusVAEntity.setCreatedBy("MOSIP_ADMIN");
		nonReleasableButExpiredStatusVAEntity.setCreatedtimes(DateUtils.getUTCCurrentDateTime().minusMonths(10));
		nonReleasableButExpiredStatusVAEntity.setUpdatedBy("MOSIP_ADMIN");
		nonReleasableButExpiredStatusVAEntity.setUpdatedtimes(DateUtils.getUTCCurrentDateTime().minusDays(10));
		nonReleasableButExpiredStatusVAEntities = new ArrayList<>();
		nonReleasableButExpiredStatusVAEntities.add(nonReleasableButExpiredStatusVAEntity);

		VidEntity nonExpiredButAssignedStatusEntity = new VidEntity("3690694284580735", 
			VidLifecycleStatus.ASSIGNED, DateUtils.getUTCCurrentDateTime().plusMinutes(30));
		nonExpiredButAssignedStatusEntity.setCreatedBy("MOSIP_ADMIN");
		nonExpiredButAssignedStatusEntity.setCreatedtimes(DateUtils.getUTCCurrentDateTime().minusMonths(10));
		nonExpiredButAssignedStatusEntity.setUpdatedBy("MOSIP_ADMIN");
		nonExpiredButAssignedStatusEntity.setUpdatedtimes(DateUtils.getUTCCurrentDateTime().minusDays(10));
		nonExpiredButAssignedStatusEntities = new ArrayList<>();
		nonExpiredButAssignedStatusEntities.add(nonExpiredButAssignedStatusEntity);

		VidEntity expiredButAssignedStatusEntity = new VidEntity("3690694284580736", 
			VidLifecycleStatus.ASSIGNED, DateUtils.getUTCCurrentDateTime().minusMinutes(30));
		expiredButAssignedStatusEntity.setCreatedBy("MOSIP_ADMIN");
		expiredButAssignedStatusEntity.setCreatedtimes(DateUtils.getUTCCurrentDateTime().minusMonths(10));
		expiredButAssignedStatusEntity.setUpdatedBy("MOSIP_ADMIN");
		expiredButAssignedStatusEntity.setUpdatedtimes(DateUtils.getUTCCurrentDateTime().minusDays(10));
		expiredButAssignedStatusEntities = new ArrayList<>();
		expiredButAssignedStatusEntities.add(expiredButAssignedStatusEntity);

	}

	@Test(expected = VidGeneratorServiceException.class)
	public void fetchVidNotFoundTest() {
		Mockito.when(vidRepository.findFirstByStatus(VidLifecycleStatus.AVAILABLE)).thenReturn(null);
		Mockito.when(authHandler.getContextUser(routingContext)).thenReturn(TEST_USER);
		vidService.fetchVid(null, routingContext);
	}

	@Test(expected = VidGeneratorServiceException.class)
	public void fetchVidGerDataAccessTest() {
		Mockito.when(vidRepository.findFirstByStatus(VidLifecycleStatus.AVAILABLE))
				.thenThrow(new DataRetrievalFailureException("DataBase error occur"));
		Mockito.when(authHandler.getContextUser(routingContext)).thenReturn(TEST_USER);
		vidService.fetchVid(null, routingContext);
	}

	@Test(expected = VidGeneratorServiceException.class)
	public void fetchVidGetExceptionTest() {
		Mockito.when(vidRepository.findFirstByStatus(VidLifecycleStatus.AVAILABLE))
				.thenThrow(new RuntimeException("DataBase error occur"));
		Mockito.when(authHandler.getContextUser(routingContext)).thenReturn(TEST_USER);
		vidService.fetchVid(null, routingContext);
	}

	@Test(expected = VidGeneratorServiceException.class)
	public void fetchVidUpdateDataAccessTest() {
		Mockito.when(vidRepository.findFirstByStatus(VidLifecycleStatus.AVAILABLE)).thenReturn(availableEntity);
		Mockito.doThrow(new DataRetrievalFailureException("DataBase error occur")).when(vidRepository).updateVid(
				Mockito.eq(VidLifecycleStatus.ASSIGNED), Mockito.anyString(), Mockito.any(),
				Mockito.eq(availableEntity.getVid()));
		Mockito.when(authHandler.getContextUser(routingContext)).thenReturn(TEST_USER);
		vidService.fetchVid(null, routingContext);
	}

	@Test(expected = VidGeneratorServiceException.class)
	public void fetchVidUpdateExceptionTest() {
		Mockito.when(vidRepository.findFirstByStatus(VidLifecycleStatus.AVAILABLE)).thenReturn(availableEntity);
		Mockito.doThrow(new RuntimeException("DataBase error occur")).when(vidRepository).updateVid(
				Mockito.eq(VidLifecycleStatus.ASSIGNED), Mockito.anyString(), Mockito.any(),
				Mockito.eq(availableEntity.getVid()));
		Mockito.when(authHandler.getContextUser(routingContext)).thenReturn(TEST_USER);
		vidService.fetchVid(null, routingContext);
	}

	@Test
	public void fetchVidTest() {
		Mockito.when(vidRepository.findFirstByStatus(VidLifecycleStatus.AVAILABLE))
				.thenReturn(availableEntityWithExpiry);
		Mockito.when(vidRepository.save(Mockito.any())).thenReturn(availableEntityWithExpiry);
		Mockito.when(authHandler.getContextUser(routingContext)).thenReturn(TEST_USER);
		vidService.fetchVid(DateUtils.getUTCCurrentDateTime().plusMonths(20), routingContext);
	}

	@Test
	public void fetchVidCountDataAccessExceptionTest() {
		Mockito.when(vidRepository.countByStatusAndIsDeletedFalse(VidLifecycleStatus.AVAILABLE))
				.thenThrow(new DataRetrievalFailureException("DataBase error occur"));
		vidService.fetchVidCount(VidLifecycleStatus.AVAILABLE);
	}

	@Test
	public void fetchVidCountsExceptionTest() {
		Mockito.when(vidRepository.countByStatusAndIsDeletedFalse(VidLifecycleStatus.AVAILABLE))
				.thenThrow(new RuntimeException("DataBase error occur"));
		vidService.fetchVidCount(VidLifecycleStatus.AVAILABLE);
	}

	@Test
	public void fetchVidCountTest() {
		Mockito.when(vidRepository.countByStatusAndIsDeletedFalse(VidLifecycleStatus.AVAILABLE)).thenReturn(200000L);
		assertThat(vidService.fetchVidCount(VidLifecycleStatus.AVAILABLE), is(200000L));
	}

	@Test
	public void expireOrReleaseDataAccessExceptionTest() {
		Mockito.when(vidRepository.findByStatusAndIsDeletedFalse(VidLifecycleStatus.ASSIGNED,20000))
				.thenThrow(new DataRetrievalFailureException("DataBase error occur"));
		vidService.expireAndRelease();
	}

	@Test
	public void expireOrReleaseExceptionTest() {
		Mockito.when(vidRepository.findByStatusAndIsDeletedFalse(VidLifecycleStatus.ASSIGNED,20000))
				.thenThrow(new RuntimeException("DataBase error occur"));
		vidService.expireAndRelease();
	}

	@Test
	public void expireOrReleaseTest() {
		Mockito.when(vidAssignedRepository.findByStatusAndIsDeletedFalse(VidLifecycleStatus.ASSIGNED,20000))
			.thenReturn(Stream.of(expiredButAssignedStatusVAEntities, notExpiredButAssignedStatusVAEntities)
				.flatMap(Collection::stream)
				.collect(Collectors.toList()));
		Mockito.when(vidAssignedRepository.saveAll(vidAssignedEntityListCaptor.capture()))
			.thenAnswer(i -> StreamSupport.stream((
				(List<VidAssignedEntity>) i.getArguments()[0]).spliterator(), false)
				.collect(Collectors.toList()));
		
		Mockito.when(vidAssignedRepository.findByStatusAndIsDeletedFalse(VidLifecycleStatus.EXPIRED,20000))
			.thenReturn(Stream.of(releasableButExpiredStatusVAEntities, nonReleasableButExpiredStatusVAEntities)
				.flatMap(Collection::stream)
				.collect(Collectors.toList()));

		vidService.expireAndRelease();
		Mockito.verify(vidAssignedRepository).deleteAll(vidAssignedEntityListCaptor2.capture());

		List<String> expiredButAssignedStatusVids = expiredButAssignedStatusVAEntities.stream()
			.flatMap(item -> Stream.of(item.getVid()))
			.collect(Collectors.toList());
		List<String> expiredStatusVids = vidAssignedEntityListCaptor.getValue().stream()
			.filter(item -> item.getStatus().equals(VidLifecycleStatus.EXPIRED))
			.flatMap(item -> Stream.of(item.getVid()))
			.collect(Collectors.toList());
		assertEquals(expiredButAssignedStatusVids, expiredStatusVids);

		List<String> releasableButExpiredStatusVids = releasableButExpiredStatusVAEntities.stream()
			.flatMap(item -> Stream.of(item.getVid()))
			.collect(Collectors.toList());
		List<String> deletedVids = vidAssignedEntityListCaptor2.getValue().stream()
			.flatMap(item -> Stream.of(item.getVid()))
			.collect(Collectors.toList());
		assertEquals(releasableButExpiredStatusVids, deletedVids);
	}

	@Test
	public void saveVIDDataAccessExceptionTest() {
		Mockito.when(vidRepository.existsById(availableEntity.getVid())).thenReturn(false);
		Mockito.when(vidRepository.saveAndFlush(availableEntity))
				.thenThrow(new DataRetrievalFailureException("DataBase error occur"));
		vidService.saveVID(availableEntity);
	}

	@Test
	public void saveVIDExceptionTest() {
		Mockito.when(vidRepository.existsById(availableEntity.getVid())).thenReturn(false);
		Mockito.when(vidRepository.saveAndFlush(availableEntity))
				.thenThrow(new RuntimeException("DataBase error occur"));
		vidService.saveVID(availableEntity);
	}

	@Test
	public void saveVIDTest() {
		Mockito.when(vidRepository.existsById(availableEntity.getVid())).thenReturn(false);
		Mockito.when(vidAssignedRepository.existsById(availableEntity.getVid())).thenReturn(false);
		Mockito.when(vidRepository.saveAndFlush(availableEntity)).thenReturn(availableEntity);
		assertThat(vidService.saveVID(availableEntity), is(true));
	}

	@Test
	public void saveVIDFalseTest1() {
		Mockito.when(vidRepository.existsById(availableEntity.getVid())).thenReturn(true);
		Mockito.when(vidAssignedRepository.existsById(availableEntity.getVid())).thenReturn(false);
		assertThat(vidService.saveVID(availableEntity), is(false));
	}

	@Test
	public void saveVIDFalseTest2() {
		Mockito.when(vidRepository.existsById(availableEntity.getVid())).thenReturn(false);
		Mockito.when(vidAssignedRepository.existsById(availableEntity.getVid())).thenReturn(true);
		assertThat(vidService.saveVID(availableEntity), is(false));
	}

	@Test
	public void isolateAssignedVidTest() {
		List<VidEntity> validTranserEntities = 
			Stream.of(nonExpiredButAssignedStatusEntities, expiredButAssignedStatusEntities)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
		Mockito.when(vidRepository.findByStatusAndIsDeletedFalse(VidLifecycleStatus.ASSIGNED,20000))
			.thenReturn(validTranserEntities);
		Mockito.when(vidAssignedRepository.saveAll(vidAssignedEntityListCaptor.capture()))
			.thenAnswer(i -> StreamSupport.stream((
				(List<VidAssignedEntity>) i.getArguments()[0]).spliterator(), false)
				.collect(Collectors.toList()));
		vidService.isolateAssignedVids();
		Mockito.verify(vidRepository).deleteAll(vidEntityListCaptor.capture());
		List<String> validTranserVids = validTranserEntities.stream()
			.flatMap(item -> Stream.of(item.getVid()))
			.collect(Collectors.toList());
		List<String> vidAssignedEntityVids = vidAssignedEntityListCaptor.getValue().stream()
			.filter(item -> item.getStatus().equals(VidLifecycleStatus.ASSIGNED))
			.flatMap(item -> Stream.of(item.getVid()))
			.collect(Collectors.toList());
		List<String> vidEntityVids = vidEntityListCaptor.getValue().stream()
			.flatMap(item -> Stream.of(item.getVid()))
			.collect(Collectors.toList());
		assertEquals(validTranserVids, vidAssignedEntityVids);
		assertEquals(validTranserVids, vidEntityVids);
	}
}