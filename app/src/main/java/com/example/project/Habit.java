package com.example.project;

import java.io.Serializable;
import java.util.Date;

public class Habit implements Serializable {
    private String name;
    private String quote;
    private String frequency; // "Daily", "Weekly", or "Interval"
    private boolean[] weekDays; // For weekly habits: [Sun, Mon, Tue, Wed, Thu, Fri, Sat]
    private String goal;
    private Date startDate;
    private String goalDays;
    private String section;
    private String reminder;
    private boolean autoPopup;

    public Habit() {
        weekDays = new boolean[7];
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public boolean[] getWeekDays() {
        return weekDays;
    }

    public void setWeekDays(boolean[] weekDays) {
        this.weekDays = weekDays;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getGoalDays() {
        return goalDays;
    }

    public void setGoalDays(String goalDays) {
        this.goalDays = goalDays;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getReminder() {
        return reminder;
    }

    public void setReminder(String reminder) {
        this.reminder = reminder;
    }

    public boolean isAutoPopup() {
        return autoPopup;
    }

    public void setAutoPopup(boolean autoPopup) {
        this.autoPopup = autoPopup;
    }
}