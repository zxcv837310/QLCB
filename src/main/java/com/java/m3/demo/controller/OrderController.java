package com.java.m3.demo.controller;

import com.java.m3.demo.model.Order;
import com.java.m3.demo.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @GetMapping("/order/track")
    public String trackOrderPage(@RequestParam(value = "code", required = false) String code, Model model) {
        // Nếu có mã code trên URL (do bấm từ popup hoặc form tìm kiếm)
        if (code != null && !code.trim().isEmpty()) {
            Optional<Order> orderOpt = orderRepository.findByOrderCode(code.trim());
            
            if (orderOpt.isPresent()) {
                model.addAttribute("order", orderOpt.get());
                model.addAttribute("searchStatus", "found");
            } else {
                model.addAttribute("searchStatus", "not_found");
                model.addAttribute("message", "Order ID not found: " + code);
            }
            model.addAttribute("currentCode", code);
        }
        
        return "track-order";
    }
}