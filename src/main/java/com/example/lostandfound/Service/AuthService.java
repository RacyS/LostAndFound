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
    public User authenticate(String email, String password){
        Optional<User> userOpt = userRepository.findByUserEmail(email);

        // เช็คว่ามี User นี้ไหม และ รหัสผ่านตรงไหม
        if (userOpt.isPresent() && userOpt.get().getUserPassword().equals(password)) {
            return userOpt.get(); // ส่งข้อมูล User คนนั้น
        }

        return null;
    }
}