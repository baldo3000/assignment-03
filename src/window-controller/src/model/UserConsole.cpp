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
    this->pLcd->setCursor(0, 0);
    this->pLcd->print("WELCOME");
}
