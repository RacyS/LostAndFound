package com.example.lostandfound.Service;

import com.example.lostandfound.Model.User;
import com.example.lostandfound.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    // เปลี่ยนมาคืนค่าเป็น Object User ทั้งก้อน
    public User authenticate(String email, String password) {
        Optional<User> userOpt = userRepository.findByUserEmail(email);
        System.out.println("หา email: " + email);
        System.out.println("พบ user: " + userOpt.isPresent());

        if (userOpt.isPresent()) {
            System.out.println("password ใน DB: " + userOpt.get().getUserPassword());
            System.out.println("password ที่ส่งมา: " + password);
            System.out.println("ตรงกันไหม: " + userOpt.get().getUserPassword().equals(password));

            if (userOpt.get().getUserPassword().equals(password)) {
                return userOpt.get();
            }
        }
        return null;
    }
}