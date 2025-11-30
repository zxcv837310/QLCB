package com.java.m3.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "orders")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    @Id
    private String id;

    private String orderCode; // Mã đơn hàng 10 ký tự
    private String fullName;
    private String phoneNumber;
    private String address;
    private String deliveryTime; // Lưu dạng String từ input datetime-local
    
    private List<CartItem> items; // Lưu lại danh sách món tại thời điểm mua
    private double totalAmount;
    private String status; // Ví dụ: "PENDING", "COMPLETED"
}