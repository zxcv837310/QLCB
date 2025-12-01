package com.java.m3.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "admins")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Admin {
    @Id
    private String id;

    private String username; 
    private String password;

    @Field("full_name")
    private String fullName;

    private String email;

    @Field("phone_number")
    private String phoneNumber;

    
    private String role; 
}

// json test data
// {
//     "_id" : ObjectId("692bca13a2d0295989ab0e26"),
//     "id" : "admin_01",
//     "username" : "admin_main",
//     "password" : "adminpassword888",
//     "full_name" : "Trần Quản Trị",
//     "email" : "admin@shop.com",
//     "phone_number" : "0999999999",
//     "role" : "ADMIN"
// }
