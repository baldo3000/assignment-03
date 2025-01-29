#ifndef WORKFLOWTASK_H
#define WORKFLOWTASK_H

#include "kernel/Task.h"
#include "model/UserConsole.h"

class WorkflowTask : public Task
{
private:
    enum State
    {
        AUTOMATIC,
        MANUAL
    } state;

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