package io.mosip.kernel.vidgenerator.verticle;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import io.mosip.kernel.core.idgenerator.spi.VidGenerator;
import io.mosip.kernel.vidgenerator.constant.EventType;
import io.mosip.kernel.vidgenerator.constant.VidLifecycleStatus;
import io.mosip.kernel.vidgenerator.entity.VidEntity;
import io.mosip.kernel.vidgenerator.generator.VidWriter;
import io.mosip.kernel.vidgenerator.utils.VIDMetaDataUtil;
import io.mosip.kernel.vidgenerator.utils.VidBloomFilter;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class VidPopulatorVerticle extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(VidPopulatorVerticle.class);

	private long vidToGenerate;

	private int batchWriteSize;

	private VidWriter vidWriter;

	private VidBloomFilter vidBloomFilter;

	private VIDMetaDataUtil metaDataUtil;

	@SuppressWarnings("unchecked")
	private VidGenerator<String> vidGenerator;

	@SuppressWarnings("unchecked")
	public VidPopulatorVerticle(final ApplicationContext context) {
		Environment environment = context.getBean(Environment.class);
		this.vidToGenerate = Objects.requireNonNullElse(
				environment.getProperty("mosip.kernel.vid.vids-to-generate", Long.class), 0L);
		this.batchWriteSize = Objects.requireNonNullElse(
				environment.getProperty("mosip.kernel.vid.batch-write-size", Integer.class), 1000);
		this.vidWriter = context.getBean(VidWriter.class);
		this.vidBloomFilter = context.getBean(VidBloomFilter.class);
		this.metaDataUtil = context.getBean(VIDMetaDataUtil.class);
		this.vidGenerator = context.getBean(VidGenerator.class);
	}

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		vertx.eventBus().consumer(EventType.GENERATEPOOL, handler -> {
			long noOfFreeVids = Long.parseLong(handler.body().toString());
			long noOfVidsToGenerate = vidToGenerate - noOfFreeVids;
			LOGGER.info("Persisting {} VIDs in pool (free={}, target={})", noOfVidsToGenerate, noOfFreeVids, vidToGenerate);

			vertx.executeBlocking(future -> {
				long vidCount = 0;
				List<VidEntity> batch = new ArrayList<>(batchWriteSize);

				vidWriter.setSession();
				try {
					while (vidCount < noOfVidsToGenerate) {
						String vid = vidGenerator.generateId();
						if (!vidBloomFilter.mightContain(vid)) {
							vidBloomFilter.put(vid);
							VidEntity entity = new VidEntity();
							entity.setVid(vid);
							entity.setStatus(VidLifecycleStatus.AVAILABLE);
							metaDataUtil.setCreateMetaData(entity);
							batch.add(entity);
							vidCount++;

							if (batch.size() >= batchWriteSize) {
								vidWriter.persistVidBatch(batch);
								batch.clear();
							}
						}
					}
					if (!batch.isEmpty()) {
						vidWriter.persistVidBatch(batch);
						batch.clear();
					}
				} finally {
					vidWriter.closeSession();
				}

				LOGGER.info("Persisted {} VIDs in pool", vidCount);
				future.complete("pool population successful");
			}, false, result -> {
				if (result.succeeded()) {
					handler.reply(result.result());
				} else {
					LOGGER.error("VID pool population failed", result.cause());
					handler.fail(500, result.cause().getMessage());
				}
			});
		});
	}
}
