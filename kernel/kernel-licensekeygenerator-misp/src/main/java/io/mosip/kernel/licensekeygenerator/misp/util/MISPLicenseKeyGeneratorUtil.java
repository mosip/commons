package io.mosip.kernel.licensekeygenerator.misp.util;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.PostConstruct;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import io.mosip.kernel.licensekeygenerator.misp.constant.MISPLicenseKeyGeneratorConstant;
import io.mosip.kernel.licensekeygenerator.misp.exception.LengthNotSameException;

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
	private int licenseKeyLength = 10;

	private SecureRandom random;

	@Value("${mosip.idgen.misp.secure-random-reinit-frequency:45}")
	private int reInitSecureRandomFrequency;

	@PostConstruct
	private void init() {
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setPoolSize(1);
		taskScheduler.initialize();
		taskScheduler.scheduleAtFixedRate(new ReInitSecureRandomTask(),
				TimeUnit.MINUTES.toMillis(reInitSecureRandomFrequency));
	}

	private class ReInitSecureRandomTask implements Runnable {

		public void run() {
			initializeSecureRandom();
		}
	}
	
	private void initializeSecureRandom() {
		random = new SecureRandom();
	}

	/**
	 * Method to generate license key.
	 * 
	 * @return the generated license key.
	 */
	public String generate() {
		if(random ==null)
			initializeSecureRandom();
		String generatedLicenseKey = RandomStringUtils.random(licenseKeyLength, 0, 0, true, true, null, random);
		if (generatedLicenseKey.length() != licenseKeyLength) {
			throw new LengthNotSameException(MISPLicenseKeyGeneratorConstant.LENGTH_NOT_SAME.getErrorCode(),
					MISPLicenseKeyGeneratorConstant.LENGTH_NOT_SAME.getErrorMessage());
		}
		return generatedLicenseKey;
	}
}
