package com.example.order_service.repository;

import com.example.order_service.entity.UserConfigEntity;
import com.example.order_service.entity.UserConfigId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserConfigRepository extends JpaRepository<UserConfigEntity, UserConfigId> {
}
