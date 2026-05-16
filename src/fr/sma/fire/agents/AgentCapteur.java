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

        // Technical MadKit adaptation: sender joins target group to send role-based messages.
        requestRole(AGRConfig.COMMUNITY, AGRConfig.GROUPE_COORDINATION, AGRConfig.ROLE_DETECTEUR);

        System.out.println("[AgentCapteur] Role Detecteur joined.");
    }

    @Override
    protected void live() {
        pause(2000);

        ZoneForet zone = new ZoneForet(3, 78.0, true, true);

        if (zone.getTemperature() > 60 && zone.isFumee()) {
            System.out.println("[AgentCapteur] Zone 3 : temperature=78°C, fumee=true -> ALERTE envoyee");

            AlerteIncendie alerte = new AlerteIncendie(zone, "AgentCapteur");

            ReturnCode result = sendMessage(
                    AGRConfig.COMMUNITY,
                    AGRConfig.GROUPE_COORDINATION,
                    AGRConfig.ROLE_COORDINATEUR,
                    new SMAFireMessage(alerte)
            );

            System.out.println("[AgentCapteur] Resultat envoi MadKit = " + result);
        }
    }
}