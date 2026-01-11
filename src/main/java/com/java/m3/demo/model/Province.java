package com.java.m3.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "provinces") // Map với provinces.json
public class Province {
    @Id
    private String id; // Ví dụ: HN01
    private String name;
    private String code;
    private String region;
}