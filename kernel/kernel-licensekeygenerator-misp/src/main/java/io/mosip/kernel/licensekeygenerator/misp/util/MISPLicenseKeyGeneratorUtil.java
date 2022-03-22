package io.mosip.kernel.licensekeygenerator.misp.util;

import java.security.SecureRandom;
import java.util.HashSet;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Component;

import io.mosip.kernel.licensekeygenerator.misp.constant.MISPLicenseKeyGeneratorConstant;
import io.mosip.kernel.licensekeygenerator.misp.exception.LengthNotSameException;
import io.mosip.kernel.licensekeygenerator.misp.impl.MISPLicenseKeyGeneratorImpl;
import net.bytebuddy.dynamic.scaffold.MethodRegistry.Handler.ForAbstractMethod;

/**
 * Class that provides utility methods.
 * 
 * @author Sagar Mahapatra
 * @since 1.0.0
 *
 */
@Component
public class MISPLicenseKeyGeneratorUtil {
	/**
	 * Specified length for the license key to be generated.
	 */
	@Value("${mosip.kernel.idgenerator.misp.license-key-length}")
	private int licenseKeyLength=10;

	private SecureRandom random;
	

	@PostConstruct
	private void init() {
		random = new SecureRandom();
	}

	/**
	 * Method to generate license key.
	 * 
	 * @return the generated license key.
	 */
	public String generate() {
		String generatedLicenseKey = RandomStringUtils.random(licenseKeyLength,0, 0, true, true,null,random);
		if (generatedLicenseKey.length() != licenseKeyLength) {
			throw new LengthNotSameException(MISPLicenseKeyGeneratorConstant.LENGTH_NOT_SAME.getErrorCode(),
					MISPLicenseKeyGeneratorConstant.LENGTH_NOT_SAME.getErrorMessage());
		}
		return generatedLicenseKey;
	}
}
