package com.example.lostandfound.Repository;

import com.example.lostandfound.Model.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {

    // ค้นหาว่า Item ชิ้นนี้ถูกใครรับไป (ใช้แสดงหลักฐานในหน้ารายละเอียดไอเทม)
    Optional<Claim> findByItemId(Long itemId);

    // ดึงประวัติการรับของของนักศึกษาคนนี้ (เผื่อตรวจสอบย้อนหลัง)
    List<Claim> findByStudentIdOrderByClaimCreatedAtDesc(Long studentId);

    // ดึงประวัติการรับของที่ Staff คนนี้เป็นคนทำรายการ
    List<Claim> findByStaffId(Long staffId);

    // ดึงรายการ Claim ทั้งหมดเรียงตามล่าสุด
    List<Claim> findAllByOrderByClaimCreatedAtDesc();
}
