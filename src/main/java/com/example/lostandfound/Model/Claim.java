package com.example.lostandfound.Model;

import jakarta.persistence.*;

import java.sql.Timestamp;
@Entity
@Table(name = "claim")
public class Claim {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long claimId;
    @Column(name = "Claim_Description")
    private String claimDescripton;
    @Column(name = "Claimed_at")
    private Timestamp claimAt;
    @Column(name = "Evidence_Picture")
    private String evidencePicture;
    @Column(name = "Evidence_Signature", columnDefinition = "TEXT")
    private String evidenceSignature;
    @Column(name = "Student_ID")
    private Long studentId;
    @Column(name = "Staff_ID")
    private Long staffId;
    @Column(name = "Chat_ID")
    private Long chatId;

    public Long getClaimId() {
        return claimId;
    }

    public void setClaimId(Long claimId) {
        this.claimId = claimId;
    }

    public String getClaimDescripton() {
        return claimDescripton;
    }

    public void setClaimDescripton(String claimDescripton) {
        this.claimDescripton = claimDescripton;
    }

    public Timestamp getClaimAt() {
        return claimAt;
    }

    public void setClaimAt(Timestamp claimAt) {
        this.claimAt = claimAt;
    }

    public String getEvidencePicture() {
        return evidencePicture;
    }

    public void setEvidencePicture(String evidencePicture) {
        this.evidencePicture = evidencePicture;
    }

    public String getEvidenceSignature() {
        return evidenceSignature;
    }

    public void setEvidenceSignature(String evidenceSignature) {
        this.evidenceSignature = evidenceSignature;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }
}
