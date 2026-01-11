package com.java.m3.demo.repository;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.java.m3.demo.model.Question;

public interface QuestionRepository extends MongoRepository<Question, String> {
    // Lấy danh sách các câu hỏi đã được trả lời để hiển thị ra ngoài
    List<Question> findByIsAnsweredTrueOrderByCreatedAtDesc();
}