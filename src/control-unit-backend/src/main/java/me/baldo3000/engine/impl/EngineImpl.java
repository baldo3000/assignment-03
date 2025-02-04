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
    private boolean resetSignal;

    public EngineImpl() {
        this.vertx = Vertx.vertx();
        this.mqttAgent = new MQTTAgentImpl();
        this.latestReportedTemperature = 0.0;
        this.resetSignal = false;
    }

    @Override
    public void initialize() {
        this.vertx.deployVerticle(this.mqttAgent);
        this.vertx.eventBus().consumer("mqtt-incoming-messages", message -> {
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
                } else if (prefix.equals("df:")) {
                    final String content = payload.substring(3);
                    if (content.equals("reset")) {
                        this.resetSignal = true;
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
                        this.vertx.eventBus().send("mqtt-outgoing-messages", "cu:" + NORMAL_SAMPLE_INTERVAL);
                    }
                    if (this.latestReportedTemperature >= HOT_THRESHOLD) {
                        setState(State.HOT);
                    }
                }
                case HOT -> {
                    if (doOnce()) {
                        System.out.println(State.HOT);
                        this.vertx.eventBus().send("mqtt-outgoing-messages", "cu:" + HOT_SAMPLE_INTERVAL);
                    }
                    if (this.latestReportedTemperature < HOT_THRESHOLD) {
                        setState(State.NORMAL);
                    } else if (this.latestReportedTemperature >= TOO_HOT_THRESHOLD) {
                        setState(State.TOO_HOT);
                    }
                }
                case TOO_HOT -> {
                    if (doOnce()) {
                        System.out.println(State.TOO_HOT);
                    }
                    if (this.latestReportedTemperature < TOO_HOT_THRESHOLD) {
                        setState(State.HOT);
                    }
                    if (System.currentTimeMillis() - this.stateTimestamp >= ALARM_TRIGGER_TIME) {
                        setState(State.ALARM);
                    }
                }
                case ALARM -> {
                    if (doOnce()) {
                        System.out.println(State.ALARM);
                    }
                    if (this.resetSignal) {
                        this.resetSignal = false;
                        setState(State.NORMAL);
                    }
                }
                default -> {
                }
            }
            try {
                Thread.sleep(100); // Add a small delay TODO: remove
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
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
