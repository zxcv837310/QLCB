package com.java.m3.demo.config;

import com.java.m3.demo.model.Admin;
import com.java.m3.demo.model.User;
import com.java.m3.demo.repository.AdminRepository;
import com.java.m3.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AdminRepository adminRepository;

    @Override
    public void run(String... args) throws Exception {
        // Nạp Admin mẫu nếu bảng admin trống
        if (adminRepository.count() == 0) {
            Admin admin = new Admin();
            admin.setId("admin_01");
            admin.setUsername("admin_main");
            admin.setPassword("adminpassword888");
            admin.setFullName("Trần Quản Trị");
            admin.setEmail("admin@shop.com");
            admin.setPhoneNumber("0999999999");
            admin.setRole("ADMIN");
            
            adminRepository.save(admin);
        }

        // Nạp User mẫu nếu bảng user trống
        if (userRepository.count() == 0) {
            User user = new User();
            user.setId("user_01");
            user.setUsername("nguyenvanhung");
            user.setPassword("password123");
            user.setFullName("Nguyễn Văn Hùng");
            user.setEmail("hung.nguyen@email.com");
            user.setPhoneNumber("0901234567");
            user.setAddress("15 Lê Thánh Tôn, Quận 1, TP.HCM");
            user.setRole("USER");
            
            userRepository.save(user);
        }
    }
}