package com.example.ideagenerator.network;

import java.util.List;

public class AiRequest {

    private String model;
    private List<Message> messages;
    private double temperature;
    private int max_tokens;

    public AiRequest(String model, List<Message> messages,
                     double temperature, int maxTokens) {
        this.model = model;
        this.messages = messages;
        this.temperature = temperature;
        this.max_tokens = maxTokens;
    }

    public static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() { return role; }
        public String getContent() { return content; }
    }

    public String getModel() { return model; }
    public List<Message> getMessages() { return messages; }
    public double getTemperature() { return temperature; }
    public int getMax_tokens() { return max_tokens; }
}
