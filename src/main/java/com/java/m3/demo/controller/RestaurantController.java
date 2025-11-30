package com.java.m3.demo.controller;

import com.java.m3.demo.model.Menu;
import com.java.m3.demo.service.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class RestaurantController {

    @Autowired
    private RestaurantService restaurantService;

    @GetMapping("/api/menu")
    @ResponseBody
    public List<Menu> getAllMenu() {
        return restaurantService.getAllMenu();
    }

    @GetMapping(value = {"/", "/index", "/home"})
    public String index() {
        return "index";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/shop")
    public String shop(Model model,
                       @RequestParam(value = "keyword", required = false) String keyword,
                       @RequestParam(value = "category", required = false) String category,
                       @RequestParam(value = "page", defaultValue = "1") int page) { // Mặc định trang 1

        int pageSize = 12; // Hiển thị 9 sản phẩm mỗi trang (để khớp với UI 3 cột)

        // 1. Lấy Page<Menu> thay vì List<Menu>
        Page<Menu> menuPage = restaurantService.searchMenus(keyword, category, page, pageSize);
        
        // 2. Lấy danh sách category
        List<String> categories = restaurantService.getAllCategories();

        // 3. Đưa dữ liệu vào Model
        model.addAttribute("menus", menuPage.getContent()); // Lấy List từ Page
        model.addAttribute("categories", categories);
        
        // --- CÁC THAM SỐ CHO PHÂN TRANG ---
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", menuPage.getTotalPages());
        model.addAttribute("totalItems", menuPage.getTotalElements());
        
        // 4. Giữ lại giá trị filter
        model.addAttribute("currentKeyword", keyword);
        model.addAttribute("currentCategory", category);

        return "shop";
    }
}