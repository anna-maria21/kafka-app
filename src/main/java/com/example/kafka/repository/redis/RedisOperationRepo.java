package com.example.kafka.repository.redis;

import com.example.kafka.entity.Operation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RedisOperationRepo extends JpaRepository<Operation, Long> {
}
