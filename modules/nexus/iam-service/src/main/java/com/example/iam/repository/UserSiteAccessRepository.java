package com.example.iam.repository;

import com.example.iam.entity.UserSiteId;
import com.example.iam.entity.UserSiteAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserSiteAccessRepository extends JpaRepository<UserSiteAccess, UserSiteId> {
    List<UserSiteAccess> findByIdUserId(UUID userId);

    List<UserSiteAccess> findByIdSiteId(UUID siteId);
}
