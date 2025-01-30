#ifndef WORKFLOWTASK_H
#define WORKFLOWTASK_H

#include "kernel/Task.h"
#include "model/UserConsole.h"
#include "devices/ServoMotorImpl.h"

class WorkflowTask : public Task
{
private:
    enum State
    {
        AUTOMATIC,
        MANUAL
    } state;

    ServoMotorImpl *pWindow;

    void setState(State state);
    long elapsedTimeInState();
    bool doOnce();
    bool checkWindowMsg();

    long stateTimestamp;
    bool justEntered;

    UserConsole *pUserConsole;

public:
    WorkflowTask(UserConsole *pUserConsole);
    void tick();
};

#endif