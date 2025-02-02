#include "Connection.h"

/* MQTT client management */
WiFiClient espClient;
PubSubClient client(espClient);

Connection::Connection(const char *ssid, const char *password, const char *mqtt_server, const char *topic, std::function<void(char *, byte *, unsigned int)> callback)
    : ssid(ssid), password(password), mqtt_server(mqtt_server), topic(topic), callback(callback), client(this->espClient)
{
}

void Connection::setup_wifi()
{
    delay(10);
    Serial.println(String("Connecting to ") + this->ssid);
    WiFi.mode(WIFI_STA);
    WiFi.begin(this->ssid, this->password);
    while (WiFi.status() != WL_CONNECTED)
    {
        delay(500);
        Serial.print(".");
    }
    Serial.println("");
    Serial.println("WiFi connected");
    Serial.println("IP address: ");
    Serial.println(WiFi.localIP());
}

void Connection::setup_mqtt()
{
    this->client.setServer(this->mqtt_server, 1883);
    this->client.setCallback(callback);
}

void Connection::init()
{
    this->setup_wifi();
    this->setup_mqtt();
}

bool Connection::loop()
{
    return this->client.loop();
}

bool Connection::isConnected()
{
    return this->client.connected();
}

void Connection::reconnect()
{
    // Loop until we're reconnected
    while (!this->client.connected())
    {
        Serial.print("Attempting MQTT connection...");

        // Create a random client ID
        const String clientId = String("baldo3000-client-") + String(random(0xffff), HEX);

        // Attempt to connect
        if (this->client.connect(clientId.c_str()))
        {
            Serial.println("connected");
            // Once connected, publish an announcement...
            // client.publish("outTopic", "hello world");
            // ... and resubscribe
            this->client.subscribe(this->topic);
        }
        else
        {
            Serial.print("failed, rc=");
            Serial.print(this->client.state());
            Serial.println(" try again in 5 seconds");
            // Wait 5 seconds before retrying
            delay(5000);
        }
    }
}

bool Connection::publish(const char *payload)
{
    return this->client.publish(this->topic, payload);
}