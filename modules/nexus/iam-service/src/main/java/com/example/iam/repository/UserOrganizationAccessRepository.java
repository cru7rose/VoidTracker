package com.example.iam.repository;

import com.example.iam.entity.UserOrgId;
import com.example.iam.entity.UserOrganizationAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserOrganizationAccessRepository extends JpaRepository<UserOrganizationAccess, UserOrgId> {
    List<UserOrganizationAccess> findByIdUserId(UUID userId);

    List<UserOrganizationAccess> findByIdOrgId(UUID orgId);
}
