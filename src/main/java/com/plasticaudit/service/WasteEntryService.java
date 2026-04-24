package com.plasticaudit.service;

import com.plasticaudit.entity.WasteEntry;
import com.plasticaudit.jdbc.BatchWasteJdbcRepository;
import com.plasticaudit.repository.WasteEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * CO3 — Service layer for WasteEntry with @Transactional management.
 * CO1 — Delegates batch operations to BatchWasteJdbcRepository (JDBC).
 */
@Service
@Transactional
public class WasteEntryService {

    @Autowired
    private WasteEntryRepository wasteEntryRepository;

    @Autowired
    private BatchWasteJdbcRepository batchWasteJdbcRepository;

    public WasteEntry save(WasteEntry entry) {
        return wasteEntryRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public Optional<WasteEntry> findById(Long id) {
        return wasteEntryRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<WasteEntry> findByIndustryId(Long industryId) {
        return wasteEntryRepository.findByIndustryId(industryId);
    }

    @Transactional(readOnly = true)
    public List<WasteEntry> findByIndustryAndPeriod(Long industryId, LocalDate start, LocalDate end) {
        return wasteEntryRepository.findByIndustryIdAndEntryDateBetween(industryId, start, end);
    }

    @Transactional(readOnly = true)
    public List<WasteEntry> findAllRecent() {
        return wasteEntryRepository.findRecentEntries();
    }

    public void deleteById(Long id) {
        wasteEntryRepository.deleteById(id);
    }

    /**
     * CO1 — Batch import via raw JDBC PreparedStatement
     */
    public int batchImport(List<WasteEntry> entries) {
        return batchWasteJdbcRepository.batchInsertWasteEntries(entries);
    }

    @Transactional(readOnly = true)
    public List<Object[]> getGlobalSdgMetrics() {
        return wasteEntryRepository.getGlobalSdgMetrics();
    }

    @Transactional(readOnly = true)
    public long getTotalCount() {
        return wasteEntryRepository.count();
    }
}
