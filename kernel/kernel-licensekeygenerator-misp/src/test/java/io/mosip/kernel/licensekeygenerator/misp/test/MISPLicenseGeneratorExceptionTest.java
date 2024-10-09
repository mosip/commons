package io.mosip.kernel.licensekeygenerator.misp.test;

import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.security.SecureRandom;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import io.mosip.kernel.licensekeygenerator.misp.exception.LengthNotSameException;
import io.mosip.kernel.licensekeygenerator.misp.util.MISPLicenseKeyGeneratorUtil;

/**
 * This class has test methods to check for exceptions.
 * 
 * @author Sagar Mahapatra
 * @since 1.0.0
 *
 */
@SpringBootTest(classes = MISPLicenseKeyGeneratorUtil.class)
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", 
	  "javax.xml.*", "org.xml.*", "org.w3c.dom.*",
	  "com.sun.org.apache.xalan.*", "jakarta.activation.*"})
@PrepareForTest(value = RandomStringUtils.class)
public class MISPLicenseGeneratorExceptionTest {
	/**
	 * The default length specified for license key.
	 */
	@Value("${mosip.kernel.idgenerator.misp.license-key-length}")
	private int licenseKeyLength;

	/**
	 * Autowired reference for {@link MISPLicenseKeyGeneratorUtil}.
	 */
	@Autowired
	MISPLicenseKeyGeneratorUtil licenseGeneratorUtil;

	/**
	 * Test Scenario : It should throw an exception when the license key generated
	 * has length different than the specified one.
	 */
	@Test(expected = LengthNotSameException.class)
	public void lengthNotSameExceptionTest() {
		String dummyValue = "";
		if (licenseKeyLength == 0) {
			dummyValue = "failTheTestCase";
		}
		mockStatic(RandomStringUtils.class);
		when(RandomStringUtils.random(Mockito.eq(licenseKeyLength),Mockito.eq(0),Mockito.eq(0),Mockito.eq(true),Mockito.eq(true),Mockito.eq(null),Mockito.any(SecureRandom.class))).thenReturn(dummyValue);
		licenseGeneratorUtil.generate();
	}
}
