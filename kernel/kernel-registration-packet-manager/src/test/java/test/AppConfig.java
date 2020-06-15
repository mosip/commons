package test;

import io.mosip.kernel.cbeffutil.impl.CbeffImpl;
import io.mosip.kernel.core.fsadapter.spi.FileSystemAdapter;
import io.mosip.kernel.crypto.jce.core.CryptoCore;
import io.mosip.kernel.keygenerator.bouncycastle.KeyGenerator;
import io.mosip.kernel.packetmanager.impl.CbeffBIRBuilder;
import io.mosip.kernel.packetmanager.impl.PacketCreatorImpl;
import io.mosip.kernel.packetmanager.spi.PacketDecryptor;
import io.mosip.kernel.packetmanager.util.IdSchemaUtils;
import io.mosip.kernel.packetmanager.util.PacketCryptoHelper;
import io.mosip.kernel.packetmanager.util.PacketManagerHelper;
import io.mosip.kernel.packetmanager.util.RestUtil;
import io.mosip.kernel.packetmanager.util.ZipUtils;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

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
	
	@Bean
	public RestTemplate getRestTemplate() {
       return new RestTemplate();
    }

	@MockBean
	private IdSchemaUtils idSchemaUtils;

	@MockBean
	private ZipUtils zipUtils;
	
	@MockBean
	private RestUtil restUtil;

	@MockBean
	private ObjectMapper mapper;
	
	@MockBean
	private CbeffImpl cbeffImpl;
	
	@MockBean
	private PacketDecryptor decryptor;

	@MockBean
	private FileSystemAdapter fileSystemAdapter;


}
