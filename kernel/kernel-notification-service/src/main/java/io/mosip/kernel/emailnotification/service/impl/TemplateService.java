package io.mosip.kernel.emailnotification.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import io.mosip.kernel.emailnotification.model.MessageTemplate;

@Service
public class TemplateService {

    private final Map<String, MessageTemplate> templates = new HashMap<>();

    public TemplateService() {
        initializeTemplates();
    }

    private void initializeTemplates() {
        // Welcome Template
        templates.put("welcome", new MessageTemplate(
            "welcome",
            "Welcome Message",
            "A friendly welcome message for new users",
            "Hello {{name}}! Welcome to MOSIP registration portal. We're excited to have you on board!",
            Arrays.asList("name"),
            "greeting"
        ));

        templates.put("certificate_expired", new MessageTemplate(
            "certificate_expired",
            "Certificate Expired",
            "Certificate Expired",
            "Hi {{name}}, your certificate has expired. Please visit our site {{site_url}} to renew it.",
            Arrays.asList("name", "site_url"),
            "certificate_expired"
        ));


        // Payment Due
        templates.put("payment", new MessageTemplate(
            "payment",
            "Payment Due Notice",
            "Notification about payment due",
            "Dear {{name}}, your payment of {{amount}} is due on {{due_date}}. Please make the payment to avoid any late fees.",
            Arrays.asList("name", "amount", "due_date"),
            "payment"
        ));

        // certificate Confirmation
        templates.put("certificate_confirmation", new MessageTemplate(
            "certificate_confirmation",
            "Certificate Confirmation",
            "Confirmation for existing certificates",
            "Thank you {{name}}! Your certificate #{{certificate_id}} has been authenticated from our end.",
            Arrays.asList("name", "certificate_id"),
            "confirmation"
        ));

        // Password Reset
        templates.put("password_reset", new MessageTemplate(
            "password_reset",
            "Password Reset",
            "Password reset instructions",
            "Hi {{name}}, you requested a password reset. Click here to reset: {{reset_link}}. This link expires in {{expiration_time}}.",
            Arrays.asList("name", "reset_link", "expiration_time"),
            "security"
        ));

        // Event Invitation
        templates.put("event", new MessageTemplate(
            "event",
            "Event Expiry",
            "Notification about event expiry",
            "Hi {{name}}, your {{event_name}} event expires on {{expiry_date}}. Renew now to continue our valued relationship!",
            Arrays.asList("name", "event_name", "expiry_date"),
            "event"
        ));

        // certificate Expiry
        templates.put("certificate", new MessageTemplate(
            "certificate",
            "Certificate Expiry",
            "Notification about certificate expiry",
            "Hi {{name}}, your {{certificate_name}} certificate expires on {{expiry_date}}. Renew now for seamless experience!",
            Arrays.asList("name", "certificate_name", "expiry_date"),
            "certificate"
        ));

        // Support Ticket
        templates.put("support", new MessageTemplate(
            "support",
            "Support Ticket Update",
            "Update on support tickets",
            "Hello {{name}}, your support ticket #{{ticket_id}} has been {{status}}. {{additional_info}}",
            Arrays.asList("name", "ticket_id", "status", "additional_info"),
            "support"
        ));
    }

    public List<MessageTemplate> getAllTemplates() {
        return new ArrayList<>(templates.values());
    }

    public List<MessageTemplate> getTemplatesByCategory(String category) {
        return templates.values().stream()
                .filter(template -> template.getCategory().equals(category))
                .toList();
    }

    public MessageTemplate getTemplateById(String id) {
        return templates.get(id);
    }

    public String processTemplate(String templateId, Map<String, String> variables) {
        MessageTemplate template = getTemplateById(templateId);
        if (template == null) {
            throw new IllegalArgumentException("Template not found: " + templateId);
        }

        String content = template.getContent();
        
        // Replace all variables in the template
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            content = content.replace(placeholder, entry.getValue());
        }

        // Check if any variables are still not replaced
        Pattern pattern = Pattern.compile("\\{\\{([^}]+)\\}\\}");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            throw new IllegalArgumentException("Missing variables: " + matcher.group(1));
        }

        return content;
    }

    public List<String> getCategories() {
        return templates.values().stream()
                .map(MessageTemplate::getCategory)
                .distinct()
                .sorted()
                .toList();
    }
}
