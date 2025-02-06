package me.baldo3000.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import me.baldo3000.common.DataValue;

import java.util.ArrayList;
import java.util.List;

public class HTTPServer extends AbstractVerticle {

    private static final int MAX_SIZE = 50;
    private final List<DataValue> values;
    private int aperture;
    private double temperature;
    private double minTemperature;
    private double maxTemperature;
    private double averageTemperature;

    public HTTPServer() {
        this.values = new ArrayList<>(MAX_SIZE);
        this.aperture = 0;
        this.temperature = 0.0;
        this.minTemperature = 0.0;
        this.maxTemperature = 0.0;
        this.averageTemperature = 0.0;
    }

    @Override
    public void start() {
        final Router router = Router.router(vertx);
        router.route().handler(StaticHandler.create().setCachingEnabled(true));
        router.route().handler(BodyHandler.create());
        router.route(HttpMethod.POST, "/api/data").handler(this::handleAddNewData);
        router.route(HttpMethod.GET, "/api/data").handler(this::handleGetData);

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080)
                .onSuccess(server -> System.out.println("HTTP server started on port " + server.actualPort()));
    }

    private void handleAddNewData(final RoutingContext routingContext) {
        final HttpServerResponse response = routingContext.response();
        final JsonObject jsonObject = routingContext.body().asJsonObject();
        if (jsonObject == null) {
            response.setStatusCode(400).end();
        } else {
            System.out.println(jsonObject);
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
            response.setStatusCode(200).end();
        }
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
