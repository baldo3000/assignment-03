package me.baldo3000.model.impl;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;

/*
 * MQTT Agent
 */
public class MQTTAgent extends AbstractVerticle {

    public static final String INCOMING_ADDRESS = "mqtt-incoming-messages";
    public static final String OUTGOING_ADDRESS = "mqtt-outgoing-messages";

    private static final String BROKER_ADDRESS = "broker.mqtt-dashboard.com";
    private static final String TOPIC_NAME = "baldo3000-assignment03";

    private MqttClient client;

    public MQTTAgent() {
    }

    @Override
    public void start() {
        this.client = MqttClient.create(this.vertx);
        log("connecting...");
        this.client.connect(1883, BROKER_ADDRESS, c -> {
            log("connected");
            log("subscribing...");

            // Receiving messages
            this.client.publishHandler(s -> {
//                System.out.println("There is a new message in topic: " + s.topicName());
//                System.out.println("Content(as string) of the message: " + s.payload().toString());
//                System.out.println("QoS: " + s.qosLevel() + "\n");
                this.vertx.eventBus().send(INCOMING_ADDRESS, s.payload().toString());
            }).subscribe(TOPIC_NAME, MqttQoS.AT_LEAST_ONCE.value());

            // Request to send messages
            this.vertx.eventBus().consumer(OUTGOING_ADDRESS, message -> {
                final String payload = message.body().toString();
                publishMessage(payload);
            });
            log("subscribed...\n");
        });


    }

    private void publishMessage(final String message) {
        this.client.publish(TOPIC_NAME, Buffer.buffer(message), MqttQoS.AT_LEAST_ONCE, false, true);
    }

    private void log(final String msg) {
        System.out.println("[MQTT AGENT] " + msg);
    }
}
