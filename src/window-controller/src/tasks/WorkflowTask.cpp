#include "WorkFlowTask.h"
#include "kernel/Logger.h"
#include "kernel/MsgService.h"
#include "config.h"
#include <Arduino.h>

#define LOG_TAG "[WF] "
#define CHANGE_MODE_TIMEOUT 250L
#define WINDOW_MAX_APERTURE_ANGLE 90

WorkflowTask::WorkflowTask(UserConsole *pUserConsole) : pUserConsole(pUserConsole)
{
    this->pWindow = new ServoMotorImpl(WINDOW_MOTOR_PIN);
    this->pWindow->on();
    this->currentAperture = 0;
    this->reportedTemperature = NAN;
    setState(AUTOMATIC);
    // this->pUserConsole->displayWelcome();
}

void WorkflowTask::setState(const State state)
{
    this->state = state;
    this->stateTimestamp = millis();
    this->justEntered = true;
}

long WorkflowTask::elapsedTimeInState()
{
    return millis() - this->stateTimestamp;
}

bool WorkflowTask::doOnce()
{
    if (this->justEntered)
    {
        this->justEntered = false;
        return true;
    }
    return false;
}

void WorkflowTask::tick()
{
    checkMsg();
    switch (this->state)
    {
    case AUTOMATIC:
        if (doOnce())
        {
            Logger.log(String(LOG_TAG) + "AUTOMATIC");
            this->pUserConsole->displayModeAutomatic();
        }
        if (this->pUserConsole->changeModeSignal() && this->elapsedTimeInState() > CHANGE_MODE_TIMEOUT)
        {
            setState(MANUAL);
        }
        this->pWindow->setPosition(this->currentAperture);
        this->pUserConsole->displayAperture(this->currentAperture);
        break;

    case MANUAL:
        if (doOnce())
        {
            Logger.log(String(LOG_TAG) + "MANUAL");
            this->pUserConsole->displayModeManual();
        }
        if (this->pUserConsole->changeModeSignal() && this->elapsedTimeInState() > CHANGE_MODE_TIMEOUT)
        {
            setState(AUTOMATIC);
        }
        this->currentAperture = this->pUserConsole->getPotValue();
        const int angle = map(this->currentAperture, 0, 100, 0, WINDOW_MAX_APERTURE_ANGLE);
        // Serial.println(String("aperture: ") + this->currentAperture + " angle: " + angle);
        this->pWindow->setPosition(angle);
        this->pUserConsole->displayAperture(this->currentAperture);
        this->pUserConsole->displayTemperature(this->reportedTemperature);
        break;
    }
}

bool isNumeric(const String string)
{
    bool numeric = true;
    if (string == "")
    {
        numeric = false;
    }
    for (unsigned int i = 0; i < string.length(); i++)
    {
        if (!isDigit(string.charAt(i)) && string.charAt(i) != '.')
        {
            numeric = false;
        }
    }
    return numeric;
}

void WorkflowTask::checkMsg()
{
    if (MsgService.isMsgAvailable())
    {
        Msg *msg = MsgService.receiveMsg();
        if (msg != NULL)
        {
            String content = msg->getContent();
            content.trim();
            // Logger.log(content);
            char contentCopy[content.length()];
            strcpy(contentCopy, content.c_str());

            // String mode = strtok(contentCopy, ";"); changing mode is disabled from the control unit
            // String aperture = strtok(NULL, ";");
            String aperture = strtok(contentCopy, ";");
            String temperature = strtok(NULL, ";");

            // Change mode checks
            // if (this->state == AUTOMATIC && mode == "M")
            // {
            //     setState(MANUAL);
            // }
            // else if (this->state == MANUAL && mode == "A")
            // {
            //     setState(AUTOMATIC);
            // }

            // Set values checks
            if (this->state == AUTOMATIC && isNumeric(aperture))
            {
                const int apertureInt = aperture.toInt();
                // Logger.log("aperture: " + String(aperture));
                if (apertureInt >= 0 && apertureInt <= 100)
                {
                    this->currentAperture = apertureInt;
                }
            }
            if (isNumeric(temperature))
            {
                const double temperatureDouble = temperature.toDouble();
                // Logger.log("temperature: " + String(temperatureDouble));
                this->reportedTemperature = temperatureDouble;
            }

            delete msg;
        }
    }
}
