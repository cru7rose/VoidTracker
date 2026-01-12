package com.example.iam.repository;

import com.example.iam.entity.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SiteRepository extends JpaRepository<Site, UUID> {
    List<Site> findByOrgId(UUID orgId);
}
