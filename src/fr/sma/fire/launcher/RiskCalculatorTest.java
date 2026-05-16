package fr.sma.fire.launcher;

import fr.sma.fire.logic.RiskCalculator;
import fr.sma.fire.messages.DonneesMeteo;
import fr.sma.fire.model.ZoneForet;

public class RiskCalculatorTest {

    public static void main(String[] args) {
        ZoneForet zone = new ZoneForet(3, 78.0, true, true);
        DonneesMeteo meteo = new DonneesMeteo("FORT", 20.0);

        RiskCalculator calculator = new RiskCalculator();

        double score = calculator.calculateScore(zone, meteo, 2, 10);
        String niveau = calculator.getRiskLevel(score);

        System.out.println("=== Risk Calculator Test ===");
        System.out.println(zone);
        System.out.println(meteo);
        System.out.println("Score = " + Math.round(score * 100.0));
        System.out.println("Niveau = " + niveau);
    }
}