package com.example.lostandfound.Repository;

import com.example.lostandfound.Model.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Optional<Item> findItemByItemId(Long itemId);
}
