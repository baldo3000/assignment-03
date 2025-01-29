#ifndef SERVOMOTOR_H
#define SERVOMOTOR_H

class ServoMotor
{
public:
    virtual void on() = 0;
    virtual void off() = 0;
    virtual void setPosition(int angle) = 0;
};

#endif