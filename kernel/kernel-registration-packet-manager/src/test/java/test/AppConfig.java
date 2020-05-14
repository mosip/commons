package test;

import io.mosip.kernel.cbeffutil.impl.CbeffImpl;
import io.mosip.kernel.crypto.jce.core.CryptoCore;
import io.mosip.kernel.keygenerator.bouncycastle.KeyGenerator;
import io.mosip.kernel.packetmanager.impl.CbeffBIRBuilder;
import io.mosip.kernel.packetmanager.impl.PacketCreatorImpl;
import io.mosip.kernel.packetmanager.util.PacketCryptoHelper;
import io.mosip.kernel.packetmanager.util.PacketManagerHelper;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@Configuration
public class AppConfig {
	
	
	@Bean
    public static PropertySourcesPlaceholderConfigurer propertiesResolver() {
		PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer(); 
		Resource[] resources = new ClassPathResource[] {new ClassPathResource("application.properties")};
		pspc.setLocation(resources[0]);		
        return pspc;
	}		

	@Bean
	public PacketManagerHelper getPacketManagerHelper() {
		PacketManagerHelper packetManagerHelper = new PacketManagerHelper();
		return packetManagerHelper;
	}	
		
	@Bean
	public KeyGenerator getKeyGenerator() {
		return new KeyGenerator();
	}
	
	@Bean
	public CbeffBIRBuilder getCbeffBIRBuilder() {
		return new CbeffBIRBuilder();
	}
	
	@Bean
	public CryptoCore getCryptoCore() {
		return new CryptoCore();
	}
	
	@Bean
	public PacketCreatorImpl getPacketCreatorImpl() {
		PacketCreatorImpl packetCreatorImpl = new PacketCreatorImpl();
		return packetCreatorImpl;
	}
	
	@Bean
	public PacketCryptoHelper getPacketCryptoHelper() {
		return new PacketCryptoHelper();
	}
	
	@MockBean
	private CbeffImpl cbeffImpl;
}
