package com.plasticaudit.service;

import com.plasticaudit.entity.Industry;
import com.plasticaudit.repository.IndustryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * CO3 — Service layer for Industry entity with full @Transactional management.
 */
@Service
@Transactional
public class IndustryService {

    @Autowired
    private IndustryRepository industryRepository;

    public Industry saveIndustry(Industry industry) {
        return industryRepository.save(industry);
    }

    @Transactional(readOnly = true)
    public List<Industry> findAll() {
        return industryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Industry> findById(Long id) {
        return industryRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Industry> searchByName(String keyword) {
        return industryRepository.searchByName(keyword);
    }

    @Transactional(readOnly = true)
    public List<Industry> findBySector(String sector) {
        return industryRepository.findBySector(sector);
    }

    public void deleteById(Long id) {
        industryRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long getTotalCount() {
        return industryRepository.count();
    }
}
