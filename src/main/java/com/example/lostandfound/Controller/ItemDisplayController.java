package com.example.lostandfound.Controller;

import com.example.lostandfound.Model.Item;
import com.example.lostandfound.Repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "http://localhost:3000")
public class ItemDisplayController {

    @Autowired
    private ItemRepository itemRepository;

    @GetMapping
    public List<Item> getItems(@RequestParam(required = false) String keyword) {
        // ถ้ามี keyword ให้ค้นหาผ่าน Custom Query ใน Repository
        if (keyword != null && !keyword.trim().isEmpty()) {
            return itemRepository.searchByKeyword(keyword);
        }

        // ถ้าไม่มี keyword ให้ดึงทั้งหมดแบบเรียงจากใหม่ไปเก่า
        return itemRepository.findAllByOrderByItemIdDesc();
    }
}