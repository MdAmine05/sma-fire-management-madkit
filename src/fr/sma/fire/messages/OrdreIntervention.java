package fr.sma.fire.messages;

import fr.sma.fire.model.ZoneForet;

public class OrdreIntervention {

    private final ZoneForet zone;
    private final String niveauPriorite;
    private final boolean evacuationNecessaire;

    public OrdreIntervention(ZoneForet zone, String niveauPriorite, boolean evacuationNecessaire) {
        this.zone = zone;
        this.niveauPriorite = niveauPriorite;
        this.evacuationNecessaire = evacuationNecessaire;
    }

    public ZoneForet getZone() {
        return zone;
    }

    public String getNiveauPriorite() {
        return niveauPriorite;
    }

    public boolean isEvacuationNecessaire() {
        return evacuationNecessaire;
    }

    @Override
    public String toString() {
        return "OrdreIntervention{" +
                "zone=" + zone +
                ", niveauPriorite='" + niveauPriorite + '\'' +
                ", evacuationNecessaire=" + evacuationNecessaire +
                '}';
    }
}