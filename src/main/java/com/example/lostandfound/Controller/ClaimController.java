package com.example.lostandfound.Controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.lostandfound.Model.Claim;
import com.example.lostandfound.Repository.ClaimRepository;
import com.example.lostandfound.Repository.ItemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/claims")
@CrossOrigin(origins = "http://localhost:3000")
public class ClaimController {

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private Cloudinary cloudinary;

    @PostMapping("/upload")
    public ResponseEntity<?> createClaim(
            @RequestParam("evidenceFile") MultipartFile evidenceFile, // รูปตอนรับของ
            @RequestParam("identityFile") MultipartFile identityFile, // รูปบัตร (แทนลายเซ็น)
            @RequestParam("claimData") String claimDataJson) {
        try {
            // 1. อัปโหลดรูปที่ 1: หลักฐานการส่งมอบ
            Map uploadEvidence = cloudinary.uploader().upload(evidenceFile.getBytes(),
                    ObjectUtils.asMap("folder", "claims/evidence"));
            String evidenceUrl = uploadEvidence.get("secure_url").toString();

            // 2. อัปโหลดรูปที่ 2: บัตรประชาชน/นักศึกษา (เก็บในช่อง Evidence_Signature)
            Map uploadIdentity = cloudinary.uploader().upload(identityFile.getBytes(),
                    ObjectUtils.asMap("folder", "claims/identity"));
            String identityUrl = uploadIdentity.get("secure_url").toString();

            // 3. แปลง JSON String เป็น Object Claim
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.findAndRegisterModules();
            Claim claim = objectMapper.readValue(claimDataJson, Claim.class);

            // 4. บันทึก URL ทั้งสองรูปลงใน Entity
            claim.setEvidencePicture(evidenceUrl);
            claim.setEvidenceSignature(identityUrl); // เอา URL รูปบัตรใส่ช่องนี้เลย

            claimRepository.save(claim);

            // 5. อัปเดตสถานะของ Item เป็น 'CLAIM'
            itemRepository.findById(claim.getItemId()).ifPresent(item -> {
                item.setItemStatus("CLAIM");
                itemRepository.save(item);
            });

            return ResponseEntity.ok("บันทึกหลักฐานและรูปบัตรสำเร็จ");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("เกิดข้อผิดพลาด: " + e.getMessage());
        }
    }
}