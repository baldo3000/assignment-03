package me.baldo3000.engine.impl;

import io.vertx.core.Vertx;
import me.baldo3000.engine.api.Engine;
import me.baldo3000.model.api.MQTTAgent;
import me.baldo3000.model.impl.MQTTAgentImpl;

public class EngineImpl implements Engine {

    private final Vertx vertx;
    private final MQTTAgent mqttAgent;

    private State state;
    private long stateTimestamp;
    private boolean justEntered;
    private double latestReportedTemperature;

    public EngineImpl() {
        this.vertx = Vertx.vertx();
        this.mqttAgent = new MQTTAgentImpl();
    }

    @Override
    public void initialize() {
        this.vertx.deployVerticle(this.mqttAgent);
        this.vertx.eventBus().consumer("mqtt-messages", message -> {
            final String payload = message.body().toString();
            System.out.println("Received message: " + payload);
            if (payload.length() >= 3) {
                final String prefix = payload.substring(0, 3);
                if (prefix.equals("ts:")) {
                    try {
                        final double temperature = Double.parseDouble(payload.substring(3));
                        this.latestReportedTemperature = temperature;
                        System.out.println("Temperature: " + temperature);
                    } catch (final NumberFormatException ignored) {
                    }
                }
            }
        });
        setState(State.NORMAL);
    }

    @Override
    public void mainLoop() {
        while (true) {
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
