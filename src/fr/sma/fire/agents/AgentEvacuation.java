package fr.sma.fire.agents;

import fr.sma.fire.config.AGRConfig;
import fr.sma.fire.messages.OrdreIntervention;
import fr.sma.fire.messages.SMAFireMessage;
import madkit.kernel.Agent;
import madkit.kernel.Message;

public class AgentEvacuation extends Agent {

    @Override
    protected void activate() {
        createGroup(AGRConfig.COMMUNITY, AGRConfig.GROUPE_SECURITE);
        requestRole(AGRConfig.COMMUNITY, AGRConfig.GROUPE_SECURITE, AGRConfig.ROLE_RESPONSABLE_EVACUATION);

        requestRole(AGRConfig.COMMUNITY, AGRConfig.GROUPE_COORDINATION, AGRConfig.ROLE_RESPONSABLE_EVACUATION);

        System.out.println("[AgentEvacuation] Role ResponsableEvacuation joined.");
    }

    @Override
    protected void live() {
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < 120000) {
            Message message = waitNextMessage(1000);

            if (message instanceof SMAFireMessage fireMessage &&
                    fireMessage.getContent() instanceof OrdreIntervention ordre) {

                if (ordre.isEvacuationNecessaire()) {
                    System.out.println("[AgentEvacuation] Alerte civile declenchee pour Zone "
                            + ordre.getZone().getId()
                            + ". Habitations proches evacuees.");
                } else {
                    System.out.println("[AgentEvacuation] Zone "
                            + ordre.getZone().getId()
                            + " : evacuation non necessaire.");
                }
            }
        }
    }
}