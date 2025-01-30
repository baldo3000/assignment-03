#include "WorkFlowTask.h"
#include "kernel/Logger.h"
#include "kernel/MsgService.h"
#include "config.h"
#include <Arduino.h>

#define LOG_TAG "[WF] "
#define CHANGE_MODE_TIMEOUT 250L

WorkflowTask::WorkflowTask(UserConsole *pUserConsole) : pUserConsole(pUserConsole)
{
    this->pWindow = new ServoMotorImpl(WINDOW_MOTOR_PIN);
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

void wakeUpNow() {}

void WorkflowTask::tick()
{
    switch (this->state)
    {
    case AUTOMATIC:
        if (this->pUserConsole->changeModeSignal() && this->elapsedTimeInState() > CHANGE_MODE_TIMEOUT)
        {
            Logger.log(String(LOG_TAG) + "MANUAL");
            setState(MANUAL);
        }
        break;

    case MANUAL:
        if (this->pUserConsole->changeModeSignal() && this->elapsedTimeInState() > CHANGE_MODE_TIMEOUT)
        {
            Logger.log(String(LOG_TAG) + "AUTOMATIC");
            setState(AUTOMATIC);
        }
        Logger.log(String(LOG_TAG) + this->pUserConsole->getPotValue());
        break;
    }
}

bool WorkflowTask::checkWindowMsg()
{
    bool level = false;
    if (MsgService.isMsgAvailable())
    {
        Msg *msg = MsgService.receiveMsg();
        if (msg != NULL)
        {
            String content = msg->getContent();
            content.trim();
            // Logger.log(content);
            if (content == "level")
            {
                level = true;
            }
            delete msg;
        }
    }
    return level;
}
