package io.mosip.kernel.emailnotification.test.service;

import io.mosip.kernel.core.notification.spi.SMSServiceProvider;
import io.mosip.kernel.emailnotification.dto.ResponseDto;
import io.mosip.kernel.emailnotification.exception.InvalidArgumentsException;
import io.mosip.kernel.emailnotification.service.impl.EmailNotificationServiceImpl;
import io.mosip.kernel.emailnotification.test.NotificationTestBootApplication;
import io.mosip.kernel.emailnotification.util.EmailNotificationUtils;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;
import java.util.concurrent.Executor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = NotificationTestBootApplication.class)
public class MailNotifierServiceTest {
    @Autowired
    private EmailNotificationServiceImpl service;
    @Autowired
    private JavaMailSender emailSender;
    @MockBean
    private EmailNotificationUtils utils;
    @MockBean
    private SMSServiceProvider sMSServiceProvider;
    @MockBean(name = "mailExecutor")
    private Executor mailExecutor;
    @Captor
    private ArgumentCaptor<MimeMessage> messageCaptor;
    // Helper to sync async executor
    private void runExecutorSync() {
        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(mailExecutor).execute(any(Runnable.class));
    }
    @Test
    public void verifyAddAttachmentFunctionality() {
        runExecutorSync();
        String[] mailTo = { "test@gmail.com" };
        String[] mailCc = { "testTwo@gmail.com" };
        String mailSubject = "Test Subject";
        String mailContent = "Test Content";
        MultipartFile attachment = new MockMultipartFile("test.txt", "test.txt", "text/plain", new byte[10]);
        MultipartFile[] attachments = { attachment };
        doNothing().when(utils).addAttachments(eq(attachments), any());
        ResponseDto response = service.sendEmail(mailTo, mailCc, mailSubject, mailContent, attachments);
        verify(utils, times(1)).addAttachments(eq(attachments), any());
        assertEquals("success", response.getStatus());
        assertEquals("Email Request submitted", response.getMessage());
    }
    @Test
    public void verifySendMessageFunctionality() {
        runExecutorSync();
        String[] mailTo = { "test@gmail.com" };
        String[] mailCc = { "testTwo@gmail.com" };
        String mailSubject = "Test Subject";
        String mailContent = "Test Content";
        MultipartFile[] attachments = { new MockMultipartFile("file.txt", new byte[0]) };
        // Capture the message and stub sendMessage
        doNothing().when(utils).sendMessage(messageCaptor.capture(), eq(emailSender));
        doNothing().when(utils).addAttachments(any(), any());
        service.sendEmail(mailTo, mailCc, mailSubject, mailContent, attachments);
        verify(utils, times(1)).sendMessage(any(MimeMessage.class), eq(emailSender));
    }
    @Test
    public void verifySendMessageFunctionalityWithFromAddress() {
        runExecutorSync();
        String fromEmail = "from.test@mosip.io";
        // Use ReflectionTestUtils to set the @Value field directly
        ReflectionTestUtils.setField(service, "fromEmailAddress", fromEmail);
        String[] mailTo = { "test@mosip.io" };
        String[] mailCc = { "testTwo@mosip.io" };
        String mailSubject = "Test Subject";
        String mailContent = "Test Content";
        MultipartFile[] attachments = { new MockMultipartFile("file.txt", new byte[0]) };
        doNothing().when(utils).sendMessage(any(), any());
        doNothing().when(utils).addAttachments(any(), any());
        service.sendEmail(mailTo, mailCc, mailSubject, mailContent, attachments);
        verify(utils, times(1)).sendMessage(any(MimeMessage.class), eq(emailSender));
    }

    @Test
    public void verifySendMessageFunctionalityWithInvalidFromAddress() throws Exception {
        runExecutorSync(); // from previous fix
        String fromEmail = "from.test";
        // Use ReflectionTestUtils to set the @Value field directly
        ReflectionTestUtils.setField(service, "fromEmailAddress", fromEmail);
        String[] mailTo = { "test@mosip.io" };
        String mailSubject = "Test Subject";
        String mailContent = "Test Content";
        MultipartFile attachment = new MockMultipartFile("test.txt", "test.txt", "", new byte[10]);
        MultipartFile[] attachments = { attachment };
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(mailTo);
        helper.setSubject(mailSubject);
        helper.setText(mailContent);

        InvalidArgumentsException ex = assertThrows(
                InvalidArgumentsException.class,
                () -> service.sendEmail(mailTo, null, mailSubject, mailContent, attachments));
    }
    @Test
    public void verifyProxyModeSkipsSending() {
        // Force proxy mode
        String fromEmail = "test@mosip.io";
        // Use ReflectionTestUtils to set the @Value field directly
        ReflectionTestUtils.setField(service, "fromEmailAddress", fromEmail);
        ReflectionTestUtils.setField(service, "isProxytrue", true);
        String[] mailTo = { "test@mosip.io" };
        ResponseDto response = service.sendEmail(mailTo, null, "Sub", "Body", null);
        verify(utils, never()).sendMessage(any(), any());
        verify(utils, never()).addAttachments(any(), any());
        assertEquals("success", response.getStatus());
        assertEquals("Email Request submitted", response.getMessage());
    }
}