/*
 *  Assignment 03 - Smart Temperature Monitoring - Andrea Baldazzi 0001071149
 */

#include <Arduino.h>
#include "config.h"
#include "kernel/Scheduler.h"
#include "kernel/Logger.h"
#include "kernel/MsgService.h"
#include "model/UserConsole.h"
#include "tasks/WorkflowTask.h"

Scheduler scheduler;
UserConsole *pUserConsole;

void setup()
{
  MsgService.init();
  scheduler.init(100);

  pUserConsole = new UserConsole();
  pUserConsole->init();
  pUserConsole->turnOnDisplay();

  WorkflowTask *pWorkflowTask = new WorkflowTask(pUserConsole);
  pWorkflowTask->init(100);

  scheduler.addTask(pWorkflowTask);
}

void loop()
{
  scheduler.schedule();
}
