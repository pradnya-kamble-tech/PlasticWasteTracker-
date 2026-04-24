package com.plasticaudit.prediction;

import com.plasticaudit.entity.Industry;
import com.plasticaudit.entity.WasteEntry;
import com.plasticaudit.repository.IndustryRepository;
import com.plasticaudit.repository.WasteEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI Prediction Service — uses Simple Linear Regression (no external ML
 * libraries).
 *
 * Algorithm:
 * y = a + b*x where x = month index (0,1,2,...), y = waste value
 * b = (n*Σxy - Σx*Σy) / (n*Σx² - (Σx)²)
 * a = (Σy - b*Σx) / n
 *
 * Also computes: growth rate, recycling efficiency trend, risk level,
 * recommendations.
 */
@Service
@Transactional(readOnly = true)
public class PredictionService {

    private static final Logger log = LoggerFactory.getLogger(PredictionService.class);
    private static final int MIN_DATA_POINTS = 3;
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("MMM yyyy");

    @Autowired
    private WasteEntryRepository wasteEntryRepository;

    @Autowired
    private IndustryRepository industryRepository;

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Full prediction + trend analysis for an industry.
     */
    public PredictionResult predictNextMonthWaste(Long industryId) {
        PredictionResult result = new PredictionResult();

        if (industryId == null) {
            result.setSufficientData(false);
            result.setInsufficientDataMessage("Industry ID must not be null.");
            return result;
        }

        Industry industry = industryRepository.findById(industryId).orElse(null);
        if (industry == null) {
            result.setSufficientData(false);
            result.setInsufficientDataMessage("Industry not found.");
            return result;
        }

        result.setIndustryId(industryId);
        result.setIndustryName(industry.getName());

        List<WasteDataPoint> dataPoints = buildChronologicalDataPoints(industryId);

        if (dataPoints.size() < MIN_DATA_POINTS) {
            result.setSufficientData(false);
            result.setInsufficientDataMessage(
                    "Not enough data for prediction. Minimum " + MIN_DATA_POINTS +
                            " monthly entries required. Currently: " + dataPoints.size() + " entr" +
                            (dataPoints.size() == 1 ? "y" : "ies") + " found.");
            result.setHistoricalData(dataPoints);
            setChartData(result, dataPoints);
            return result;
        }

        result.setSufficientData(true);
        result.setHistoricalData(dataPoints);

        // ── Linear regression predictions ──
        double[] xArr = buildXArray(dataPoints);
        double[] genArr = dataPoints.stream().mapToDouble(WasteDataPoint::getGenerated).toArray();
        double[] recArr = dataPoints.stream().mapToDouble(WasteDataPoint::getRecycled).toArray();
        double[] elimArr = dataPoints.stream().mapToDouble(WasteDataPoint::getEliminated).toArray();

        int nextX = dataPoints.size(); // next time index
        double predGen = Math.max(0, linearPredict(xArr, genArr, nextX));
        double predRec = Math.max(0, linearPredict(xArr, recArr, nextX));
        double predElim = Math.max(0, linearPredict(xArr, elimArr, nextX));
        // Ensure recycled doesn't exceed predicted generated
        predRec = Math.min(predRec, predGen);
        predElim = Math.min(predElim, predGen - predRec);

        result.setPredictedGenerated(round2(predGen));
        result.setPredictedRecycled(round2(predRec));
        result.setPredictedEliminated(round2(predElim));
        result.setPredictedRecyclingRate(round2(predGen > 0 ? (predRec / predGen) * 100.0 : 0));
        result.setPredictedReductionRate(round2(predGen > 0 ? ((predRec + predElim) / predGen) * 100.0 : 0));

        // ── Trend analytics ──
        computeTrends(result, dataPoints);

        // ── Risk level ──
        assessRisk(result);

        // ── Recommendations ──
        result.setRecommendations(buildRecommendations(result));

        // ── Chart data ──
        setChartData(result, dataPoints);

        log.info("[PredictionService] Prediction complete for industry '{}': predicted gen={} kg, rec={} kg",
                industry.getName(), predGen, predRec);

        return result;
    }

    /**
     * Lightweight trend summary — subset of predictNextMonthWaste, used for
     * dashboard widgets.
     */
    public PredictionResult getTrendAnalysis(Long industryId) {
        return predictNextMonthWaste(industryId);
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    /**
     * Aggregate WasteEntry rows by YearMonth, build chronological list.
     */
    private List<WasteDataPoint> buildChronologicalDataPoints(Long industryId) {
        List<WasteEntry> entries = wasteEntryRepository.findByIndustryId(industryId);

        // Group by YearMonth
        Map<YearMonth, double[]> monthMap = new TreeMap<>();
        for (WasteEntry e : entries) {
            YearMonth ym = YearMonth.from(e.getEntryDate());
            monthMap.merge(ym, new double[] {
                    e.getPlasticGeneratedKg(),
                    e.getPlasticRecycledKg(),
                    e.getPlasticEliminatedKg()
            }, (a, b) -> new double[] { a[0] + b[0], a[1] + b[1], a[2] + b[2] });
        }

        List<WasteDataPoint> result = new ArrayList<>();
        int idx = 0;
        for (Map.Entry<YearMonth, double[]> entry : monthMap.entrySet()) {
            double[] vals = entry.getValue();
            result.add(new WasteDataPoint(idx++,
                    entry.getKey().format(MONTH_FMT),
                    vals[0], vals[1], vals[2]));
        }
        return result;
    }

    /** Build x array [0,1,2,...,n-1] */
    private double[] buildXArray(List<WasteDataPoint> points) {
        double[] x = new double[points.size()];
        for (int i = 0; i < x.length; i++)
            x[i] = i;
        return x;
    }

    /**
     * OLS Simple Linear Regression — predict y at xPredict.
     */
    private double linearPredict(double[] x, double[] y, int xPredict) {
        int n = x.length;
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < n; i++) {
            sumX += x[i];
            sumY += y[i];
            sumXY += x[i] * y[i];
            sumX2 += x[i] * x[i];
        }
        double denom = n * sumX2 - sumX * sumX;
        if (denom == 0)
            return sumY / n; // flat line
        double b = (n * sumXY - sumX * sumY) / denom;
        double a = (sumY - b * sumX) / n;
        return a + b * xPredict;
    }

    /** Compute growth rate and recycling efficiency trend */
    private void computeTrends(PredictionResult result, List<WasteDataPoint> points) {
        int n = points.size();
        double avgRec = points.stream().mapToDouble(WasteDataPoint::getRecyclingRate).average().orElse(0);
        double avgRed = points.stream().mapToDouble(WasteDataPoint::getReductionRate).average().orElse(0);
        result.setAverageRecyclingRate(round2(avgRec));
        result.setAverageReductionRate(round2(avgRed));

        // Growth rate = % change from first to last month's generation
        double firstGen = points.get(0).getGenerated();
        double lastGen = points.get(n - 1).getGenerated();
        double growthRate = (firstGen > 0) ? ((lastGen - firstGen) / firstGen) * 100.0 : 0;
        result.setGrowthRatePercent(round2(growthRate));

        // Recycling efficiency trend = first vs last recycling rate
        double firstRecRate = points.get(0).getRecyclingRate();
        double lastRecRate = points.get(n - 1).getRecyclingRate();
        double recTrend = lastRecRate - firstRecRate;
        result.setRecyclingEfficiencyTrend(round2(recTrend));
    }

    /**
     * Classify risk based on growth rate, recycling rate, and predicted reduction
     */
    private void assessRisk(PredictionResult result) {
        double avgRec = result.getAverageRecyclingRate();
        double growth = result.getGrowthRatePercent();
        double predRedRate = result.getPredictedReductionRate();

        if (avgRec >= 50 && growth <= 5 && predRedRate >= 40) {
            result.setRiskLevel(PredictionResult.RiskLevel.LOW);
            result.setRiskReason("Strong recycling rate and stable waste generation. Keep up the good work.");
        } else if (avgRec >= 25 || (growth > 5 && growth <= 20)) {
            result.setRiskLevel(PredictionResult.RiskLevel.MEDIUM);
            result.setRiskReason("Moderate recycling performance or rising waste generation trend detected.");
        } else {
            result.setRiskLevel(PredictionResult.RiskLevel.HIGH);
            result.setRiskReason("Low recycling rate or high waste growth. Immediate corrective action required.");
        }
    }

    /** Contextual recommendation engine */
    private List<String> buildRecommendations(PredictionResult result) {
        List<String> recs = new ArrayList<>();
        double growth = result.getGrowthRatePercent();
        double recTrend = result.getRecyclingEfficiencyTrend();
        double avgRec = result.getAverageRecyclingRate();
        double predRec = result.getPredictedRecyclingRate();
        double predGen = result.getPredictedGenerated();

        if (growth > 10) {
            recs.add("⚠️ Waste generation is increasing at " + round2(growth) +
                    "%. Audit your production processes for single-use plastic substitutes.");
        }
        if (growth > 20) {
            recs.add(
                    "🚨 Critical growth rate detected. Consider mandatory plastic reduction targets for next quarter.");
        }
        if (recTrend < -5) {
            recs.add("📉 Your recycling rate dropped by " + Math.abs(round2(recTrend)) +
                    "% over time. Consider increasing segregation stations and recycling vendor partners.");
        }
        if (avgRec < 30) {
            recs.add("♻️ Average recycling rate is below 30%. Invest in on-site material recovery facilities (MRFs).");
        }
        if (predRec < 20) {
            recs.add("🔴 Predicted next-month recycling is critically low (" + round2(predRec) +
                    "%). Immediate supplier and logistics review recommended.");
        }
        if (predGen > 5000) {
            recs.add("🏭 Predicted waste generation (" + round2(predGen) +
                    " kg) is high. Evaluate bulk packaging reduction or material substitution programs.");
        }
        if (result.getAverageReductionRate() >= 50) {
            recs.add(
                    "✅ Excellent overall reduction rate. Document your best practices to replicate across facilities.");
        }
        if (recs.isEmpty()) {
            recs.add(
                    "✅ Performance is on track. Continue monitoring monthly data and maintain current recycling practices.");
        }
        return recs;
    }

    /**
     * Populate chart-ready label and value lists, appending the prediction as a
     * dashed point
     */
    private void setChartData(PredictionResult result, List<WasteDataPoint> points) {
        List<String> labels = points.stream().map(WasteDataPoint::getMonthLabel).collect(Collectors.toList());
        List<Double> gen = points.stream().map(p -> round2(p.getGenerated())).collect(Collectors.toList());
        List<Double> rec = points.stream().map(p -> round2(p.getRecycled())).collect(Collectors.toList());

        // Append the prediction point
        labels.add("Next Month (Pred)");
        gen.add(result.getPredictedGenerated());
        rec.add(result.getPredictedRecycled());

        result.setChartLabels(labels);
        result.setChartGenerated(gen);
        result.setChartRecycled(rec);
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
