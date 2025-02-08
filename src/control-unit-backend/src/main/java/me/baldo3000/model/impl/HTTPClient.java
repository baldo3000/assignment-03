package me.baldo3000.model.impl;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebSocketClient;

import java.util.concurrent.TimeUnit;

public class HTTPClient extends AbstractVerticle {

    public static final String INCOMING_ADDRESS = "http-incoming-messages";
    public static final String OUTGOING_ADDRESS = "http-outgoing-messages";

    @Override
    public void start() {
        startClient();
    }

    private void startClient() {
        final WebSocketClient client = vertx.createWebSocketClient();
        client.connect(8080, "127.0.0.1", "/ws")
                .onSuccess(ws -> {
                    System.out.println("Websocket connected to http server!");
                    ws.textMessageHandler(msg -> {
                        // System.out.println("Received message from server: " + msg);
                        this.vertx.eventBus().send(INCOMING_ADDRESS, msg);
                    }).closeHandler((__) -> {
                        System.out.println("Connection with http server lost, reconnecting in 10 seconds");
                        restart(client, 10);
                    });
                    this.vertx.eventBus().consumer(OUTGOING_ADDRESS, message -> {
                        sendMessage(ws, message.body().toString());
                    });
                }).onFailure(e -> {
                    System.out.println("Connection with http server failed, retrying in 10 seconds");
                    restart(client, 10);
                });
    }

    private void sendMessage(final WebSocket ws, String msg) {
        ws.writeTextMessage(msg)
                .onFailure(response -> System.out.println("Could not send stats, error: " + response.getMessage()));
    }

    private void restart(final WebSocketClient client, final int delay) {
        client.close();
        this.vertx.eventBus().consumer(OUTGOING_ADDRESS).unregister();
        vertx.setTimer(TimeUnit.SECONDS.toMillis(delay), (__) -> {
            startClient();
        });
    }
}
