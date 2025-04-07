package com.example.project;


public class Task implements java.io.Serializable {
    private int id;
    private String title;
    private String description;
    private int priority; // 1-4 representing quadrants
    private String reminderDate; // To store date for reminder
    private int categoryId;
    private boolean isCompleted;
    private String type; // "note" or "task"


    public Task(String title, String description, int priority) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.isCompleted = false;
    }


    public Task(String title, String description, int priority, int category) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.categoryId = category;
    }

    public Task(String title, String description, int priority, boolean isCompleted) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.isCompleted = isCompleted;
    }

    // Constructor for converting from Note data
    public Task(int id, String title, int categoryId) {
        this.id = id;
        this.title = title;
        this.categoryId = categoryId;
        this.isCompleted = false;
    }

    public Task() {


    }

    // Getter and setter for category ID
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getId() {
        return id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setPriority(int priority) {
        this.priority = priority;
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


    public int getCategoryId() {
        return categoryId;
    }


    public void setCategory(int category) {
        this.categoryId = category;
    }
    public boolean isCompleted() {
        return isCompleted;
    }
    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type != null ? type : "task"; // Default is task
    }

    public boolean isNote() {
        return "note".equals(type);
    }
}
