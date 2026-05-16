package fr.sma.fire.messages;

import fr.sma.fire.model.ZoneForet;

public class ConfirmationIncendie {

    private final ZoneForet zone;
    private final boolean incendieConfirme;

    public ConfirmationIncendie(ZoneForet zone, boolean incendieConfirme) {
        this.zone = zone;
        this.incendieConfirme = incendieConfirme;
    }

    public ZoneForet getZone() {
        return zone;
    }

    public boolean isIncendieConfirme() {
        return incendieConfirme;
    }

    @Override
    public String toString() {
        return "ConfirmationIncendie{" +
                "zone=" + zone +
                ", incendieConfirme=" + incendieConfirme +
                '}';
    }
}