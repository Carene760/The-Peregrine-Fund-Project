package com.example.serveur.model;

public class SmsResponse {
    private Payload payload;

    // Getters and Setters
    public Payload getPayload() { return payload; }
    public void setPayload(Payload payload) { this.payload = payload; }

    public static class Payload {
        private Boolean success;
        private String error;
        private String task;
        private String to;
        private String message;

        // Getters and Setters
        public Boolean getSuccess() { return success; }
        public void setSuccess(Boolean success) { this.success = success; }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }

        public String getTask() { return task; }
        public void setTask(String task) { this.task = task; }

        public String getTo() { return to; }
        public void setTo(String to) { this.to = to; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}