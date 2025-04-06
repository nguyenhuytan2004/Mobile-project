package com.example.project;

public class Category {
    private int id;
    private String name;
    private int listId;
    private TaskList list; // Reference to the parent list

    // Default constructor
    public Category() {
    }

    // Constructor with basic properties
    public Category(int id, String name, int listId) {
        this.id = id;
        this.name = name;
        this.listId = listId;
    }

    // Constructor with list object
    public Category(int id, String name, TaskList list) {
        this.id = id;
        this.name = name;
        this.listId = list.getId();
        this.list = list;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getListId() {
        return listId;
    }

    public void setListId(int listId) {
        this.listId = listId;
    }

    public TaskList getList() {
        return list;
    }

    public void setList(TaskList list) {
        this.list = list;
        if (list != null) {
            this.listId = list.getId();
        }
    }

    @Override
    public String toString() {
        return name;
    }
}