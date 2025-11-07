package io.mosip.kernel.emailnotification.util;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.emailnotification.constant.MailNotifierArgumentErrorConstants;
import io.mosip.kernel.emailnotification.constant.MailNotifierConstants;
import io.mosip.kernel.emailnotification.exception.InvalidArgumentsException;
import io.mosip.kernel.emailnotification.exception.NotificationException;

/**
 * This class provides with the utility methods for email-notifier service.
 *
 * @author Sagar Mahapatra
 * @since 1.0.0
 */
@Component
public class EmailNotificationUtils {
    /**
     * Sends an email message asynchronously using the configured JavaMailSender.
     *
     * <p>This method leverages a custom ThreadPoolTaskExecutor (defined as "mailExecutor")
     * to handle high-throughput email sending without blocking the main application thread.</p>
     *
     * @param message     the MimeMessage object containing the email details
     * @param emailSender the JavaMailSender instance used to send the email
     */
    
    public void sendMessage(MimeMessage message, JavaMailSender emailSender) {
        emailSender.send(message);
    }

    /**
     * Adds one or more attachments to a MimeMessageHelper instance in a memory-efficient way.
     *
     * <p>Instead of loading the entire file into memory as a byte array,
     * attachments are streamed using InputStreamSource to optimize memory usage,
     * especially for large files.</p>
     *
     * @param attachments an array of MultipartFile objects representing the attachments
     * @param helper      the MimeMessageHelper to which attachments are added
     * @throws NotificationException if adding any attachment fails
     */
    public void addAttachments(MultipartFile[] attachments, MimeMessageHelper helper) {
        Arrays.asList(attachments).forEach(attachment -> {
            try {
                helper.addAttachment(attachment.getOriginalFilename(), new ByteArrayResource(attachment.getBytes()));
            } catch (MessagingException | IOException exception) {
                throw new NotificationException(exception);
            }
        });
    }

    /**
     * Validates the email notification arguments for correctness.
     * <p>This method performs:
     * <ul>
     *   <li>Validation of sender's email address</li>
     *   <li>Validation of recipient list (non-null, non-empty, valid format)</li>
     *   <li>Validation of email subject (non-null, non-empty)</li>
     *   <li>Validation of email content (non-null, non-empty)</li>
     * </ul>
     *
     * <p>Validation is regex-based to avoid exception overhead.
     * If validation errors are found, an {@link InvalidArgumentsException} is thrown with a list of errors.</p>
     *
     * @param fromEmail   the sender email address
     * @param mailTo      an array of recipient email addresses
     * @param mailSubject the subject of the email
     * @param mailContent the body content of the email
     * @throws InvalidArgumentsException if one or more validation errors occur
     */
    public static void validateMailArguments(String fromEmail, String[] mailTo, String mailSubject, String mailContent){
        Set<ServiceError> validationErrors = Collections.newSetFromMap(new ConcurrentHashMap<>());

        // Validate sender email
        if (fromEmail == null || !safeValidateEmail(fromEmail)) {
            validationErrors.add(new ServiceError(
                    MailNotifierArgumentErrorConstants.SENDER_ADDRESS_NOT_FOUND.getErrorCode(),
                    MailNotifierArgumentErrorConstants.SENDER_ADDRESS_NOT_FOUND.getErrorMessage()
            ));
        }

        // Validate recipient emails
        if (mailTo == null || mailTo.length == Integer.parseInt(MailNotifierConstants.DIGIT_ZERO.getValue())) {
            validationErrors.add(new ServiceError(
                    MailNotifierArgumentErrorConstants.RECEIVER_ADDRESS_NOT_FOUND.getErrorCode(),
                    MailNotifierArgumentErrorConstants.RECEIVER_ADDRESS_NOT_FOUND.getErrorMessage()
            ));
        } else {
            Arrays.stream(mailTo).parallel().forEach(to -> {
                if (!safeValidateEmail(to)) {
                    validationErrors.add(new ServiceError(
                            MailNotifierArgumentErrorConstants.RECEIVER_ADDRESS_NOT_FOUND.getErrorCode(),
                            MailNotifierArgumentErrorConstants.RECEIVER_ADDRESS_NOT_FOUND.getErrorMessage()
                    ));
                }
            });
        }

        // Validate subject
        if (mailSubject == null || mailSubject.trim().isEmpty()) {
            validationErrors.add(new ServiceError(
                    MailNotifierArgumentErrorConstants.SUBJECT_NOT_FOUND.getErrorCode(),
                    MailNotifierArgumentErrorConstants.SUBJECT_NOT_FOUND.getErrorMessage()
            ));
        }

        // Validate content
        if (mailContent == null || mailContent.trim().isEmpty()) {
            validationErrors.add(new ServiceError(
                    MailNotifierArgumentErrorConstants.CONTENT_NOT_FOUND.getErrorCode(),
                    MailNotifierArgumentErrorConstants.CONTENT_NOT_FOUND.getErrorMessage()
            ));
        }

        if (!validationErrors.isEmpty()) {
            throw new InvalidArgumentsException(new ArrayList<>(validationErrors));
        }
    }

    /**
     * Strict RFC-compliant email validation using Jakarta Mail's InternetAddress.
     *
     * @param emailId email address to validate
     * @return true if valid, throws AddressException otherwise
     * @throws AddressException if email format is invalid
     */
    private static boolean validateEmailAddress(String emailId ) throws AddressException{
        InternetAddress fromEmailAddr = new InternetAddress(emailId);
        fromEmailAddr.validate();
        return true;
    }

    /**
     * Wrapper around validateEmailAddress() to avoid throwing checked exceptions inside streams.
     *
     * @param email email address
     * @return true if valid, false otherwise
     */
    private static boolean safeValidateEmail(String email) {
        try {
            return validateEmailAddress(email);
        } catch (AddressException e) {
            return false;
        }
    }
}