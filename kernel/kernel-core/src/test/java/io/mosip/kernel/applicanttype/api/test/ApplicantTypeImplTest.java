package io.mosip.kernel.applicanttype.api.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.kernel.applicanttype.api.impl.ApplicantTypeImpl;
import io.mosip.kernel.core.applicanttype.exception.InvalidApplicantArgumentException;
import io.mosip.kernel.core.applicanttype.spi.ApplicantType;

public class ApplicantTypeImplTest {

	private ApplicantType applicantType;
	private String childCode;

	private String adultCode;

	private String ageLimit;

	@Before
	public void setUp() throws Exception {
		applicantType = new ApplicantTypeImpl();
		MockitoAnnotations.initMocks(this);
	}

	@Test(expected = InvalidApplicantArgumentException.class)
	public void testInvalidQueryException() throws Exception {
		childCode = "CHILD";
		adultCode = "ADULT";
		ageLimit = "5";
		ReflectionTestUtils.setField(applicantType, "ageGroups", "{'INFANT':'0-5','MINOR':'6-17','ADULT':'18-200'}");
		ReflectionTestUtils.setField(applicantType, "script", FileUtils.readFileToString(new File("./src/test/resources/applicanttype.mvel")));
		String[] arr = "FR,FLE,ADL,FALSE".split(",");
		Map<String, Object> map = new HashMap<>();
		map.put("individualTypeCode", arr[0]);
		map.put("dateofbirth", getDobDate(arr[2]));
		map.put("genderCode", arr[1]);
		map.put("biometricAvailable", arr[3]);
	    applicantType.getApplicantType(map);
	}
	
	@Test(expected = InvalidApplicantArgumentException.class)
	public void testInvalidDOBException() throws Exception {
		childCode = "CHILD";
		adultCode = "ADULT";
		ageLimit = "5";
		ReflectionTestUtils.setField(applicantType, "ageGroups", "{'INFANT':'0-5','MINOR':'6-17','ADULT':'18-200'}");
		ReflectionTestUtils.setField(applicantType, "script", FileUtils.readFileToString(new File("./src/test/resources/applicanttype.mvel")));
		String[] arr = "NFR,FLE,ADL,TRUE".split(",");
		String DATE_PATTERN = "yyyy/MM/dd";
		Map<String, Object> map = new HashMap<>();
		map.put("individualTypeCode", arr[0]);
		map.put("dateOfBirth", LocalDate.now(ZoneId.of("UTC")).plusYears(10).format(DateTimeFormatter.ofPattern(DATE_PATTERN)));
		map.put("genderCode", arr[1]);
		map.put("biometricAvailable", arr[3]);
	    applicantType.getApplicantType(map);
	}
	
	@Test
	public void testApplicantTypeImpl() throws Exception {
		childCode = "CHL";
		adultCode = "ADL";
		ageLimit = "7";
		ReflectionTestUtils.setField(applicantType, "ageGroups", "{'INFANT':'0-5','MINOR':'6-17','ADULT':'18-200'}");
		ReflectionTestUtils.setField(applicantType, "script", FileUtils.readFileToString(new File("./src/test/resources/applicanttype.mvel")));
		String[] arr = "FR,MLE,ADL,TRUE".split(",");
		Map<String, Object> map = new HashMap<>();
		map.put("residenceStatusCode", arr[0]);
		map.put("dateOfBirth", getDobDate(arr[2]));
		map.put("genderCode", arr[1]);
		map.put("biometricAvailable", arr[3]);
	    String code=applicantType.getApplicantType(map);
	    assertThat(code,is("002"));
	}

	private String getDobDate(String string) {
		int age = Integer.parseInt(ageLimit);
		LocalDate  ldt =LocalDate.now(ZoneId.of("UTC"));
		String DATE_PATTERN = "yyyy/MM/dd";
		if (string.equals(childCode)) {
			return ldt.minusYears(age - 2).format(DateTimeFormatter.ofPattern(DATE_PATTERN));
		} else if (string.equals(adultCode)) {
			return ldt.minusYears(age + 1).format(DateTimeFormatter.ofPattern(DATE_PATTERN));
		}
		return null;
	}

}