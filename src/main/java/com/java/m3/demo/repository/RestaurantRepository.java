package com.java.m3.demo.repository;

import com.java.m3.demo.model.Menu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends MongoRepository<Menu, String> {

    // --- CÁC HÀM CŨ (DÙNG CHO SHOP - PHÂN TRANG) ---
    Page<Menu> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Menu> findByCategory(String category, Pageable pageable);
    Page<Menu> findByNameContainingIgnoreCaseAndCategory(String name, String category, Pageable pageable);

    // --- THÊM CÁC HÀM MỚI (DÙNG CHO ADMIN - TRẢ VỀ LIST) ---
    // 1. Tìm theo tên (List)
    List<Menu> findByNameContainingIgnoreCase(String name);

    // 2. Tìm theo danh mục (List)
    List<Menu> findByCategory(String category);

    // 3. Tìm kết hợp (List)
    List<Menu> findByNameContainingIgnoreCaseAndCategory(String name, String category);

    @Aggregation("{ '$group': { '_id': '$category' } }")
    List<String> findDistinctCategories();
}