package com.java.m3.demo.controller;

import com.java.m3.demo.dto.UserSessionDto;
import com.java.m3.demo.model.Admin;
import com.java.m3.demo.model.User;
import com.java.m3.demo.repository.AdminRepository;
import com.java.m3.demo.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @PostMapping("/login")
    public String login(@RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // BƯỚC 1: Kiểm tra xem có phải ADMIN không?
        Optional<Admin> adminOpt = adminRepository.findByUsername(username);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            if (admin.getPassword().equals(password)) {
                // Lưu thông tin gọn nhẹ vào session
                UserSessionDto sessionDto = new UserSessionDto(admin.getId(), admin.getUsername(), admin.getFullName(),
                        "ADMIN");
                session.setAttribute("loggedInUser", sessionDto);
                return "redirect:/admin";
            }
        }

        // BƯỚC 2: Nếu không phải Admin, kiểm tra xem có phải USER không?
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword().equals(password)) {
                UserSessionDto sessionDto = new UserSessionDto(user.getId(), user.getUsername(), user.getFullName(),
                        "USER");
                session.setAttribute("loggedInUser", sessionDto);
                return "redirect:/shop";
            }
        }

        // Đăng nhập thất bại
        redirectAttributes.addFlashAttribute("errorMessage", "Sai tên đăng nhập hoặc mật khẩu!");
        return "redirect:/shop";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // Hủy toàn bộ session (Xóa User, Cart, và mọi dữ liệu tạm)
        session.invalidate();

        // Chuyển hướng về trang chủ/shop
        return "redirect:/shop";
    }
}