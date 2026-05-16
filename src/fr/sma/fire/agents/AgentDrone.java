package fr.sma.fire.agents;

import fr.sma.fire.config.AGRConfig;
import fr.sma.fire.messages.AlerteIncendie;
import fr.sma.fire.messages.ConfirmationIncendie;
import fr.sma.fire.messages.SMAFireMessage;
import madkit.kernel.Agent;
import madkit.kernel.Message;

public class AgentDrone extends Agent {

    @Override
    protected void activate() {
        createGroup(AGRConfig.COMMUNITY, AGRConfig.GROUPE_SURVEILLANCE);
        requestRole(AGRConfig.COMMUNITY, AGRConfig.GROUPE_SURVEILLANCE, AGRConfig.ROLE_OBSERVATEUR);

        requestRole(AGRConfig.COMMUNITY, AGRConfig.GROUPE_COORDINATION, AGRConfig.ROLE_OBSERVATEUR);

        System.out.println("[AgentDrone] Role Observateur joined.");
    }

    @Override
    protected void live() {
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < 70000) {
            Message message = waitNextMessage(1000);

            if (message instanceof SMAFireMessage fireMessage &&
                    fireMessage.getContent() instanceof AlerteIncendie alerte) {

                System.out.println("[AgentDrone] Zone " + alerte.getZone().getId()
                        + " inspectee -> Incendie CONFIRME.");

                sendMessage(
                        AGRConfig.COMMUNITY,
                        AGRConfig.GROUPE_COORDINATION,
                        AGRConfig.ROLE_COORDINATEUR,
                        new SMAFireMessage(new ConfirmationIncendie(alerte.getZone(), true))
                );
            }
        }
    }
}