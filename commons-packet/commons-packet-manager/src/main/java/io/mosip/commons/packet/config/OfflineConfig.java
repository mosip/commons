package io.mosip.commons.packet.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ConditionalOnProperty(value = "objectstore.crypto.name", havingValue = "OfflinePacketCryptoServiceImpl")
@ComponentScan(basePackages = {"io.mosip.kernel.cryptomanager.*", "io.mosip.kernel.signature.*",
        "io.mosip.kernel.keymanagerservice.*", "io.mosip.kernel.keymanager.*", "io.mosip.kernel.core.*",
        "io.mosip.kernel.keygenerator.*", "io.mosip.kernel.crypto.*", "io.mosip.kernel.clientcrypto.*"})
@EnableJpaRepositories(basePackages = {"io.mosip.kernel.keymanagerservice.repository.*"})
public class OfflineConfig {

}
