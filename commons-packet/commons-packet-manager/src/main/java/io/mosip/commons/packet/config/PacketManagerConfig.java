package io.mosip.commons.packet.config;

import io.mosip.commons.packet.constants.LoggerFileConstant;
import io.mosip.commons.packet.spi.IPacketReader;
import io.mosip.commons.packet.util.PacketManagerLogger;
import io.mosip.kernel.auth.adapter.config.RestTemplateInterceptor;
import io.mosip.kernel.core.logger.spi.Logger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableCaching
public class PacketManagerConfig {

    private static final Logger logger = PacketManagerLogger.getLogger(PacketManagerConfig.class);

    /** The env. */
    @Autowired
    private Environment env;

    @Bean
    @ConfigurationProperties(prefix = "mosip.commons.provider.referenceprovider")
    public Map<String, String> configurations() {
        return new HashMap<>();
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList(new RestTemplateInterceptor()));
        return restTemplate;
    }

    /**
     * Validate the reference provider.
     *
     * @throws ClassNotFoundException the class not found exception
     */
    @PostConstruct
    public void validateReferenceReaderProvider() throws ClassNotFoundException {
        if (StringUtils.isNotBlank(env.getProperty("mosip.commons.provider.referenceReaderProviders"))) {
            String[] ClassNameWithPackage = env.getProperty("mosip.commons.provider.referenceReaderProviders").split(",");
            for (String className : ClassNameWithPackage) {
                logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), null,
                        "Validating the reference provider readers are present or not.");
                Class.forName(className);
            }
        }
        logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), null,
                "Reference provider reader class is not provided.");
    }

    /**
     * Validate the reference provider.
     *
     * @throws ClassNotFoundException the class not found exception
     */
    @PostConstruct
    public void validateReferenceWriterProvider() throws ClassNotFoundException {
        if (StringUtils.isNotBlank(env.getProperty("mosip.commons.provider.referenceWriterProviders"))) {
            String[] ClassNameWithPackage = env.getProperty("mosip.commons.provider.referenceWriterProviders").split(",");
            for (String className : ClassNameWithPackage) {
                logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), null,
                        "Validating the reference provider writers are present or not.");
                Class.forName(className);
            }
        }
        logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), null,
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
    public List<IPacketReader> referenceReaderProviders()
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        List<IPacketReader> iPacketReaders = new ArrayList<>();
        if (StringUtils.isNotBlank(env.getProperty("mosip.commons.provider.referenceReaderProviders"))) {
            String[] ClassNameWithPackage = env.getProperty("mosip.commons.provider.referenceReaderProviders").split(",");
            for (String className : ClassNameWithPackage) {
                logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), null,
                        "Creating reference provider reader instances.");
                iPacketReaders.add((IPacketReader) Class.forName(className).newInstance());
            }
            return iPacketReaders;

        } else {
            logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), null,
                    "reference provider reader is not present.");
            return null;
        }
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
    public List<IPacketReader> referenceWriterProviders()
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        List<IPacketReader> iPacketReaders = new ArrayList<>();
        if (StringUtils.isNotBlank(env.getProperty("mosip.commons.provider.referenceWriterProviders"))) {
            String[] ClassNameWithPackage = env.getProperty("mosip.commons.provider.referenceWriterProviders").split(",");
            for (String className : ClassNameWithPackage) {
                logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), null,
                        "Creating reference provider writer instances.");
                iPacketReaders.add((IPacketReader) Class.forName(className).newInstance());
            }
            return iPacketReaders;

        } else {
            logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), null,
                    "reference provider writer is not present.");
            return null;
        }
    }
}
