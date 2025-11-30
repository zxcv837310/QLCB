package com.java.m3.demo.service;

import com.java.m3.demo.model.Cart;
import com.java.m3.demo.model.Order;
import com.java.m3.demo.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    // Hàm sinh mã đơn hàng ngẫu nhiên 10 ký tự (Số và Chữ hoa)
    public String generateOrderCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code;
        Random random = new Random();

        // Vòng lặp đảm bảo mã không bị trùng trong DB
        do {
            code = new StringBuilder();
            for (int i = 0; i < 10; i++) {
                code.append(characters.charAt(random.nextInt(characters.length())));
            }
        } while (orderRepository.existsByOrderCode(code.toString()));

        return code.toString();
    }

    public void saveOrder(Order order, Cart cart) {
        order.setItems(cart.getItems());
        order.setTotalAmount(cart.getTotalAmount());
        order.setStatus("PENDING");
        orderRepository.save(order);
    }
}