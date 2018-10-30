package com.scottstots.task;

import java.util.List;

public class TaskElement
{
	private String key;
	private String name;
	private String designation; // Number of executions in this status
	private String image;
	private List<TaskElement> subordinates;
	
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDesignation() {
		return designation;
	}
	public void setDesignation(String designation) {
		this.designation = designation;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public List<TaskElement> getSubordinates() {
		return subordinates;
	}
	public void setSubordinates(List<TaskElement> subordinates) {
		this.subordinates = subordinates;
	}
}
