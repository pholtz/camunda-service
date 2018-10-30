package com.scottstots.task.hierarchy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.scottstots.task.TaskElement;


@Service
public class TaskHierarchyService
{
	private static final Logger log = LoggerFactory.getLogger(TaskHierarchyService.class);
	private final ProcessEngine processEngine;
	private final BpmnModelInstance bpmnModel;

	public TaskHierarchyService(ProcessEngine processEngine, @Qualifier("bpmnResq") BpmnModelInstance bpmnModel) {
		this.processEngine = processEngine;
		this.bpmnModel = bpmnModel;
	}

	public TaskElement retrieveTasksWithHierarchy() {
		org.camunda.bpm.engine.TaskService taskService = this.processEngine.getTaskService();
		TaskQuery taskQuery = taskService.createTaskQuery().active();
		List<Task> tasks = taskQuery.list();

		Map<String, TaskElement> aggregatedTaskElements = new HashMap<>();
		for (Task task : tasks) {
			// Retrieve the partially aggregated task element using the "taskDefinitionKey"
			// field. Which is the "id" field on the bpmn modeler. Let's assume different
			// tasks have different id's
			TaskElement taskElement = aggregatedTaskElements.get(task.getTaskDefinitionKey());
			if (taskElement == null) {
				taskElement = new TaskElement();
				taskElement.setKey(task.getTaskDefinitionKey());
				taskElement.setName(task.getName());
				taskElement.setDesignation("0");
				taskElement.setImage("");
			}
			// This is kinda janky but increment the "designation" (number of executions in
			// this state) as a string
			int elementCount = Integer.valueOf(taskElement.getDesignation()).intValue();
			taskElement.setDesignation(String.valueOf(elementCount + 1));
			aggregatedTaskElements.put(task.getTaskDefinitionKey(), taskElement);
		}

		// Populate the subordinates of each aggregated task
		for (Entry<String, TaskElement> entry : aggregatedTaskElements.entrySet()) {
			UserTask element = null;
			ModelElementInstance modelElement = this.bpmnModel.getModelElementById(entry.getKey());
			if (modelElement instanceof UserTask) {
				element = (UserTask) modelElement;
				List<FlowNode> successors = this.retrieveSuccessorTasksRecursively(element);
				List<TaskElement> subordinates = new ArrayList<>();
				// For each successor contained by the element, nest any mapped instances under
				// this element
				for (FlowNode successor : successors) {
					log.info("Found successor node for {} with id {}", element.getId(), successor.getId());
					TaskElement subordinate = aggregatedTaskElements.get(successor.getId());
					subordinates.add(subordinate);
				}
				entry.getValue().setSubordinates(subordinates);
			}
		}

		// Gather the start events -- for demo purposes we are just going to take the
		// first task of the first start event
		UserTask rootTask = this.retrieveRootTask();

		// Recursively arrange the aggregated, subordinated tasks under the root task
		TaskElement rootTaskElement = this.foldSubordinatedTasks(rootTask.getId(), aggregatedTaskElements);
		
		return rootTaskElement;
	}
	
	/**
	 * 
	 * @return
	 */
	private TaskElement foldSubordinatedTasks(String key, Map<String, TaskElement> aggregatedTaskElements) {
		TaskElement taskElement = aggregatedTaskElements.get(key);
		if(taskElement == null || CollectionUtils.isEmpty(taskElement.getSubordinates())) {
			return taskElement;
		}
		
		List<TaskElement> foldedSubordinates = new ArrayList<>();
		for(TaskElement subordinate : taskElement.getSubordinates()) {
			TaskElement foldedSubordinate = this.foldSubordinatedTasks(subordinate.getKey(), aggregatedTaskElements);
			foldedSubordinates.add(foldedSubordinate);
		}
		taskElement.setSubordinates(foldedSubordinates);
		return taskElement;
	}

	/**
	 * This method assumes that your bpmn has a single start event -- probably not a
	 * great idea long-term considering bpmn is a full directed acyclic graph with
	 * multiple potential entry points.
	 * 
	 * @return
	 */
	private UserTask retrieveRootTask() {
		Collection<StartEvent> startEvents = this.bpmnModel.getModelElementsByType(StartEvent.class);
		for (StartEvent startEvent : startEvents) {
			for (FlowNode firstTask : this.retrieveSuccessorTasksRecursively(startEvent)) {
				if (firstTask instanceof UserTask) {
					UserTask rootTask = (UserTask) firstTask;
					return rootTask;
				}
			}
		}
		return null;
	}

	/**
	 * Given a starting task node, search the bpmn tree in all downstream directions
	 * until a terminal or task node is reached. Returns and aggregate any found
	 * task leaf nodes.
	 * 
	 * @return
	 */
	private List<FlowNode> retrieveSuccessorTasksRecursively(FlowNode sourceTask) {
		List<FlowNode> successorTasks = new ArrayList<>();
		if (sourceTask == null) { // Base case
			return successorTasks;
		}

		for (FlowNode successor : sourceTask.getSucceedingNodes().list()) {
			if (successor instanceof UserTask) {
				// Hit leaf node -- stop traversing this branch and just add it to the list of
				// tasks
				successorTasks.add(successor);
			} else {
				// This is either a terminal point or a continuing branch -- keep traversing to
				// find out
				List<FlowNode> subbranchTasks = this.retrieveSuccessorTasksRecursively(successor);
				successorTasks.addAll(subbranchTasks);
			}
		}
		return successorTasks;
	}
	
	private TaskElement generateSampleData() {
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
}
