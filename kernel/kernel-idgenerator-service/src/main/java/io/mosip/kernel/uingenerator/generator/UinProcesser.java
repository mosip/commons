package io.mosip.kernel.uingenerator.generator;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.idgenerator.spi.UinGenerator;
import io.mosip.kernel.uingenerator.constant.UinGeneratorConstant;
import io.mosip.kernel.uingenerator.repository.UinRepository;

/**
 * Decides whether the unused UIN pool is below threshold and asks {@link UinGenerator} to refill it.
 */
@Component
public class UinProcesser {
	// private static final Logger LOGGER =
	// LoggerFactory.getLogger(UinProcesser.class);

	/**
	 * Field for uinRepository
	 */
	@Autowired
	private UinRepository uinRepository;

	/**
	 * Field for uinGeneratorImpl
	 */
	@Autowired
	private UinGenerator uinGeneratorImpl;

	/**
	 * Long field for uin threshold count
	 */
	/**
	 * Unused-UIN count that triggers generation ({@code mosip.kernel.uin.min-unused-threshold}).
	 */
	@Value("${mosip.kernel.uin.min-unused-threshold}")
	private long thresholdUinCount;

	/**
	 * Target unused UIN count ({@code mosip.kernel.uin.uins-to-generate}).
	 */
	@Value("${mosip.kernel.uin.uins-to-generate}")
	long uinsCount;

	/**
	 * Returns whether unused UIN count is below {@code mosip.kernel.uin.min-unused-threshold}.
	 *
	 * @return {@code true} when generation should run
	 */
	public boolean shouldGenerateUins() {
		// LOGGER.info("Uin threshold is {}", thresholdUinCount);
		long freeUinsCount = uinRepository.countByStatus(UinGeneratorConstant.UNUSED);
		// LOGGER.info("Number of free UINs in database is {}", freeUinsCount);
		return freeUinsCount < thresholdUinCount;
	}

	/**
	 * Generates enough unused UINs to reach {@code mosip.kernel.uin.uins-to-generate}.
	 */
	public void generateUins() {
		long noOfUnUsedUins = uinRepository.countByStatusAndIsDeletedFalse(UinGeneratorConstant.UNUSED);
		uinGeneratorImpl.generateId(uinsCount <= noOfUnUsedUins ? 0 : uinsCount - noOfUnUsedUins);
	}

}
