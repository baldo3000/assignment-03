package me.baldo3000.engine.impl;

import io.vertx.core.Vertx;
import me.baldo3000.engine.api.Engine;
import me.baldo3000.model.api.MQTTAgent;
import me.baldo3000.model.impl.MQTTAgentImpl;

public class EngineImpl implements Engine {

    final Vertx vertx;
    final MQTTAgent mqttAgent;

    private State state;
    private long stateTimestamp;
    private boolean justEntered;
    private String latestMQTTMessage;

    public EngineImpl() {
        this.vertx = Vertx.vertx();
        this.mqttAgent = new MQTTAgentImpl();
        this.vertx.deployVerticle(this.mqttAgent);
        vertx.eventBus().consumer("mqtt-messages", message -> {
            this.latestMQTTMessage = message.body().toString();
            System.out.println("Received message: " + this.latestMQTTMessage);
        });
    }

    @Override
    public void initialize() {
        setState(State.NORMAL);
    }

    @Override
    public void mainLoop() {
        while (true) {
            // final String latestMessage = this.mqttAgent.getLatestMessage();
            // System.out.println("Latest message: " + latestMessage);
            switch (this.state) {
                case NORMAL -> {
                    if (doOnce()) {
                        System.out.println(State.NORMAL);
                    }
                }
                case HOT -> {
                    if (doOnce()) {
                        System.out.println(State.HOT);
                    }
                }
                case TOO_HOT -> {
                    if (doOnce()) {
                        System.out.println(State.TOO_HOT);
                    }
                }
                case ALARM -> {
                    if (doOnce()) {
                        System.out.println(State.ALARM);
                    }
                }
                default -> {
                }
            }
        }
    }

    @Override
    public void terminate() {

    }

    private boolean doOnce() {
        if (this.justEntered) {
            this.justEntered = false;
            return true;
        }
        return false;
    }

    @Override
    public void setState(final State state) {
        this.state = state;
        this.stateTimestamp = System.currentTimeMillis();
        this.justEntered = true;
    }

    @Override
    public State getState() {
        return this.state;
    }
}
