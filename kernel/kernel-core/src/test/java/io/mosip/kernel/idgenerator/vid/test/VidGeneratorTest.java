package io.mosip.kernel.idgenerator.vid.test;

import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertThat;

import io.mosip.kernel.idgenerator.vid.impl.VidGeneratorImpl;
import io.mosip.kernel.idgenerator.vid.util.VidFilterUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import io.mosip.kernel.core.idgenerator.spi.VidGenerator;

/**
 * Test class for vid generator.
 * 
 * @author Ritesh Sinha
 * @since 1.0.0
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { VidGeneratorImpl.class, VidFilterUtils.class})
public class VidGeneratorTest {

	@Value("${mosip.kernel.vid.length}")
	private int vidLength;

	@Value("${mosip.kernel.vid.test.random-value-number}")
	private String random;

	@Value("${mosip.kernel.vid.test.random-counter-number}")
	private String key;

	@Autowired
	VidGenerator<String> vidGenerator;

	@Test
	public void vidSequenceTest() {
		assertThat(vidGenerator.generateId(), isA(String.class));
	}

}