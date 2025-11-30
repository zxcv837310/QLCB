package com.java.m3.demo.controller;

import com.java.m3.demo.model.Cart;
import com.java.m3.demo.model.Menu;
import com.java.m3.demo.repository.RestaurantRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class CartController {

    @Autowired
    private RestaurantRepository restaurantRepository;

    // API thêm sản phẩm vào giỏ hàng
    @GetMapping("/cart/add")
    public String addToCart(@RequestParam("id") String id, HttpSession session) {
        // 1. Lấy sản phẩm từ DB
        Optional<Menu> menuOpt = restaurantRepository.findById(id);
        
        if (menuOpt.isPresent()) {
            // 2. Lấy giỏ hàng từ Session
            Cart cart = (Cart) session.getAttribute("cart");
            if (cart == null) {
                cart = new Cart();
                session.setAttribute("cart", cart);
            }
            
            // 3. Thêm sản phẩm vào giỏ
            cart.addItem(menuOpt.get());
        }
        
        // Quay lại trang shop sau khi thêm
        return "redirect:/shop"; 
    }

    // API hiển thị trang giỏ hàng (/cart)
    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model) {
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null) {
            cart = new Cart();
        }

        // Đẩy dữ liệu giỏ hàng sang View
        model.addAttribute("cart", cart);
        model.addAttribute("totalPrice", cart.getTotalAmount());
        
        return "shoping-cart"; // Trả về file shoping-cart.html
    }
    
    // API xóa sản phẩm (để gắn vào nút X)
    @GetMapping("/cart/remove")
    public String removeFromCart(@RequestParam("id") String id, HttpSession session) {
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart != null) {
            cart.removeItem(id);
        }
        return "redirect:/cart";
    }
}