package io.mosip.kernel.emailnotification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * <h1>Mail Notifier Application</h1>
 *
 * <p>This Spring Boot application is responsible for sending email notifications.
 * with an asynchronous task executor to handle high-volume email delivery efficiently.
 * It also exposes SMS send APIs under the same {@code /v1/notifier} servlet path.</p>
 *
 * <p>Features:
 * <ul>
 *   <li>Asynchronous email sending for improved performance</li>
 *   <li>Asynchronous SMS sending through a configurable provider SPI</li>
 *   <li>Thread pool configuration tuned for high throughput</li>
 *   <li>Excludes database auto-config to run as a lightweight microservice</li>
 * </ul>
 * </p>
 *
 * @author Sagar Mahapatra
 * @since 1.0.0
 *
 */
@SpringBootApplication(scanBasePackages = {
        "io.mosip.kernel.emailnotification.*",
        "${mosip.auth.adapter.impl.basepackage}",
        "io.mosip.kernel.core.logger.config",
        "io.mosip.kernel.smsserviceprovider.*"
})
@EnableAutoConfiguration(excludeName = {
				"org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration",
				"org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration",
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
@EnableAsync
public class NotificationBootApplication {

    /**
     * Main method to start the Mail Notifier Application (email and SMS).
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(NotificationBootApplication.class, args);
    }
}