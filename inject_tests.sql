USE plastic_waste_db;

-- Insert Normal Waste Entry Test (Test Case 2)
INSERT INTO waste_entries (industry_id, entry_date, plastic_generated_kg, plastic_recycled_kg, plastic_eliminated_kg, entry_type, notes, verified)
VALUES (1, '2026-04-10', 500, 200, 50, 'MONTHLY', 'Standard monthly entry', 0);

-- Insert Zero Waste Entry Test (Test Case 3)
INSERT INTO waste_entries (industry_id, entry_date, plastic_generated_kg, plastic_recycled_kg, plastic_eliminated_kg, entry_type, notes, verified)
VALUES (1, '2026-04-15', 0, 0, 0, 'DAILY', 'Zero Waste Day - Plant Maintenance', 0);

-- Insert High Volume / Annual Entry Test (Test Case 4)
INSERT INTO waste_entries (industry_id, entry_date, plastic_generated_kg, plastic_recycled_kg, plastic_eliminated_kg, entry_type, notes, verified)
VALUES (2, '2026-04-20', 99999.5, 50000.5, 10000.0, 'ANNUAL', 'Annual corporate payload', 0);

-- Insert Report Generation Test (Admin)
INSERT IGNORE INTO audit_reports (industry_id, generated_by_user_id, period_start, period_end, total_generated_kg, total_recycled_kg, total_eliminated_kg, reduction_rate_percent, recycling_ratio_percent, status, remarks)
VALUES (1, 1, '2026-04-01', '2026-04-30', 500.0, 200.0, 50.0, 50.0, 40.0, 0, 'Auto-generated test report');

-- Update Report Status Test (status VERIFIED)
INSERT IGNORE INTO audit_reports (industry_id, generated_by_user_id, period_start, period_end, total_generated_kg, total_recycled_kg, total_eliminated_kg, reduction_rate_percent, recycling_ratio_percent, status, remarks)
VALUES (2, 1, '2026-03-01', '2026-03-31', 1000.0, 500.0, 100.0, 60.0, 50.0, 2, 'Verified by admin');
