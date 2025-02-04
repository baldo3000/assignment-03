#include "PotImpl.h"
#include "Arduino.h"

PotImpl::PotImpl(const int pin) : pin(pin) {}

/*
 * Map function from Arduino modified for rounding to nearest integer
 */
int mapRound(double x, double in_min, double in_max, double out_min, double out_max)
{
    return round((x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min);
}

int PotImpl::getValue()
{
    return mapRound(analogRead(this->pin), 0, 1023, 0, 100);
}
