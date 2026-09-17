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
 * Generates alphanumeric MISP license keys with {@link SecureRandom}.
 * <p>
 * Length is {@code mosip.kernel.idgenerator.misp.license-key-length}. The
 * {@link SecureRandom} instance is (re)initialized on a schedule given by
 * {@code mosip.idgen.misp.secure-random-reinit-frequency} minutes.
 * </p>
 *
 * @author Sagar Mahapatra
 * @since 1.0.0
 */
@Component
public class MISPLicenseKeyGeneratorUtil {
	/**
	 * Required license-key length from {@code mosip.kernel.idgenerator.misp.license-key-length}.
	 */
	@Value("${mosip.kernel.idgenerator.misp.license-key-length}")
	private int licenseKeyLength = 10;

	private SecureRandom random;

	/** Minutes between {@link SecureRandom} re-initialization from {@code mosip.idgen.misp.secure-random-reinit-frequency}. */
	@Value("${mosip.idgen.misp.secure-random-reinit-frequency:45}")
	private int reInitSecureRandomFrequency;

	/**
	 * Schedules periodic {@link SecureRandom} re-initialization.
	 */
	@PostConstruct
	private void init() {
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setPoolSize(1);
		taskScheduler.initialize();
		taskScheduler.scheduleAtFixedRate(new ReInitSecureRandomTask(),
				TimeUnit.MINUTES.toMillis(reInitSecureRandomFrequency));
	}

	/** Periodic task that replaces {@link #random}. */
	private class ReInitSecureRandomTask implements Runnable {

		/**
		 * Re-initializes {@link #random}.
		 */
		public void run() {
			initializeSecureRandom();
		}
	}
	
	/**
	 * Assigns a new {@link SecureRandom} to {@link #random}.
	 */
	private void initializeSecureRandom() {
		random = new SecureRandom();
	}

	/**
	 * Returns an alphanumeric license key of {@link #licenseKeyLength}.
	 *
	 * @return generated license key
	 * @throws LengthNotSameException if Apache Commons returns a string of a different length
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
