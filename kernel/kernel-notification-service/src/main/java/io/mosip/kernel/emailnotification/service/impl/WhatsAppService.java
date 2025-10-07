package io.mosip.kernel.emailnotification.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WhatsAppService {

    @Value("${green.api.instance.id}")
    private String instanceId;

    @Value("${green.api.token}")
    private String apiToken;

    public void sendWhatsAppMessage(String recipient, String message) {
        // Check if Green API is properly configured
        if (apiToken.equals("YOUR_GREEN_API_TOKEN") || instanceId.equals("YOUR_INSTANCE_ID")) {
            System.err.println("Green API not properly configured. Please update application.properties with valid credentials.");
            return;
        }

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        // Format phone number with @c.us suffix if not already present
        String chatId = recipient;
        if (!chatId.contains("@c.us")) {
            chatId = recipient + "@c.us";
        }

        // Create Green API payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("chatId", chatId);
        payload.put("message", message);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            String url = String.format("https://7105.api.greenapi.com/waInstance%s/sendMessage/%s", 
                instanceId, apiToken);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            System.out.println("Green API Response: " + response.getBody());
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                System.err.println("Green API Error: " + response.getStatusCode() + " - " + response.getBody());
                throw new RuntimeException("Failed to send WhatsApp message: " + response.getBody());
            }
        // } catch (org.springframework.web.client.RestClientException | org.springframework.web.client.HttpClientErrorException e) {
        //     System.err.println("Error while sending WhatsApp message: " + e.getMessage());
        //     throw e;
        } catch (Exception e) {
            System.err.println("Unexpected error while sending WhatsApp message: " + e.getMessage());
            throw new RuntimeException("Failed to send WhatsApp message", e);
        }
    }
}