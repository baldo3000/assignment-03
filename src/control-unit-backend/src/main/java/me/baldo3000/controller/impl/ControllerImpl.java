package me.baldo3000.controller.impl;

import io.vertx.core.Vertx;
import me.baldo3000.controller.api.Controller;
import me.baldo3000.model.api.SerialCommChannel;
import me.baldo3000.model.impl.MQTTAgent;
import me.baldo3000.model.impl.SerialCommChannelImpl;

public class ControllerImpl implements Controller {

    private final Vertx vertx;
    private final MQTTAgent mqttAgent;
    private final SerialCommChannel channel;

    private State state;
    private long stateTimestamp;
    private boolean justEntered;
    private double previouslyReportedTemperature;
    private double latestReportedTemperature;
    private boolean resetSignal;
    private int windowAperture;

    public ControllerImpl(final Vertx vertx, final MQTTAgent mqttAgent, final SerialCommChannel channel) {
        this.vertx = vertx;
        this.mqttAgent = mqttAgent;
        this.channel = channel;
        this.previouslyReportedTemperature = 0.0;
        this.latestReportedTemperature = 0.0;
        this.resetSignal = false;
        this.windowAperture = 0;
    }

    @Override
    public void initialize() {
        this.vertx.deployVerticle(this.mqttAgent);
        this.vertx.eventBus().consumer(MQTTAgent.INCOMING_ADDRESS, message -> {
            final String payload = message.body().toString();
            System.out.println("Received message: " + payload);
            if (payload.length() >= 3) {
                final String prefix = payload.substring(0, 3);
                if (prefix.equals("ts:")) {
                    try {
                        this.latestReportedTemperature = Double.parseDouble(payload.substring(3));
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
                        this.vertx.eventBus().send(MQTTAgent.OUTGOING_ADDRESS, "cu:" + NORMAL_SAMPLE_INTERVAL);
                    }
                    if (this.latestReportedTemperature >= HOT_THRESHOLD) {
                        setState(State.HOT);
                    }
                }
                case HOT -> {
                    if (doOnce()) {
                        System.out.println(State.HOT);
                        this.vertx.eventBus().send(MQTTAgent.OUTGOING_ADDRESS, "cu:" + HOT_SAMPLE_INTERVAL);
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

            // Send message to window controller if temperature have changed from last reading
            if (this.latestReportedTemperature != this.previouslyReportedTemperature) {
                this.windowAperture = temperatureToWindowAperture(this.latestReportedTemperature);
                this.channel.sendMsg(this.windowAperture + ";" + this.latestReportedTemperature);
                this.previouslyReportedTemperature = this.latestReportedTemperature;
            }

            // Wait a bit
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

    private boolean doOnce() {
        if (this.justEntered) {
            this.justEntered = false;
            return true;
        }
        return false;
    }

    private int temperatureToWindowAperture(final double temperature) {
        if (temperature < HOT_THRESHOLD) {
            return 0;
        } else if (temperature > TOO_HOT_THRESHOLD) {
            return 100;
        } else {
            return (int) ((temperature - HOT_THRESHOLD) / (TOO_HOT_THRESHOLD - HOT_THRESHOLD) * 100);
        }
    }
}
