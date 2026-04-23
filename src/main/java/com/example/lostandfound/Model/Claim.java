package com.example.lostandfound.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "claim") // ใช้ชื่อตารางตามภาพ
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Claim_ID")
    private Long claimId;

    @Column(name = "Claim_Description", columnDefinition = "TEXT")
    private String claimDescription;

    // datetime ใน DB
    @Column(name = "Claimed_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime claimedAt;

    // timestamp ใน DB ที่มี current_timestamp()
    @CreationTimestamp
    @Column(name = "Claim_Created_at", nullable = false, updatable = false)
    private LocalDateTime claimCreatedAt;

    // varchar(255) ตามภาพ
    @Column(name = "Evidence_Picture", length = 255)
    private String evidencePicture;

    // varchar(255) ตามภาพ
    @Column(name = "Evidence_Signature", length = 255)
    private String evidenceSignature;

    // bigint(20) ใน DB แมตช์กับ Long ใน Java
    @Column(name = "Student_ID")
    private Long studentId;

    @Column(name = "Staff_ID")
    private Long staffId;

    @Column(name = "Item_ID")
    private Long itemId;

    @Column(name = "Chat_ID")
    private Long chatId;

    // --- Getters & Setters ---

    public Long getClaimId() { return claimId; }
    public void setClaimId(Long claimId) { this.claimId = claimId; }

    public String getClaimDescription() { return claimDescription; }
    public void setClaimDescription(String claimDescription) { this.claimDescription = claimDescription; }

    public LocalDateTime getClaimedAt() { return claimedAt; }
    public void setClaimedAt(LocalDateTime claimedAt) { this.claimedAt = claimedAt; }

    public LocalDateTime getClaimCreatedAt() { return claimCreatedAt; }
    public void setClaimCreatedAt(LocalDateTime claimCreatedAt) { this.claimCreatedAt = claimCreatedAt; }

    public String getEvidencePicture() { return evidencePicture; }
    public void setEvidencePicture(String evidencePicture) { this.evidencePicture = evidencePicture; }

    public String getEvidenceSignature() { return evidenceSignature; }
    public void setEvidenceSignature(String evidenceSignature) { this.evidenceSignature = evidenceSignature; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Long getStaffId() { return staffId; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public Long getChatId() { return chatId; }
    public void setChatId(Long chatId) { this.chatId = chatId; }
}