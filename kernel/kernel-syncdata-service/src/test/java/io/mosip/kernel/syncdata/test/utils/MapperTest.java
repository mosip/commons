package io.mosip.kernel.syncdata.test.utils;

import static io.mosip.kernel.syncdata.utils.MapperUtils.map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
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
import io.mosip.kernel.syncdata.entity.RegistrationCenter;
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
			JSONObject expected = new JSONObject("{\"code\":\"AO1\",\"name\":\"app1\",\"description\":\"app desc\",\"isDeleted\":null,\"langCode\":null,\"isActive\":null}");
			JSONObject actual = new JSONObject(mapperUtils.getObjectAsJsonString(dto));
			assertEquals(expected.getString("code"), actual.getString("code"));
			assertEquals(expected.getString("name"), actual.getString("name"));
			assertEquals(expected.getString("description"), actual.getString("description"));
			assertEquals(expected.getString("langCode"), actual.getString("langCode"));
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test 
	public void testObjectMapperForLocalDateTimeFormat() { 
		try { 
			MachineDto dto = new MachineDto();
			dto.setName("Test machine");
			dto.setValidityDateTime(LocalDateTime.MIN);			
			JSONObject actual = new JSONObject(mapperUtils.getObjectAsJsonString(dto));			
			assertEquals("-999999999-01-01T00:00:00", actual.getString("validityDateTime")); 
		} catch (Exception e) {
			Assert.fail(e.getMessage()); 
		}
	}
	
	@Test
	public void testLocalTimeFormat() {
		LocalTime localTime = LocalTime.parse("09:30:04");
		RegistrationCenter registrationCenter = new RegistrationCenter();
		registrationCenter.setId("1011");
		registrationCenter.setAddressLine1("address-line1");
		registrationCenter.setAddressLine2("address-line2");
		registrationCenter.setAddressLine3("address-line3");
		registrationCenter.setCenterEndTime(localTime);
		registrationCenter.setCenterStartTime(localTime);
		registrationCenter.setCenterTypeCode("T1011");
		registrationCenter.setContactPerson("admin");
		registrationCenter.setContactPhone("9865123456");
		registrationCenter.setHolidayLocationCode("LOC01");
		registrationCenter.setIsActive(true);
		registrationCenter.setLangCode("ENG");
		registrationCenter.setWorkingHours("9");
		registrationCenter.setLunchEndTime(localTime);
		registrationCenter.setLunchStartTime(localTime);
		
		try {
			String jsonString = mapperUtils.getObjectAsJsonString(registrationCenter);
			JSONObject actual = new JSONObject(jsonString);	
			assertEquals("09:30:04", actual.getString("lunchEndTime"));
			assertEquals("09:30:04", actual.getString("lunchStartTime"));
			assertEquals("09:30:04", actual.getString("centerEndTime"));
			assertEquals("09:30:04", actual.getString("centerStartTime"));
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
}
