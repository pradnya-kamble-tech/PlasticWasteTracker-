package com.plasticaudit.repository;

import com.plasticaudit.entity.WasteEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WasteEntryRepository extends JpaRepository<WasteEntry, Long> {

        @Query("SELECT w FROM WasteEntry w JOIN FETCH w.industry WHERE w.industry.id = :industryId")
        List<WasteEntry> findByIndustryId(@Param("industryId") Long industryId);

        List<WasteEntry> findByIndustryIdAndEntryDateBetween(Long industryId, LocalDate start, LocalDate end);

        // CO3: HQL — sum total generated for an industry in a date range
        @Query("SELECT SUM(w.plasticGeneratedKg) FROM WasteEntry w WHERE w.industry.id = :industryId " +
                        "AND w.entryDate BETWEEN :start AND :end")
        Double sumGeneratedByIndustryAndPeriod(@Param("industryId") Long industryId,
                        @Param("start") LocalDate start,
                        @Param("end") LocalDate end);

        @Query("SELECT SUM(w.plasticRecycledKg) FROM WasteEntry w WHERE w.industry.id = :industryId " +
                        "AND w.entryDate BETWEEN :start AND :end")
        Double sumRecycledByIndustryAndPeriod(@Param("industryId") Long industryId,
                        @Param("start") LocalDate start,
                        @Param("end") LocalDate end);

        @Query("SELECT SUM(w.plasticEliminatedKg) FROM WasteEntry w WHERE w.industry.id = :industryId " +
                        "AND w.entryDate BETWEEN :start AND :end")
        Double sumEliminatedByIndustryAndPeriod(@Param("industryId") Long industryId,
                        @Param("start") LocalDate start,
                        @Param("end") LocalDate end);

        // CO3: HQL — global SDG metric aggregate
        @Query("SELECT SUM(w.plasticGeneratedKg), SUM(w.plasticRecycledKg), SUM(w.plasticEliminatedKg) FROM WasteEntry w")
        List<Object[]> getGlobalSdgMetrics();

        // HQL — recent entries for dashboard
        @Query("SELECT w FROM WasteEntry w JOIN FETCH w.industry ORDER BY w.entryDate DESC")
        List<WasteEntry> findRecentEntries();
}
