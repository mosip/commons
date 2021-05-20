package io.mosip.kernel.migrate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.migrate.impl.BaseKeysMigrator;
import io.mosip.kernel.keymanagerservice.logger.KeymanagerLogger;

@SpringBootApplication(scanBasePackages = {"io.mosip.kernel.keygenerator.*", "io.mosip.kernel.keymanagerservice.*", 
										   "io.mosip.kernel.keymanager.*", "io.mosip.kernel.crypto.*", "io.mosip.kernel.cryptomanager.*",
										   " io.mosip.kernel.migrate.*" })
										   
public class MigrateBaseKeysApplication implements CommandLineRunner {

	private static final Logger LOGGER = KeymanagerLogger.getLogger(MigrateBaseKeysApplication.class);

	@Autowired
	BaseKeysMigrator keysMigrator;

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}


	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext run = SpringApplication.run(MigrateBaseKeysApplication.class, args);
		SpringApplication.exit(run);
	}

	@Override
	public void run(String... args) throws Exception {	
		
		LOGGER.info("Keys Migration started......" );
		keysMigrator.migrateKeys();
		LOGGER.info("Keys Migration Completed......" );
	}
}
