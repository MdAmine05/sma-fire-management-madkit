package fr.sma.fire.agents;

import fr.sma.fire.config.AGRConfig;
import fr.sma.fire.messages.*;
import fr.sma.fire.model.ZoneForet;
import madkit.kernel.Agent;
import madkit.kernel.Message;

public class AgentCoordinateur extends Agent {

    private ZoneForet currentZone;
    private DonneesMeteo currentMeteo;

    @Override
    protected void activate() {
        createGroup(AGRConfig.COMMUNITY, AGRConfig.GROUPE_COORDINATION);
        requestRole(AGRConfig.COMMUNITY, AGRConfig.GROUPE_COORDINATION, AGRConfig.ROLE_COORDINATEUR);

        createGroup(AGRConfig.COMMUNITY, AGRConfig.GROUPE_SECURITE);
        requestRole(AGRConfig.COMMUNITY, AGRConfig.GROUPE_SECURITE, AGRConfig.ROLE_SUPERVISEUR);

        System.out.println("[AgentCoordinateur] Roles Coordinateur and Superviseur joined.");
    }

    @Override
    protected void live() {
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < 70000) {
            Message message = waitNextMessage(1000);

            if (!(message instanceof SMAFireMessage fireMessage)) {
                continue;
            }

            Object content = fireMessage.getContent();

            if (content instanceof AlerteIncendie alerte) {
                currentZone = alerte.getZone();
                System.out.println("[AgentCoordinateur] Alerte recue. Demande de confirmation au drone.");
                notifyInterface("Alerte recue depuis AgentCapteur pour Zone " + alerte.getZone().getId());
                notifyInterface("Demande de confirmation envoyee a AgentDrone.");


                sendMessage(
                        AGRConfig.COMMUNITY,
                        AGRConfig.GROUPE_COORDINATION,
                        AGRConfig.ROLE_OBSERVATEUR,
                        new SMAFireMessage(alerte)
                );
            }

            if (content instanceof ConfirmationIncendie confirmation && confirmation.isIncendieConfirme()) {
                System.out.println("[AgentCoordinateur] Incendie confirme. Demande des donnees meteo.");
                notifyInterface("Incendie confirme par AgentDrone pour Zone " + confirmation.getZone().getId());
                notifyInterface("Demande des donnees meteo envoyee a AgentMeteo.");


                sendMessage(
                        AGRConfig.COMMUNITY,
                        AGRConfig.GROUPE_COORDINATION,
                        AGRConfig.ROLE_FOURNISSEUR_CONTEXTE,
                        new SMAFireMessage("REQUEST_METEO")
                );
            }

            if (content instanceof DonneesMeteo meteo) {
                currentMeteo = meteo;
                System.out.println("[AgentCoordinateur] Donnees meteo recues. Demande analyse propagation.");
                notifyInterface("Donnees meteo recues : vent=" + meteo.getVent() + ", humidite=" + meteo.getHumidite() + "%");
                notifyInterface("Demande d'analyse envoyee a AgentPropagation.");

                sendMessage(
                        AGRConfig.COMMUNITY,
                        AGRConfig.GROUPE_COORDINATION,
                        AGRConfig.ROLE_ANALYSTE_RISQUE,
                        new SMAFireMessage(new Object[]{currentZone, currentMeteo})
                );
                notifyInterface("Analyse de propagation demandée.");
            }

            if (content instanceof RisquePropagation risque) {
                System.out.println("[AgentCoordinateur] Priorite " + risque.getNiveau()
                        + ". Envoi pompiers + evacuation si necessaire.");
                notifyInterface("Risque calcule pour Zone " + risque.getZone().getId()
                        + " : " + risque.getNiveau()
                        + " | Score=" + Math.round(risque.getScore() * 100));

                notifyInterface("Ordre d'intervention envoye a AgentPompier pour Zone " + risque.getZone().getId());

                if (risque.getZone().isProcheHabitations()) {
                    notifyInterface("Ordre d'evacuation envoye a AgentEvacuation.");
                } else {
                    notifyInterface("Pas d'evacuation necessaire pour cette zone.");
                }

                OrdreIntervention ordre = new OrdreIntervention(
                        risque.getZone(),
                        risque.getNiveau(),
                        risque.getZone().isProcheHabitations()
                );
                sendMessage(
                        AGRConfig.COMMUNITY,
                        AGRConfig.GROUPE_COORDINATION,
                        "InterfaceObserver",
                        new SMAFireMessage(risque)
                );

                sendMessage(
                        AGRConfig.COMMUNITY,
                        AGRConfig.GROUPE_COORDINATION,
                        AGRConfig.ROLE_INTERVENANT,
                        new SMAFireMessage(ordre)
                );

                sendMessage(
                        AGRConfig.COMMUNITY,
                        AGRConfig.GROUPE_COORDINATION,
                        AGRConfig.ROLE_RESPONSABLE_EVACUATION,
                        new SMAFireMessage(ordre)
                );

            }
        }

        System.out.println("[AgentCoordinateur] Simulation terminee sans scenario complet.");
    }
    private void notifyInterface(String text) {
        sendMessage(
                AGRConfig.COMMUNITY,
                AGRConfig.GROUPE_COORDINATION,
                "InterfaceObserver",
                new SMAFireMessage(text)
        );
    }
}