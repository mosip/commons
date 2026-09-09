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
public class ViberService {

    @Value("${viber.api.url}")
    private String viberApiUrl;

    @Value("${viber.api.token}")
    private String viberApiToken;

    @Value("${viber.sender.name}")
    private String senderName;

    public void sendViberMessage(String recipient, String message) {
        // Check if Viber API is properly configured
        if (viberApiToken.equals("YOUR_VIBER_API_TOKEN") || viberApiUrl.contains("<API_URL>")) {
            System.err.println("Viber API not properly configured. Please update application.properties with valid credentials.");
            return;
        }

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "App " + viberApiToken);
        headers.set("Content-Type", "application/json");
        headers.set("Accept", "application/json");

        // Create Viber API payload
        Map<String, Object> destination = new HashMap<>();
        destination.put("to", recipient);

        Map<String, Object> content = new HashMap<>();
        content.put("text", message);
        content.put("type", "TEXT");

        Map<String, Object> messageObj = new HashMap<>();
        messageObj.put("sender", senderName);
        messageObj.put("destinations", new Object[]{destination});
        messageObj.put("content", content);

        Map<String, Object> payload = new HashMap<>();
        payload.put("messages", new Object[]{messageObj});

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(viberApiUrl, request, String.class);
            System.out.println("Viber API Response: " + response.getBody());
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                System.err.println("Viber API Error: " + response.getStatusCode() + " - " + response.getBody());
                throw new RuntimeException("Failed to send Viber message: " + response.getBody());
            }
        } catch (org.springframework.web.client.RestClientException e) {
            System.err.println("Error while sending Viber message: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Unexpected error while sending Viber message: " + e.getMessage());
            throw new RuntimeException("Failed to send Viber message", e);
        }
    }
}
