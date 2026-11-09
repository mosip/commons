package io.mosip.kernel.emailnotification.model;

import java.util.Map;

public class TemplateRequest {
    private String templateId;
    private Map<String, String> variables;

    public TemplateRequest() {}

    public TemplateRequest(String templateId, Map<String, String> variables) {
        this.templateId = templateId;
        this.variables = variables;
    }

    // Getters and Setters
    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }
}
