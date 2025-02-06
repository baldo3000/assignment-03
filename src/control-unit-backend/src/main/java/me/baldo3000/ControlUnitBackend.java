package me.baldo3000;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import me.baldo3000.controller.api.Controller;
import me.baldo3000.controller.impl.ControllerImpl;
import me.baldo3000.model.api.SerialCommChannel;
import me.baldo3000.model.impl.MQTTAgent;
import me.baldo3000.model.impl.SerialCommChannelImpl;

public class ControlUnitBackend {
    public static void main(String[] args) {
        final Vertx vertx = Vertx.vertx();
        final MQTTAgent mqttAgent = new MQTTAgent();
        final WebClient webClient = WebClient.create(vertx);
        final SerialCommChannel channel;
        try {
            channel = new SerialCommChannelImpl();
            System.out.println("Waiting Arduino for rebooting...");
            Thread.sleep(4000);
            System.out.println("Ready.");
            final Controller controller = new ControllerImpl(vertx, mqttAgent, webClient, channel);
            controller.initialize();
            controller.mainLoop();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}