package com.anwar.aws.todolist.model;

import org.json.simple.JSONObject;

public class Task {
	private String userId;
	private String taskTitle;
	private String taskId;
	private String taskDate;
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	
	public String getTaskDate() {
		return taskDate;
	}
	public void setTaskDate(String date) {
		this.taskDate = date;
	}
	public String getTaskTitle() {
		return taskTitle;
	}
	public void setTaskTitle(String taskTitle) {
		this.taskTitle = taskTitle;
	}
	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	
	public JSONObject toJSON() {

        JSONObject jo = new JSONObject();
        jo.put("userId", userId);
        jo.put("taskId", taskId);
        jo.put("taskTitle", taskTitle);
        jo.put("taskDate", taskDate);
        return jo;
    }
}
