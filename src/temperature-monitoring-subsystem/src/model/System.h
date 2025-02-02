#ifndef SYSTEM_H
#define SYSTEM_H

#include "devices/Led.h"
#include "devices/TemperatureSensorTMP36.h"
#include "config.h"

class System
{
private:
    Led ledGreen;
    Led ledRed;
    TemperatureSensorTMP36 temperatureSensor;

public:
    System();
    void ok();
    void problem();
    float getTemperature();
};

#endif