package com.scottstots.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TaskController
{
	private static final Logger log = LoggerFactory.getLogger(TaskController.class);
	private final ProcessEngine processEngine;
	
	public TaskController(ProcessEngine processEngine) {
		this.processEngine = processEngine;
	}
	
	@GetMapping("/v1/us/task-hierarchy")
	public TaskElement retrieveTasksWithHierarchy() {
		TaskService taskService = processEngine.getTaskService();
		TaskQuery taskQuery = taskService.createTaskQuery().active();
		List<Task> tasks = taskQuery.list();
		
		Map<String, TaskElement> aggregatedTaskElements = new HashMap<>();
		for(Task task : tasks) {
			log.info("Iterating over task with id {} and parent task id {}", task.getId(), task.getParentTaskId());
			// Retrieve the partially aggregated task element using the "id" field of the bpmn
			TaskElement taskElement = aggregatedTaskElements.get(task.getTaskDefinitionKey());
			if(taskElement == null) {
				taskElement = new TaskElement();
				taskElement.setName(task.getTaskDefinitionKey());
				taskElement.setDesignation("0");
				taskElement.setImage("");
			}
			int elementCount = Integer.valueOf(taskElement.getDesignation()).intValue();
			taskElement.setDesignation(String.valueOf(elementCount + 1));
			aggregatedTaskElements.put(task.getTaskDefinitionKey(), taskElement);
		}
		
		return null;
	}
}
