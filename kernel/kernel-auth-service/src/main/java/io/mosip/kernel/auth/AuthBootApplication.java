package io.mosip.kernel.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point for the MOSIP auth manager HTTP service.
 * <p>
 * The runnable artifact is authmanager, typically listening on port {@code 8091}
 * with servlet path {@code /v1/authmanager}. Component scan includes REST
 * controllers, servlet configuration, the configured auth-adapter and IAM
 * implementation packages, and kernel-core logger configuration. Identity is
 * backed by Keycloak. kernel-core auto-config (applicant type, id generators,
 * websub, …) is excluded; those beans belong to other MOSIP services.
 */
@SpringBootApplication(scanBasePackages = {"io.mosip.kernel.auth.controller","io.mosip.kernel.auth.config","${mosip.auth.adapter.impl.basepackage}","${mosip.iam.impl.basepackage}", "io.mosip.kernel.core.logger.config"})
@EnableAutoConfiguration(excludeName = {
		"io.mosip.kernel.idgenerator.vid.impl.VidGeneratorImpl",
		"io.mosip.kernel.idgenerator.vid.util.VidFilterUtils",
		"io.mosip.kernel.idgenerator.tokenid.impl.TokenIdGeneratorImpl",
		"io.mosip.kernel.idgenerator.machineid.impl.MachineIdGeneratorImpl",
		"io.mosip.kernel.idgenerator.regcenterid.impl.RegistrationCenterIdGeneratorImpl",
		"io.mosip.kernel.idgenerator.mispid.impl.MispIdGeneratorImpl",
		"io.mosip.kernel.licensekeygenerator.misp.impl.MISPLicenseKeyGeneratorImpl",
		"io.mosip.kernel.licensekeygenerator.misp.util.MISPLicenseKeyGeneratorUtil",
		"io.mosip.kernel.idgenerator.rid.impl.RidGeneratorImpl",
		"io.mosip.kernel.idvalidator.prid.impl.PridValidatorImpl",
		"io.mosip.kernel.idvalidator.rid.impl.RidValidatorImpl",
		"io.mosip.kernel.idvalidator.uin.impl.UinValidatorImpl",
		"io.mosip.kernel.idvalidator.vid.impl.VidValidatorImpl",
		"io.mosip.kernel.idvalidator.mispid.impl.MispIdValidatorImpl",
		"io.mosip.kernel.templatemanager.velocity.builder.TemplateManagerBuilderImpl",
		"io.mosip.kernel.pdfgenerator.impl.PDFGeneratorImpl",
		"io.mosip.kernel.qrcode.generator.zxing.QrcodeGeneratorImpl",
		"io.mosip.kernel.transliteration.icu4j.impl.TransliterationImpl",
		"io.mosip.kernel.applicanttype.api.impl.ApplicantTypeImpl",
		"io.mosip.kernel.idobjectvalidator.config.IdObjectValidatorConfig",
		"io.mosip.kernel.websub.api.config.IntentVerificationConfig",
		"io.mosip.kernel.websub.api.config.WebSubClientConfig",
		"io.mosip.kernel.websub.api.config.publisher.WebSubPublisherClientConfig",
		"io.mosip.kernel.websub.api.config.publisher.RestTemplateHelper"
})
public class AuthBootApplication {

	/**
	 * Starts the Spring Boot application context for authmanager.
	 *
	 * @param args command-line arguments passed to {@link SpringApplication}
	 */
	public static void main(String[] args) {
		SpringApplication.run(AuthBootApplication.class, args);

	}
}
