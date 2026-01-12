package com.example.planning_service.repository;

import com.example.planning_service.entity.PostalCodeRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostalCodeRuleRepository extends JpaRepository<PostalCodeRuleEntity, UUID> {
    
    @Query("SELECT r FROM PostalCodeRuleEntity r JOIN FETCH r.zone z WHERE z.countryCode = :countryCode ORDER BY r.priority DESC")
    List<PostalCodeRuleEntity> findAllByCountryCode(@Param("countryCode") String countryCode);
}
