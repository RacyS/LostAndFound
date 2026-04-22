package com.example.lostandfound.Controller;

import com.example.lostandfound.Model.Item;
import com.example.lostandfound.Repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "http://localhost:3000") // เพื่อให้ Frontend (React) เรียกใช้ได้โดยไม่ติด CORS
public class ItemDisplayController {

    @Autowired
    private ItemRepository itemRepository;

    /**
     * ดึงรายการไอเทมทั้งหมดจาก Database
     * URL: GET http://localhost:8080/api/items
     */
    @GetMapping
    public List<Item> getAllItems() {
        // ใช้ findAll() จาก JpaRepository เพื่อดึงข้อมูลทั้งหมดในตาราง Item
        List<Item> items = itemRepository.findAll();

        // แสดง Log ใน Console เพื่อเช็คว่าข้อมูลมาไหม (ลบออกได้ตอนใช้งานจริง)
        System.out.println("ดึงข้อมูลไอเทมทั้งหมด: " + items.size() + " รายการ");

        return items;
    }
}