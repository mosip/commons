package io.mosip.kernel.vidgenerator.utils;

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

import io.mosip.kernel.vidgenerator.repository.VidAssignedRepository;
import io.mosip.kernel.vidgenerator.repository.VidRepository;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Thread-safe Bloom filter covering both the VID pool table and the assigned VID table.
 *
 * A single mightContain() check replaces two existsById() DB calls during pool generation.
 * VIDs that expire and are deleted from vid_assigned become ghost entries — they cause
 * false positives which are resolved by the DB fallback in VidServiceImpl, not incorrect results.
 */
@Component
public class VidBloomFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(VidBloomFilter.class);

	private static final int INIT_PAGE_SIZE = 50_000;

	@Autowired
	private VidRepository vidRepository;

	@Autowired
	private VidAssignedRepository vidAssignedRepository;

	@Value("${mosip.kernel.vid.bloom-filter.expected-insertions:50000000}")
	private long expectedInsertions;

	@Value("${mosip.kernel.vid.bloom-filter.fpp:0.001}")
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

		// Load active VID pool (bounded by vids-to-generate config, typically small)
		Pageable pageable = PageRequest.of(0, INIT_PAGE_SIZE);
		Page<String> page;
		do {
			page = vidRepository.findAllVids(pageable);
			for (String vid : page.getContent()) {
				bf.put(vid);
				count++;
			}
			pageable = page.nextPageable();
		} while (page.hasNext());

		long poolCount = count;

		// Load assigned VIDs (large, ever-growing; ghost entries for expired+released VIDs are harmless)
		pageable = PageRequest.of(0, INIT_PAGE_SIZE);
		do {
			page = vidAssignedRepository.findAllVids(pageable);
			for (String vid : page.getContent()) {
				bf.put(vid);
				count++;
			}
			pageable = page.nextPageable();
		} while (page.hasNext());

		filter = bf;

		long assignedCount = count - poolCount;
		if (count >= expectedInsertions * 0.9) {
			LOGGER.warn(
				"VID Bloom filter loaded {} VIDs (pool={}, assigned={}) which is >= 90% of configured capacity {}. " +
				"Increase mosip.kernel.vid.bloom-filter.expected-insertions to maintain low false-positive rate.",
				count, poolCount, assignedCount, expectedInsertions);
		}
		LOGGER.info("VID Bloom filter initialized: {} VIDs loaded (pool={}, assigned={}) in {} ms (expectedInsertions={}, fpp={})",
				count, poolCount, assignedCount, System.currentTimeMillis() - start, expectedInsertions, fpp);
	}

	/**
	 * Returns false if the VID is definitely NOT in either the pool or assigned table.
	 * Returns true if it might be present in either — requires DB confirmation.
	 */
	public boolean mightContain(String vid) {
		return filter.mightContain(vid);
	}

	/**
	 * Records a VID as known. Safe to call from multiple threads.
	 * Call after a VID is successfully persisted to the pool table.
	 */
	public void put(String vid) {
		filter.put(vid);
	}
}
