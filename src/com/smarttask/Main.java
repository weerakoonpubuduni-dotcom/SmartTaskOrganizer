package com.smarttask;

import com.smarttask.avl.AVLTree;
import com.smarttask.model.Task;
import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {

        AVLTree tree = new AVLTree();

        // Insert tasks
        tree.insert(new Task("Submit Assignment",  LocalDate.now().plusDays(2), 5));
        tree.insert(new Task("Read Chapter 4",     LocalDate.now().plusDays(7), 3));
        tree.insert(new Task("Team Meeting Prep",  LocalDate.now().plusDays(1), 4));
        tree.insert(new Task("Fix Project Bug",    LocalDate.now().plusDays(1), 5));
        tree.insert(new Task("Update Resume",      LocalDate.now().plusDays(14), 2));

        // Print sorted by priority
        System.out.println("======= TASKS BY PRIORITY (Highest First) =======");
        int rank = 1;
        for (Task t : tree.getSortedTasks()) {
            System.out.println("#" + rank++ + " " + t);
        }

        // Highest priority task
        System.out.println("\n>>> Next Task to Do: "
                + tree.getHighestPriority().getName());

        // Test delete
        System.out.println("\n--- Deleting 'Read Chapter 4' ---");
        tree.delete("Read Chapter 4");

        System.out.println("\n======= UPDATED TASK LIST =======");
        rank = 1;
        for (Task t : tree.getSortedTasks()) {
            System.out.println("#" + rank++ + " " + t);
        }
    }
}