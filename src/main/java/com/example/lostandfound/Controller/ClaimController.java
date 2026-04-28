package com.example.lostandfound.Controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.lostandfound.Model.Claim;
import com.example.lostandfound.Model.User;
import com.example.lostandfound.Repository.ClaimRepository;
import com.example.lostandfound.Repository.ItemRepository;
import com.example.lostandfound.Repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api") // 🟢 เปลี่ยนมาใช้ /api เป็นตัวตั้งต้น
@CrossOrigin(origins = "http://localhost:3000")
public class ClaimController {

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private ItemRepository itemRepository;

    // 🟢 1. เพิ่ม UserRepository เพื่อให้หา User จากรหัส 13 หลักได้
    @Autowired
    private UserRepository userRepository;

    // 🟢 2. เรียกใช้ตัวแปร cloudinary ให้ถูกชื่อ (ตามที่คุณประกาศไว้ด้านบน)
    @Autowired
    private Cloudinary cloudinary;

    // ==========================================
    // 1. ส่วนของหน้า Claim History (ประวัติการเคลม)
    // ==========================================

    @GetMapping("/claimHistory")
    public ResponseEntity<List<Claim>> getAllClaims() {
        List<Claim> claims = claimRepository.findAll();
        return ResponseEntity.ok(claims);
    }

    @DeleteMapping("/claimHistory/{id}")
    public ResponseEntity<?> deleteClaim(@PathVariable Long id) {
        if (claimRepository.existsById(id)) {
            claimRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/claimHistory/{id}")
    public ResponseEntity<?> updateClaim(@PathVariable Long id, @RequestBody Claim updatedClaim) {
        Optional<Claim> existingClaimOpt = claimRepository.findById(id);

        if (existingClaimOpt.isPresent()) {
            Claim existingClaim = existingClaimOpt.get();
            // อัปเดตข้อมูลทั่วไป
            existingClaim.setItemId(updatedClaim.getItemId());
            existingClaim.setStudentId(updatedClaim.getStudentId());
            existingClaim.setStaffId(updatedClaim.getStaffId());
            existingClaim.setChatId(updatedClaim.getChatId());
            existingClaim.setClaimDescription(updatedClaim.getClaimDescription());

            // 🟢 เพิ่มส่วนนี้: อัปเดตวันเวลาที่ส่งมอบ
            if (updatedClaim.getClaimedAt() != null) {
                existingClaim.setClaimedAt(updatedClaim.getClaimedAt());
            }

            // อัปเดตรูปภาพ
            if (updatedClaim.getEvidencePicture() != null) {
                existingClaim.setEvidencePicture(updatedClaim.getEvidencePicture());
            }
            if (updatedClaim.getEvidenceSignature() != null) {
                existingClaim.setEvidenceSignature(updatedClaim.getEvidenceSignature());
            }

            claimRepository.save(existingClaim);
            return ResponseEntity.ok(existingClaim);
        }
        return ResponseEntity.notFound().build();
    }

    // ==========================================
    // 2. ส่วนของหน้า Upload Evidence (บันทึกเคลมใหม่)
    // ==========================================

    // ใน ClaimController.java
    @PostMapping("/claims/upload")
    public ResponseEntity<?> createClaim(
            @RequestParam("evidenceFile") MultipartFile evidenceFile,
            @RequestParam("identityFile") MultipartFile identityFile,
            @RequestParam("claimData") String claimDataJson) {
        try {
            // 1. อัปโหลดรูป (ปกติ)
            Map uploadEvidence = cloudinary.uploader().upload(evidenceFile.getBytes(), ObjectUtils.asMap("folder", "claims/evidence"));
            Map uploadIdentity = cloudinary.uploader().upload(identityFile.getBytes(), ObjectUtils.asMap("folder", "claims/identity"));

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.findAndRegisterModules();

            // 2. อ่านข้อมูลจาก JSON
            Map<String, Object> map = objectMapper.readValue(claimDataJson, Map.class);

            // 🟢 วิธีแก้: รับ Student ID มาตรงๆ (เลข 1, 2, 3) โดยไม่ต้องไปหา findByStudentId อีก
            // ถ้า React ส่ง ID ลำดับมาแล้ว ให้ใช้ค่านั้นได้เลย
            Long studentId = Long.parseLong(map.get("studentId").toString());

            Claim claim = objectMapper.readValue(claimDataJson, Claim.class);

            // เซ็ตค่าต่างๆ ให้ครบ
            claim.setStudentId(studentId); // บันทึกเลข ID ลำดับ (เช่น 1) ลงตาราง
            claim.setEvidencePicture(uploadEvidence.get("secure_url").toString());
            claim.setEvidenceSignature(uploadIdentity.get("secure_url").toString());

            claimRepository.save(claim);

            // อัปเดตสถานะ Item
            itemRepository.findById(claim.getItemId()).ifPresent(item -> {
                item.setItemStatus("CLAIM");
                itemRepository.save(item);
            });

            return ResponseEntity.ok("บันทึกสำเร็จ (เก็บด้วย ID ลำดับ)");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}