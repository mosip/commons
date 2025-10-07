package io.mosip.kernel.emailnotification.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.PriorityBlockingQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.emailnotification.model.MessageRequest;

// import jakarta.annotation.PostConstruct;

@Service
public class MessageService {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${green.api.instance.id}")
    private String instanceId;

    @Value("${green.api.token}")
    private String apiToken;

    private final ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();

    private final PriorityBlockingQueue<MessageRequest> messageQueue = 
        new PriorityBlockingQueue<>(11, (MessageRequest a, MessageRequest b) -> a.getDeliveryTime().compareTo(b.getDeliveryTime()));

    @Autowired
    private TelegramService telegramService;

    @Autowired
    private WhatsAppService whatsAppService;

    @Autowired
    private ViberService viberService;

    @PostConstruct
    public void init() {
        taskScheduler.initialize();
        taskScheduler.scheduleAtFixedRate(this::processMessages, Duration.ofSeconds(1));
    }

    public void scheduleMessage(MessageRequest messageRequest) {
        System.out.println("📝 Scheduling message for: " + messageRequest.getPhoneNumber() + " (Telegram: " + messageRequest.getTelegramUserId() + ")");
        System.out.println("📅 Scheduled time: " + messageRequest.getDeliveryTime());
        System.out.println("⏰ Current time: " + LocalDateTime.now());
        System.out.println("⏳ Time until delivery: " + java.time.Duration.between(LocalDateTime.now(), messageRequest.getDeliveryTime()).toMinutes() + " minutes");
        messageQueue.add(messageRequest);
    }

    private void processMessages() {
        LocalDateTime now = LocalDateTime.now();
        if (!messageQueue.isEmpty()) {
            System.out.println("🔍 Checking " + messageQueue.size() + " scheduled messages at " + now);
        }
        while (!messageQueue.isEmpty()) {
            MessageRequest message = messageQueue.peek();
            System.out.println("🔍 Checking message scheduled for: " + message.getDeliveryTime() + " (current: " + now + ")");
            
            // Convert the scheduled time to local timezone for comparison
            LocalDateTime scheduledTime = message.getDeliveryTime();
            // System.out.println("⏰ Scheduled time: " + scheduledTime);
            // System.out.println("⏰ Current time: " + now);
            // System.out.println("⏰ Is scheduled time before now? " + scheduledTime.isBefore(now));
            // System.out.println("⏰ Is scheduled time equal to now? " + scheduledTime.isEqual(now));
            
            if (scheduledTime.isBefore(now) || scheduledTime.isEqual(now)) {
                message = messageQueue.poll();
                System.out.println("📅 Processing scheduled message for: " + message.getPhoneNumber() + " (Telegram: " + message.getTelegramUserId() + ") at " + now);
                sendMessageToAllPlatforms(message.getPhoneNumber(), message.getTelegramUserId(), message.getText());
            } else {
                // Message is scheduled for future, break the loop
                System.out.println("⏳ Message scheduled for future, waiting...");
                break;
            }
        }
    }

    public void sendMessage(MessageRequest message) {
        // Use the new sendMessageToAllPlatforms method
        sendMessageToAllPlatforms(message.getPhoneNumber(), message.getTelegramUserId(), message.getText());
    }

    public void sendWhatsAppMessage(String recipient, String message) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        // Format phone number with @c.us suffix if not already present
        String chatId = recipient;
        if (!chatId.contains("@c.us")) {
            chatId = recipient + "@c.us";
        }

        // Create Green API payload
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("chatId", chatId);
        payload.put("message", message);

        HttpEntity<java.util.Map<String, Object>> request = new HttpEntity<>(payload, headers);

        String url = String.format("https://%s.api.greenapi.com/waInstance%s/sendMessage/%s", 
            instanceId, instanceId, apiToken);
        
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to send WhatsApp message: " + response.getBody());
        }
    }

    public void sendTelegramMessage(String recipient, String message) {
        String url = String.format("https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s", botToken, recipient, message);
        try {
            java.net.URI uri = java.net.URI.create(url);
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder(uri).GET().build();
            client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid URL: " + e.getMessage());
        } catch (java.io.IOException | InterruptedException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }

    // public void sendWatiMessage(String recipient, String message) {
    //     RestTemplate restTemplate = new RestTemplate();

    //     HttpHeaders headers = new HttpHeaders();
    //     headers.set("Authorization", "Bearer " + watiApiToken);
    //     headers.set("Content-Type", "application/json");

    //     String payload = "{" +
    //             "\"phone\":\"" + recipient + "\"," +
    //             "\"message\":\"" + message + "\"}";

    //     HttpEntity<String> request = new HttpEntity<>(payload, headers);

    //     ResponseEntity<String> response = restTemplate.postForEntity(watiApiUrl, request, String.class);
    //     if (!response.getStatusCode().is2xxSuccessful()) {
    //         throw new RuntimeException("Failed to send WATI message: " + response.getBody());
    //     }
    // }

    public void sendMessageToAllPlatforms(String phoneNumber, String telegramUserId, String message) {
        boolean telegramSuccess = false;
        boolean whatsappSuccess = false;
        boolean viberSuccess = false;
        
        // Try to send to Telegram (use Telegram user ID)
        if (telegramUserId != null && !telegramUserId.trim().isEmpty()) {
            try {
                telegramService.sendTelegramMessage(telegramUserId, message);
                telegramSuccess = true;
                System.out.println("✅ Telegram message sent successfully");
            } catch (Exception e) {
                System.err.println("❌ Telegram Error: " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ Telegram user ID not provided, skipping Telegram");
        }

        // Try to send to WhatsApp (use phone number)
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            try {
                whatsAppService.sendWhatsAppMessage(phoneNumber, message);
                whatsappSuccess = true;
                System.out.println("✅ WhatsApp message sent successfully");
            } catch (Exception e) {
                System.err.println("❌ WhatsApp Error: " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ Phone number not provided, skipping WhatsApp");
        }

        // Try to send to Viber (use phone number)
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            try {
                viberService.sendViberMessage(phoneNumber, message);
                viberSuccess = true;
                System.out.println("✅ Viber message sent successfully");
            } catch (Exception e) {
                System.err.println("❌ Viber Error: " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ Phone number not provided, skipping Viber");
        }

        // Log overall status
        int successCount = (telegramSuccess ? 1 : 0) + (whatsappSuccess ? 1 : 0) + (viberSuccess ? 1 : 0);
        
        if (successCount == 3) {
            System.out.println("🎉 All messages sent successfully to all three platforms (Telegram, WhatsApp, Viber)");
        } else if (successCount > 0) {
            System.out.println("⚠️ Partial success: " + 
                (telegramSuccess ? "Telegram ✓" : "Telegram ✗") + ", " +
                (whatsappSuccess ? "WhatsApp ✓" : "WhatsApp ✗") + ", " +
                (viberSuccess ? "Viber ✓" : "Viber ✗") + " (" + successCount + "/3 platforms)");
        } else {
            System.err.println("💥 Failed to send messages to all platforms");
        }
    }
}