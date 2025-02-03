package me.baldo3000.model.api;

import io.vertx.core.Verticle;

public interface MQTTAgent extends Verticle {

    String getLatestMessage();

    void publishMessage(String message);
}
