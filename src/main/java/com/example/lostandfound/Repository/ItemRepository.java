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

    // แก้ไข Query ให้ค้นหาจาก ID ได้ด้วย
    @Query("SELECT i FROM Item i WHERE " +
            "LOWER(i.itemName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(i.itemDetail) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "CAST(i.itemId AS string) LIKE CONCAT('%', :keyword, '%') " + // <--- เพิ่มบรรทัดนี้
            "ORDER BY i.itemId DESC")
    List<Item> searchByKeyword(@Param("keyword") String keyword);

    List<Item> findAllByOrderByItemIdDesc();
}