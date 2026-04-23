package com.smarttask.manager;

import com.smarttask.avl.AVLTree;
import com.smarttask.heap.MinHeap;
import com.smarttask.model.Task;
import com.smarttask.storage.TaskStorage;

import java.time.LocalDate;
import java.util.List;

public class TaskManager {

    private AVLTree avlTree;
    private MinHeap minHeap;
    private int alertThreshold;

    /**
     * Captured BEFORE loadPersistedTasks() runs.
     * This is the only reliable moment to check — once load() runs for the
     * first time it may create the file, making subsequent isFirstRun() calls
     * return false even on the very first launch.
     */
    private final boolean firstRun;

    public TaskManager(int alertThresholdDays) {
        this.alertThreshold = alertThresholdDays;
        this.avlTree = new AVLTree();
        this.minHeap = new MinHeap(alertThresholdDays);

        // MUST check before loading — loading may write the file on first run
        this.firstRun = TaskStorage.isFirstRun();

        loadPersistedTasks();
    }

    // ── Persistence ──────────────────────────────────────

    private void loadPersistedTasks() {
        List<Task> saved = TaskStorage.load();
        for (Task t : saved) {
            t.refreshPriority();   // recalculate urgency based on today's date
            avlTree.insert(t);
            minHeap.insert(t);
        }
        if (!saved.isEmpty()) {
            System.out.println("📂 Loaded " + saved.size()
                    + " task(s) from: " + TaskStorage.getStoragePath());
        }
    }

    private void persistTasks() {
        List<Task> all = avlTree.getSortedTasks();
        TaskStorage.save(all);
        System.out.println("💾 Saved " + all.size()
                + " task(s) → " + TaskStorage.getStoragePath());
    }

    // ── Public API ───────────────────────────────────────

    public void addTask(String name, LocalDate deadline, int importance) {
        Task task = new Task(name, deadline, importance);
        avlTree.insert(task);
        minHeap.insert(task);
        persistTasks();   // write to disk immediately on every add
        System.out.println("✅ Task added: " + name);
    }

    public void removeTask(String name) {
        avlTree.delete(name);
        minHeap.remove(name);
        persistTasks();   // write to disk immediately on every remove
        System.out.println("🗑️  Task removed: " + name);
    }

    public void showTasksByPriority() {
        List<Task> tasks = avlTree.getSortedTasks();
        if (tasks.isEmpty()) { System.out.println("No tasks available."); return; }
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║           TASKS RANKED BY PRIORITY                      ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        int rank = 1;
        for (Task t : tasks) System.out.println("  #" + rank++ + "  " + t);
    }

    public void showTasksByDeadline() {
        List<Task> tasks = minHeap.getAllSortedByDeadline();
        if (tasks.isEmpty()) { System.out.println("No tasks available."); return; }
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║           TASKS RANKED BY DEADLINE                      ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        int rank = 1;
        for (Task t : tasks) System.out.println("  #" + rank++ + "  " + t);
    }

    public void showDeadlineAlerts() {
        List<Task> alerts = minHeap.getDeadlineAlerts();
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║           ⚠️  DEADLINE ALERTS                            ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        if (alerts.isEmpty()) {
            System.out.println("  ✅ No upcoming deadlines within " + alertThreshold + " days.");
        } else {
            for (Task t : alerts) {
                long days = t.getDaysUntilDeadline();
                if (days <= 0)
                    System.out.println("  🔴 OVERDUE  : " + t.getName()
                            + " (deadline was " + t.getDeadline() + ")");
                else
                    System.out.println("  ⏰ " + days + " day(s) left : "
                            + t.getName() + " (due " + t.getDeadline() + ")");
            }
        }
    }

    public void showNextTask() {
        Task next = avlTree.getHighestPriority();
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║            RECOMMENDED NEXT TASK                      ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        if (next == null) System.out.println("  No tasks available.");
        else System.out.println("  👉 " + next);
    }

    public void showDashboard() {
        showDeadlineAlerts();
        showNextTask();
        showTasksByPriority();
    }

    // ── Getters ──────────────────────────────────────────


    public boolean isFirstRun()       { return firstRun; }
    public boolean hasTasks()         { return !avlTree.isEmpty(); }
    public int getAlertThreshold()    { return alertThreshold; }
    public List<Task> getAllTasks()    { return avlTree.getSortedTasks(); }
    public List<Task> getAlerts()     { return minHeap.getDeadlineAlerts(); }
    public Task getNextTask()         { return avlTree.getHighestPriority(); }
}