package io.mosip.kernel.emailnotification.util;

import io.mosip.kernel.core.notification.spi.SMSServiceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Utility class for sending SMS notifications via {@link SMSServiceProvider}.
 * <p>
 * This class provides helper methods to send SMS messages asynchronously, 
 * allowing non-blocking operations for high-throughput scenarios.
 * </p>
 *
 * <p>
 * The asynchronous execution is handled by a custom ThreadPoolTaskExecutor
 * named "smsExecutor".
 * </p>
 *
 * @author Kamesh
 * @version 1.3.0
 * @since 1.3.0
 */
@Component
public class SmsNotificationUtils {
    
    @Autowired
    private SMSServiceProvider smsServiceProvider;

    /**
     * Sends an SMS message asynchronously to the specified contact number.
     * <p>
     * This method leverages the {@code smsExecutor} ThreadPoolTaskExecutor 
     * to ensure that SMS sending does not block the main application thread.
     * </p>
     *
     * @param contactNumber  the recipient's contact number in international format
     * @param contentMessage the message content to be sent via SMS
     *
     * @throws IllegalArgumentException if {@code contactNumber} or {@code contentMessage} is null or empty
     * @see SMSServiceProvider#sendSms(String, String)
     */
    @Async("smsExecutor")
    public void sendSms(String contactNumber, String contentMessage) {
        smsServiceProvider.sendSms(contactNumber, contentMessage);
    }
}