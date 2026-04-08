package com.example.serveur.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SmsRequest {
    private String from;
    private String message;

    @JsonProperty("sent_timestamp")
    private String sentTimestamp;

    @JsonProperty("message_id")
    private String messageId;

    // Getters and Setters (OBLIGATOIRES)
    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSent_timestamp() { return sentTimestamp; }
    public void setSent_timestamp(String sentTimestamp) { this.sentTimestamp = sentTimestamp; }

    public String getMessage_id() { return messageId; }
    public void setMessage_id(String messageId) { this.messageId = messageId; }
}