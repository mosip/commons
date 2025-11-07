package io.mosip.kernel.emailnotification.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.mosip.kernel.core.notification.spi.EmailNotification;
import io.mosip.kernel.emailnotification.constant.MailNotifierConstants;
import io.mosip.kernel.emailnotification.dto.ResponseDto;
import io.mosip.kernel.emailnotification.exception.NotificationException;
import io.mosip.kernel.emailnotification.util.EmailNotificationUtils;

import java.util.concurrent.Executor;

/**
 * Service implementation class for {@link EmailNotification}.
 *
 * @author Sagar Mahapatra
 * @since 1.0.0
 */
@Service
public class EmailNotificationServiceImpl implements EmailNotification<MultipartFile[], ResponseDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailNotificationServiceImpl.class);
    /**
     * Autowired reference for {@link JavaMailSender}
     */
    @Autowired
    private JavaMailSender emailSender;

    /**
     * Autowired reference for {@link EmailNotificationUtils}
     */
    @Autowired
    private EmailNotificationUtils emailNotificationUtils;

    /**
     * Optionally an email address can be configured.
     */
    @Nullable
    @Value("${mosip.kernel.notification.email.from:#{null}}")
    private String fromEmailAddress;

    @Value("${mosip.kernel.mail.proxy-mail:false}")
    private boolean isProxytrue;

    @Value("${mosip.kernel.mail.content.html.enable:true}")
    private boolean isHtmlEnable;

    @Autowired
    @Qualifier("mailExecutor")
    private Executor mailExecutor;

    /**
     * Sends an email with the specified parameters. In proxy mode, skips actual sending.
     *
     * @param mailTo      recipient email addresses
     * @param mailCc      CC email addresses (optional)
     * @param mailSubject subject line of the email
     * @param mailContent body content of the email
     * @param attachments optional attachments
     * @return a {@link ResponseDto} indicating send status
     */
    @Override
    public ResponseDto sendEmail(String[] mailTo, String[] mailCc, String mailSubject, String mailContent,
                                 MultipartFile[] attachments) {
        ResponseDto dto = new ResponseDto();
        LOGGER.debug("To Request : " + String.join(",", mailTo));

        if (!isProxytrue) {
            mailExecutor.execute(() -> {
                try {
                    send(mailTo, mailCc, mailSubject, mailContent, attachments);
                } catch (Exception e) {
                    LOGGER.error("Error sending email async: {}", e.getMessage(), e);
                }
            });
        }

        dto.setStatus(MailNotifierConstants.MESSAGE_SUCCESS_STATUS.getValue());
        dto.setMessage(MailNotifierConstants.MESSAGE_REQUEST_SENT.getValue());
        return dto;
    }

    /**
     * Asynchronously builds and sends the email message.
     *
     * @param mailTo      recipient addresses
     * @param mailCc      CC addresses
     * @param mailSubject subject
     * @param mailContent content
     * @param attachments files to attach
     */
    public void send(String[] mailTo, String[] mailCc, String mailSubject, String mailContent,
                     MultipartFile[] attachments) {
        EmailNotificationUtils.validateMailArguments(fromEmailAddress, mailTo, mailSubject, mailContent);
        /**
         * Creates the message.
         */
        MimeMessage message = emailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            /**
             * Sets to, subject, content.
             */
            helper.setTo(mailTo);

            if (null != fromEmailAddress) {
                helper.setFrom(fromEmailAddress);
            }

            if (mailCc != null) {
                helper.setCc(mailCc);
            }

            if (mailSubject != null) {
                helper.setSubject(mailSubject);
            }

            helper.setText(mailContent, isHtmlEnable);

            if (attachments != null) {
                /**
                 * Adds attachments.
                 */
                emailNotificationUtils.addAttachments(attachments, helper);
            }
            /**
             * Sends the mail.
             */
            emailNotificationUtils.sendMessage(message, emailSender);
        } catch (MessagingException exception) {
            throw new NotificationException(exception);
        }
    }
}