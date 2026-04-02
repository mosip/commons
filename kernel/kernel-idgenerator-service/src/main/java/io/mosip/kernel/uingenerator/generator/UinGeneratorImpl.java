package io.mosip.kernel.uingenerator.generator;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import io.mosip.kernel.core.idgenerator.spi.UinGenerator;
import io.mosip.kernel.core.util.ChecksumUtils;
import io.mosip.kernel.uingenerator.constant.UinGeneratorConstant;
import io.mosip.kernel.uingenerator.entity.UinEntity;
import io.mosip.kernel.uingenerator.service.UinService;
import io.mosip.kernel.uingenerator.util.UINMetaDataUtil;
import io.mosip.kernel.uingenerator.util.UinFilterUtil;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceException;

/**
 * This class generates a list of uins
 * 
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
@Component
public class UinGeneratorImpl implements UinGenerator {
	/**
	 * instance of {@link UinFilterUtil}
	 */
	@Autowired
	private UinFilterUtil uinFilterUtils;

	/**
	 * instance of {@link UINMetaDataUtil}
	 */
	@Autowired
	private UINMetaDataUtil metaDataUtil;

	@Autowired
	private UinService uinService;

	/**
	 * Field for UinWriter
	 */
	@Autowired
	private UinWriter uinWriter;

	@Autowired
	private EntityManagerFactory entityManagerFactory;

	/**
	 * The logger instance
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(UinGeneratorImpl.class);

	/**
	 * Field for number of uins to generate
	 */
	private final long uinsCount;

	/**
	 * The length of the uin
	 */
	private final int uinLength;

	/**
	 * The uin default status
	 */
	private final String uinDefaultStatus;
	private SecureRandom random;

	@Value("${mosip.idgen.uin.secure-random-reinit-frequency:45}")
	private int reInitSecureRandomFrequency;

	private Set<String> generatedSet = new HashSet<>();

	/*
	 * ✅ Can the Bloom Filter Hold 1 Billion Records?
	 *
	 * Yes, but sufficient memory must be allocated.
	 *
	 * 📌 Memory Requirement Estimation: Using the formula: m = -(n * ln(f)) /
	 * (ln(2)^2)
	 *
	 * Where: n = 1,000,000,000 // number of elements f = 0.001 // desired false
	 * positive rate (0.1%)
	 *
	 * Calculation: m ≈ 1,000,000,000 * 6.91 / 0.48 ≈ 14.4 billion bits ≈ 1.8
	 * billion bytes ≈ 1.68 GB of RAM
	 *
	 * 🧠 Important: - Increase JVM heap size using: -Xmx4g or higher - Consider
	 * serializing the Bloom filter to disk to avoid rebuilding
	 */
	private BloomFilter<CharSequence> uinBloomFilter;

	@SuppressWarnings("deprecation")
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
	 * Constructor to set {@link #uinsCount} and {@link #uinLength}
	 * 
	 * @param uinsCount The number of uins to generate
	 * @param uinLength The length of the uin
	 */
	public UinGeneratorImpl(@Value("${mosip.kernel.uin.uins-to-generate}") long uinsCount,
			@Value("${mosip.kernel.uin.length}") int uinLength) {
		this.uinsCount = uinsCount;
		this.uinLength = uinLength;
		this.uinDefaultStatus = UinGeneratorConstant.UNUSED;
	}

	// private static final RandomDataGenerator RANDOM_DATA_GENERATOR = new
	// RandomDataGenerator();

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.core.spi.idgenerator.IdGenerator#generateId()
	 */

	/*
	 * @Override public void generateId(long noOfUINToGenerate) { int
	 * generatedIdLength = uinLength - 1; long uinCount = 0; long upperBound =
	 * Long.parseLong(StringUtils.repeat(UinGeneratorConstant.NINE,
	 * generatedIdLength)); long lowerBound =
	 * Long.parseLong(StringUtils.repeat(UinGeneratorConstant.ZERO,
	 * generatedIdLength)); uinWriter.setSession(); while (uinCount <
	 * noOfUINToGenerate) {
	 *
	 * String generatedUIN = generateSingleId(generatedIdLength, lowerBound,
	 * upperBound); if (uinFilterUtils.isValidId(generatedUIN) &&
	 * !uinService.uinExist(generatedUIN)) { UinEntity uinBean = new
	 * UinEntity(generatedUIN, uinDefaultStatus);
	 * metaDataUtil.setCreateMetaData(uinBean); uinWriter.persistUin(uinBean);
	 * uinCount++; }
	 *
	 * String generatedUIN = null; do { generatedUIN =
	 * generateSingleId(generatedIdLength, lowerBound, upperBound); } while
	 * (generatedSet.contains(generatedUIN) ||
	 * !uinFilterUtils.isValidId(generatedUIN) ||
	 * uinService.uinExist(generatedUIN));
	 *
	 * generatedSet.add(generatedUIN); // add only after confirming uniqueness
	 *
	 * UinEntity uinBean = new UinEntity(generatedUIN, uinDefaultStatus);
	 * metaDataUtil.setCreateMetaData(uinBean); uinWriter.persistUin(uinBean);
	 * uinCount++; } uinWriter.closeSession(); LOGGER.info("Generated {} uins ",
	 * uinsCount); }
	 */

	public void initializeBloomFilter() {
		long expectedUinCount = Math.max(getExistingUinCountFromDB(), 100000); // Get UIN count for optimal sizing
		this.uinBloomFilter = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8),
				expectedUinCount,
				0.001 // 0.1% false positive rate
		);

		LOGGER.info("📦 Preloading Bloom filter with ~{} UINs...", expectedUinCount);

		EntityManager em = entityManagerFactory.createEntityManager();
		try {
			// Stream UINs in chunks to avoid memory pressure
			int batchSize = 10000;
			int offset = 0;
			while (true) {
				List<String> uins = em.createQuery("SELECT u.uin FROM UinEntity u", String.class).setFirstResult(offset)
						.setMaxResults(batchSize).getResultList();

				if (uins.isEmpty())
					break;

				for (String uin : uins) {
					uinBloomFilter.put(uin);
				}

				offset += uins.size();
				LOGGER.info("🔄 Loaded {} UINs into Bloom filter...", offset);
			}

			LOGGER.info("✅ Bloom filter initialized with all existing UINs.");
		} finally {
			em.close();
		}
	}

	public long getExistingUinCountFromDB() {
		EntityManager em = entityManagerFactory.createEntityManager();
		try {
			return em.createQuery("SELECT COUNT(u) FROM UinEntity u", Long.class).getSingleResult();
		} finally {
			em.close();
		}
	}

	@Override
	public void generateId(long noOfUINToGenerate) {
		LOGGER.info("✅ Started {} UINs (single-threaded, batched)", noOfUINToGenerate);
		initializeBloomFilter();

		long startTime = System.nanoTime(); // Start timer

		int generatedIdLength = uinLength - 1;
		long upperBound = Long.parseLong(StringUtils.repeat(UinGeneratorConstant.NINE, generatedIdLength));
		long lowerBound = Long.parseLong(StringUtils.repeat(UinGeneratorConstant.ZERO, generatedIdLength));

		int batchSize = 5000;
		int adjustedBatchSize = (int) Math.min(batchSize, noOfUINToGenerate);

		EntityManager em = entityManagerFactory.createEntityManager();
		EntityTransaction tx = em.getTransaction();

		try {
			long count = 0;
			List<UinEntity> batch = new ArrayList<>(batchSize);

			while (count < noOfUINToGenerate) {
				String uin = generateSingleId(generatedIdLength, lowerBound, upperBound);
				if (!uinBloomFilter.mightContain(uin) && uinFilterUtils.isValidId(uin)) {
					uinBloomFilter.put(uin);

					UinEntity entity = new UinEntity(uin, uinDefaultStatus);
					metaDataUtil.setCreateMetaData(entity);
					batch.add(entity);
					count++;

					if (batch.size() >= adjustedBatchSize || count == noOfUINToGenerate) {
						count -= batch.size(); // Reset counter before retry
						int inserted = insertBatch(em, batch);
						count += inserted;
						batch.clear();
					}
				}
			}

			long endTime = System.nanoTime();
			long durationMillis = (endTime - startTime) / 1_000_000;
			LOGGER.info("✅ Generated {} UINs (single-threaded, batched)", noOfUINToGenerate);
			LOGGER.info("⏱️ Total time taken for (single-threaded, batched): {} ms (~{} seconds)", durationMillis, durationMillis / 1000);
		} catch (Exception e) {
			if (tx.isActive()) {
				tx.rollback();
			}
			LOGGER.error("❌ UIN generation failed", e);
		} finally {
			em.close();
		}
	}

	/**
	 * Generates a id and then generate checksum
	 * 
	 * @param generatedIdLength The length of id to generate
	 * @param lowerBound        The lowerbound for generating id
	 * @param upperBound        The upperbound for generating id
	 * @return the uin with checksum
	 */
	private String generateSingleId(int generatedIdLength, long lowerBound, long upperBound) {
		byte[] randomSeedBytes = new byte[generatedIdLength];
		if(random==null) {
			initializeSecureRandom();
		}
		random.nextBytes(randomSeedBytes);

		long range = upperBound - lowerBound + 1;
		// long randomNumber = ThreadLocalRandom.current().nextLong(range) + lowerBound;
		long randomNumber = (Math.abs(random.nextLong()) % range) + lowerBound;
		String generatedID = String.format("%0" + generatedIdLength + "d", randomNumber);

		// String generatedID = new
		// BigInteger(randomSeedBytes).abs().toString().substring(0, generatedIdLength);
		String verhoeffDigit = ChecksumUtils.generateChecksumDigit(String.valueOf(generatedID));
		return appendChecksum(generatedIdLength, generatedID, verhoeffDigit);
	}

	/**
	 * Appends a checksum to generated id
	 * 
	 * @param generatedIdLength The length of id
	 * @param generatedID       The generated id
	 * @param verhoeffDigit     The checksum to append
	 * @return uin with checksum
	 */
	/*
	 * private String appendChecksum(int generatedIdLength, String generatedID,
	 * String verhoeffDigit) { StringBuilder uinStringBuilder = new StringBuilder();
	 * uinStringBuilder.setLength(uinLength); return uinStringBuilder.insert(0,
	 * generatedID).insert(generatedID.length(), verhoeffDigit).toString().trim(); }
	 */

	private String appendChecksum(int generatedIdLength, String generatedID, String verhoeffDigit) {
		return generatedID + verhoeffDigit;
	}

	private int insertBatch(EntityManager em, List<UinEntity> batch) {
		if (batch == null || batch.isEmpty()) return 0;

		List<String> uinStrings = batch.stream().map(UinEntity::getUin).toList();
		List<String> existingUins = em.createQuery("SELECT u.uin FROM UinEntity u WHERE u.uin IN :uins", String.class)
				.setParameter("uins", uinStrings)
				.getResultList();
		Set<String> existingSet = new HashSet<>(existingUins);

		List<UinEntity> filteredBatch = batch.stream()
				.filter(u -> !existingSet.contains(u.getUin()))
				.toList();

		if (filteredBatch.isEmpty()) return 0;

		EntityTransaction tx = em.getTransaction();
		int insertedCount = 0;

		try {
			tx.begin();
			for (UinEntity entity : filteredBatch) {
				em.persist(entity);
				insertedCount++;
			}
			em.flush();
			em.clear();
			tx.commit();
		} catch (PersistenceException e) {
			tx.rollback();
			LOGGER.warn("⚠️ Batch insert failed. Retrying individually...");

			for (UinEntity entity : filteredBatch) {
				EntityTransaction retryTx = em.getTransaction();
				try {
					boolean exists = em.createQuery("SELECT COUNT(u) FROM UinEntity u WHERE u.uin = :uin", Long.class)
							.setParameter("uin", entity.getUin())
							.getSingleResult() > 0;
					if (!exists) {
						retryTx.begin();
						em.persist(entity);
						em.flush();
						retryTx.commit();
						insertedCount++;
					}
				} catch (Exception retryEx) {
					retryTx.rollback();
					LOGGER.warn("❌ Retry failed for UIN: {}", entity.getUin());
				}
			}
		}
		return insertedCount;
	}

}
