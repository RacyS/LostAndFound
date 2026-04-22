package com.example.lostandfound.Repository;

import com.example.lostandfound.Model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    Optional<Item> findItemByItemId(Long itemId);

    // แก้ไข Query ให้ตรงกับชื่อตัวแปรใน Class Item
    @Query("SELECT i FROM Item i WHERE " +
            "LOWER(i.itemName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(i.itemDetail) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY i.itemId DESC")
    List<Item> searchByKeyword(@Param("keyword") String keyword);

    // ดึงทั้งหมดเรียงตาม itemId จากมากไปน้อย (ของใหม่ขึ้นก่อน)
    List<Item> findAllByOrderByItemIdDesc();
}