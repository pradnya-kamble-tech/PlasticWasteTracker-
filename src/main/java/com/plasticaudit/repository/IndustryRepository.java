package com.plasticaudit.repository;

import com.plasticaudit.entity.Industry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IndustryRepository extends JpaRepository<Industry, Long> {

    Optional<Industry> findByRegistrationNo(String registrationNo);

    List<Industry> findBySector(String sector);

    // CO3: HQL — search by name keyword
    @Query("SELECT i FROM Industry i WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Industry> searchByName(@Param("keyword") String keyword);

    // HQL — find industries with target set (SDG metric tracking)
    @Query("SELECT i FROM Industry i WHERE i.annualPlasticTargetKg IS NOT NULL ORDER BY i.annualPlasticTargetKg DESC")
    List<Industry> findIndustriesWithTargets();
}
