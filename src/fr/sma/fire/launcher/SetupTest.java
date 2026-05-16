package fr.sma.fire.launcher;

import fr.sma.fire.config.AGRConfig;
import fr.sma.fire.model.ZoneForet;

public class SetupTest {

    public static void main(String[] args) {
        ZoneForet zone = new ZoneForet(3, 78.0, true, true);

        System.out.println("=== Setup Test ===");
        System.out.println("Community: " + AGRConfig.COMMUNITY);
        System.out.println(zone);
        System.out.println("Setup OK.");
    }
}