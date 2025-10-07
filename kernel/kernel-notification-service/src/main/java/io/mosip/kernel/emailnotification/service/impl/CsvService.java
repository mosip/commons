package io.mosip.kernel.emailnotification.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.mosip.kernel.emailnotification.model.BulkMessage;
import io.mosip.kernel.emailnotification.model.BulkMessageRequest;

@Service
public class CsvService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public BulkMessageRequest parseCsv(MultipartFile file) throws IOException {
        List<BulkMessage> messages = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                // Skip header row
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                String[] columns = line.split(",");
                if (columns.length >= 5) {
                    try {
                        String phoneNumber = columns[0].trim();
                        String telegramUserId = columns[1].trim();
                        String message = columns[2].trim();
                        LocalDateTime scheduledTime = LocalDateTime.parse(columns[3].trim(), DATE_TIME_FORMATTER);
                        String platforms = columns[4].trim();
                        
                        BulkMessage bulkMessage = new BulkMessage(phoneNumber, telegramUserId, message, scheduledTime, platforms);
                        messages.add(bulkMessage);
                    } catch (Exception e) {
                        System.err.println("Error parsing CSV line: " + line + " - " + e.getMessage());
                    }
                }
            }
        }
        
        return new BulkMessageRequest(messages);
    }
}
