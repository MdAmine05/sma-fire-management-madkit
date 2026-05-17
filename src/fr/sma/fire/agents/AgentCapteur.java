package fr.sma.fire.agents;

import fr.sma.fire.config.AGRConfig;
import fr.sma.fire.messages.AlerteIncendie;
import fr.sma.fire.messages.SMAFireMessage;
import fr.sma.fire.model.ZoneForet;
import madkit.kernel.Agent;
import madkit.kernel.AbstractAgent.ReturnCode;

public class AgentCapteur extends Agent {

    @Override
    protected void activate() {
        createGroup(AGRConfig.COMMUNITY, AGRConfig.GROUPE_SURVEILLANCE);
        requestRole(AGRConfig.COMMUNITY, AGRConfig.GROUPE_SURVEILLANCE, AGRConfig.ROLE_DETECTEUR);

        // Technical MadKit adaptation: allows sending alerts to coordination roles.
        requestRole(AGRConfig.COMMUNITY, AGRConfig.GROUPE_COORDINATION, AGRConfig.ROLE_DETECTEUR);

        System.out.println("[AgentCapteur] Role Detecteur joined.");
    }

    @Override
    protected void live() {
        ZoneForet[] observations = {
                new ZoneForet(1, 42.0, false, false),
                new ZoneForet(3, 78.0, true, true),
                new ZoneForet(5, 66.0, true, false),
                new ZoneForet(2, 88.0, true, true)
        };

        for (ZoneForet zone : observations) {
            pause(10000);

            System.out.println("[AgentCapteur] Nouvelle observation : " + zone);

            sendMessage(
                    AGRConfig.COMMUNITY,
                    AGRConfig.GROUPE_COORDINATION,
                    AGRConfig.ROLE_INTERFACE_OBSERVER,
                    new SMAFireMessage("Nouvelle observation capteur : " + zone)
            );

            if (zone.getTemperature() > 60 && zone.isFumee()) {
                System.out.println("[AgentCapteur] Zone " + zone.getId()
                        + " : temperature=" + zone.getTemperature()
                        + "°C, fumee=true -> ALERTE envoyee");

                sendMessage(
                        AGRConfig.COMMUNITY,
                        AGRConfig.GROUPE_COORDINATION,
                        AGRConfig.ROLE_INTERFACE_OBSERVER,
                        new SMAFireMessage("ALERTE : Zone " + zone.getId()
                                + " | temperature=" + zone.getTemperature()
                                + "°C | fumee=true")
                );

                AlerteIncendie alerte = new AlerteIncendie(zone, "AgentCapteur");

                ReturnCode result = sendMessage(
                        AGRConfig.COMMUNITY,
                        AGRConfig.GROUPE_COORDINATION,
                        AGRConfig.ROLE_COORDINATEUR,
                        new SMAFireMessage(alerte)
                );

                System.out.println("[AgentCapteur] Resultat envoi MadKit = " + result);
            } else {
                System.out.println("[AgentCapteur] Zone " + zone.getId()
                        + " : situation normale, aucune alerte.");

                sendMessage(
                        AGRConfig.COMMUNITY,
                        AGRConfig.GROUPE_COORDINATION,
                        AGRConfig.ROLE_INTERFACE_OBSERVER,
                        new SMAFireMessage("Zone " + zone.getId() + " normale : aucune alerte.")
                );
            }
        }
    }
}