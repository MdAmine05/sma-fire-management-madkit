package fr.sma.fire.messages;

import fr.sma.fire.model.ZoneForet;

public class AlerteIncendie {

    private final ZoneForet zone;
    private final String sourceAgent;

    public AlerteIncendie(ZoneForet zone, String sourceAgent) {
        this.zone = zone;
        this.sourceAgent = sourceAgent;
    }

    public ZoneForet getZone() {
        return zone;
    }

    public String getSourceAgent() {
        return sourceAgent;
    }

    @Override
    public String toString() {
        return "AlerteIncendie{" +
                "zone=" + zone +
                ", sourceAgent='" + sourceAgent + '\'' +
                '}';
    }
}