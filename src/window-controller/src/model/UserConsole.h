#ifndef USERCONSOLE_H
#define USERCONSOLE_H

#include "config.h"
#include "devices/ButtonImpl.h"
#include <Wire.h>
#include <LiquidCrystal_I2C.h>

class UserConsole
{
private:
    ButtonImpl *pChangeModeButton;
    LiquidCrystal_I2C *pLcd;

public:
    UserConsole();

    void init();

    void turnOffDisplay();
    void turnOnDisplay();

    bool changeModeSignal();

    void displayWelcome();
};

#endif