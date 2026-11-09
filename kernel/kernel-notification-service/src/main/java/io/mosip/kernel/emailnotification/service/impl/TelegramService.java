package io.mosip.kernel.emailnotification.service.impl;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TelegramService {

    @Value("${telegram.bot.token}")
    private String botToken;

    public void sendTelegramMessage(String chatId, String message) {
        try {
            // URL encode the message to handle special characters
            String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
            String url = String.format("https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s", botToken, chatId, encodedMessage);
            
            URI uri = URI.create(url);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            System.out.println("Telegram API Response: " + response.body());
            
            // Check if the response indicates an error
            if (response.body().contains("\"ok\":false")) {
                System.err.println("Telegram API Error: " + response.body());
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid URL: " + e.getMessage());
        } catch (java.io.IOException | InterruptedException e) {
            System.err.println("Error sending Telegram message: " + e.getMessage());
        }
    }
}