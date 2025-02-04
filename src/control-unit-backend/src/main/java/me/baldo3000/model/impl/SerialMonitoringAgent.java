package me.baldo3000.model.impl;

import me.baldo3000.model.api.SerialCommChannel;

public class SerialMonitoringAgent extends Thread {

    private final SerialCommChannel channel;
    private String latestMessage;

    public SerialMonitoringAgent(final SerialCommChannel channel) throws Exception {
        this.channel = channel;
    }

    public void run() {
        while (true) {
            try {
                this.latestMessage = channel.receiveMsg();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
