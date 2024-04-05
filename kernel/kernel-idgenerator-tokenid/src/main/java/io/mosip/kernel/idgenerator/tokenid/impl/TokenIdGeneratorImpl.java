package io.mosip.kernel.idgenerator.tokenid.impl;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import jakarta.persistence.PersistenceException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.core.idgenerator.spi.TokenIdGenerator;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.idgenerator.tokenid.constant.TokenIDExceptionConstant;
import io.mosip.kernel.idgenerator.tokenid.constant.TokenIdPropertyConstant;
import io.mosip.kernel.idgenerator.tokenid.entity.TokenIdSeed;
import io.mosip.kernel.idgenerator.tokenid.entity.TokenIdSequence;
import io.mosip.kernel.idgenerator.tokenid.exception.TokenIdGeneratorException;
import io.mosip.kernel.idgenerator.tokenid.repository.TokenIdSeedRepository;
import io.mosip.kernel.idgenerator.tokenid.repository.TokenIdSequenceRepository;

/**
 * Implementation class for {@link TokenIdGenerator}.
 * 
 * @author Sagar Mahapatra
 * @author Ritesh Sinha
 * @since 1.0.0
 *
 */
@Component
public class TokenIdGeneratorImpl implements TokenIdGenerator<String> {

	@Autowired
	private TokenIdSeedRepository seedRepository;

	@Autowired
	private TokenIdSequenceRepository sequenceRepository;

	private SecureRandom random;

	@Value("${mosip.idgen.tokenid.secure-random-reinit-frequency:45}")
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
	 * The length of Token ID.[fetched from configuration]
	 */
	@Value("${mosip.kernel.tokenid.length}")
	private Integer tokenIdLength;

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.core.idgenerator.spi.TokenIdGenerator#generateId()
	 */
	@Transactional(isolation = Isolation.READ_COMMITTED)
	@Override
	public String generateId() {
		String counterSecureRandom = null;
		String randomSeed = null;
		String tokenId = null;
		if(random==null) {
			initializeSecureRandom();
		}

		List<TokenIdSeed> listOfSeed = null;
		TokenIdSequence sequenceEntity = null;
		try {
			listOfSeed = seedRepository.findAll();
			sequenceEntity = sequenceRepository.findMaxSequence();
		} catch (DataAccessLayerException | DataAccessException e) {
			throw new TokenIdGeneratorException(TokenIDExceptionConstant.TOKENID_FETCH_EXCEPTION.getErrorCode(),
					TokenIDExceptionConstant.TOKENID_FETCH_EXCEPTION.getErrorMessage(), e);
		}
		try {
			TokenIdSequence counterEntity = new TokenIdSequence();
			counterEntity.setCreatedBy("SYSTEM");
			counterEntity.setCreatedDateTime(LocalDateTime.now(ZoneId.of("UTC")));
			counterEntity.setDeletedDateTime(null);
			counterEntity.setIsDeleted(false);

			if (sequenceEntity == null) {
				do {
					byte[] randomSeedBytes = new byte[Integer
							.parseInt(TokenIdPropertyConstant.RANDOM_NUMBER_SIZE.getProperty())];
					random.nextBytes(randomSeedBytes);
					counterSecureRandom = new BigInteger(randomSeedBytes).abs().toString().substring(0,
							Integer.parseInt(TokenIdPropertyConstant.RANDOM_NUMBER_SIZE.getProperty()));
				} while (counterSecureRandom.charAt(0) == '0');
				counterEntity.setSequenceNumber(counterSecureRandom);
			} else {
				counterSecureRandom = sequenceEntity.getSequenceNumber();
				counterSecureRandom = new BigInteger(counterSecureRandom).add(BigInteger.ONE).toString();
				counterEntity.setSequenceNumber(counterSecureRandom);
			}

			sequenceRepository.saveAndFlush(counterEntity);

			if (listOfSeed.isEmpty()) {
				byte[] randomSeedBytes = new byte[Integer
						.parseInt(TokenIdPropertyConstant.RANDOM_NUMBER_SIZE.getProperty())];
				random.nextBytes(randomSeedBytes);
				randomSeed = new BigInteger(randomSeedBytes).abs().toString().substring(0,
						Integer.parseInt(TokenIdPropertyConstant.RANDOM_NUMBER_SIZE.getProperty()));
				TokenIdSeed seedEntity = new TokenIdSeed();
				seedEntity.setCreatedBy("SYSTEM");
				seedEntity.setCreatedDateTime(LocalDateTime.now(ZoneId.of("UTC")));
				seedEntity.setDeletedDateTime(null);
				seedEntity.setIsDeleted(false);
				seedEntity.setSeedNumber(randomSeed);
				seedRepository.saveAndFlush(seedEntity);
			} else {
				randomSeed = listOfSeed.get(0).getSeedNumber();
			}
			SecretKey secretKey = new SecretKeySpec(counterSecureRandom.getBytes(),
					TokenIdPropertyConstant.ENCRYPTION_ALGORITHM.getProperty());
			byte[] encryptedData = CryptoUtil.symmetricEncrypt(secretKey, randomSeed.getBytes());
			BigInteger bigInteger = new BigInteger(encryptedData);
			tokenId = String.valueOf(bigInteger.abs());
		} catch (DataAccessLayerException | DataAccessException e) {
			if (e.getCause().getClass() == PersistenceException.class) {
				return generateId();
			} else {
				throw new TokenIdGeneratorException(TokenIDExceptionConstant.TOKENID_INSERTION_EXCEPTION.getErrorCode(),
						TokenIDExceptionConstant.TOKENID_INSERTION_EXCEPTION.getErrorMessage(), e);
			}
		}

		return tokenId.substring(0, tokenIdLength);

	}
}
