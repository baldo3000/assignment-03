package me.baldo3000.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class HTTPServer extends AbstractVerticle {

    private static final int MAX_SIZE = 10;
    private final JsonArray values;

    public HTTPServer() {
        this.values = new JsonArray();
    }

    @Override
    public void start() {
        final Router router = Router.router(vertx);
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
//            final int aperture = res.getInteger("aperture");
//            final double temperature = res.getDouble("temperature");
//            final long time = res.getLong("time");
            System.out.println(jsonObject);
            this.values.add(0, jsonObject);
            if (this.values.size() > MAX_SIZE) {
                this.values.remove(MAX_SIZE);
            }
            response.setStatusCode(200).end();
        }
    }

    private void handleGetData(final RoutingContext routingContext) {
        routingContext.response()
                .putHeader("content-type", "application/json")
                .end(this.values.encodePrettily());
    }
}
