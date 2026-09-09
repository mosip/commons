package io.mosip.kernel.emailnotification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class TelegramConfig {

    private final String botToken = "YOUR_TELEGRAM_BOT_TOKEN";
    private final String chatId = "YOUR_CHAT_ID";

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public String getBotToken() {
        return botToken;
    }

    public String getChatId() {
        return chatId;
    }
}