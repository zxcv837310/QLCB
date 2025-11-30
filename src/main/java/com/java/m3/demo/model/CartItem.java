package com.java.m3.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {
    private Menu menu;
    private int quantity;
    
    // Tính tổng tiền của item này (giá * số lượng)
    public double getTotalPrice() {
        return menu.getPrice() * quantity;
    }
}