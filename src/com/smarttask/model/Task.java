package com.smarttask.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Task {
    private String name;
    private LocalDate deadline;
    private int importance;
    private double priorityScore;

    public Task(String name, LocalDate deadline,int importance){
        if (importance <1 || importance >5){
            throw new IllegalArgumentException("Imporance must be between 1 and 5");
        }
        this.name = name;
        this.deadline = deadline;
        this.importance = importance;
        this.priorityScore = calculatePriority();
    }
    private double calculatePriority(){
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(),deadline);

        if (daysLeft<=0) daysLeft =1;
        double urgency = 1.0/ daysLeft;
        return urgency * importance;
    }

    public void refreshPriority(){
        this.priorityScore = calculatePriority();
    }

    public String getName () {
        return name;
    }
    public LocalDate getDeadline(){
        return deadline;
    }
    public int getImportance(){
        return importance;
    }
    public double getPriorityScore(){
        return priorityScore;
    }

    public long getDaysUntilDeadline(){
        return ChronoUnit.DAYS.between(LocalDate.now(), deadline);
    }

    public boolean isDeadlineApproching(int thresholdDays){
        return getDaysUntilDeadline() <= thresholdDays;
    }

    @Override
    public String toString(){
        long daysLeft = getDaysUntilDeadline();
        String urgencyLabel;
        if (daysLeft <= 0) urgencyLabel = "OVERDUE";
        else if (daysLeft <= 1) urgencyLabel = "CRITICAL";
        else if (daysLeft <= 3) urgencyLabel = "HIGH";
        else if (daysLeft <= 7) urgencyLabel = "MEDIUM";
        else urgencyLabel = "LOW";

        return String.format(
                "%-25s | Deadline: %s | Importance: %d/5 | Priority: %.4f | %s",
                name,deadline,importance,priorityScore,urgencyLabel
        );
    }
}
