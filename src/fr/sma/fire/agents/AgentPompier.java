package fr.sma.fire.agents;

import fr.sma.fire.config.AGRConfig;
import fr.sma.fire.messages.OrdreIntervention;
import fr.sma.fire.messages.SMAFireMessage;
import madkit.kernel.Agent;
import madkit.kernel.Message;

public class AgentPompier extends Agent {

    @Override
    protected void activate() {
        createGroup(AGRConfig.COMMUNITY, AGRConfig.GROUPE_INTERVENTION);
        requestRole(AGRConfig.COMMUNITY, AGRConfig.GROUPE_INTERVENTION, AGRConfig.ROLE_INTERVENANT);

        requestRole(AGRConfig.COMMUNITY, AGRConfig.GROUPE_COORDINATION, AGRConfig.ROLE_INTERVENANT);

        System.out.println("[AgentPompier] Role Intervenant joined.");
    }

    @Override
    protected void live() {
        Message message = waitNextMessage(20000);

        if (message instanceof SMAFireMessage fireMessage &&
                fireMessage.getContent() instanceof OrdreIntervention ordre) {

            System.out.println("[AgentPompier] Intervention lancee sur Zone " + ordre.getZone().getId()
                    + " avec priorite " + ordre.getNiveauPriorite());
        }
    }
}