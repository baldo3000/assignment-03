#include <Arduino.h>
#include "config.h"
#include "model/Connection.h"
#include "model/System.h"
#define MSG_BUFFER_SIZE 50

void callback(char *topic, byte *payload, unsigned int length);
Connection connection(WIFI_SSID, WIFI_PASSWORD, MQTT_SERVER, TOPIC, callback);
System monitoringSystem;

enum State
{
  CONNECTED,
  NETWORK_PROBLEM
} state;

char msg[MSG_BUFFER_SIZE];
unsigned long samplingPeriod;
unsigned long lastSampleTime;

void setup()
{
  Serial.begin(115200);
  randomSeed(micros());
  connection.init();
  state = NETWORK_PROBLEM; // The system always start not connected
  samplingPeriod = 2000;   // 2 seconds
  lastSampleTime = 0;
}

void loop()
{
  switch (state)
  {
  case CONNECTED:
    if (!connection.isConnected())
    {
      state = NETWORK_PROBLEM;
      monitoringSystem.problem();
    }
    else
    {
      connection.loop();
      if (millis() - lastSampleTime > samplingPeriod)
      {
        lastSampleTime = millis();
        const float temperature = monitoringSystem.getTemperature();
        // snprintf(msg, MSG_BUFFER_SIZE, "%f\0", temperature);
        Serial.println("Publishing: " + String(temperature));
        connection.publish(("ts:" + String(temperature)).c_str());
      }
    }
    break;

  case NETWORK_PROBLEM:
    connection.reconnect();
    if (connection.isConnected())
    {
      state = CONNECTED;
      monitoringSystem.ok();
    }
    break;
  }
}

bool isNumeric(const String string)
{
  bool numeric = true;
  if (string == "")
  {
    numeric = false;
  }
  for (unsigned int i = 0; i < string.length(); i++)
  {
    if (!isDigit(string.charAt(i)) && string.charAt(i) != '.')
    {
      numeric = false;
    }
  }
  return numeric;
}

void callback(char *topic, byte *payload, unsigned int length)
{
  // Ensure the payload is null-terminated
  char message[length + 1];
  memcpy(message, payload, length);
  message[length] = '\0';

  Serial.println(String("Message arrived on [") + topic + "] len: " + length + ", message: " + String(message));

  // Detect if the message comes from Control Unit (CU)
  if (strncmp(message, "cu:", 3) == 0)
  {
    const String content = String(message).substring(3);
    if (isNumeric(content))
    {
      samplingPeriod = content.toInt();
      Serial.println("Sampling period changed to: " + String(samplingPeriod));
    }
  }
}