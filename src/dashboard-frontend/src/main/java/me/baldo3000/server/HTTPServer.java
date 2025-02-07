package me.baldo3000.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import me.baldo3000.common.DataValue;

import java.util.*;

public class HTTPServer extends AbstractVerticle {

    private enum State {
        NORMAL, HOT, TOO_HOT, ALARM
    }

    // Records
    private static final int MAX_SIZE = 50;
    private final List<DataValue> values;

    // Current state
    private State state;
    private int aperture;
    private double temperature;
    private double minTemperature;
    private double maxTemperature;
    private double averageTemperature;

    // WebSocket connection
    private final Set<ServerWebSocket> webSockets;

    public HTTPServer() {
        this.values = new ArrayList<>(MAX_SIZE);
        this.state = State.NORMAL;
        this.aperture = 0;
        this.temperature = 0.0;
        this.minTemperature = 0.0;
        this.maxTemperature = 0.0;
        this.averageTemperature = 0.0;
        this.webSockets = new HashSet<>();
    }

    @Override
    public void start() {
        startServer();
    }

    private void startServer() {
        final Router router = Router.router(vertx);
        router.route().handler(StaticHandler.create().setCachingEnabled(true));
        router.route().handler(BodyHandler.create());
        router.route(HttpMethod.POST, "/api/alarm").handler(this::handleAlarmNotification);
        router.route(HttpMethod.GET, "/api/data").handler(this::handleGetData);

        vertx.createHttpServer()
                .requestHandler(router)
                .webSocketHandler(this::handleWebSocket)
                .listen(8080)
                .onSuccess(server -> System.out.println("HTTP server started on port " + server.actualPort()));
    }

    private void handleAlarmNotification(final RoutingContext routingContext) {
        final JsonObject body = routingContext.body().asJsonObject();
        if (body == null || !body.containsKey("reset")) {
            routingContext.response().setStatusCode(400).end();
            return;
        }

        final boolean reset = body.getBoolean("reset");
        if (reset) {
            this.webSockets.forEach(ws -> {
                if (!ws.isClosed()) {
                    ws.writeTextMessage("df:reset");
                }
            });
        }
        routingContext.response().setStatusCode(200).end();
    }

    private void handleGetData(final RoutingContext routingContext) {
        final JsonArray arr = new JsonArray();
        for (final DataValue value : this.values) {
            final JsonObject obj = new JsonObject();
            obj.put("aperture", value.aperture());
            obj.put("temperature", value.temperature());
            obj.put("time", value.time());
            arr.add(obj);
        }
        final JsonObject stats = new JsonObject();
        stats.put("state", this.state.toString());
        stats.put("aperture", this.aperture);
        stats.put("temperature", this.temperature);
        stats.put("minTemperature", this.minTemperature);
        stats.put("maxTemperature", this.maxTemperature);
        stats.put("averageTemperature", this.averageTemperature);

        arr.add(stats);
        routingContext.response()
                .putHeader("content-type", "application/json")
                .end(arr.encodePrettily());
    }

    private void handleWebSocket(final ServerWebSocket ws) {
        if (!ws.path().equals("/ws")) {
            ws.reject();
            return;
        }
        System.out.println("New websocket connected");
        this.webSockets.add(ws);
        ws.handler(this::addNewData);
        ws.closeHandler(v -> {
            System.out.println("Websocket disconnected");
            this.webSockets.remove(ws);
        });
        ws.exceptionHandler(e -> this.webSockets.remove(ws));
    }

    private void addNewData(final Buffer buffer) {
        System.out.println("Received message: " + buffer.toString());
        final JsonObject jsonObject = buffer.toJsonObject();
        if (jsonObject != null) {
            // System.out.println(jsonObject);
            // Update stats
            final Integer aperture = jsonObject.getInteger("aperture");
            final Double temperature = jsonObject.getDouble("temperature");
            final Long time = jsonObject.getLong("time");
            if (aperture != null && temperature != null && time != null) {
                this.values.addFirst(new DataValue(aperture, temperature, time));
                if (this.values.size() > MAX_SIZE) {
                    this.values.removeLast();
                }
                updateStats();
            }
            // Update state
            try {
                this.state = State.valueOf(jsonObject.getString("state"));
            } catch (final IllegalArgumentException ignored) {
            }
        }
    }

    private void updateStats() {
        if (values.isEmpty()) {
            return;
        }

        double sumTemperature = 0.0;
        double minTemp = Double.MAX_VALUE;
        double maxTemp = Double.MIN_VALUE;

        for (final DataValue value : values) {
            sumTemperature += value.temperature();
            if (value.temperature() < minTemp) {
                minTemp = value.temperature();
            }
            if (value.temperature() > maxTemp) {
                maxTemp = value.temperature();
            }
        }

        this.temperature = values.getFirst().temperature();
        this.aperture = values.getFirst().aperture();
        this.minTemperature = minTemp;
        this.maxTemperature = maxTemp;
        this.averageTemperature = sumTemperature / values.size();
    }
}
