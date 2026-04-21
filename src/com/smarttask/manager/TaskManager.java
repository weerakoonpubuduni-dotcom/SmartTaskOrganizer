package com.smarttask.manager;

import com.smarttask.avl.AVLTree;
import com.smarttask.heap.MinHeap;
import com.smarttask.model.Task;

import java.time.LocalDate;
import java.util.List;

public class TaskManager {

    private AVLTree avlTree;   // stores tasks sorted by priority
    private MinHeap minHeap;   // stores tasks sorted by deadline
    private int alertThreshold;

    public TaskManager(int alertThresholdDays) {
        this.alertThreshold = alertThresholdDays;
        this.avlTree = new AVLTree();
        this.minHeap = new MinHeap(alertThresholdDays);
    }
    public void addTask(String name, LocalDate deadline, int importance) {
        Task task = new Task(name, deadline, importance);
        avlTree.insert(task);
        minHeap.insert(task);
        System.out.println("✅ Task added: " + name);
    }

    public void removeTask(String name) {
        avlTree.delete(name);
        minHeap.remove(name);
        System.out.println("🗑️  Task removed: " + name);
    }
    // Task ranking using priority score
    public void showTasksByPriority() {
        List<Task> tasks = avlTree.getSortedTasks();
        if (tasks.isEmpty()) {
            System.out.println("No tasks available.");
            return;
        }
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║           TASKS RANKED BY PRIORITY                      ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        int rank = 1;
        for (Task t : tasks) {
            System.out.println("  #" + rank++ + "  " + t);
        }
    }

    // Show tasks ranked by deadline
    public void showTasksByDeadline() {
        List<Task> tasks = minHeap.getAllSortedByDeadline();
        if (tasks.isEmpty()) {
            System.out.println("No tasks available.");
            return;
        }
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║           TASKS RANKED BY DEADLINE                      ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        int rank = 1;
        for (Task t : tasks) {
            System.out.println("  #" + rank++ + "  " + t);
        }
    }
    // Show deadline alerts
    public void showDeadlineAlerts() {
        List<Task> alerts = minHeap.getDeadlineAlerts();
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║           ⚠️  DEADLINE ALERTS                            ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        if (alerts.isEmpty()) {
            System.out.println("  ✅ No upcoming deadlines within "
                    + alertThreshold + " days.");
        } else {
            for (Task t : alerts) {
                long days = t.getDaysUntilDeadline();
                if (days <= 0) {
                    System.out.println("  🔴 OVERDUE  : " + t.getName()
                            + " (deadline was " + t.getDeadline() + ")");
                } else {
                    System.out.println("  ⏰ " + days + " day(s) left : "
                            + t.getName() + " (due " + t.getDeadline() + ")");
                }
            }
        }
    }

    // Show next recommended task
    public void showNextTask() {
        Task next = avlTree.getHighestPriority();
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║           🎯 RECOMMENDED NEXT TASK                      ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        if (next == null) {
            System.out.println("  No tasks available.");
        } else {
            System.out.println("  👉 " + next);
        }
    }

    public void showDashboard() {
        showDeadlineAlerts();
        showNextTask();
        showTasksByPriority();
    }

    public boolean hasTasks() {
        return !avlTree.isEmpty();
    }

    public int getAlertThreshold() {
        return alertThreshold;
    }
    // Get all tasks sorted by priority (for dashboard list)
    public List<Task> getAllTasks() {
        return avlTree.getSortedTasks();
    }

    // Get deadline alerts (for alert panel)
    public List<Task> getAlerts() {
        return minHeap.getDeadlineAlerts();
    }

    // Get highest priority task (for next task panel)
    public Task getNextTask() {
        return avlTree.getHighestPriority();
    }
}