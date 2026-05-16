package fr.sma.fire.logic;

import fr.sma.fire.messages.DonneesMeteo;
import fr.sma.fire.model.ZoneForet;

public class RiskCalculator {

    public double calculateScore(ZoneForet zone, DonneesMeteo meteo, int pompiersDisponibles, int pompiersMax) {
        double tempNorm = normalizeTemperature(zone.getTemperature());
        double fumeeNorm = zone.isFumee() ? 1.0 : 0.0;
        double ventNorm = normalizeVent(meteo.getVent());
        double proxNorm = zone.isProcheHabitations() ? 1.0 : 0.1;
        double ressNorm = pompiersMax == 0 ? 0.0 : (double) pompiersDisponibles / pompiersMax;

        return (0.30 * tempNorm)
                + (0.25 * fumeeNorm)
                + (0.20 * ventNorm)
                + (0.15 * proxNorm)
                - (0.10 * ressNorm);
    }

    public String getRiskLevel(double score) {
        if (score >= 0.81) return "CRITIQUE";
        if (score >= 0.61) return "ELEVE";
        if (score >= 0.31) return "MOYEN";
        return "FAIBLE";
    }

    private double normalizeTemperature(double temperature) {
        double normalized = (temperature - 25.0) / 75.0;
        return Math.max(0.0, Math.min(1.0, normalized));
    }

    private double normalizeVent(String vent) {
        return switch (vent.toUpperCase()) {
            case "FORT" -> 1.0;
            case "MOYEN" -> 0.6;
            case "FAIBLE" -> 0.2;
            default -> 0.0;
        };
    }
}