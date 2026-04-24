package com.plasticaudit.prediction;

/**
 * Immutable data point representing one month's aggregated waste data.
 * Used as input for the linear regression engine.
 */
public class WasteDataPoint {

    private final int monthIndex; // 0-based chronological index
    private final String monthLabel; // e.g. "2025-01"
    private final double generated;
    private final double recycled;
    private final double eliminated;

    public WasteDataPoint(int monthIndex, String monthLabel,
            double generated, double recycled, double eliminated) {
        this.monthIndex = monthIndex;
        this.monthLabel = monthLabel;
        this.generated = generated;
        this.recycled = recycled;
        this.eliminated = eliminated;
    }

    public int getMonthIndex() {
        return monthIndex;
    }

    public String getMonthLabel() {
        return monthLabel;
    }

    public double getGenerated() {
        return generated;
    }

    public double getRecycled() {
        return recycled;
    }

    public double getEliminated() {
        return eliminated;
    }

    public double getRecyclingRate() {
        return generated > 0 ? (recycled / generated) * 100.0 : 0.0;
    }

    public double getReductionRate() {
        return generated > 0 ? ((recycled + eliminated) / generated) * 100.0 : 0.0;
    }
}
