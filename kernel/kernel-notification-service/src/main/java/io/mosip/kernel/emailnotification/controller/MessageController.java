package io.mosip.kernel.emailnotification.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.mosip.kernel.emailnotification.model.BulkMessage;
import io.mosip.kernel.emailnotification.model.BulkMessageRequest;
import io.mosip.kernel.emailnotification.model.MessageRequest;
import io.mosip.kernel.emailnotification.model.MessageTemplate;
import io.mosip.kernel.emailnotification.model.TemplateRequest;
import io.mosip.kernel.emailnotification.service.impl.CsvService;
import io.mosip.kernel.emailnotification.service.impl.MessageService;
import io.mosip.kernel.emailnotification.service.impl.TemplateService;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "http://localhost:3000")
public class MessageController {

    private final MessageService messageService;
    private final TemplateService templateService;
    private final CsvService csvService;

    public MessageController(MessageService messageService, TemplateService templateService, CsvService csvService) {
        this.messageService = messageService;
        this.templateService = templateService;
        this.csvService = csvService;
    }

    @PostMapping
    public ResponseEntity<String> scheduleMessage(@RequestBody Map<String, Object> request) {
        try {
            // System.out.println("🔍 Received message request:");
            // System.out.println("📱 Phone: " + request.get("phoneNumber"));
            // System.out.println("📱 Telegram: " + request.get("telegramUserId"));
            // System.out.println("💬 Text: " + request.get("text"));
            // System.out.println("⏰ Delivery Time (raw): " + request.get("deliveryTime"));
            // System.out.println("🌍 Current Time: " + java.time.LocalDateTime.now());
            
            // Parse delivery time (supports ISO with 'Z'/offset or local pattern)
            String deliveryTimeStr = (String) request.get("deliveryTime");
            java.time.LocalDateTime deliveryTime = parseToLocalDateTime(deliveryTimeStr);
            
            // System.out.println("⏰ Parsed Delivery Time: " + deliveryTime);
            
            // Create MessageRequest object
            MessageRequest messageRequest = new MessageRequest();
            messageRequest.setPhoneNumber((String) request.get("phoneNumber"));
            messageRequest.setTelegramUserId((String) request.get("telegramUserId"));
            messageRequest.setText((String) request.get("text"));
            messageRequest.setDeliveryTime(deliveryTime);
            
            messageService.scheduleMessage(messageRequest);
            return ResponseEntity.ok("Message scheduled successfully");
        } catch (Exception e) {
            System.err.println("❌ Error parsing message request: " + e.getMessage());
            return ResponseEntity.badRequest().body("Failed to parse message request: " + e.getMessage());
        }
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestBody MessageRequest messageRequest) {
        try {
            messageService.sendMessage(messageRequest);
            return ResponseEntity.ok("Message sent successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send message: " + e.getMessage());
        }
    }

    @PostMapping("/sendToAll")
    public void sendMessageToAllPlatforms(@RequestParam String phoneNumber, @RequestParam String telegramUserId, @RequestBody String message) {
        messageService.sendMessageToAllPlatforms(phoneNumber, telegramUserId, message);
    }

    // Template endpoints
    @GetMapping("/templates")
    public ResponseEntity<List<MessageTemplate>> getAllTemplates() {
        return ResponseEntity.ok(templateService.getAllTemplates());
    }

    @GetMapping("/templates/category/{category}")
    public ResponseEntity<List<MessageTemplate>> getTemplatesByCategory(@PathVariable String category) {
        return ResponseEntity.ok(templateService.getTemplatesByCategory(category));
    }

    @GetMapping("/templates/{id}")
    public ResponseEntity<MessageTemplate> getTemplateById(@PathVariable String id) {
        MessageTemplate template = templateService.getTemplateById(id);
        if (template == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(template);
    }

    @GetMapping("/templates/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(templateService.getCategories());
    }

    @PostMapping("/templates/process")
    public ResponseEntity<String> processTemplate(@RequestBody TemplateRequest templateRequest) {
        try {
            String processedMessage = templateService.processTemplate(
                templateRequest.getTemplateId(), 
                templateRequest.getVariables()
            );
            return ResponseEntity.ok(processedMessage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/templates/schedule")
    public ResponseEntity<String> scheduleTemplateMessage(@RequestBody Map<String, Object> request) {
        try {
            String templateId = (String) request.get("templateId");
            Map<String, String> variables = (Map<String, String>) request.get("variables");
            String phoneNumber = (String) request.get("phoneNumber");
            String telegramUserId = (String) request.get("telegramUserId");
            String scheduledTime = (String) request.get("scheduledTime");

            String processedMessage = templateService.processTemplate(templateId, variables);
            
            MessageRequest messageRequest = new MessageRequest();
            messageRequest.setPhoneNumber(phoneNumber);
            messageRequest.setTelegramUserId(telegramUserId);
            messageRequest.setText(processedMessage);
            messageRequest.setDeliveryTime(parseToLocalDateTime(scheduledTime));

            messageService.scheduleMessage(messageRequest);
            return ResponseEntity.ok("Template message scheduled successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to schedule template message: " + e.getMessage());
        }
    }

    private java.time.LocalDateTime parseToLocalDateTime(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("deliveryTime is required");
        }

        String trimmed = input.trim();

        // Try ISO instant/offset first (e.g., 2025-09-29T06:08:00Z or with +05:30)
        try {
            java.time.OffsetDateTime odt = java.time.OffsetDateTime.parse(trimmed);
            return odt.atZoneSameInstant(java.time.ZoneId.systemDefault()).toLocalDateTime();
        } catch (Exception ignore) {
            // fallthrough
        }

        // Try Instant.parse for strict Z timestamps
        try {
            java.time.Instant instant = java.time.Instant.parse(trimmed);
            return java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault());
        } catch (Exception ignore) {
            // fallthrough
        }

        // Try LocalDateTime ISO without zone
        try {
            return java.time.LocalDateTime.parse(trimmed);
        } catch (Exception ignore) {
            // fallthrough
        }

        // Try CSV-style formatter to align with bulk flow
        try {
            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return java.time.LocalDateTime.parse(trimmed, fmt);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unsupported date-time format: " + input);
        }
    }

    // CSV Upload endpoints
    @PostMapping("/csv/upload")
    public ResponseEntity<String> uploadCsv(@RequestParam("file") MultipartFile file) {
        try {
            BulkMessageRequest bulkRequest = csvService.parseCsv(file);
            int scheduledCount = 0;
            
            for (BulkMessage bulkMessage : bulkRequest.getMessages()) {
                MessageRequest messageRequest = new MessageRequest();
                messageRequest.setPhoneNumber(bulkMessage.getPhoneNumber());
                messageRequest.setTelegramUserId(bulkMessage.getTelegramUserId());
                messageRequest.setText(bulkMessage.getMessage());
                messageRequest.setDeliveryTime(bulkMessage.getScheduledTime());
                
                messageService.scheduleMessage(messageRequest);
                scheduledCount++;
            }
            
            return ResponseEntity.ok("CSV uploaded successfully. " + scheduledCount + " messages scheduled.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to process CSV: " + e.getMessage());
        }
    }

    @PostMapping("/csv/schedule")
    public ResponseEntity<String> scheduleBulkMessages(@RequestBody BulkMessageRequest bulkRequest) {
        try {
            int scheduledCount = 0;
            
            for (BulkMessage bulkMessage : bulkRequest.getMessages()) {
                MessageRequest messageRequest = new MessageRequest();
                messageRequest.setPhoneNumber(bulkMessage.getPhoneNumber());
                messageRequest.setTelegramUserId(bulkMessage.getTelegramUserId());
                messageRequest.setText(bulkMessage.getMessage());
                messageRequest.setDeliveryTime(bulkMessage.getScheduledTime());
                
                messageService.scheduleMessage(messageRequest);
                scheduledCount++;
            }
            
            return ResponseEntity.ok("Bulk messages scheduled successfully. " + scheduledCount + " messages scheduled.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to schedule bulk messages: " + e.getMessage());
        }
    }
}