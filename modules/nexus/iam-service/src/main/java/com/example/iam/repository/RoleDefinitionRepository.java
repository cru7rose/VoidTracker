package com.example.iam.repository;

import com.example.iam.entity.RoleDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleDefinitionRepository extends JpaRepository<RoleDefinition, String> {
}
