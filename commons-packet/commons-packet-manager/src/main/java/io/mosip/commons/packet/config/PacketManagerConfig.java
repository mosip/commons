package io.mosip.commons.packet.config;

import io.mosip.commons.packet.constants.LoggerFileConstant;
import io.mosip.commons.packet.dto.Document;
import io.mosip.commons.packet.dto.ProviderInfo;
import io.mosip.commons.packet.spi.IPacketReader;
import io.mosip.commons.packet.spi.PacketSigner;
import io.mosip.commons.packet.util.PacketManagerLogger;
import io.mosip.kernel.auth.adapter.config.RestTemplateInterceptor;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.core.logger.spi.Logger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Collections;
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
    public void validateReferenceProvider() throws ClassNotFoundException {
        if (StringUtils.isNotBlank(env.getProperty("mosip.commons.provider.referenceprovider"))) {
            logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), null,
                    "Validating the reference provider is present or not.");
            Class.forName(env.getProperty("mosip.commons.provider.referenceprovider"));
        }
        logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), null,
                "Reference provider class is not provided.");
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
    public IPacketReader referenceProvider()
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (StringUtils.isNotBlank(env.getProperty("mosip.commons.provider.referenceprovider"))) {
            logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), null,
                    "Creating reference provider instance.");
            return (IPacketReader) Class.forName(env.getProperty("mosip.commons.provider.referenceprovider")).newInstance();
        } else {
            logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), null,
                    "reference provider is not present.");
            return new IPacketReader() {

                @Override
                public ProviderInfo init(String schemaUrl, byte[] publicKey, PacketSigner signer) {
                    return null;
                }

                @Override
                public boolean validatePacket(String id, String process) {
                    return false;
                }

                @Override
                public Map<String, Object> getAll(String id, String process) {
                    return null;
                }

                @Override
                public String getField(String id, String field, String process) {
                    return null;
                }

                @Override
                public Map<String, String> getFields(String id, List<String> fields, String process) {
                    return null;
                }

                @Override
                public Document getDocument(String id, String documentName, String process) {
                    return null;
                }

                @Override
                public BiometricRecord getBiometric(String id, String person, List<BiometricType> modalities, String process) {
                    return null;
                }

                @Override
                public Map<String, String> getMetaInfo(String id, String source, String process) {
                    return null;
                }
            };
        }
    }
}
