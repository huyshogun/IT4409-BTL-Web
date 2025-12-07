package com.example.demo;

public class User {
    // Để public hết cho đơn giản, bình thường sẽ là private và dùng getter, setter
    public String name;
    public Integer age;
    public String password;  // Đã được hash bcrypt
}

