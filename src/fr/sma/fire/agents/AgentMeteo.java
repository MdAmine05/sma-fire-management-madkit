package fr.sma.fire.agents;

import fr.sma.fire.config.AGRConfig;
import fr.sma.fire.messages.DonneesMeteo;
import fr.sma.fire.messages.SMAFireMessage;
import madkit.kernel.Agent;
import madkit.kernel.Message;

public class AgentMeteo extends Agent {

    @Override
    protected void activate() {
        createGroup(AGRConfig.COMMUNITY, AGRConfig.GROUPE_SURVEILLANCE);
        requestRole(AGRConfig.COMMUNITY, AGRConfig.GROUPE_SURVEILLANCE, AGRConfig.ROLE_FOURNISSEUR_CONTEXTE);

        requestRole(AGRConfig.COMMUNITY, AGRConfig.GROUPE_COORDINATION, AGRConfig.ROLE_FOURNISSEUR_CONTEXTE);

        System.out.println("[AgentMeteo] Role FournisseurContexte joined.");
    }

    @Override
    protected void live() {
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < 70000) {
            Message message = waitNextMessage(1000);

            if (message instanceof SMAFireMessage fireMessage &&
                    "REQUEST_METEO".equals(fireMessage.getContent())) {

                DonneesMeteo meteo = new DonneesMeteo("FORT", 20.0);

                System.out.println("[AgentMeteo] Vent=FORT, humidite=20%");

                sendMessage(
                        AGRConfig.COMMUNITY,
                        AGRConfig.GROUPE_COORDINATION,
                        AGRConfig.ROLE_COORDINATEUR,
                        new SMAFireMessage(meteo)
                );
            }
        }
    }
}