#include "UserConsole.h"
#include <Arduino.h>
#include "config.h"

UserConsole::UserConsole()
{
    this->pLcd = new LiquidCrystal_I2C(0x27, 20, 4);
    this->pChangeModeButton = new ButtonImpl(MODE_BUTTON_PIN);
    this->pPot = new PotImpl(POT_PIN);
}

void UserConsole::turnOnDisplay()
{
    this->pLcd->display();
    this->pLcd->clear();
}

void UserConsole::turnOffDisplay()
{
    this->pLcd->noDisplay();
}

void UserConsole::init()
{
    this->pLcd->init();
    this->pLcd->backlight();
    this->pLcd->noDisplay();
}

bool UserConsole::changeModeSignal()
{
    this->pChangeModeButton->sync();
    return this->pChangeModeButton->isPressed();
}

int UserConsole::getPotValue()
{
    return this->pPot->getValue();
}

void UserConsole::displayWelcome()
{
    this->pLcd->clear();
    this->pLcd->setCursor(6, 1);
    this->pLcd->print("WELCOME");
}

void UserConsole::displayModeAutomatic()
{
    this->pLcd->setCursor(19, 0);
    this->pLcd->print("A");
    this->pLcd->setCursor(0, 3);
    this->pLcd->print("                    ");
}

void UserConsole::displayModeManual()
{
    this->pLcd->setCursor(19, 0);
    this->pLcd->print("M");
}

void UserConsole::displayAperture(const int aperture)
{
    this->pLcd->setCursor(0, 1);
    this->pLcd->print("Aperture: " + String(aperture));
}

void UserConsole::displayTemperature(const double temperature)
{
    this->pLcd->setCursor(0, 3);
    this->pLcd->print("Temperature: " + String(temperature));
}
