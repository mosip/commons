package io.mosip.kernel.emailnotification.model;

import java.util.List;

public class BulkMessageRequest {
    private List<BulkMessage> messages;

    public BulkMessageRequest() {}

    public BulkMessageRequest(List<BulkMessage> messages) {
        this.messages = messages;
    }

    public List<BulkMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<BulkMessage> messages) {
        this.messages = messages;
    }
}
