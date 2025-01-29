#include "ServoMotorImpl.h"
#include "Arduino.h"

ServoMotorImpl::ServoMotorImpl(const int pin) : pin(pin) {}

void ServoMotorImpl::on()
{
    this->motor.attach(this->pin);
}

void ServoMotorImpl::off()
{
    this->motor.detach();
}

void ServoMotorImpl::setPosition(const int angle)
{
    int correctedAngle = angle;
    if (angle > 180)
    {
        correctedAngle = 180;
    }
    else if (angle < 0)
    {
        correctedAngle = 0;
    }

    const float coeff = (MAX - MIN) / 180;
    this->motor.write(MIN + correctedAngle * coeff);
}