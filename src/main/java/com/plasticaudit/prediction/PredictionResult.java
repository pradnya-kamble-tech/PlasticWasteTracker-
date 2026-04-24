package com.plasticaudit.prediction;

import java.util.List;

/**
 * Full prediction output returned by PredictionService.
 * Carries historical trend, next-month forecast, risk level, and
 * recommendations.
 */
public class PredictionResult {

    public enum RiskLevel {
        LOW, MEDIUM, HIGH
    }

    // ── Metadata ────────────────────────────────────────────────
    private Long industryId;
    private String industryName;
    private boolean sufficientData; // false when < 3 data points
    private String insufficientDataMessage;

    // ── Historical series ────────────────────────────────────────
    private List<WasteDataPoint> historicalData;

    // ── Predictions (next month) ─────────────────────────────────
    private double predictedGenerated;
    private double predictedRecycled;
    private double predictedEliminated;
    private double predictedRecyclingRate;
    private double predictedReductionRate;

    // ── Trend analytics ─────────────────────────────────────────
    private double growthRatePercent; // % change in waste generation
    private double recyclingEfficiencyTrend; // % change in recycling rate
    private double averageRecyclingRate;
    private double averageReductionRate;
    private RiskLevel riskLevel;
    private String riskReason;

    // ── Recommendations ─────────────────────────────────────────
    private List<String> recommendations;

    // ── Serialised labels for Chart.js ──────────────────────────
    private List<String> chartLabels;
    private List<Double> chartGenerated;
    private List<Double> chartRecycled;

    // ── Constructors ────────────────────────────────────────────
    public PredictionResult() {
    }

    // ── Getters & Setters ────────────────────────────────────────
    public Long getIndustryId() {
        return industryId;
    }

    public void setIndustryId(Long v) {
        this.industryId = v;
    }

    public String getIndustryName() {
        return industryName;
    }

    public void setIndustryName(String v) {
        this.industryName = v;
    }

    public boolean isSufficientData() {
        return sufficientData;
    }

    public void setSufficientData(boolean v) {
        this.sufficientData = v;
    }

    public String getInsufficientDataMessage() {
        return insufficientDataMessage;
    }

    public void setInsufficientDataMessage(String v) {
        this.insufficientDataMessage = v;
    }

    public List<WasteDataPoint> getHistoricalData() {
        return historicalData;
    }

    public void setHistoricalData(List<WasteDataPoint> v) {
        this.historicalData = v;
    }

    public double getPredictedGenerated() {
        return predictedGenerated;
    }

    public void setPredictedGenerated(double v) {
        this.predictedGenerated = v;
    }

    public double getPredictedRecycled() {
        return predictedRecycled;
    }

    public void setPredictedRecycled(double v) {
        this.predictedRecycled = v;
    }

    public double getPredictedEliminated() {
        return predictedEliminated;
    }

    public void setPredictedEliminated(double v) {
        this.predictedEliminated = v;
    }

    public double getPredictedRecyclingRate() {
        return predictedRecyclingRate;
    }

    public void setPredictedRecyclingRate(double v) {
        this.predictedRecyclingRate = v;
    }

    public double getPredictedReductionRate() {
        return predictedReductionRate;
    }

    public void setPredictedReductionRate(double v) {
        this.predictedReductionRate = v;
    }

    public double getGrowthRatePercent() {
        return growthRatePercent;
    }

    public void setGrowthRatePercent(double v) {
        this.growthRatePercent = v;
    }

    public double getRecyclingEfficiencyTrend() {
        return recyclingEfficiencyTrend;
    }

    public void setRecyclingEfficiencyTrend(double v) {
        this.recyclingEfficiencyTrend = v;
    }

    public double getAverageRecyclingRate() {
        return averageRecyclingRate;
    }

    public void setAverageRecyclingRate(double v) {
        this.averageRecyclingRate = v;
    }

    public double getAverageReductionRate() {
        return averageReductionRate;
    }

    public void setAverageReductionRate(double v) {
        this.averageReductionRate = v;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel v) {
        this.riskLevel = v;
    }

    public String getRiskReason() {
        return riskReason;
    }

    public void setRiskReason(String v) {
        this.riskReason = v;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> v) {
        this.recommendations = v;
    }

    public List<String> getChartLabels() {
        return chartLabels;
    }

    public void setChartLabels(List<String> v) {
        this.chartLabels = v;
    }

    public List<Double> getChartGenerated() {
        return chartGenerated;
    }

    public void setChartGenerated(List<Double> v) {
        this.chartGenerated = v;
    }

    public List<Double> getChartRecycled() {
        return chartRecycled;
    }

    public void setChartRecycled(List<Double> v) {
        this.chartRecycled = v;
    }
}
