package com.java.m3.demo.repository;

import com.java.m3.demo.model.Province;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProvinceRepository extends MongoRepository<Province, String> {
}