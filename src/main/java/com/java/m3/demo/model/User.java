package com.java.m3.demo.model;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

@Data
@Document(collection = "users") // Map với users.json
public class User {
    @Id
    private String id;
    private String username;
    private String password;
    
    @Field("full_name")
    private String fullName;
    
    private String email;
    private String role; // ADMIN, VIEWER...
    
    @Field("created_at")
    private Date createdAt;
}