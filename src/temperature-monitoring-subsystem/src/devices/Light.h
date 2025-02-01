#ifndef LIGHT_H
#define LIGHT_H

class Light
{
public:
    virtual void switchOn() = 0;
    virtual void switchOff() = 0;
};

#endif