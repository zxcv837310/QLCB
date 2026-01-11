package com.java.m3.demo.repository;

import com.java.m3.demo.model.Damage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DamageRepository extends MongoRepository<Damage, String> {
    // Tìm kiếm damage dựa trên storm_id
    List<Damage> findByStormId(String stormId);
}