#include "Scheduler.h"
#include <TimerOne.h>

volatile bool timerFlag;

void timerHandler(void)
{
    timerFlag = true;
}

void Scheduler::init(const int basePeriod)
{
    this->basePeriod = basePeriod;
    timerFlag = false;
    long period = 1000l * basePeriod;
    Timer1.initialize(period);
    Timer1.attachInterrupt(timerHandler);
    this->nTasks = 0;
}

bool Scheduler::addTask(Task *task)
{
    if (this->nTasks < MAX_TASKS - 1)
    {
        this->taskList[nTasks] = task;
        this->nTasks++;
        return true;
    }
    else
    {
        return false;
    }
}

void Scheduler::schedule()
{
    while (!timerFlag)
    {
    }
    timerFlag = false;
    for (int i = 0; i < this->nTasks; i++)
    {
        Task *task = this->taskList[i];
        if (task->isActive() && task->updateAndCheckTime(this->basePeriod))
        {
            task->tick();
        }
    }
}
