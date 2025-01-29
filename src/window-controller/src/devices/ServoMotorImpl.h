#ifndef __SERVO_MOTOR_IMPL__
#define __SERVO_MOTOR_IMPL__

#include "ServoMotor.h"
#include "utilities/ServoTimer2.h"

#define MIN 544.0
#define MAX 2400.0

class ServoMotorImpl : public ServoMotor
{
private:
    int pin;
    ServoTimer2 motor;

public:
    ServoMotorImpl(int pin);
    void on();
    void off();
    void setPosition(int angle);
};

#endif