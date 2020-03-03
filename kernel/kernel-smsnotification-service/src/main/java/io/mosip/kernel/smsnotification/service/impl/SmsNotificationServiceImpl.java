package io.mosip.kernel.smsnotification.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.notification.model.SMSResponseDto;
import io.mosip.kernel.core.notification.spi.SMSServiceProvider;
import io.mosip.kernel.smsnotification.service.SmsNotification;

/**
 * This service class send SMS on the contact number provided.
 * 
 * @author Ritesh Sinha
 * @since 1.0.0
 *
 */
@RefreshScope
@Service
public class SmsNotificationServiceImpl implements SmsNotification {

	@Autowired
	private SMSServiceProvider smsServiceProvider;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.notification.spi.SmsNotification#sendSmsNotification(
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public SMSResponseDto sendSmsNotification(String contactNumber, String contentMessage) {
		return smsServiceProvider.sendSms(contactNumber, contentMessage);
	}
}