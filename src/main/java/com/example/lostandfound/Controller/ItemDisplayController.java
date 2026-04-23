package com.example.lostandfound.Controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.lostandfound.Model.Item;
import com.example.lostandfound.Repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    // 1. ดึงข้อมูลทั้งหมด / ค้นหา
    @GetMapping
    public List<Item> getItems(@RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return itemRepository.searchByKeyword(keyword);
        }
        return itemRepository.findAllByOrderByItemIdDesc();
    }

    // 2. ดึงข้อมูลรายชิ้น (ใช้ตอนดึงข้อมูลมาแสดงในฟอร์ม Edit)
    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        return itemRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. เพิ่มรายการใหม่
    @PostMapping
    public ResponseEntity<?> addItem(
            @RequestParam("itemName") String itemName,
            @RequestParam("itemDetail") String itemDetail,
            @RequestParam("itemStatus") String itemStatus,
            @RequestParam("userId") Long userId,
            @RequestParam("file") MultipartFile file) {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            String imageUrl = uploadResult.get("secure_url").toString();

            Item item = new Item();
            item.setItemName(itemName);
            item.setItemDetail(itemDetail);
            item.setItemStatus(itemStatus);
            item.setItemPicture(imageUrl);
            item.setUserId(userId);

            itemRepository.save(item);
            return ResponseEntity.ok("อัปโหลดสำเร็จเเล้ว");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // 4. แก้ไขข้อมูล (Update)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateItem(
            @PathVariable Long id,
            @RequestParam("itemName") String itemName,
            @RequestParam("itemDetail") String itemDetail,
            @RequestParam("itemStatus") String itemStatus,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        try {
            Item item = itemRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("ไม่พบรายการ ID: " + id));

            item.setItemName(itemName);
            item.setItemDetail(itemDetail);
            item.setItemStatus(itemStatus);

            if (file != null && !file.isEmpty()) {
                Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
                item.setItemPicture(uploadResult.get("secure_url").toString());
            }

            itemRepository.save(item);
            return ResponseEntity.ok("แก้ไขข้อมูลสำเร็จเเล้ว");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error ในการแก้ไข: " + e.getMessage());
        }
    }

    // --- 5. ลบข้อมูล (Delete) ---
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable Long id) {
        try {
            // เช็คก่อนว่ามีของไหม
            if (!itemRepository.existsById(id)) {
                return ResponseEntity.status(404).body("ไม่พบข้อมูลที่ต้องการลบ");
            }
            itemRepository.deleteById(id);
            return ResponseEntity.ok("ลบข้อมูลเรียบร้อยแล้ว");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("เกิดข้อผิดพลาดในการลบ: " + e.getMessage());
        }
    }
}