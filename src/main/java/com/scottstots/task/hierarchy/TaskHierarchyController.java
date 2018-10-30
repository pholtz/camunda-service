package com.scottstots.task.hierarchy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scottstots.task.TaskElement;

@RestController
public class TaskHierarchyController
{
	private static final Logger log = LoggerFactory.getLogger(TaskHierarchyController.class);
	private final TaskHierarchyService taskHierarchyService;
	
	public TaskHierarchyController(TaskHierarchyService taskHierarchyService) {
		this.taskHierarchyService = taskHierarchyService;
	}
	
	@GetMapping("/task-hierarchy")
	public TaskElement retrieveTasksWithHierarchy() {
		TaskElement rootTask = this.taskHierarchyService.retrieveTasksWithHierarchy();
		return rootTask;
	}
}
