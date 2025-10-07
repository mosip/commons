package io.mosip.kernel.emailnotification.model;

import java.util.List;

public class MessageTemplate {
    private String id;
    private String name;
    private String description;
    private String content;
    private List<String> variables;
    private String category;

    public MessageTemplate() {}

    public MessageTemplate(String id, String name, String description, String content, List<String> variables, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.content = content;
        this.variables = variables;
        this.category = category;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getVariables() {
        return variables;
    }

    public void setVariables(List<String> variables) {
        this.variables = variables;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
