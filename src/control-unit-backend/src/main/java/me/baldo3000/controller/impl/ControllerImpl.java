package me.baldo3000.controller.impl;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import me.baldo3000.controller.api.Controller;
import me.baldo3000.model.api.SerialCommChannel;
import me.baldo3000.model.impl.HTTPClient;
import me.baldo3000.model.impl.MQTTAgent;
import me.baldo3000.model.impl.SerialCommChannelImpl;

public class ControllerImpl implements Controller {

    private final Vertx vertx;
    private final MQTTAgent mqttAgent;
    private final SerialCommChannel channel;
    private final HTTPClient httpClient;

    private State state;
    private long stateTimestamp;
    private boolean justEntered;
    private double latestReportedTemperature;
    private boolean resetSignal;
    private int windowAperture;

    public ControllerImpl() throws Exception {
        this.vertx = Vertx.vertx();
        this.channel = new SerialCommChannelImpl();
        this.mqttAgent = new MQTTAgent();
        this.httpClient = new HTTPClient();
        this.latestReportedTemperature = 0.0;
        this.resetSignal = false;
        this.windowAperture = 0;
    }

    @Override
    public void initialize() {
        this.vertx.deployVerticle(this.mqttAgent);
        this.vertx.deployVerticle(this.httpClient);
        initializeConsumers();
        setState(State.NORMAL);
    }

    @Override
    public void mainLoop() throws InterruptedException {
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
                        if (this.latestReportedTemperature >= TOO_HOT_THRESHOLD) {
                            setState(State.TOO_HOT);
                        } else if (this.latestReportedTemperature >= HOT_THRESHOLD) {
                            setState(State.HOT);
                        } else {
                            setState(State.NORMAL);
                        }
                    }
                }
                default -> {
                }
            }

            // Wait a bit
            Thread.sleep(100); // Add a small delay TODO: remove
        }
    }

    @Override
    public void terminate() {
        this.channel.close();
        this.vertx.close();
        System.exit(0);
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

    private void initializeConsumers() {
        this.vertx.eventBus().consumer(MQTTAgent.INCOMING_ADDRESS, message -> {
            final String payload = message.body().toString();
            // System.out.println("Received message from MQTT: " + payload);
            if (payload.length() >= 3) {
                final String prefix = payload.substring(0, 3);
                if (prefix.equals("ts:")) {
                    try {
                        final double temperature = Double.parseDouble(payload.substring(3));
                        if (temperature != this.latestReportedTemperature) {
                            this.latestReportedTemperature = temperature;
                            this.windowAperture = temperatureToWindowAperture(this.latestReportedTemperature);
                            // Send over serial only if changed to avoid congestions
                            sendStatsSerial();
                        }
                        sendStatsHTTP();
                    } catch (final NumberFormatException ignored) {
                    }
                }
            }
        });
        this.vertx.eventBus().consumer(HTTPClient.INCOMING_ADDRESS, message -> {
            if (this.state.equals(State.ALARM) && message.body().toString().equals("df:reset")) {
                System.out.println("Reset signal received");
                this.resetSignal = true;
            }
        });
    }

    private boolean doOnce() {
        if (this.justEntered) {
            this.justEntered = false;
            return true;
        }
        return false;
    }

    private void sendStatsSerial() {
        if (this.channel.isOpen()) {
            this.channel.sendMsg(this.windowAperture + ";" + this.latestReportedTemperature);
        }
    }

    private void sendStatsHTTP() {
        final JsonObject obj = new JsonObject();
        obj.put("state", this.state.toString());
        obj.put("aperture", this.windowAperture);
        obj.put("temperature", this.latestReportedTemperature);
        obj.put("time", System.currentTimeMillis());
        this.vertx.eventBus().send(HTTPClient.OUTGOING_ADDRESS, obj.encode());
    }

    private int temperatureToWindowAperture(final double temperature) {
        if (temperature < HOT_THRESHOLD) {
            return 0;
        } else if (temperature > TOO_HOT_THRESHOLD || this.state.equals(State.ALARM)) {
            return 100;
        } else {
            return (int) ((temperature - HOT_THRESHOLD) / (TOO_HOT_THRESHOLD - HOT_THRESHOLD) * 100);
        }
    }
}
