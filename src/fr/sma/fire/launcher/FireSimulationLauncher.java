package fr.sma.fire.launcher;

import fr.sma.fire.agents.*;
import madkit.kernel.AbstractAgent;

import static java.lang.Thread.sleep;

public class FireSimulationLauncher extends AbstractAgent {

    @Override
    protected void activate() {
        System.out.println("=== Fire Simulation Launcher ===");

        launchAgent(new AgentCoordinateur());
        launchAgent(new AgentDrone());
        launchAgent(new AgentMeteo());
        launchAgent(new AgentPropagation());
        launchAgent(new AgentPompier());
        launchAgent(new AgentEvacuation());

        try {
            sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        launchAgent(new AgentInterface());
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        launchAgent(new AgentCapteur());
    }

    public static void main(String[] args) {
        executeThisAgent(args);
    }
}