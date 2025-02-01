#include "Led.h"
#include "Arduino.h"

Led::Led(const int pin) : pin(pin)
{
    pinMode(pin, OUTPUT);
    switchOff();
}

void Led::switchOn()
{
    digitalWrite(this->pin, HIGH);
}

void Led::switchOff()
{
    digitalWrite(this->pin, LOW);
}