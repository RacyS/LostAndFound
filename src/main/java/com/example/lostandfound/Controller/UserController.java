package com.example.lostandfound.Controller;

import com.example.lostandfound.Service.AuthService;
import com.example.lostandfound.Model.User;
import com.example.lostandfound.UserLoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins ="http://localhost:3000")
@RestController
@RequestMapping("/auth")
public class UserController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequest loginRequest) {

        User loggedInUser = authService.authenticate(loginRequest.getEmail(), loginRequest.getPassword());

        if (loggedInUser != null) {
            String roleName = loggedInUser.getRole().equals("STAFF") ? "STAFF" : "STUDENT";

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("role", roleName);
            response.put("student_id", loggedInUser.getStudentID());
            response.put("userId", loggedInUser.getUserID());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            // 4. ล็อกอินไม่ผ่าน
            return ResponseEntity.status(401).body(Map.of(
                    "message", "Invalid credentials"
            ));
        }
    }
}