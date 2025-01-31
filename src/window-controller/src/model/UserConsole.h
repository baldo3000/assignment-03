#ifndef USERCONSOLE_H
#define USERCONSOLE_H

#include "config.h"
#include "devices/ButtonImpl.h"
#include "devices/PotImpl.h"
#include <Wire.h>
#include <LiquidCrystal_I2C.h>

class UserConsole
{
private:
    ButtonImpl *pChangeModeButton;
    PotImpl *pPot;
    LiquidCrystal_I2C *pLcd;

public:
    UserConsole();

    void init();

    void turnOffDisplay();
    void turnOnDisplay();

    bool changeModeSignal();
    int getPotValue();

    void displayWelcome();
    void displayAperture(int aperture);
    void displayTemperature(double temperature);
    void displayModeAutomatic();
    void displayModeManual();
};

#endif