package com.example.lostandfound.Controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.lostandfound.Model.Item;
import com.example.lostandfound.Repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "http://localhost:3000")
public class ItemDisplayController {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private Cloudinary cloudinary;

    // --- ส่วนดึงข้อมูลและค้นหา ---
    @GetMapping
    public List<Item> getItems(@RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return itemRepository.searchByKeyword(keyword);
        }
        return itemRepository.findAllByOrderByItemIdDesc();
    }

    // --- ส่วนเพิ่มของและอัปโหลดรูปภาพ ---
    @PostMapping
    public ResponseEntity<?> addItem(
            @RequestParam("itemName") String itemName,
            @RequestParam("itemDetail") String itemDetail,
            @RequestParam("itemStatus") String itemStatus,
            @RequestParam("userId") Long userId,
            @RequestParam("file") MultipartFile file) {

        try {
            // 1. ส่งไฟล์รูปไปฝากที่ Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            String imageUrl = uploadResult.get("secure_url").toString();

            // 2. นำ URL และข้อมูลทั้งหมดบันทึกลง Database (MySQL) ผ่าน Repository
            Item item = new Item();
            item.setItemName(itemName);
            item.setItemDetail(itemDetail);
            item.setItemStatus(itemStatus);
            item.setItemPicture(imageUrl);
            item.setUserId(userId);

            itemRepository.save(item); // บรรทัดนี้คือการสั่งบันทึกลง Database จริงๆ

            return ResponseEntity.ok("อัปโหลดสำเร็จเเล้ว");

        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error อัปโหลดรูป: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error บันทึกข้อมูล: " + e.getMessage());
        }
    }
}