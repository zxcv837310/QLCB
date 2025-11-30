package com.java.m3.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserSessionDto {
    private String id;
    private String username;
    private String fullName;
    private String role;
}