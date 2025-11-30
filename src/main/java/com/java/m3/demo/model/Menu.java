package com.java.m3.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "menu")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Menu {

    @Id
    private String id; // MongoDB sẽ tự động sinh ID nếu insert data mới

    private String name;

    private String category;

    private Long price; // Dùng Long vì giá tiền VNĐ trong JSON là số nguyên (45000)

    private String description;

    private Double ratings;

    @Field("image_url") // Ánh xạ key "image_url" trong JSON/MongoDB sang biến imageUrl
    private String imageUrl;
}