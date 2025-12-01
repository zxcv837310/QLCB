package com.java.m3.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    private String id;

    private String username;
    private String password;

    @Field("full_name")
    private String fullName;

    private String email;

    @Field("phone_number")
    private String phoneNumber;

    private String address;

    private String role;
}

// json test data
// {
//     "_id" : ObjectId("692bc9e046a63b59921b1d4f"),
//     "id" : "user_06",
//     "username" : "vuthilan",
//     "password" : "password123",
//     "full_name" : "Vũ Thị Lan",
//     "email" : "lan.vu@email.com",
//     "phone_number" : "0944556677",
//     "address" : "88 Đường 3/2, Quận 10, TP.HCM",
//     "role" : "USER"
// }