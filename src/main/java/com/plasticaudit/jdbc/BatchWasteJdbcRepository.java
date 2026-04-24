package com.plasticaudit.jdbc;

import com.plasticaudit.entity.WasteEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

/**
 * CO1 — Raw JDBC with PreparedStatement for batch waste entry operations.
 * This demonstrates direct JDBC usage alongside Spring Data JPA (Hibernate).
 * Used for bulk imports of waste data (e.g., CSV batch upload).
 */
@Repository
public class BatchWasteJdbcRepository {

    private static final Logger log = LoggerFactory.getLogger(BatchWasteJdbcRepository.class);

    private static final String BATCH_INSERT_SQL = "INSERT INTO waste_entries (industry_id, entry_date, plastic_generated_kg, "
            +
            "plastic_recycled_kg, plastic_eliminated_kg, entry_type, notes, verified) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    @Autowired
    private DataSource dataSource;

    /**
     * CO1 — Batch insert waste entries using PreparedStatement.addBatch()
     * 
     * @param entries list of WasteEntry objects to batch insert
     * @return number of rows inserted
     */
    public int batchInsertWasteEntries(List<WasteEntry> entries) {
        int totalInserted = 0;
        log.info("[JDBC Batch] Starting batch insert of {} waste entries", entries.size());

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false); // Manual transaction

            try (PreparedStatement ps = conn.prepareStatement(BATCH_INSERT_SQL)) {
                int batchCount = 0;

                for (WasteEntry entry : entries) {
                    ps.setLong(1, entry.getIndustry().getId());
                    ps.setDate(2, Date.valueOf(entry.getEntryDate()));
                    ps.setDouble(3, entry.getPlasticGeneratedKg());
                    ps.setDouble(4, entry.getPlasticRecycledKg());
                    ps.setDouble(5, entry.getPlasticEliminatedKg());
                    ps.setString(6, entry.getEntryType().name());
                    ps.setString(7, entry.getNotes());
                    ps.setBoolean(8, entry.isVerified());
                    ps.addBatch();
                    batchCount++;

                    // Execute every 50 rows
                    if (batchCount % 50 == 0) {
                        int[] counts = ps.executeBatch();
                        for (int c : counts)
                            totalInserted += (c > 0 ? c : 0);
                        log.debug("[JDBC Batch] Executed batch of 50 rows");
                    }
                }

                // Execute remaining rows
                int[] counts = ps.executeBatch();
                for (int c : counts)
                    totalInserted += (c > 0 ? c : 0);
                conn.commit();

                log.info("[JDBC Batch] Completed. Total rows inserted: {}", totalInserted);
            } catch (SQLException ex) {
                conn.rollback();
                log.error("[JDBC Batch] Batch insert failed, rolled back", ex);
                throw new RuntimeException("Batch insert failed: " + ex.getMessage(), ex);
            }
        } catch (SQLException ex) {
            log.error("[JDBC Batch] Failed to acquire connection", ex);
            throw new RuntimeException("JDBC connection error: " + ex.getMessage(), ex);
        }

        return totalInserted;
    }

    /**
     * CO1 — Raw JDBC PreparedStatement: get summary stats for an industry.
     * Falls back to JDBC when Hibernate doesn't serve the need.
     */
    public double[] getIndustrySummary(Long industryId) {
        String sql = "SELECT SUM(plastic_generated_kg), SUM(plastic_recycled_kg), SUM(plastic_eliminated_kg) " +
                "FROM waste_entries WHERE industry_id = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, industryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new double[] {
                            rs.getDouble(1),
                            rs.getDouble(2),
                            rs.getDouble(3)
                    };
                }
            }
        } catch (SQLException ex) {
            log.error("[JDBC] Failed to fetch industry summary for id={}", industryId, ex);
        }
        return new double[] { 0, 0, 0 };
    }
}
