package com.scottstots.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.Query;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TaskController
{
	private static final Logger log = LoggerFactory.getLogger(TaskController.class);
	private final ProcessEngine processEngine;
	private final BpmnModelInstance bpmnModel;
	
	public TaskController(ProcessEngine processEngine,
			BpmnModelInstance bpmnModel) {
		this.processEngine = processEngine;
		this.bpmnModel = bpmnModel;
	}
	
	@GetMapping("/task-hierarchy")
	public TaskElement retrieveTasksWithHierarchy() {
		TaskService taskService = this.processEngine.getTaskService();
		TaskQuery taskQuery = taskService.createTaskQuery().active();
		List<Task> tasks = taskQuery.list();
		
		Map<String, TaskElement> aggregatedTaskElements = new HashMap<>();
		for(Task task : tasks) {
			// Retrieve the partially aggregated task element using the "name" field of the bpmn
			// Assume that different tasks will have unique names
			TaskElement taskElement = aggregatedTaskElements.get(task.getName());
			if(taskElement == null) {
				taskElement = new TaskElement();
				taskElement.setName(task.getName());
				taskElement.setDesignation("0");
				taskElement.setImage("");
			}
			int elementCount = Integer.valueOf(taskElement.getDesignation()).intValue();
			taskElement.setDesignation(String.valueOf(elementCount + 1));
			aggregatedTaskElements.put(task.getName(), taskElement);
		}
		
		ModelElementInstance instance = this.bpmnModel.getModelElementById("approved_state");
		log.info("{}", instance);
		
		for(Entry<String, TaskElement> entry : aggregatedTaskElements.entrySet()) {
			UserTask element = null;
			ModelElementInstance modelElement = this.bpmnModel.getModelElementById(entry.getKey());
			if(modelElement instanceof UserTask) {
				element = (UserTask) modelElement;
				Query<FlowNode> query = element.getSucceedingNodes();
				List<FlowNode> successorNodes = query.list();
				for(FlowNode successor : successorNodes) {
					String id = successor.getId();
					log.info("Found successor node for {} with id {}", element.getId(), id);
				}
			}
		}
		
		TaskElement root = new TaskElement();
		root.setName("Root");
		root.setDesignation("1");
		root.setImage("");
		List<TaskElement> children = new ArrayList<>();
		TaskElement left = new TaskElement();
		left.setName("Left");
		left.setDesignation("2");
		left.setImage("");
		left.setSubordinates(new ArrayList<>());
		children.add(left);
		TaskElement right = new TaskElement();
		right.setName("Right");
		right.setDesignation("3");
		right.setImage("");
		right.setSubordinates(new ArrayList<>());
		children.add(right);
		root.setSubordinates(children);
		return root;
	}
	
	@GetMapping("/tasks/types/{type}")
	public List<TaskDetail> retrieveTaskDetailsByType(@PathVariable("type") String type) {
		TaskService taskService = this.processEngine.getTaskService();
		TaskQuery taskQuery = taskService.createTaskQuery().taskName(type);
		List<Task> tasks = taskQuery.list();
		
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
