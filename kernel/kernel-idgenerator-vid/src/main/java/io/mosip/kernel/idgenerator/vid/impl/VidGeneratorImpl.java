package io.mosip.kernel.idgenerator.vid.impl;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.idgenerator.spi.VidGenerator;
import io.mosip.kernel.core.util.ChecksumUtils;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.idgenerator.vid.constant.VidPropertyConstant;
import io.mosip.kernel.idgenerator.vid.util.VidFilterUtils;

/**
 * This class generates a Vid.
 * 
 * @author Ritesh Sinha
 * @author Urvil Joshi
 * @author Megha Tanga
 * 
 * @since 1.0.0
 *
 */
@Component
public class VidGeneratorImpl implements VidGenerator<String> {

	boolean init = true;

	private String randomSeed;

	private String counter;

	/**
	 * Field to hold vidFilterUtils object
	 */
	@Autowired
	VidFilterUtils vidFilterUtils;

	/**
	 * The length of the VId
	 */
	@Value("${mosip.kernel.vid.length}")
	private int vidLength;
	
	@Value("${mosip.idgen.vid.secure-random-reinit-frequency:45}")
	private int reInitSecureRandomFrequency;
	
	@PostConstruct
	private void init() {
			ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
			taskScheduler.setPoolSize(1);
			taskScheduler.initialize();
			taskScheduler.scheduleAtFixedRate(new ReInitSecureRandomTask(), TimeUnit.MINUTES.toMillis(reInitSecureRandomFrequency));
	}

	private class ReInitSecureRandomTask implements Runnable {

		public void run() {
			initialize();
		}

		
	}
	
	private void initialize() {
		SecureRandom random = new SecureRandom();
		byte[] randomSeedBytes = new byte[Integer.parseInt(VidPropertyConstant.RANDOM_NUMBER_SIZE.getProperty())];
		random.nextBytes(randomSeedBytes);
		randomSeed = new BigInteger(randomSeedBytes).abs().toString().substring(0,
				Integer.parseInt(VidPropertyConstant.RANDOM_NUMBER_SIZE.getProperty()));
		do {
			byte[] counterBytes = new byte[Integer.parseInt(VidPropertyConstant.RANDOM_NUMBER_SIZE.getProperty())];
			random.nextBytes(counterBytes);
			counter = new BigInteger(counterBytes).abs().toString().substring(0,
					Integer.parseInt(VidPropertyConstant.RANDOM_NUMBER_SIZE.getProperty()));
		} while (counter.charAt(0) == '0');
	}

	/**
	 * Generates a Vid
	 * 
	 * @return a vid
	 */
	@Override
	public String generateId() {
		String generatedVid = generateRandomId();
		while (!vidFilterUtils.isValidId(generatedVid) || generatedVid.contains(" ")) {
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
		String vid = null;
		if(counter == null) {
			initialize();
		}
		counter = init ? counter : new BigInteger(counter).add(BigInteger.ONE).toString();
		init = false;
		SecretKey secretKey = new SecretKeySpec(counter.getBytes(),
				VidPropertyConstant.ENCRYPTION_ALGORITHM.getProperty());
		byte[] encryptedData = CryptoUtil.symmetricEncrypt(secretKey, randomSeed.getBytes());
		BigInteger bigInteger = new BigInteger(encryptedData);
		vid = String.valueOf(bigInteger.abs());
		vid = vid.substring(0, vidLength - 1);
		String verhoeffDigit = ChecksumUtils.generateChecksumDigit(vid);
		return appendChecksum(vid, verhoeffDigit);
	}

	/**
	 * Appends a checksum to generated id
	 * 
	 * @param generatedIdLength The length of id
	 * @param generatedID       The generated id
	 * @param verhoeffDigit     The checksum to append
	 * @return VId with checksum
	 */
	private String appendChecksum(String generatedVId, String verhoeffDigit) {
		StringBuilder vidSb = new StringBuilder();
		vidSb.setLength(vidLength);
		return vidSb.insert(0, generatedVId).insert(generatedVId.length(), verhoeffDigit).toString().trim();
	}

}
