package io.mosip.kernel.idgenerator.prid.impl;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.idgenerator.spi.PridGenerator;
import io.mosip.kernel.core.util.ChecksumUtils;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.idgenerator.prid.constant.PridPropertyConstant;
import io.mosip.kernel.idgenerator.prid.util.PridFilterUtils;

/**
 * PridGenerator to generate PRID and generated PRID after the validation from
 * IdFilter
 * 
 * @author Rupika
 * @author Megha Tanga
 * @author Ritesh Sinha
 * @since 1.0.0
 *
 */
@Component
public class PridGeneratorImpl implements PridGenerator<String> {

	boolean init = true;

	private String randomSeed;

	private String counter;

	/**
	 * Field to hold PridFilterUtils object
	 */
	@Autowired
	PridFilterUtils pridFilterUtils;

	/**
	 * Field that takes Integer.This field decides the length of the PRID. It is
	 * read from the properties file.
	 */
	@Value("${mosip.kernel.prid.length}")
	private int pridLength;

	@Value("${mosip.idgen.prid.secure-random-reinit-frequency:45}")
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
			initialize();
		}	
	}
	
	private void initialize() {
		SecureRandom random = new SecureRandom();
		byte[] randomSeedBytes = new byte[Integer.parseInt(PridPropertyConstant.RANDOM_NUMBER_SIZE.getProperty())];
		random.nextBytes(randomSeedBytes);
		randomSeed = new BigInteger(randomSeedBytes).abs().toString().substring(0,
				Integer.parseInt(PridPropertyConstant.RANDOM_NUMBER_SIZE.getProperty()));
		do {
			byte[] counterBytes = new byte[Integer.parseInt(PridPropertyConstant.RANDOM_NUMBER_SIZE.getProperty())];
			random.nextBytes(counterBytes);
			counter = new BigInteger(counterBytes).abs().toString().substring(0,
					Integer.parseInt(PridPropertyConstant.RANDOM_NUMBER_SIZE.getProperty()));
		} while (counter.charAt(0) == '0');
	}

	@Override
	public String generateId() {
		String generatedVid = generateRandomId();
		while (!pridFilterUtils.isValidId(generatedVid) || generatedVid.contains(" ")) {
			generatedVid = generateRandomId();
		}
		return generatedVid;
	}

	/**
	 * Generates a id and then generate checksum
	 * 
	 * @param generatedIdLength The length of id to generate
	 * @return the VId with checksum
	 */
	private String generateRandomId() {
		String prid = null;
		if(counter == null) {
			initialize();
		}
		counter = init ? counter : new BigInteger(counter).add(BigInteger.ONE).toString();
		init = false;
		SecretKey secretKey = new SecretKeySpec(counter.getBytes(),
				PridPropertyConstant.ENCRYPTION_ALGORITHM.getProperty());
		byte[] encryptedData = CryptoUtil.symmetricEncrypt(secretKey, randomSeed.getBytes());
		BigInteger bigInteger = new BigInteger(encryptedData);
		prid = String.valueOf(bigInteger.abs());
		prid = prid.substring(0, pridLength - 1);
		String verhoeffDigit = ChecksumUtils.generateChecksumDigit(prid);
		return appendChecksum(prid, verhoeffDigit);
	}

	/**
	 * Appends a checksum to generated id
	 * 
	 * @param generatedIdLength The length of id
	 * @param generatedID       The generated id
	 * @param verhoeffDigit     The checksum to append
	 * @return PRID with checksum
	 */
	private String appendChecksum(String generatedPrid, String verhoeffDigit) {
		StringBuilder pridStringbuilder = new StringBuilder();
		pridStringbuilder.setLength(pridLength);
		return pridStringbuilder.insert(0, generatedPrid).insert(generatedPrid.length(), verhoeffDigit).toString()
				.trim();
	}

}