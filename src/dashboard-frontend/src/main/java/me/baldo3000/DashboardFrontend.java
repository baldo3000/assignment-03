package me.baldo3000;

import io.vertx.core.Vertx;
import me.baldo3000.server.HTTPServer;

public class DashboardFrontend {
    public static void main(String[] args) {
        final Vertx vertx = Vertx.vertx();
        final HTTPServer server = new HTTPServer();
        vertx.deployVerticle(server);
    }
}