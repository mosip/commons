package io.mosip.kernel.saltgenerator.step.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.item.Chunk;

import io.mosip.kernel.core.saltgenerator.constant.SaltGeneratorErrorConstants;
import io.mosip.kernel.core.saltgenerator.exception.SaltGeneratorException;
import io.mosip.kernel.saltgenerator.entity.SaltEntity;
import io.mosip.kernel.saltgenerator.repository.SaltRepository;
import io.mosip.kernel.saltgenerator.step.SaltWriter;

@RunWith(MockitoJUnitRunner.class)
public class SaltWriterTest {

	@InjectMocks
	SaltWriter writer;

	@Mock
	SaltRepository repo;

	@Test
	public void testWriter() throws Exception {
		SaltEntity entity = new SaltEntity();
		entity.setId(0l);
		when(repo.countByIdIn(Mockito.any())).thenReturn(0l);
		writer.write(Chunk.of(entity));
	}

	@Test
	public void testWriterRecordExists() throws Exception {
		try {
			SaltEntity entity = new SaltEntity();
			entity.setId(0l);
			when(repo.countByIdIn(Mockito.any())).thenReturn(1l);
			writer.write(Chunk.of(entity));
		} catch (SaltGeneratorException e) {
			assertEquals(SaltGeneratorErrorConstants.RECORD_EXISTS.getErrorCode(), e.getErrorCode());
			assertEquals(SaltGeneratorErrorConstants.RECORD_EXISTS.getErrorMessage(), e.getErrorText());
		}
	}

}
