package me.baldo3000;

import me.baldo3000.controller.api.Controller;
import me.baldo3000.controller.impl.ControllerImpl;

public class ControlUnitBackend {
    public static void main(String[] args) {
        try {
            System.out.println("Waiting Arduino for rebooting...");
            Thread.sleep(4000);
            final Controller controller = new ControllerImpl();
            controller.initialize();
            controller.mainLoop();
        } catch (final Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}