package com.java.m3.demo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.java.m3.demo.model.User;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    // Tìm user theo username để giả lập đăng nhập
    User findByUsername(String username);
}