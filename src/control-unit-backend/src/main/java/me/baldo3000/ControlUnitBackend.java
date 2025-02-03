package me.baldo3000;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;

public class ControlUnitBackend {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        MQTTAgent agent = new MQTTAgent();
        vertx.deployVerticle(agent);
    }
}