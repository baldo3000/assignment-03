#include "PotImpl.h"
#include "Arduino.h"

PotImpl::PotImpl(const int pin) : pin(pin) {}

int PotImpl::getValue()
{
    return map(analogRead(this->pin), 0, 1023, 0, 100);
}
