package fr.sma.fire.messages;

public class DonneesMeteo {

    private final String vent;
    private final double humidite;

    public DonneesMeteo(String vent, double humidite) {
        this.vent = vent;
        this.humidite = humidite;
    }

    public String getVent() {
        return vent;
    }

    public double getHumidite() {
        return humidite;
    }

    @Override
    public String toString() {
        return "DonneesMeteo{" +
                "vent='" + vent + '\'' +
                ", humidite=" + humidite +
                "%}";
    }
}