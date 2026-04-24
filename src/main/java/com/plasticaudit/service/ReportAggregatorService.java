package com.plasticaudit.service;

import com.plasticaudit.entity.AuditReport;
import com.plasticaudit.entity.Industry;
import com.plasticaudit.entity.User;
import com.plasticaudit.repository.AuditReportRepository;
import com.plasticaudit.repository.IndustryRepository;
import com.plasticaudit.repository.WasteEntryRepository;
import com.plasticaudit.socket.AlertServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * CO1 — Multithreaded Report Aggregator Service.
 * Uses @Async + ThreadPoolExecutor to aggregate waste reports for
 * all industries in parallel (configurable thread pool from
 * application.properties).
 * CO4 — Triggers socket-based real-time alert once reports are ready.
 */
@Service
public class ReportAggregatorService {

    private static final Logger log = LoggerFactory.getLogger(ReportAggregatorService.class);

    @Autowired
    private IndustryRepository industryRepository;

    @Autowired
    private WasteEntryRepository wasteEntryRepository;

    @Autowired
    private AuditReportRepository auditReportRepository;

    @Autowired
    private AlertServer alertServer;

    /**
     * CO1 — @Async runs this in the Spring-managed thread pool (ReportAggregator-N)
     * Aggregates all industries' waste data for the given period concurrently.
     */
    @Async
    @Transactional
    public CompletableFuture<List<AuditReport>> aggregateReportsForAllIndustries(
            LocalDate periodStart, LocalDate periodEnd, User requestedBy) {

        log.info("[Multithreading] Starting async report aggregation | Thread: {}",
                Thread.currentThread().getName());

        List<Industry> industries = industryRepository.findAll();
        List<AuditReport> generatedReports = new ArrayList<>();

        // CO1 — ThreadPoolExecutor for per-industry parallel processing
        ExecutorService executor = new ThreadPoolExecutor(
                4, 8, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
                r -> new Thread(r, "ReportWorker-" + System.nanoTime()));

        List<Future<AuditReport>> futures = new ArrayList<>();

        for (Industry industry : industries) {
            Future<AuditReport> future = executor.submit(() -> {
                log.debug("[ReportWorker] Processing industry '{}' on thread: {}",
                        industry.getName(), Thread.currentThread().getName());
                return buildReport(industry, periodStart, periodEnd, requestedBy);
            });
            futures.add(future);
        }

        for (Future<AuditReport> future : futures) {
            try {
                AuditReport report = future.get(30, TimeUnit.SECONDS);
                if (report != null) {
                    generatedReports.add(report);
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error("[Multithreading] Report aggregation task failed", e);
                Thread.currentThread().interrupt();
            }
        }

        executor.shutdown();
        auditReportRepository.saveAll(generatedReports);

        // CO4 — Alert socket: notify connected regulators
        alertServer.broadcastAlert("[AUDIT COMPLETE] Report aggregation finished for " +
                generatedReports.size() + " industries. Period: " +
                periodStart + " to " + periodEnd);

        log.info("[Multithreading] Async aggregation complete. {} reports generated.", generatedReports.size());
        return CompletableFuture.completedFuture(generatedReports);
    }

    private AuditReport buildReport(Industry industry, LocalDate start, LocalDate end, User requestedBy) {
        Double generated = wasteEntryRepository.sumGeneratedByIndustryAndPeriod(industry.getId(), start, end);
        Double recycled = wasteEntryRepository.sumRecycledByIndustryAndPeriod(industry.getId(), start, end);
        Double eliminated = wasteEntryRepository.sumEliminatedByIndustryAndPeriod(industry.getId(), start, end);

        generated = generated == null ? 0.0 : generated;
        recycled = recycled == null ? 0.0 : recycled;
        eliminated = eliminated == null ? 0.0 : eliminated;

        double reductionRate = (generated > 0) ? ((recycled + eliminated) / generated) * 100.0 : 0.0;
        double recyclingRatio = (generated > 0) ? (recycled / generated) * 100.0 : 0.0;

        AuditReport report = new AuditReport();
        report.setIndustry(industry);
        report.setGeneratedBy(requestedBy);
        report.setPeriodStart(start);
        report.setPeriodEnd(end);
        report.setTotalGeneratedKg(generated);
        report.setTotalRecycledKg(recycled);
        report.setTotalEliminatedKg(eliminated);
        report.setReductionRatePercent(reductionRate);
        report.setRecyclingRatioPercent(recyclingRatio);
        report.setStatus(AuditReport.ReportStatus.DRAFT);
        report.setRemarks("Auto-generated by multithreaded aggregator");
        return report;
    }

    @Transactional(readOnly = true)
    public List<AuditReport> findAllReports() {
        return auditReportRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<AuditReport> findReportsByIndustry(Long industryId) {
        return auditReportRepository.findReportsByIndustry(industryId);
    }

    @Transactional
    public void updateReportStatus(Long reportId, AuditReport.ReportStatus status) {
        auditReportRepository.findById(reportId).ifPresent(r -> {
            r.setStatus(status);
            auditReportRepository.save(r);
        });
    }
}
