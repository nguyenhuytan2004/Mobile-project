package com.example.project;

public class Task {
    private String taskName;
    private String taskDescription;
    private int priority;

    public Task(String taskName, String taskDescription) {
        this.taskName = taskName;
        this.taskDescription = taskDescription;
    }

    public Task(String taskName, String taskDescription, int priority) {
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.priority = priority;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public int getPriority() {
        return priority;
    }
}
