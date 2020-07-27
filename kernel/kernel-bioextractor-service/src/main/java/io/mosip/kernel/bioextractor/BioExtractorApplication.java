package io.mosip.kernel.bioextractor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Spring-boot class for Biometric Extractor Application.
 *
 * @author Loganathan Sekar
 */
@SpringBootApplication()
/*@Import(value = { UinValidatorImpl.class, VidValidatorImpl.class, IDAMappingConfig.class, KeyManager.class,
		RestHelperImpl.class, RestRequestFactory.class, IdInfoFetcherImpl.class, OTPManager.class,
		MasterDataManager.class, MatchInputBuilder.class, IdRepoManager.class, NotificationManager.class,
		NotificationServiceImpl.class, IdTemplateManager.class, TemplateManagerBuilderImpl.class,
		IdAuthExceptionHandler.class, AuthFacadeImpl.class, OTPAuthServiceImpl.class, IdInfoHelper.class,
		CbeffImpl.class, IdServiceImpl.class, AuditRequestFactory.class, DemoAuthServiceImpl.class,
		BioAuthServiceImpl.class, TokenIdManager.class, SwaggerConfig.class, AuditHelper.class,
		PinAuthServiceImpl.class, IdAuthExceptionHandler.class, AuthRequestValidator.class, PinValidatorImpl.class, DemoNormalizerImpl.class,
		OTPServiceImpl.class, OTPRequestValidator.class, IdaTransactionInterceptor.class, IdAuthSecurityManager.class,
		AuthtypeStatusImpl.class, CryptoCore.class, PartnerServiceImpl.class, CryptomanagerServiceImpl.class,
		KeyGenerator.class, CryptomanagerUtils.class, KeymanagerServiceImpl.class, KeymanagerUtil.class,
	  TokenIDGeneratorServiceImpl.class,TokenIDGenerator.class,PartnerServiceManager.class })*/
@ComponentScan(basePackages={ "io.mosip.kernel.bioextractor" })
@EnableAsync
public class BioExtractorApplication {
	
	/**
	 * The main method.
	 *
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(BioExtractorApplication.class, args);
	}
	

}