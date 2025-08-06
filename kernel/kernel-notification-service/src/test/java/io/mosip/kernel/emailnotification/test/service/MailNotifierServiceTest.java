package io.mosip.kernel.emailnotification.test.service;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import jakarta.mail.internet.MimeMessage;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import io.mosip.kernel.core.notification.spi.SMSServiceProvider;
import io.mosip.kernel.emailnotification.exception.InvalidArgumentsException;
import io.mosip.kernel.emailnotification.service.impl.EmailNotificationServiceImpl;
import io.mosip.kernel.emailnotification.test.NotificationTestBootApplication;
import io.mosip.kernel.emailnotification.util.EmailNotificationUtils;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NotificationTestBootApplication.class)
public class MailNotifierServiceTest {

	@Autowired
	private JavaMailSender emailSender;

	@MockBean
	EmailNotificationUtils utils;

	@Autowired
	EmailNotificationServiceImpl service;
	
	@MockBean
	private SMSServiceProvider sMSServiceProvider;

	@Test
	public void verifyAddAttachmentFunctionality() throws Exception {
		String[] mailTo = { "test@gmail.com" };
		String[] mailCc = { "testTwo@gmail.com" };
		String mailSubject = "Test Subject";
		String mailContent = "Test Content";
		MultipartFile attachment = new MockMultipartFile("test.txt", "test.txt", "", new byte[10]);
		MultipartFile[] attachments = { attachment };
		MimeMessage message = emailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		helper.setTo(mailTo);
		helper.setCc(mailCc);
		helper.setSubject(mailSubject);
		helper.setText(mailContent);
		doNothing().when(utils).addAttachments(Mockito.any(), Mockito.any());
		service.sendEmail(mailTo, mailCc, mailSubject, mailContent, attachments);
		verify(utils, times(1)).addAttachments(Mockito.any(), Mockito.any());
	}

	@Test
	public void verifySendMessageFunctionality() throws Exception {
		String fromEmail = "from.test@mosip.io";
		String[] mailTo = { "test@gmail.com" };
		String[] mailCc = { "testTwo@gmail.com" };
		String mailSubject = "Test Subject";
		String mailContent = "Test Content";
		MultipartFile attachment = new MockMultipartFile("test.txt", "test.txt", "", new byte[10]);
		MultipartFile[] attachments = { attachment };
		MimeMessage message = emailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		//helper.setFrom(fromEmail);
		helper.setTo(mailTo);
		helper.setCc(mailCc);
		helper.setSubject(mailSubject);
		helper.setText(mailContent);
		ReflectionTestUtils.setField(service, "fromEmailAddress", fromEmail);
		doNothing().when(utils).sendMessage(Mockito.any(), Mockito.any());
		service.sendEmail(mailTo, mailCc, mailSubject, mailContent, attachments);
		verify(utils, times(1)).sendMessage(Mockito.any(), Mockito.any());
	}

	@Test
	public void verifySendMessageFunctionalityWithFromAddress() throws Exception {
		String fromEmail = "from.test@mosip.io";
		String[] mailTo = { "test@mosip.io" };
		String[] mailCc = { "testTwo@mosip.io" };
		String mailSubject = "Test Subject";
		String mailContent = "Test Content";
		MultipartFile attachment = new MockMultipartFile("test.txt", "test.txt", "", new byte[10]);
		MultipartFile[] attachments = { attachment };
		MimeMessage message = emailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		//helper.setFrom(fromEmail);
		helper.setTo(mailTo);
		helper.setCc(mailCc);
		helper.setSubject(mailSubject);
		helper.setText(mailContent);
		ReflectionTestUtils.setField(service, "fromEmailAddress", fromEmail);
		doNothing().when(utils).sendMessage(Mockito.any(), Mockito.any());
		service.sendEmail(mailTo, mailCc, mailSubject, mailContent, attachments);
		verify(utils, times(1)).sendMessage(Mockito.any(), Mockito.any());
	}

	@Test(expected=InvalidArgumentsException.class)
	public void verifySendMessageFunctionalityWithInvalidFromAddress() throws Exception {
		String fromEmail = "invalid.email";
		String[] mailTo = { "test@mosip.io" };
		String[] mailCc = { "testTwo@mosip.io" };
		String mailSubject = "Test Subject";
		String mailContent = "Test Content";
		MultipartFile attachment = new MockMultipartFile("test.txt", "test.txt", "", new byte[10]);
		MultipartFile[] attachments = { attachment };
		MimeMessage message = emailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		helper.setFrom(fromEmail);
		helper.setTo(mailTo);
		helper.setCc(mailCc);
		helper.setSubject(mailSubject);
		helper.setText(mailContent);
		ReflectionTestUtils.setField(service, "fromEmailAddress", fromEmail);
		//System.setProperty("mosip.kernel.notification.email.from", fromEmail);
		doNothing().when(utils).sendMessage(Mockito.any(), Mockito.any());
		service.sendEmail(mailTo, mailCc, mailSubject, mailContent, attachments);
		verify(utils, times(1)).sendMessage(Mockito.any(), Mockito.any());
	}
}
