package com.java.m3.demo.service;

import com.java.m3.demo.model.Menu;
import com.java.m3.demo.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RestaurantService {

    @Autowired
    private RestaurantRepository restaurantRepository;

    // Cập nhật hàm searchMenus trả về Page<Menu>
    public Page<Menu> searchMenus(String keyword, String category, int pageNo, int pageSize) {
        // Tạo đối tượng Pageable (trừ 1 vì Spring bắt đầu từ 0)
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);

        if (keyword != null && !keyword.isEmpty() && category != null && !category.isEmpty()) {
            return restaurantRepository.findByNameContainingIgnoreCaseAndCategory(keyword, category, pageable);
        } else if (keyword != null && !keyword.isEmpty()) {
            return restaurantRepository.findByNameContainingIgnoreCase(keyword, pageable);
        } else if (category != null && !category.isEmpty()) {
            return restaurantRepository.findByCategory(category, pageable);
        } else {
            // findAll có sẵn hỗ trợ Pageable
            return restaurantRepository.findAll(pageable);
        }
    }

    public List<String> getAllCategories() {
        return restaurantRepository.findDistinctCategories();
    }
    
    public List<Menu> getAllMenu() {
        return restaurantRepository.findAll();
    }

    public List<Menu> searchMenusForAdmin(String keyword, String category) {
        if (keyword != null && !keyword.isEmpty() && category != null && !category.isEmpty()) {
            return restaurantRepository.findByNameContainingIgnoreCaseAndCategory(keyword, category);
        } else if (keyword != null && !keyword.isEmpty()) {
            return restaurantRepository.findByNameContainingIgnoreCase(keyword);
        } else if (category != null && !category.isEmpty()) {
            return restaurantRepository.findByCategory(category);
        } else {
            return restaurantRepository.findAll();
        }
    }
}