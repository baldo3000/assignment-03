#ifndef CONNECTION_H
#define CONNECTION_H

#include <WiFi.h>
#include <PubSubClient.h>

class Connection
{
private:
    const char *ssid;
    const char *password;
    const char *mqtt_server;
    const char *topic;

    WiFiClient espClient;
    PubSubClient client;

    std::function<void(char *, uint8_t *, unsigned int)> callback;

    void setup_wifi();
    void setup_mqtt();

public:
    Connection(const char *ssid, const char *password, const char *mqtt_server, const char *topic, std::function<void(char *, byte *, unsigned int)> callback);
    void init();
    bool loop();
    bool isConnected();
    void reconnect();
    bool publish(const char *payload);
};

#endif