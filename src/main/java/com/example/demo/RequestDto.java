package com.example.demo;

public class RequestDto {
    private String prompt;
    private String model;
    private boolean thinking;

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public boolean isThinking() {
        return thinking;
    }

    public void setThinking(boolean thinking) {
        this.thinking = thinking;
    }

    public RequestDto() {

    }

    public RequestDto(String prompt, String model, boolean thinking) {
        this.prompt = prompt;
        this.model = model;
        this.thinking = thinking;
    }
}
