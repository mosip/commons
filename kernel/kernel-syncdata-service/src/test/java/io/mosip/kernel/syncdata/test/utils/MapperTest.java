package io.mosip.kernel.syncdata.test.utils;

import static io.mosip.kernel.syncdata.utils.MapperUtils.map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import io.mosip.kernel.syncdata.dto.ApplicationDto;
import io.mosip.kernel.syncdata.dto.MachineDto;
import io.mosip.kernel.syncdata.dto.RegistrationCenterDeviceDto;
import io.mosip.kernel.syncdata.entity.Language;
import io.mosip.kernel.syncdata.utils.MapperUtils;

/**
 * 
 * @author Bal Vikash Sharma
 * @since 1.0.0
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class MapperTest {
	
	@Autowired
	private MapperUtils mapperUtils;

	
	@Test(expected = NullPointerException.class)
	public void testMapSourceNull() {
		map(null, new Language());
	}

	@Test(expected = NullPointerException.class)
	public void testMapDestinationNull() {
		map(new Language(), null);
	}

	@Test
	public void testObjectMapperWithNullArg() {
		try {
			assertNull(mapperUtils.getObjectAsJsonString(null));
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testObjectMapperWithValidArg() {
		try {
			ApplicationDto dto = new ApplicationDto("AO1", "app1", "app desc");			
			assertEquals("{\"code\":\"AO1\",\"name\":\"app1\",\"description\":\"app desc\",\"isDeleted\":null,\"langCode\":null,\"isActive\":null}", mapperUtils.getObjectAsJsonString(dto));
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test 
	public void testObjectMapperForLocalDateFormat() { 
		try { 
			MachineDto dto = new MachineDto();
			dto.setName("Test machine");
			dto.setValidityDateTime(LocalDateTime.MIN);
			
			assertEquals("{\"id\":null,\"name\":\"Test machine\",\"serialNum\":null,\"macAddress\":null,\"ipAddress\":null,\"machineSpecId\":null,\"validityDateTime\":\"-999999999-01-01T00:00:00\",\"keyIndex\":null,\"publicKey\":null,\"isDeleted\":null,\"langCode\":null,\"isActive\":null}", mapperUtils.getObjectAsJsonString(dto)); 
		} catch (Exception e) {
			Assert.fail(e.getMessage()); 
		}
	}
}
