package me.baldo3000;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;

/*
 * MQTT Agent
 */
public class MQTTAgent extends AbstractVerticle {

    private static final String BROKER_ADDRESS = "broker.mqtt-dashboard.com";
    private static final String TOPIC_NAME = "baldo-assignment03";

    public MQTTAgent() {
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

    private void log(String msg) {
        System.out.println("[MQTT AGENT] " + msg);
    }
}