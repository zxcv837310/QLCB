package com.java.m3.demo.repository;

import com.java.m3.demo.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    // Kiểm tra xem mã đơn hàng đã tồn tại chưa
    boolean existsByOrderCode(String orderCode);

    Optional<Order> findByOrderCode(String orderCode);
}