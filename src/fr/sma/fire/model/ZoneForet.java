package fr.sma.fire.model;

public class ZoneForet {

    private final int id;
    private final double temperature;
    private final boolean fumee;
    private final boolean procheHabitations;

    public ZoneForet(int id, double temperature, boolean fumee, boolean procheHabitations) {
        this.id = id;
        this.temperature = temperature;
        this.fumee = fumee;
        this.procheHabitations = procheHabitations;
    }

    public int getId() {
        return id;
    }

    public double getTemperature() {
        return temperature;
    }

    public boolean isFumee() {
        return fumee;
    }

    public boolean isProcheHabitations() {
        return procheHabitations;
    }

    @Override
    public String toString() {
        return "Zone " + id +
                " | temperature=" + temperature +
                "°C | fumee=" + fumee +
                " | procheHabitations=" + procheHabitations;
    }
}