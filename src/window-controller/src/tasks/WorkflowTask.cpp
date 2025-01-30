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
    setState(AUTOMATIC);
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
        }
        if (this->pUserConsole->changeModeSignal() && this->elapsedTimeInState() > CHANGE_MODE_TIMEOUT)
        {
            setState(MANUAL);
        }
        this->pWindow->setPosition(this->currentAperture);
        break;

    case MANUAL:
        if (doOnce())
        {
            Logger.log(String(LOG_TAG) + "MANUAL");
        }
        if (this->pUserConsole->changeModeSignal() && this->elapsedTimeInState() > CHANGE_MODE_TIMEOUT)
        {
            setState(AUTOMATIC);
        }
        this->currentAperture = this->pUserConsole->getPotValue();
        const int angle = map(this->currentAperture, 0, 100, 0, WINDOW_MAX_APERTURE_ANGLE);
        this->pWindow->setPosition(angle);
        break;
    }
}

bool isNumeric(String string)
{
    bool numeric = true;
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
            if (content == "manual" && this->state == AUTOMATIC)
            {
                setState(MANUAL);
            }
            else if (content == "automatic" && this->state == MANUAL)
            {
                setState(AUTOMATIC);
            }
            else if (this->state == AUTOMATIC && isNumeric(content))
            {
                const int aperture = content.toInt();
                // Logger.log("aperture: " + String(aperture));
                if (aperture >= 0 && aperture <= WINDOW_MAX_APERTURE_ANGLE)
                {
                    this->currentAperture = aperture;
                }
            }
            delete msg;
        }
    }
}
