package com.smarttask;

import com.smarttask.heap.MinHeap;
import com.smarttask.model.Task;
import java.time.LocalDate;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        MinHeap heap = new MinHeap(3); // alert if deadline within 3 days

        heap.insert(new Task("Submit Assignment",  LocalDate.now().plusDays(2), 5));
        heap.insert(new Task("Read Chapter 4",     LocalDate.now().plusDays(7), 3));
        heap.insert(new Task("Team Meeting Prep",  LocalDate.now().plusDays(1), 4));
        heap.insert(new Task("Fix Project Bug",    LocalDate.now().plusDays(0), 5));
        heap.insert(new Task("Update Resume",      LocalDate.now().plusDays(14), 2));

        // All tasks sorted by deadline
        System.out.println("======= TASKS BY DEADLINE =======");
        for (Task t : heap.getAllSortedByDeadline()) {
            System.out.println(t);
        }

        // Deadline alerts
        System.out.println("\n  DEADLINE ALERTS (within 3 days):");
        List<Task> alerts = heap.getDeadlineAlerts();
        if (alerts.isEmpty()) {
            System.out.println("No upcoming deadlines.");
        } else {
            for (Task t : alerts) {
                System.out.println("   " + t.getName()
                        + " — " + t.getDaysUntilDeadline() + " day(s) left!");
            }
        }

        // Extract soonest deadline task
        System.out.println("\n>>> Most Urgent Deadline: "
                + heap.extractMin().getName());
    }
}