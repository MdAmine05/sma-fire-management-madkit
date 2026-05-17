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

        while (System.currentTimeMillis() - start < 120000) {
            Message message = waitNextMessage(1000);

            if (!(message instanceof SMAFireMessage fireMessage)) continue;
            if (!(fireMessage.getContent() instanceof AlerteIncendie alerte)) continue;

            // Zone 13 → forced false alarm (demo button D).
            // Zones 10-12 → manual buttons A/B/C, always confirm so the demo is predictable.
            // Zones 1-9  → automatic sensors, random 75 % confirm / 25 % false alarm.
            int zoneId = alerte.getZone().getId();
            boolean confirmed;
            if (zoneId == 13) {
                confirmed = false;
            } else if (zoneId >= 10) {
                confirmed = true;
            } else {
                confirmed = Math.random() > 0.25;
            }

            if (confirmed) {
                System.out.println("[AgentDrone] Zone " + alerte.getZone().getId()
                        + " inspectee -> Incendie CONFIRME.");
            } else {
                System.out.println("[AgentDrone] Zone " + alerte.getZone().getId()
                        + " inspectee -> FAUSSE ALERTE. Annulation.");
            }

            String droneLog = confirmed
                    ? "[Drone] Zone " + alerte.getZone().getId() + " -> Incendie CONFIRME"
                    : "[Drone] Zone " + alerte.getZone().getId() + " -> FAUSSE ALERTE — annulation";

            sendMessage(
                    AGRConfig.COMMUNITY,
                    AGRConfig.GROUPE_COORDINATION,
                    AGRConfig.ROLE_INTERFACE_OBSERVER,
                    new SMAFireMessage(droneLog)
            );

            sendMessage(
                    AGRConfig.COMMUNITY,
                    AGRConfig.GROUPE_COORDINATION,
                    AGRConfig.ROLE_COORDINATEUR,
                    new SMAFireMessage(new ConfirmationIncendie(alerte.getZone(), confirmed))
            );
        }
    }
}
