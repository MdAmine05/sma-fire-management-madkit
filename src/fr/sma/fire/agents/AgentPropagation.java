package fr.sma.fire.agents;

import fr.sma.fire.config.AGRConfig;
import fr.sma.fire.logic.RiskCalculator;
import fr.sma.fire.messages.DonneesMeteo;
import fr.sma.fire.messages.RisquePropagation;
import fr.sma.fire.messages.SMAFireMessage;
import fr.sma.fire.model.ZoneForet;
import madkit.kernel.Agent;
import madkit.kernel.Message;

public class AgentPropagation extends Agent {

    @Override
    protected void activate() {
        createGroup(AGRConfig.COMMUNITY, AGRConfig.GROUPE_COORDINATION);
        requestRole(AGRConfig.COMMUNITY, AGRConfig.GROUPE_COORDINATION, AGRConfig.ROLE_ANALYSTE_RISQUE);

        System.out.println("[AgentPropagation] Role AnalysteRisque joined.");
    }

    @Override
    protected void live() {
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < 120000) {
            Message message = waitNextMessage(1000);

            if (message instanceof SMAFireMessage fireMessage &&
                    fireMessage.getContent() instanceof Object[] data) {

                ZoneForet zone = (ZoneForet) data[0];
                DonneesMeteo meteo = (DonneesMeteo) data[1];

                RiskCalculator calculator = new RiskCalculator();
                double score = calculator.calculateScore(zone, meteo, 0, 10);
                String niveau = calculator.getRiskLevel(score);

                System.out.println("[AgentPropagation] Zone " + zone.getId()
                        + " ScoreRisque=" + Math.round(score * 100)
                        + " -> Niveau " + niveau);

                sendMessage(
                        AGRConfig.COMMUNITY,
                        AGRConfig.GROUPE_COORDINATION,
                        AGRConfig.ROLE_COORDINATEUR,
                        new SMAFireMessage(new RisquePropagation(zone, score, niveau))
                );
            }
        }
    }
}