package fr.sma.fire.launcher;

import fr.sma.fire.messages.*;
import fr.sma.fire.model.ZoneForet;

public class MessageTest {

    public static void main(String[] args) {
        ZoneForet zone = new ZoneForet(3, 78.0, true, true);

        AlerteIncendie alerte = new AlerteIncendie(zone, "AgentCapteur");
        ConfirmationIncendie confirmation = new ConfirmationIncendie(zone, true);
        DonneesMeteo meteo = new DonneesMeteo("FORT", 20.0);
        RisquePropagation risque = new RisquePropagation(zone, 82.4, "CRITIQUE");
        OrdreIntervention ordre = new OrdreIntervention(zone, "CRITIQUE", true);

        System.out.println(alerte);
        System.out.println(confirmation);
        System.out.println(meteo);
        System.out.println(risque);
        System.out.println(ordre);
    }
}