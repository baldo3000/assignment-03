#ifndef TASK_H
#define TASK_H

class Task
{
private:
    int myPeriod;
    int timeElapsed;
    bool active;

public:
    Task()
    {
        this->active = false;
    }

    virtual void init(const int period)
    {
        this->myPeriod = period;
        this->active = true;
        this->timeElapsed = 0;
    }

    virtual void tick() = 0;

    bool updateAndCheckTime(const int basePeriod)
    {
        this->timeElapsed += basePeriod;
        if (this->timeElapsed >= this->myPeriod)
        {
            this->timeElapsed = 0;
            return true;
        }
        else
        {
            return false;
        }
    }

    bool isActive()
    {
        return this->active;
    }

    virtual void setActive(const bool active)
    {
        this->timeElapsed = 0;
        this->active = active;
    }
};

#endif