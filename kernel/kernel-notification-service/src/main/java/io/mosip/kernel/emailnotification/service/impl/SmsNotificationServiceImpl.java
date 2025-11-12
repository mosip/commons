package io.mosip.kernel.emailnotification.service.impl;

import io.mosip.kernel.emailnotification.util.EmailNotificationUtils;
import io.mosip.kernel.emailnotification.util.SmsNotificationUtils;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.notification.model.SMSResponseDto;
import io.mosip.kernel.core.notification.spi.SMSServiceProvider;
import io.mosip.kernel.emailnotification.service.SmsNotification;

import java.util.concurrent.Executor;

/**
 * <h1>SMS Notification Service Implementation</h1>
 *
 * <p>
 * This service class is optimized to handle high-volume SMS delivery requests
 * with minimal latency and resource usage. It supports both real and proxy
 * modes to allow testing without an external SMS gateway.
 * </p>
 *
 * @author Ritesh Sinha
 * @since 1.0.0
 *
 */
@RefreshScope
@Service
public class SmsNotificationServiceImpl implements SmsNotification {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmsNotificationServiceImpl.class);

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Value("${mosip.kernel.sms.proxy-sms:false}")
    private boolean isProxytrue;

    @Value("${mosip.kernel.sms.success-message:SMS request sent}")
    private String sucessMessage;
    
    @Autowired
    private SmsNotificationUtils smsNotificationUtils;
    
    /**
     * Pre-built static success response for proxy/local mode to reduce allocations.
     */
    private SMSResponseDto cachedSuccessResponse;

    @PostConstruct
    private void initSuccessResponse() {
        SMSResponseDto response = new SMSResponseDto();
        response.setMessage(sucessMessage);
        response.setStatus("success");
        cachedSuccessResponse = response;
    }
    
    /**
     * Sends an SMS notification to the specified contact number.
     * In local or proxy mode, a simulated success response is returned without
     * invoking the external SMS service provider.
     *
     * @param contactNumber  The target phone number (must be non-null, non-empty).
     * @param contentMessage The SMS content to send (must be non-null, non-empty).
     * @return A {@link SMSResponseDto} indicating success or failure.
     */
    @Override
    public SMSResponseDto sendSmsNotification(String contactNumber, String contentMessage) {

        // Check for proxy or local mode
        boolean isLocalProfile = "local".equalsIgnoreCase(activeProfile);
        if (!isLocalProfile && !isProxytrue) {
            send(contactNumber, contentMessage);
        }

        return cachedSuccessResponse;
    }
    
    public void send(String contactNumber, String contentMessage) {
        smsNotificationUtils.sendSms(contactNumber, contentMessage);
    }
}