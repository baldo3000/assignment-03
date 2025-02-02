#include "System.h"

System::System() : ledGreen(LED_GREEN_PIN), ledRed(LED_RED_PIN), temperatureSensor(TEMPERATURE_SENSOR_PIN)
{
    problem(); // The system always start not connected
}

void System::ok()
{
    ledGreen.switchOn();
    ledRed.switchOff();
}

void System::problem()
{
    ledGreen.switchOff();
    ledRed.switchOn();
}

float System::getTemperature()
{
    return temperatureSensor.getTemperature();
}