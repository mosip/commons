package io.mosip.kernel.keygenerator;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import io.mosip.kernel.keygenerator.generator.KeysGenerator;

@SpringBootApplication(scanBasePackages = {"io.mosip.kernel.keygenerator.*", "io.mosip.kernel.keymanagerservice.*", 
										   "io.mosip.kernel.keymanager.*", "io.mosip.kernel.crypto.*", "io.mosip.kernel.cryptomanager.*" })
public class KeysGeneratorApplication implements CommandLineRunner {

	private static final Logger LOGGER = Logger.getLogger(KeysGeneratorApplication.class.getName());
	
	@Autowired
	KeysGenerator keysGenerator;

	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext run = SpringApplication.run(KeysGeneratorApplication.class, args);
		SpringApplication.exit(run);
	}

	@Override
	public void run(String... args) throws Exception {	
		
		LOGGER.info("Keys generation stated......" );
		keysGenerator.generateKeys();
		LOGGER.info("Keys generated." );
	}
}
