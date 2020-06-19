package io.mosip.kernel.packetmanager.config;

import io.mosip.kernel.packetmanager.impl.PacketDecryptorImpl;
import io.mosip.kernel.packetmanager.impl.PacketReaderServiceImpl;
import io.mosip.kernel.packetmanager.spi.PacketDecryptor;
import io.mosip.kernel.packetmanager.spi.PacketReaderService;
import io.mosip.kernel.packetmanager.util.IdSchemaUtils;
import io.mosip.kernel.packetmanager.util.RestUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class PacketUtilityConfig {

	@Bean
	@Primary
	public PacketDecryptor getPacketDecryptor() {
		return new PacketDecryptorImpl();
	}

	@Bean
	public IdSchemaUtils getIdSchemaUtils() {
		return new IdSchemaUtils();
	}

	@Bean
	public PacketReaderService getPacketReaderService() {
		return new PacketReaderServiceImpl();
	}

	@Bean
	public RestUtil getRestUtil() {
		return new RestUtil();
	}
}
