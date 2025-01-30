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

    long stateTimestamp;
    bool justEntered;
    int currentAperture;

    UserConsole *pUserConsole;
    ServoMotorImpl *pWindow;

    void setState(State state);
    long elapsedTimeInState();
    bool doOnce();
    void checkMsg();

public:
    WorkflowTask(UserConsole *pUserConsole);
    void tick();
};

#endif