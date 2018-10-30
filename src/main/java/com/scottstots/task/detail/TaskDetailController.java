package com.scottstots.task.detail;

import java.util.List;

import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.qvc.productplanning.exceptions.DataNotFoundException;
import com.scottstots.task.TaskDetail;

@RestController
public class TaskDetailController
{
	private final TaskDetailService taskDetailService;
	
	public TaskDetailController(TaskDetailService taskDetailService) {
		this.taskDetailService = taskDetailService;
	}
	
	@GetMapping("/tasks/types/{type}")
	public List<TaskDetail> retrieveTaskDetailsByType(@PathVariable("type") String type) {
		List<TaskDetail> taskDetails = this.taskDetailService.retrieveTaskDetailsByType(type);
		if(CollectionUtils.isEmpty(taskDetails)) {
			throw new DataNotFoundException("No task details found for given type");
		}
		return taskDetails;
	}
}
