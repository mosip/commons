package io.mosip.kernel.vidgenerator.verticle;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import io.mosip.kernel.core.idgenerator.spi.VidGenerator;
import io.mosip.kernel.vidgenerator.constant.EventType;
import io.mosip.kernel.vidgenerator.constant.VidLifecycleStatus;
import io.mosip.kernel.vidgenerator.entity.VidEntity;
import io.mosip.kernel.vidgenerator.generator.VidWriter;
import io.mosip.kernel.vidgenerator.utils.VIDMetaDataUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.Objects;

/**
 * Worker verticle that generates unused VIDs into {@code kernel.vid}.
 * <p>
 * Consumes {@link EventType#GENERATEPOOL}. Target pool size is
 * {@code mosip.kernel.vid.vids-to-generate}.
 * </p>
 */
public class VidPopulatorVerticle extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(VidPopulatorVerticle.class);

	private long vidToGenerate;

	private Environment environment;

	private VidWriter vidWriter;

	private VIDMetaDataUtil metaDataUtil;

	private VidGenerator<String> vidGenerator;

	/**
	 * Resolves generator, writer, and {@code mosip.kernel.vid.vids-to-generate} from {@code context}.
	 *
	 * @param context Spring context
	 */
	@SuppressWarnings("unchecked")
	public VidPopulatorVerticle(final ApplicationContext context) {
		this.environment = context.getBean(Environment.class);
		this.vidToGenerate = Objects.requireNonNullElse(environment.getProperty("mosip.kernel.vid.vids-to-generate", Long.class), 0L);
		this.vidWriter = context.getBean("vidWriter", VidWriter.class);
		this.metaDataUtil = context.getBean(VIDMetaDataUtil.class);
		this.vidGenerator = context.getBean(VidGenerator.class);
	}

	/**
	 * Registers the GENERATEPOOL consumer that persists unused VIDs until the target count is reached.
	 *
	 * @param startFuture completed after the consumer is registered
	 * @throws Exception unused; Vert.x {@code start} contract
	 */
	@Override
	public void start(Future<Void> startFuture) throws Exception {
		vertx.eventBus().consumer(EventType.GENERATEPOOL, handler -> {
			long noOfFreeVids = Long.parseLong(handler.body().toString());
			long noOfVidsToGenerate = vidToGenerate - noOfFreeVids;
			LOGGER.info("Persisting {} vids in pool", noOfVidsToGenerate);
			long count = 0;
			while (count < vidToGenerate) {
				String vid = vidGenerator.generateId();
				VidEntity entity = new VidEntity();
				entity.setVid(vid);
				entity.setStatus(VidLifecycleStatus.AVAILABLE);
				metaDataUtil.setCreateMetaData(entity);
				boolean isPersisted = vidWriter.persistVids(entity);
				if (isPersisted) {
					count++;
				}
			}
			handler.reply("pool population successfull");

			LOGGER.info("No of vids persisted are {}", count);
		});
	}
}
