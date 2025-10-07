package io.mosip.kernel.emailnotification.model;

import java.time.LocalDateTime;

public class BulkMessage {
    private String phoneNumber; // For WhatsApp and Viber
    private String telegramUserId; // For Telegram
    private String message;
    private LocalDateTime scheduledTime;
    private String platforms; // comma-separated: "telegram,whatsapp,viber"

    public BulkMessage() {}

    public BulkMessage(String phoneNumber, String telegramUserId, String message, LocalDateTime scheduledTime, String platforms) {
        this.phoneNumber = phoneNumber;
        this.telegramUserId = telegramUserId;
        this.message = message;
        this.scheduledTime = scheduledTime;
        this.platforms = platforms;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getTelegramUserId() {
        return telegramUserId;
    }

    public void setTelegramUserId(String telegramUserId) {
        this.telegramUserId = telegramUserId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public String getPlatforms() {
        return platforms;
    }

    public void setPlatforms(String platforms) {
        this.platforms = platforms;
    }
}
