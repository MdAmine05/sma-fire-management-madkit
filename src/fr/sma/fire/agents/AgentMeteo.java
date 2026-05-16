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
        Message message = waitNextMessage(15000);

        if (message instanceof SMAFireMessage) {
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