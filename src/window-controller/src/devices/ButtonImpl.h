#ifndef BUTTONIMPL_H
#define BUTTONIMPL_H

#include "Button.h"

#define DEBOUNCE_TIME 30

class ButtonImpl : public Button
{
private:
    int pin;
    bool pressed;
    long lastTimeSync;

public:
    ButtonImpl(const int pin);
    bool isPressed();
    void sync();
};

#endif