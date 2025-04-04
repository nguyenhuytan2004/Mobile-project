package com.example.project;


public class Task {
    private String title;
    private String description;
    private int priority; // 1-4 representing quadrants
    private String reminderDate; // To store date for reminder
    private String category;
    private boolean isCompleted;


    public Task(String title, String description, int priority) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.category = "";
        this.isCompleted = false;
    }


    public Task(String title, String description, int priority, String category) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.category = category;
    }


    public Task() {


    }


    public String getTitle() {
        return title;
    }


    public String getDescription() {
        return description;
    }


    public int getPriority() {
        return priority;
    }


    public String getReminderDate() {
        return reminderDate;
    }


    public void setReminderDate(String reminderDate) {
        this.reminderDate = reminderDate;
    }


    public boolean hasReminder() {
        return reminderDate != null && !reminderDate.isEmpty();
    }


    public String getCategory() {
        return category;
    }


    public void setCategory(String category) {
        this.category = category;
    }
    public boolean isCompleted() {
        return isCompleted;
    }
    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
