package io.mosip.kernel.idgenerator.rid.test;

import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.core.idgenerator.spi.RidGenerator;
import io.mosip.kernel.idgenerator.rid.entity.Rid;
import io.mosip.kernel.idgenerator.rid.exception.EmptyInputException;
import io.mosip.kernel.idgenerator.rid.exception.InputLengthException;
import io.mosip.kernel.idgenerator.rid.exception.NullValueException;
import io.mosip.kernel.idgenerator.rid.exception.RidException;
import io.mosip.kernel.idgenerator.rid.repository.RidRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RidGeneratorTest {

	@Value("${mosip.kernel.rid.test.centerId}")
	private String centerId;

	@Value("${mosip.kernel.rid.test.machineId}")
	private String machineId;

	@Value("${mosip.kernel.rid.test.invalid-length-centerId}")
	private String invalidLengthCenterId;

	@Value("${mosip.kernel.rid.test.invalid-length-machineId}")
	private String invalidMachineId;

	@Value("${mosip.kernel.rid.test.centerId-length}")
	private int centerIdLength;

	@Value("${mosip.kernel.rid.test.machineId-length}")
	private int machineIDLength;

	@Value("${mosip.kernel.rid.test.invalid-centerId-length}")
	private int invalidCenterIdLength;

	@Value("${mosip.kernel.rid.test.valid-rid}")
	private String validRid;

	@Value("${mosip.kernel.rid.test.invalid-centerid-rid}")
	private String invalidCenterIdRid;

	@Value("${mosip.kernel.rid.test.invalid-machineid-rid}")
	private String invalidMachineIdRid;

	@Value("${mosip.kernel.rid.sequence-length}")
	private int sequenceLimit;

	@Value("${mosip.kernel.rid.timestamp-length}")
	private int timestampLength;

	@MockBean
	RidRepository repository;

	@Autowired
	RidGenerator<String> ridGeneratorImpl;

	@MockBean
	private RestTemplate restTemplate;

	@Test
	public void generateIdTypeTest() {
		Rid entity = new Rid();
		entity.setCurrentSequenceNo(00001);
		when(repository.findLastRid()).thenReturn(entity);
		assertThat(ridGeneratorImpl.generateId(centerId, machineId), isA(String.class));
	}

	@Test(expected = NullValueException.class)
	public void centerIdNullExceptionTest() {
		ridGeneratorImpl.generateId(null, machineId);
	}

	@Test(expected = NullValueException.class)
	public void dongleIdNullExceptionTest() {
		ridGeneratorImpl.generateId(centerId, null);
	}

	@Test(expected = EmptyInputException.class)
	public void centerIdEmptyExceptionTest() {
		ridGeneratorImpl.generateId("", machineId);
	}

	@Test(expected = EmptyInputException.class)
	public void dongleIdEmptyExceptionTest() {
		ridGeneratorImpl.generateId(centerId, "");
	}

	@Test(expected = InputLengthException.class)
	public void centreIdLengthTest() {
		ridGeneratorImpl.generateId(invalidLengthCenterId, machineId);
	}

	@Test(expected = InputLengthException.class)
	public void dongleIdLengthTest() {
		ridGeneratorImpl.generateId(centerId, invalidMachineId);
	}

	@Test
	public void generateFirstIdTypeTest() {
		when(repository.findLastRid()).thenReturn(null);
		assertThat(ridGeneratorImpl.generateId(centerId, machineId), isA(String.class));
	}

	@Test
	public void generateIdMaxSequenceTypeTest() {
		Rid entity = new Rid();
		entity.setCurrentSequenceNo(99999);
		when(repository.findLastRid()).thenReturn(entity);
		assertThat(ridGeneratorImpl.generateId(centerId, machineId), isA(String.class));
	}

	@Test
	public void generateIdTest() {
		Rid entity = new Rid();
		entity.setCurrentSequenceNo(00001);
		when(repository.findLastRid()).thenReturn(entity);
		assertThat(ridGeneratorImpl.generateId(centerId, machineId, centerIdLength, machineIDLength, sequenceLimit,
				timestampLength), isA(String.class));
	}

	@Test(expected = RidException.class)
	public void generateIdFetchExceptionTest() {
		Rid entity = new Rid();
		entity.setCurrentSequenceNo(00001);
		when(repository.findLastRid()).thenThrow(DataRetrievalFailureException.class);
		ridGeneratorImpl.generateId(centerId, machineId, centerIdLength, machineIDLength, sequenceLimit,
				timestampLength);
	}

	@SuppressWarnings("unchecked")
	// @Test(expected = RidException.class)
	public void generateIdUpdateExceptionTest() {
		Rid entity = new Rid();
		entity.setCurrentSequenceNo(00001);
		when(repository.save(entity)).thenThrow(DataRetrievalFailureException.class, DataAccessLayerException.class);
		ridGeneratorImpl.generateId(centerId, machineId, centerIdLength, machineIDLength, sequenceLimit,
				timestampLength);
	}

	@Test(expected = InputLengthException.class)
	public void generateIdInvalidCenterIdLengthTest() {
		Rid entity = new Rid();
		entity.setCurrentSequenceNo(00001);
		when(repository.findLastRid()).thenReturn(entity);
		assertThat(ridGeneratorImpl.generateId(centerId, machineId, invalidCenterIdLength, machineIDLength,
				sequenceLimit, timestampLength), isA(String.class));
	}

	@Test(expected = InputLengthException.class)
	public void generateIdInvalidSeqLengthTest() {
		Rid entity = new Rid();
		entity.setCurrentSequenceNo(00001);
		when(repository.findLastRid()).thenReturn(entity);
		assertThat(
				ridGeneratorImpl.generateId(centerId, machineId, centerIdLength, machineIDLength, 0, timestampLength),
				isA(String.class));
	}
}
