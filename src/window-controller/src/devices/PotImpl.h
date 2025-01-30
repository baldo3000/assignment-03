#ifndef POTIMPL_H
#define POTIMPL_H

#include "Pot.h"

class PotImpl : public Pot
{
private:
    int pin;

public:
    PotImpl(const int pin);
    int getValue();
};

#endif