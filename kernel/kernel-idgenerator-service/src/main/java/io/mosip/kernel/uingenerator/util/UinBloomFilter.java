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

import io.mosip.kernel.uingenerator.repository.UinRepositoryAssigned;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Thread-safe Bloom filter over the assigned UIN table.
 *
 * Eliminates DB round-trips for UINs that are definitely not assigned.
 * False positives fall through to a DB confirmation in UinServiceImpl.
 */
@Component
public class UinBloomFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(UinBloomFilter.class);

	private static final int INIT_PAGE_SIZE = 50_000;

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
		Pageable pageable = PageRequest.of(0, INIT_PAGE_SIZE);
		Page<String> page;
		do {
			page = uinRepositoryAssigned.findAllUins(pageable);
			for (String uin : page.getContent()) {
				bf.put(uin);
				count++;
			}
			pageable = page.nextPageable();
		} while (page.hasNext());

		filter = bf;

		if (count >= expectedInsertions * 0.9) {
			LOGGER.warn(
				"Bloom filter loaded {} UIns which is >= 90% of configured capacity {}. " +
				"Increase mosip.kernel.uin.bloom-filter.expected-insertions to maintain low false-positive rate.",
				count, expectedInsertions);
		}
		LOGGER.info("Bloom filter initialized: {} UIns loaded in {} ms (expectedInsertions={}, fpp={})",
				count, System.currentTimeMillis() - start, expectedInsertions, fpp);
	}

	/**
	 * Returns false if the UIN is definitely NOT in the assigned table.
	 * Returns true if it might be (requires DB confirmation).
	 */
	public boolean mightContain(String uin) {
		return filter.mightContain(uin);
	}

	/**
	 * Records a UIN as assigned. Safe to call from multiple threads.
	 */
	public void put(String uin) {
		filter.put(uin);
	}
}
