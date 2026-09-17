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

	/** Reused PRNG; {@link #initialize()} reseeds VID material, not a new generator each time. */
	private SecureRandom random;

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
	
	/** Minutes between {@link SecureRandom} re-initialization from {@code mosip.idgen.vid.secure-random-reinit-frequency}. */
	@Value("${mosip.idgen.vid.secure-random-reinit-frequency:45}")
	private int reInitSecureRandomFrequency;
	
	/**
	 * Schedules periodic {@link #initialize()} of the random seed and counter.
	 */
	@PostConstruct
	private void init() {
			ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
			taskScheduler.setPoolSize(1);
			taskScheduler.initialize();
			taskScheduler.scheduleAtFixedRate(new ReInitSecureRandomTask(), TimeUnit.MINUTES.toMillis(reInitSecureRandomFrequency));
	}

	/** Periodic task that replaces the VID seed and counter. */
	private class ReInitSecureRandomTask implements Runnable {

		/**
		 * Re-initializes seed and counter.
		 */
		public void run() {
			initialize();
		}

		
	}
	
	/**
	 * Draws a new {@link #randomSeed} and non-zero-leading {@link #counter}.
	 */
	private void initialize() {
		if (random == null) {
			random = new SecureRandom();
		}
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
	 * Returns a VID that passes {@link VidFilterUtils#isValidId(String)}.
	 *
	 * @return VID of configured length including checksum
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
	 * AES-wraps the seed with the incrementing counter, truncates, and appends a Verhoeff checksum.
	 *
	 * @return candidate VID (may fail {@link VidFilterUtils})
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
	 * Concatenates {@code generatedVId} and {@code verhoeffDigit} to {@link #vidLength}.
	 *
	 * @param generatedVId  VID digits without checksum
	 * @param verhoeffDigit checksum digit
	 * @return padded VID string
	 */
	private String appendChecksum(String generatedVId, String verhoeffDigit) {
		StringBuilder vidSb = new StringBuilder();
		vidSb.setLength(vidLength);
		return vidSb.insert(0, generatedVId).insert(generatedVId.length(), verhoeffDigit).toString().trim();
	}

}
