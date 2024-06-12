package com.example.kafka.repository;

import com.example.kafka.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepo extends JpaRepository<Account, Long> {
    @Query(value = "SELECT * FROM account WHERE id = ?1", nativeQuery = true)
    Optional<Account> findById(Long id);
}
