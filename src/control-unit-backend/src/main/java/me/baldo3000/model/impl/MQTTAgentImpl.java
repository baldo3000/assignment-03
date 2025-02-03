package me.baldo3000.model.impl;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.mqtt.MqttClient;
import me.baldo3000.model.api.MQTTAgent;

/*
 * MQTT Agent
 */
public class MQTTAgentImpl extends AbstractVerticle implements MQTTAgent {

    private static final String BROKER_ADDRESS = "broker.mqtt-dashboard.com";
    private static final String TOPIC_NAME = "baldo-assignment03";

    public MQTTAgentImpl() {
    }

    @Override
    public void start() {
        final MqttClient client = MqttClient.create(vertx);

        client.connect(1883, BROKER_ADDRESS, c -> {
            log("connected");
            log("subscribing...\n");
            client.publishHandler(s -> {
                System.out.println("There are new message in topic: " + s.topicName());
                System.out.println("Content(as string) of the message: " + s.payload().toString());
                System.out.println("QoS: " + s.qosLevel() + "\n");
            }).subscribe(TOPIC_NAME, MqttQoS.AT_LEAST_ONCE.value());

            // log("publishing a msg");
            // client.publish(TOPIC_NAME, Buffer.buffer("hello"), MqttQoS.AT_LEAST_ONCE, false, true);
        });
    }

    @Override
    public String getLatestMessage() {
        return "";
    }

    @Override
    public void publishMessage(final String message) {

    }

    private void log(final String msg) {
        System.out.println("[MQTT AGENT] " + msg);
    }
}