#include "TemperatureSensorTMP36.h"
#include <Arduino.h>

TemperatureSensorTMP36::TemperatureSensorTMP36(const int pin) : pin(pin) {}

float TemperatureSensorTMP36::getTemperature()
{
    const float value = analogRead(this->pin);
    return (value / 1023.0F - 0.5F) * 100.0F;
}