package io.mosip.kernel.uingenerator.generator;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.idgenerator.spi.UinGenerator;
import io.mosip.kernel.core.util.ChecksumUtils;
import io.mosip.kernel.uingenerator.constant.UinGeneratorConstant;
import io.mosip.kernel.uingenerator.entity.UinEntity;
import io.mosip.kernel.uingenerator.service.UinService;
import io.mosip.kernel.uingenerator.util.UINMetaDataUtil;
import io.mosip.kernel.uingenerator.util.UinFilterUtil;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * This class generates a list of uins
 *
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
@Component
public class UinGeneratorImpl implements UinGenerator {

	@Autowired
	private UinFilterUtil uinFilterUtils;

	@Autowired
	private UINMetaDataUtil metaDataUtil;

	@Autowired
	private UinService uinService;

	@Autowired
	private UinWriter uinWriter;

	private static final Logger LOGGER = LoggerFactory.getLogger(UinGeneratorImpl.class);

	private final long uinsCount;
	private final int uinLength;
	private final String uinDefaultStatus;
	private SecureRandom random;

	@Value("${mosip.idgen.uin.secure-random-reinit-frequency:45}")
	private int reInitSecureRandomFrequency;

	@Value("${mosip.kernel.uin.batch-write-size:1000}")
	private int batchWriteSize;

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

	public UinGeneratorImpl(@Value("${mosip.kernel.uin.uins-to-generate}") long uinsCount,
			@Value("${mosip.kernel.uin.length}") int uinLength) {
		this.uinsCount = uinsCount;
		this.uinLength = uinLength;
		this.uinDefaultStatus = UinGeneratorConstant.UNUSED;
	}

	@Override
	public void generateId(long noOfUINToGenerate) {
		if (noOfUINToGenerate <= 0) {
			return;
		}
		int generatedIdLength = uinLength - 1;
		long uinCount = 0;
		long upperBound = Long.parseLong(StringUtils.repeat(UinGeneratorConstant.NINE, generatedIdLength));
		long lowerBound = Long.parseLong(StringUtils.repeat(UinGeneratorConstant.ZERO, generatedIdLength));

		uinWriter.setSession();
		List<UinEntity> batch = new ArrayList<>(batchWriteSize);
		try {
			while (uinCount < noOfUINToGenerate) {
				String generatedUIN = generateSingleId(generatedIdLength, lowerBound, upperBound);
				if (uinFilterUtils.isValidId(generatedUIN) && !uinService.uinExist(generatedUIN)) {
					UinEntity uinBean = new UinEntity(generatedUIN, uinDefaultStatus);
					metaDataUtil.setCreateMetaData(uinBean);
					batch.add(uinBean);
					uinCount++;

					if (batch.size() >= batchWriteSize) {
						uinWriter.persistUinBatch(batch);
						batch.clear();
					}
				}
			}
			if (!batch.isEmpty()) {
				uinWriter.persistUinBatch(batch);
				batch.clear();
			}
		} finally {
			uinWriter.closeSession();
		}
		LOGGER.info("Generated and persisted {} UIns", noOfUINToGenerate);
	}

	private String generateSingleId(int generatedIdLength, long lowerBound, long upperBound) {
		byte[] randomSeedBytes = new byte[generatedIdLength];
		if (random == null) {
			initializeSecureRandom();
		}
		random.nextBytes(randomSeedBytes);
		String generatedID = new BigInteger(randomSeedBytes).abs().toString().substring(0, generatedIdLength);
		String verhoeffDigit = ChecksumUtils.generateChecksumDigit(String.valueOf(generatedID));
		return appendChecksum(generatedIdLength, generatedID, verhoeffDigit);
	}

	private String appendChecksum(int generatedIdLength, String generatedID, String verhoeffDigit) {
		StringBuilder uinStringBuilder = new StringBuilder();
		uinStringBuilder.setLength(uinLength);
		return uinStringBuilder.insert(0, generatedID).insert(generatedID.length(), verhoeffDigit).toString().trim();
	}

}
