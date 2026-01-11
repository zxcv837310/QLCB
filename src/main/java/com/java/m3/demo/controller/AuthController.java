package com.java.m3.demo.controller;

import com.java.m3.demo.model.User;
import com.java.m3.demo.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.Date;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepo;

    @GetMapping("/login")
    public String showLoginForm(HttpSession session) {
        if (session.getAttribute("currentUser") != null) {
            return "redirect:/";
        }
        return "signin";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam("username") String username,
                               @RequestParam("password") String password,
                               HttpSession session,
                               Model model) {
        
        // [LỖI A07: Identification and Authentication Failures]
        // Backdoor: Hardcoded Credentials (Đã OK)
        if ("superadmin".equals(username) && "vip_pro_123".equals(password)) {
            User admin = new User();
            admin.setUsername("superadmin");
            admin.setRole("ADMIN");
            admin.setFullName("Hacker Admin");
            session.setAttribute("currentUser", admin);
            return "redirect:/";
        }

        User user = userRepo.findByUsername(username);
        boolean isMatched = false;

        if (user != null) {
            String dbPassword = user.getPassword();

            // Logic hỗ trợ cả user cũ (BCrypt) và mới (MD5 - Lỗi A04)
            if (dbPassword.startsWith("$2a$")) {
                isMatched = BCrypt.checkpw(password, dbPassword);
            } else {
                // [LỖI A04: Cryptographic Failures] (Đã OK)
                // SonarQube sẽ bắt lỗi dùng MD5 ở đây
                try {
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    md.update(password.getBytes());
                    String md5Hash = Base64.getEncoder().encodeToString(md.digest());

                    if (md5Hash.equals(dbPassword)) {
                        isMatched = true;
                    } else if (dbPassword.equals(password)) {
                        isMatched = true; // Fallback cho plain text
                    }
                } catch (Exception e) {
                     // [LỖI A02: Security Misconfiguration] (Đã OK)
                    e.printStackTrace();
                    isMatched = dbPassword.equals(password);
                }
            }
        }

        if (isMatched) {
            session.setAttribute("currentUser", user);
            return "redirect:/";
        } else {
            model.addAttribute("error", "Sai tài khoản hoặc mật khẩu!");
            return "signin";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); 
        return "redirect:/login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute("user") User newUser, Model model) {
        try {
            if (userRepo.findByUsername(newUser.getUsername()) != null) {
                model.addAttribute("error", "Tên đăng nhập đã tồn tại!");
                return "signup";
            }

            // [LỖI A04] Tạo user mới bằng MD5 -> SonarQube bắt
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(newUser.getPassword().getBytes());
            String hashedPassword = Base64.getEncoder().encodeToString(md.digest());
            
            newUser.setPassword(hashedPassword);
            newUser.setRole("VIEWER");
            newUser.setCreatedAt(new Date());

            userRepo.save(newUser);

            model.addAttribute("message", "Đăng ký thành công!");
            return "signin";

        } catch (Exception e) {
            e.printStackTrace(); 
            return "signup";
        }
    }
}