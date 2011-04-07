#include "config.h"
#include "task.h"
#include <stddef.h>

static task task_list[MAX_TASKS];
static int nbr_tasks;

int add_task(task task)
{
	if (nbr_tasks < MAX_TASKS
		&& task != NULL) {
		task_list[nbr_tasks++] = task;
	}

	return nbr_tasks;
}

int run_tasks(int ms)
{
	int run_again = 0,
		i = 0;

	while (task_list[i]) {
		if ((*task_list[i])(ms)) { 
			run_again = 1;
		}
		i++;
	}

	return run_again;
}
