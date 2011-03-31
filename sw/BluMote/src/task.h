#ifndef SCHEDULER_H_
#define SCHEDULER_H_

typedef int (*task)(void);

/*
 * Add a task to the task list.  Returns the number of allocated tasks.
 * If task is NULL then just return the number of tasks. 
 */ 
int add_task(task task);

int run_tasks();

#endif /*SCHEDULER_H_*/
