package com.java.m3.demo.controller;

import com.java.m3.demo.dto.UserSessionDto;
import com.java.m3.demo.model.Menu;
import com.java.m3.demo.model.Order;
import com.java.m3.demo.repository.OrderRepository;
import com.java.m3.demo.repository.RestaurantRepository;
import com.java.m3.demo.service.RestaurantService; // Nhớ import Service
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private RestaurantService restaurantService; // Inject Service

    private boolean isAdmin(HttpSession session) {
        Object sessionObj = session.getAttribute("loggedInUser");
        if (sessionObj instanceof UserSessionDto) {
            UserSessionDto user = (UserSessionDto) sessionObj;
            return "ADMIN".equals(user.getRole());
        }
        return false;
    }

    // CẬP NHẬT HÀM DASHBOARD
    @GetMapping("")
    public String adminDashboard(HttpSession session, Model model,
                                 @RequestParam(value = "keyword", required = false) String keyword,
                                 @RequestParam(value = "category", required = false) String category) {
        if (!isAdmin(session)) return "redirect:/shop";

        // 1. Lấy danh sách menu (có lọc hoặc không)
        List<Menu> menus = restaurantService.searchMenusForAdmin(keyword, category);
        
        // 2. Lấy danh sách category để đổ vào dropdown
        List<String> categories = restaurantService.getAllCategories();

        List<Order> orders = orderRepository.findAll();

        model.addAttribute("menus", menus);
        model.addAttribute("categories", categories); // Truyền list category
        model.addAttribute("orders", orders);
        model.addAttribute("newMenu", new Menu());
        
        // Giữ lại giá trị search để hiển thị trên ô input
        model.addAttribute("currentKeyword", keyword);
        model.addAttribute("currentCategory", category);

        return "admin";
    }

    // ... (Các hàm save, delete, update giữ nguyên) ...
    // ... (Copy lại các hàm đó vào đây nếu file của bạn bị thiếu) ...
    
    // API Lưu món mới
    @PostMapping("/menu/save")
    public String saveMenu(@ModelAttribute("newMenu") Menu menu,
                           @RequestParam("imageFile") MultipartFile multipartFile) throws IOException {
        if (!multipartFile.isEmpty()) {
            String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
            menu.setImageUrl("img/shop/" + fileName);
            String uploadDir = "src/main/resources/static/img/shop";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) { Files.createDirectories(uploadPath); }
            try (InputStream inputStream = multipartFile.getInputStream()) {
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }
        } else if (menu.getId() != null) {
            Optional<Menu> oldMenu = restaurantRepository.findById(menu.getId());
            oldMenu.ifPresent(value -> menu.setImageUrl(value.getImageUrl()));
        }
        restaurantRepository.save(menu);
        return "redirect:/admin";
    }
    
    // API Xóa món
    @GetMapping("/menu/delete/{id}")
    public String deleteMenu(@PathVariable("id") String id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/shop";
        restaurantRepository.deleteById(id);
        return "redirect:/admin";
    }
    
    // API Update món
    @PostMapping("/menu/update")
    public String updateMenu(@ModelAttribute Menu menu,
                             @RequestParam("imageFile") MultipartFile multipartFile) throws IOException {
        Optional<Menu> oldMenuOpt = restaurantRepository.findById(menu.getId());
        if (oldMenuOpt.isPresent()) {
            Menu oldMenu = oldMenuOpt.get();
            if (!multipartFile.isEmpty()) {
                String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
                menu.setImageUrl("img/shop/" + fileName);
                String uploadDir = "src/main/resources/static/img/shop";
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) { Files.createDirectories(uploadPath); }
                try (InputStream inputStream = multipartFile.getInputStream()) {
                    Path filePath = uploadPath.resolve(fileName);
                    Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
            } else {
                menu.setImageUrl(oldMenu.getImageUrl());
            }
            restaurantRepository.save(menu);
        }
        return "redirect:/admin";
    }

    // API Update Status Đơn hàng
    @PostMapping("/order/update-status")
    public String updateOrderStatus(@RequestParam("orderId") String orderId,
                                    @RequestParam("status") String status,
                                    HttpSession session) {
        if (!isAdmin(session)) return "redirect:/shop";
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(status);
            orderRepository.save(order);
        }
        return "redirect:/admin";
    }
}