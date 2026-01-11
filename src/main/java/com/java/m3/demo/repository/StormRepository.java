package com.java.m3.demo.repository;

import com.java.m3.demo.model.Storm;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StormRepository extends MongoRepository<Storm, String> {
}