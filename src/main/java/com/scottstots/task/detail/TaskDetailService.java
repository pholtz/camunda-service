package com.scottstots.task.detail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.scottstots.task.TaskDetail;
import com.scottstots.task.hierarchy.TaskHierarchyService;

@Service
public class TaskDetailService
{
	private static final Logger log = LoggerFactory.getLogger(TaskHierarchyService.class);
	private final ProcessEngine processEngine;
	
	public TaskDetailService(ProcessEngine processEngine) {
		this.processEngine = processEngine;
	}
	
	public List<TaskDetail> retrieveTaskDetailsByType(String type) {
		org.camunda.bpm.engine.TaskService taskService = this.processEngine.getTaskService();
		TaskQuery taskQuery = taskService.createTaskQuery().taskName(type);
		List<Task> tasks = taskQuery.list();
		log.info("Active task query returned {} results", tasks.size());
		
		List<TaskDetail> taskDetails = new ArrayList<>();
		for(Task task : tasks) {
			TaskDetail taskDetail = new TaskDetail();
			taskDetail.setId(task.getId());
			taskDetail.setStatus(type);
			taskDetail.setRequestType("");
			taskDetail.setRequestedBy("");
			taskDetail.setTitle("");
			
			Map<String, Object> taskVariables = taskService.getVariables(task.getId());
			
			Object requestType = taskVariables.get("RequestType");
			if(requestType instanceof String) {
				taskDetail.setRequestType((String) requestType);
			}
			
			Object requestedBy = taskVariables.get("RequestedBy");
			if(requestedBy instanceof String) {
				taskDetail.setRequestedBy((String) requestedBy);
			}
			
			Object title = taskVariables.get("Title");
			if(title instanceof String) {
				taskDetail.setTitle((String) title);
			}
			taskDetails.add(taskDetail);
		}
		return taskDetails;
	}
}
