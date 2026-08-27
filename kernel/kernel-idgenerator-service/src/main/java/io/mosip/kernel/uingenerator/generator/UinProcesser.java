package io.mosip.kernel.uingenerator.generator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.idgenerator.spi.UinGenerator;
import io.mosip.kernel.uingenerator.constant.UinGeneratorConstant;
import io.mosip.kernel.uingenerator.repository.UinRepository;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

@Component
public class UinProcesser {

	private static final Logger LOGGER = LoggerFactory.getLogger(UinProcesser.class);

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
	@Value("${mosip.kernel.uin.min-unused-threshold}")
	private long thresholdUinCount;

	@Value("${mosip.kernel.uin.uins-to-generate}")
	long uinsCount;

	/**
	 * Check whether to generate uin or not
	 * 
	 * @return true, if needs to generate uin
	 */
	public boolean shouldGenerateUins() {
		// LOGGER.info("Uin threshold is {}", thresholdUinCount);
		long freeUinsCount = uinRepository.countByStatus(UinGeneratorConstant.UNUSED);
		// LOGGER.info("Number of free UINs in database is {}", freeUinsCount);
		return freeUinsCount < thresholdUinCount;
	}

	/**
	 * Create list of uins
	 */
	public void generateUins() {
		long noOfUnUsedUins = uinRepository.countByStatusAndIsDeletedFalse(UinGeneratorConstant.UNUSED);
		long toGenerate = uinsCount <= noOfUnUsedUins ? 0 : uinsCount - noOfUnUsedUins;
		LOGGER.info("UIN pool check: uins-to-generate={}, unused-in-pool={}, generating={}", uinsCount, noOfUnUsedUins, toGenerate);
		uinGeneratorImpl.generateId(toGenerate);
	}

}
