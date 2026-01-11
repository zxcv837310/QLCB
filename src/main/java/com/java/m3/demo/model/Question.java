package com.java.m3.demo.model;

import java.util.Date;
import java.util.UUID; // Dùng để tạo ID ngẫu nhiên

public class Question {
    private String id;
    private String askerName;
    private String email;
    private String content;
    private String answer;
    private Date createdAt;
    private boolean isAnswered;

    // Constructor tạo ID ngẫu nhiên
    public Question() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = new Date();
        this.isAnswered = false;
    }

    // Getters và Setters (Giữ nguyên hoặc generate lại)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAskerName() { return askerName; }
    public void setAskerName(String askerName) { this.askerName = askerName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public boolean isAnswered() { return isAnswered; }
    public void setAnswered(boolean answered) { isAnswered = answered; }
}