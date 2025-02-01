#include <Arduino.h>
#include "config.h"
#include "Connection.h"
#define MSG_BUFFER_SIZE 50

/* Wifi network info */
const char *ssid = "Casa Baldazzi";
const char *password = "casakikina2020";

/* MQTT connection info */
const char *mqtt_server = "broker.mqtt-dashboard.com";
const char *topic = "baldo-assignment03";

void callback(char *topic, byte *payload, unsigned int length)
{
  Serial.println(String("Message arrived on [") + topic + "] len: " + length);
}

Connection connection(ssid, password, mqtt_server, topic, callback);

unsigned long lastMsgTime = 0;
char msg[MSG_BUFFER_SIZE];
int value = 0;

void setup()
{
  Serial.begin(115200);
  connection.init();
  randomSeed(micros());
}

void loop()
{
  if (!connection.isConnected())
  {
    connection.reconnect();
  }
  connection.loop();

  unsigned long now = millis();
  if (now - lastMsgTime > 10000)
  {
    lastMsgTime = now;
    value++;

    /* creating a msg in the buffer */
    snprintf(msg, MSG_BUFFER_SIZE, "hello world #%ld", value);

    Serial.println(String("Publishing message: ") + msg);

    /* publishing the msg */
    connection.publish(msg);
  }
}