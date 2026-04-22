package com.example.lostandfound.Repository;

import com.example.lostandfound.Model.Claim;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimRepository extends JpaRepository<Claim, Long> {

}
