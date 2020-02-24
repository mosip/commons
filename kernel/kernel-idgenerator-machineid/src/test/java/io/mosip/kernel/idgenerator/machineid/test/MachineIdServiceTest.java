package io.mosip.kernel.idgenerator.machineid.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.core.idgenerator.spi.MachineIdGenerator;
import io.mosip.kernel.idgenerator.machineid.entity.MachineId;
import io.mosip.kernel.idgenerator.machineid.exception.MachineIdServiceException;
import io.mosip.kernel.idgenerator.machineid.repository.MachineIdRepository;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MachineIdServiceTest {

	@Value("${mosip.kernel.mid.test.valid-initial-mid}")
	private int initialMid;

	@Value("${mosip.kernel.mid.test.valid-new-mid}")
	private int newMid;

	@Autowired
	MachineIdGenerator<String> service;

	@MockBean
	MachineIdRepository repository;

	@MockBean
	private RestTemplate restTemplate;

	@Test
	public void generateMachineIdTest() {
		MachineId entity = new MachineId();
		entity.setMId(initialMid);
		when(repository.findLastMID()).thenReturn(null);
		when(repository.create(Mockito.any())).thenReturn(entity);
		assertThat(service.generateMachineId(), is(Integer.toString(initialMid)));
	}

	@Test
	public void generateNextMachineIdTest() {
		MachineId entity = new MachineId();
		entity.setMId(initialMid);
		MachineId entityResponse = new MachineId();
		entityResponse.setMId(newMid);
		when(repository.findLastMID()).thenReturn(entity);
		when(repository.create(Mockito.any())).thenReturn(entityResponse);
		assertThat(service.generateMachineId(), is(Integer.toString(newMid)));
	}

	@Test(expected = MachineIdServiceException.class)
	public void generateIdFetchExceptionTest() {
		when(repository.findLastMID()).thenThrow(new DataAccessLayerException("", "cannot execute statement", null));
		service.generateMachineId();
	}

	@Test(expected = MachineIdServiceException.class)
	public void generateIdInsertExceptionTest() {
		when(repository.findLastMID()).thenReturn(null);
		when(repository.create(Mockito.any()))
				.thenThrow(new MachineIdServiceException("", "cannot execute statement", new RuntimeException()));
		service.generateMachineId();
	}

	@Test(expected = MachineIdServiceException.class)
	public void idServiceFetchExceptionTest() throws Exception {

		when(repository.findLastMID())
				.thenThrow(new DataAccessLayerException("", "cannot execute statement", new RuntimeException()));
		service.generateMachineId();
	}

	@Test(expected = MachineIdServiceException.class)
	public void idServiceInsertExceptionTest() throws Exception {
		when(repository.create(Mockito.any()))
				.thenThrow(new MachineIdServiceException("", "cannot execute statement", new RuntimeException()));
		service.generateMachineId();
	}

	@Test(expected = MachineIdServiceException.class)
	public void machineIdServiceExceptionTest() throws Exception {
		MachineId entity = new MachineId();
		entity.setMId(1000);
		when(repository.findLastMID()).thenReturn(entity);
		when(repository.create(Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "cannot execute statement", new RuntimeException()));
		service.generateMachineId();
	}

}
