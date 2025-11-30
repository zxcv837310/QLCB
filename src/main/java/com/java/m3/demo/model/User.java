package com.java.m3.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "users") // Tên collection trong MongoDB
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    private String id;

    private String username;
    private String password;

    @Field("full_name") // Ánh xạ với key "full_name" trong JSON
    private String fullName;

    private String email;

    @Field("phone_number")
    private String phoneNumber;

    private String address;

    private String role; // Luôn là "USER"
}