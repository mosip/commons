package io.mosip.kernel.uingenerator.util;

import java.nio.charset.StandardCharsets;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import io.mosip.kernel.uingenerator.repository.UinRepository;
import io.mosip.kernel.uingenerator.repository.UinRepositoryAssigned;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Thread-safe Bloom filter covering both the UIN pool table and the assigned UIN table.
 *
 * A single mightContain() check replaces two existsById() DB calls during pool generation.
 * False positives (bloom filter returns true but UIN is new) fall through to DB confirmation
 * in UinServiceImpl — they produce no incorrect results, only an extra DB lookup.
 */
@Component
public class UinBloomFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(UinBloomFilter.class);

	private static final int INIT_PAGE_SIZE = 50_000;

	@Autowired
	private UinRepository uinRepository;

	@Autowired
	private UinRepositoryAssigned uinRepositoryAssigned;

	@Value("${mosip.kernel.uin.bloom-filter.expected-insertions:10000000}")
	private long expectedInsertions;

	@Value("${mosip.kernel.uin.bloom-filter.fpp:0.001}")
	private double fpp;

	private volatile BloomFilter<String> filter;

	@PostConstruct
	public void init() {
		long start = System.currentTimeMillis();
		BloomFilter<String> bf = BloomFilter.create(
				Funnels.stringFunnel(StandardCharsets.UTF_8),
				expectedInsertions,
				fpp);

		long count = 0;

		// Load active UIN pool (bounded by uins-to-generate config)
		Pageable pageable = PageRequest.of(0, INIT_PAGE_SIZE);
		Page<String> page;
		do {
			page = uinRepository.findAllUins(pageable);
			for (String uin : page.getContent()) {
				bf.put(uin);
				count++;
			}
			pageable = page.nextPageable();
		} while (page.hasNext());

		long poolCount = count;

		// Load assigned UIns (large, ever-growing)
		pageable = PageRequest.of(0, INIT_PAGE_SIZE);
		do {
			page = uinRepositoryAssigned.findAllUins(pageable);
			for (String uin : page.getContent()) {
				bf.put(uin);
				count++;
			}
			pageable = page.nextPageable();
		} while (page.hasNext());

		filter = bf;

		long assignedCount = count - poolCount;
		if (count >= expectedInsertions * 0.9) {
			LOGGER.warn(
				"UIN Bloom filter loaded {} UIns (pool={}, assigned={}) which is >= 90% of configured capacity {}. " +
				"Increase mosip.kernel.uin.bloom-filter.expected-insertions to maintain low false-positive rate.",
				count, poolCount, assignedCount, expectedInsertions);
		}
		LOGGER.info("UIN Bloom filter initialized: {} UIns loaded (pool={}, assigned={}) in {} ms (expectedInsertions={}, fpp={})",
				count, poolCount, assignedCount, System.currentTimeMillis() - start, expectedInsertions, fpp);
	}

	/**
	 * Returns false if the UIN is definitely NOT in either the pool or assigned table.
	 * Returns true if it might be present in either — requires DB confirmation.
	 */
	public boolean mightContain(String uin) {
		return filter.mightContain(uin);
	}

	/**
	 * Records a UIN as known. Safe to call from multiple threads.
	 */
	public void put(String uin) {
		filter.put(uin);
	}
}
