package com.scottstots.task;

public class TaskDetail
{
/*
 * 'id':
16,

'requestType':
'Mouse',

'title':
'Please help need mouse',

'requestedBy':
'Andres',

'status':
'Manager Approval'

 */
	
	private String id;
	private String requestType;
	private String title;
	private String requestedBy;
	private String status;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getRequestType() {
		return requestType;
	}
	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getRequestedBy() {
		return requestedBy;
	}
	public void setRequestedBy(String requestedBy) {
		this.requestedBy = requestedBy;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
}
