package io.mosip.commons.packet.config;

import io.mosip.commons.packet.constants.LoggerFileConstant;
import io.mosip.commons.packet.spi.IPacketReader;
import io.mosip.commons.packet.spi.IPacketWriter;
import io.mosip.commons.packet.util.PacketHelper;
import io.mosip.commons.packet.util.PacketManagerLogger;
import io.mosip.kernel.core.logger.spi.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
@EnableCaching
@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = {
		"io.mosip.kernel.cbeffutil.impl.CbeffImpl"}), basePackages = {"io.mosip.commons.packet.*", "io.mosip.commons.khazana.*",
        "io.mosip.kernel.cbeffutil.*", "io.mosip.kernel.auth.*"})
@Import({OfflineConfig.class})
public class PacketManagerConfig {

    private static final Logger logger = PacketManagerLogger.getLogger(PacketManagerConfig.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    @ConfigurationProperties(prefix = "provider.packetreader")
    public Map<String, String> readerConfiguration() {
        return new HashMap<>();
    }

    @Bean
    @ConfigurationProperties(prefix = "provider.packetwriter")
    public Map<String, String> writerConfiguration() {
        return new HashMap<>();
    }

    /**
     * Validate the reference provider.
     *
     * @throws ClassNotFoundException the class not found exception
     */
    @PostConstruct
    public void validateReferenceReaderProvider() throws ClassNotFoundException {
            Set<String> readerProviders = PacketHelper.getReaderProvider(readerConfiguration());
            if (!CollectionUtils.isEmpty(readerProviders)) {
                for (String className : readerProviders) {
                    logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.ID.toString(), null,
                            "Validating the reference provider readers are present or not.");
                    getBean(className);
                }
            } else
            logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.ID.toString(), null,
                    "Reference provider reader class is not provided.");
    }

    /**
     * Validate the reference provider.
     *
     * @throws ClassNotFoundException the class not found exception
     */
    @PostConstruct
    public void validateReferenceWriterProvider() throws ClassNotFoundException {
        Set<String> writerProviders = PacketHelper.getWriterProvider(writerConfiguration());
        if (!CollectionUtils.isEmpty(writerProviders)) {
            for (String className : writerProviders) {
                logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.ID.toString(), null,
                        "Validating the reference provider writers are present or not.");
                getBean(className);
            }
        } else
            logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.ID.toString(), null,
                "Reference provider writer class is not provided.");
    }

    /**
     * Instantiate the reference provider bean
     *
     * @return the id object validator
     * @throws ClassNotFoundException the class not found exception
     * @throws InstantiationException the instantiation exception
     * @throws IllegalAccessException the illegal access exception
     */
    @Bean
    @Lazy
    public List<IPacketReader> referenceReaderProviders() throws ClassNotFoundException {
        List<IPacketReader> iPacketReaders = new ArrayList<>();
        Set<String> readerProviders = PacketHelper.getReaderProvider(readerConfiguration());
        if (!CollectionUtils.isEmpty(readerProviders)) {
            for (String className : readerProviders) {
                logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.ID.toString(), null,
                        "Validating the reference provider readers are present or not.");
                iPacketReaders.add((IPacketReader) getBean(className));
            }
        } else {
            logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.ID.toString(), null,
                    "reference provider reader is not present.");
        }
        return iPacketReaders;
    }

    /**
     * Instantiate the reference provider bean
     *
     * @return the id object validator
     * @throws ClassNotFoundException the class not found exception
     * @throws InstantiationException the instantiation exception
     * @throws IllegalAccessException the illegal access exception
     */
    @Bean
    @Lazy
    public List<IPacketWriter> referenceWriterProviders() throws ClassNotFoundException {
        List<IPacketWriter> iPacketWriters = new ArrayList<>();
        Set<String> writerProviders = PacketHelper.getWriterProvider(writerConfiguration());
        if (!CollectionUtils.isEmpty(writerProviders)) {
            for (String className : writerProviders) {
                logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.ID.toString(), null,
                        "Validating the reference provider writers are present or not.");
                iPacketWriters.add((IPacketWriter) getBean(className));
            }
        } else {
            logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.ID.toString(), null,
                    "reference provider writer is not present.");
        }
        return iPacketWriters;
    }

    private Object getBean(String className) throws ClassNotFoundException {
        Class<?> clazz = Class.forName(className);
        return applicationContext.getBean(clazz);
    }
}
