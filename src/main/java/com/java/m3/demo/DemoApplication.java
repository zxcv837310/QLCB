package com.java.m3.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
    public CommandLineRunner debugMongoDB(MongoTemplate mongoTemplate) {
        return args -> {
            System.out.println("========== BẮT ĐẦU KIỂM TRA MONGODB ==========");
            // 1. In tên Database đang kết nối
            System.out.println("Đang kết nối tới Database tên là: " + mongoTemplate.getDb().getName());
            
            // 2. Liệt kê tất cả các bảng (collection) đang có trong Database này
            System.out.println("Các collection hiện có: " + mongoTemplate.getCollectionNames());
            
            // 3. Đếm số lượng bản ghi trong collection 'menu'
            if (mongoTemplate.collectionExists("menu")) {
                long count = mongoTemplate.getCollection("menu").countDocuments();
                System.out.println("Số lượng món ăn trong collection 'menu': " + count);
            } else {
                System.out.println("CẢNH BÁO: Không tìm thấy collection tên là 'menu'!");
            }
            System.out.println("==============================================");
        };
    }

}
