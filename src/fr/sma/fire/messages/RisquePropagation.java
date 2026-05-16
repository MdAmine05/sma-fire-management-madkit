package fr.sma.fire.messages;

import fr.sma.fire.model.ZoneForet;

public class RisquePropagation {

    private final ZoneForet zone;
    private final double score;
    private final String niveau;

    public RisquePropagation(ZoneForet zone, double score, String niveau) {
        this.zone = zone;
        this.score = score;
        this.niveau = niveau;
    }

    public ZoneForet getZone() {
        return zone;
    }

    public double getScore() {
        return score;
    }

    public String getNiveau() {
        return niveau;
    }

    @Override
    public String toString() {
        return "RisquePropagation{" +
                "zone=" + zone +
                ", score=" + score +
                ", niveau='" + niveau + '\'' +
                '}';
    }
}