package io.mosip.kernel.emailnotification.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.notification.model.SMSResponseDto;
import io.mosip.kernel.core.notification.spi.SMSServiceProvider;
import io.mosip.kernel.emailnotification.service.SmsNotification;
import io.mosip.kernel.smsserviceprovider.msg91.constant.SmsPropertyConstant;

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

	@Value("${spring.profiles.active}")
	String activeProfile;

	@Autowired
	private SMSServiceProvider smsServiceProvider;
	
	@Value("${mosip.kernel.sms.proxy-sms:false}")
	private boolean isProxytrue;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.notification.spi.SmsNotification#sendSmsNotification(
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public SMSResponseDto sendSmsNotification(String contactNumber, String contentMessage) {
		if (activeProfile.equalsIgnoreCase("local") || isProxytrue) {
			SMSResponseDto smsResponseDTO = new SMSResponseDto();
			smsResponseDTO.setMessage(SmsPropertyConstant.SUCCESS_RESPONSE.getProperty());
			smsResponseDTO.setStatus("success");
			return smsResponseDTO;
		}
		return smsServiceProvider.sendSms(contactNumber, contentMessage);
	}
}