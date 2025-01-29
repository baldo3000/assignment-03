#ifndef LOGGER_H
#define LOGGER_H

#include "Arduino.h"

class LoggerService
{
public:
    void log(const String &msg);
};

extern LoggerService Logger;

#endif