#ifndef SCHEDULER_H
#define SCHEDULER_H

#include "Task.h"

#define MAX_TASKS 2

class Scheduler
{
private:
    int basePeriod;
    int nTasks;
    Task *taskList[MAX_TASKS];

public:
    void init(int basePeriod);
    virtual bool addTask(Task *task);
    virtual void schedule();
};

#endif