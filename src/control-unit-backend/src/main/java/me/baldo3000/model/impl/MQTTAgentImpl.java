package me.baldo3000.model.impl;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;
import me.baldo3000.model.api.MQTTAgent;

/*
 * MQTT Agent
 */
public class MQTTAgentImpl extends AbstractVerticle implements MQTTAgent {

    private static final String BROKER_ADDRESS = "broker.mqtt-dashboard.com";
    private static final String TOPIC_NAME = "baldo-assignment03";

    private final MqttClient client;

    public MQTTAgentImpl() {
        this.client = MqttClient.create(this.vertx);
    }

    @Override
    public void start() {
        log("connecting...");
        this.client.connect(1883, BROKER_ADDRESS, c -> {
            log("connected");
            log("subscribing...");
            this.client.publishHandler(s -> {
//                System.out.println("There are new message in topic: " + s.topicName());
//                System.out.println("Content(as string) of the message: " + s.payload().toString());
//                System.out.println("QoS: " + s.qosLevel() + "\n");
                this.vertx.eventBus().send("mqtt-incoming-messages", s.payload().toString());
            }).subscribe(TOPIC_NAME, MqttQoS.AT_LEAST_ONCE.value());
            log("subscribed...\n");
        });

        this.vertx.eventBus().consumer("mqtt-outgoing-messages", message -> {
            final String payload = message.body().toString();
            publishMessage(payload);
        });
    }

    private void publishMessage(final String message) {
//        log("publishing a msg");
        this.client.publish(TOPIC_NAME, Buffer.buffer(message), MqttQoS.AT_LEAST_ONCE, false, true);
    }

    private void log(final String msg) {
        System.out.println("[MQTT AGENT] " + msg);
    }
}