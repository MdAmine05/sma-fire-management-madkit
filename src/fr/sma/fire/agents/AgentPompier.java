package fr.sma.fire.agents;

import fr.sma.fire.config.AGRConfig;
import fr.sma.fire.messages.OrdreIntervention;
import fr.sma.fire.messages.SMAFireMessage;
import madkit.kernel.Agent;
import madkit.kernel.Message;

public class AgentPompier extends Agent {

    private boolean available = true;

    @Override
    protected void activate() {
        createGroup(AGRConfig.COMMUNITY, AGRConfig.GROUPE_INTERVENTION);
        requestRole(AGRConfig.COMMUNITY, AGRConfig.GROUPE_INTERVENTION, AGRConfig.ROLE_INTERVENANT);

        requestRole(AGRConfig.COMMUNITY, AGRConfig.GROUPE_COORDINATION, AGRConfig.ROLE_INTERVENANT);

        System.out.println("[AgentPompier] Role Intervenant joined. Etat=AVAILABLE");
    }

    @Override
    protected void live() {
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < 120000) {
            Message message = waitNextMessage(1000);

            if (message instanceof SMAFireMessage fireMessage &&
                    fireMessage.getContent() instanceof OrdreIntervention ordre) {

                if (!available) {
                    System.out.println("[AgentPompier] Indisponible. Intervention Zone "
                            + ordre.getZone().getId() + " mise en attente.");
                    continue;
                }

                available = false;

                System.out.println("[AgentPompier] Intervention lancee sur Zone "
                        + ordre.getZone().getId()
                        + " avec priorite " + ordre.getNiveauPriorite()
                        + ". Etat=BUSY");

                pause(8000);

                available = true;

                System.out.println("[AgentPompier] Intervention terminee sur Zone "
                        + ordre.getZone().getId()
                        + ". Etat=AVAILABLE");
            }
        }
    }
}