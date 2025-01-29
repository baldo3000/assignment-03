#include "WorkFlowTask.h"
#include "kernel/Logger.h"
#include "kernel/MsgService.h"
#include "config.h"
#include <Arduino.h>
#include <avr/sleep.h>
#include <EnableInterrupt.h>

#define LOG_TAG "[WF] "

WorkflowTask::WorkflowTask(UserConsole *pUserConsole) : pUserConsole(pUserConsole)
{
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
        checkWindowMsg();
        break;

    case MANUAL:
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
            Logger.log(msg->getContent());
            if (msg->getContent() == "level")
            {
                level = true;
            }
            delete msg;
        }
    }
    Logger.log(String(level));
    return level;
}
