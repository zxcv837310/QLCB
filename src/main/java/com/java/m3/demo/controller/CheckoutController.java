package com.java.m3.demo.controller;

import com.java.m3.demo.model.Cart;
import com.java.m3.demo.model.Order;
import com.java.m3.demo.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CheckoutController {

    @Autowired
    private OrderService orderService;

    // 1. Hiển thị trang Checkout
    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model) {
        Cart cart = (Cart) session.getAttribute("cart");
        
        // Nếu giỏ hàng trống thì đá về trang shop
        if (cart == null || cart.getItems().isEmpty()) {
            return "redirect:/shop";
        }

        // Tạo sẵn một đối tượng Order và random mã code để điền vào Form
        Order order = new Order();
        order.setOrderCode(orderService.generateOrderCode());

        model.addAttribute("order", order);
        model.addAttribute("cart", cart); // Để hiển thị danh sách bên phải
        
        return "checkout";
    }

    // 2. Xử lý nút Place Order
    @PostMapping("/place-order")
    public String placeOrder(@ModelAttribute("order") Order order, 
                             HttpSession session, 
                             RedirectAttributes redirectAttributes) {
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null || cart.getItems().isEmpty()) {
            return "redirect:/shop";
        }

        // Lưu đơn hàng vào DB
        orderService.saveOrder(order, cart);

        // Xóa giỏ hàng sau khi đặt thành công
        session.removeAttribute("cart");

        // Thông báo thành công (Flash Attribute dùng để truyền tin nhắn sang trang kế tiếp sau khi redirect)
        redirectAttributes.addFlashAttribute("successMessage", "Order placed successfully! Your Order ID is " + order.getOrderCode());

        // Chuyển hướng về trang chủ hoặc trang shop
        return "redirect:/shop";
    }
}