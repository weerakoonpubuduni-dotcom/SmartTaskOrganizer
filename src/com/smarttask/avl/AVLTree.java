package com.smarttask.avl;

import com.smarttask.model.Task;
import java.util.ArrayList;
import java.util.List;

public class AVLTree {

    private AVLNode root;

    public void insert(Task task) {
        root = insertNode(root, task);    // Insert a task
    }

    public void delete(String taskName) {
        root = deleteNode(root, taskName);    // Delete a task by name
    }

    public List<Task> getSortedTasks() {
        List<Task> result = new ArrayList<>();
        reverseInOrder(root, result);   // Get all tasks sorted by priority (highest first)
        return result;
    }

    // Get the highest priority task
    public Task getHighestPriority() {
        if (root == null) return null;
        AVLNode current = root;
        while (current.right != null) {
            current = current.right;
        }
        return current.task;
    }
    public boolean isEmpty() {
        return root == null;  // Check if tree is empty
    }

    private int height(AVLNode node) {
        return (node == null) ? 0 : node.height;
    }

    private void updateHeight(AVLNode node) {
        node.height = 1 + Math.max(height(node.left), height(node.right));
    }

    // Balance factor: left heavy = positive, right heavy = negative
    private int getBalance(AVLNode node) {
        return (node == null) ? 0 : height(node.left) - height(node.right);
    }

    private AVLNode rotateRight(AVLNode y) {
        AVLNode x  = y.left;
        AVLNode T2 = x.right;

        x.right = y;
        y.left  = T2;

        updateHeight(y);
        updateHeight(x);

        return x;  // x becomes new root
    }

    private AVLNode rotateLeft(AVLNode x) {
        AVLNode y  = x.right;
        AVLNode T2 = y.left;

        y.left  = x;
        x.right = T2;

        updateHeight(x);
        updateHeight(y);

        return y;  // y becomes new root
    }


    private AVLNode insertNode(AVLNode node, Task task) {

        // Insert Logic-->Standard BST insert by priority score
        if (node == null) return new AVLNode(task);

        if (task.getPriorityScore() < node.task.getPriorityScore()) {
            node.left  = insertNode(node.left, task);
        } else if (task.getPriorityScore() > node.task.getPriorityScore()) {
            node.right = insertNode(node.right, task);
        } else {
            // Equal priority — go right
            node.right = insertNode(node.right, task);
        }
        updateHeight(node);
        int balance = getBalance(node);
        // Left Left Case
        if (balance > 1 && task.getPriorityScore() < node.left.task.getPriorityScore()) {
            return rotateRight(node);
        }

        // Right Right Case
        if (balance < -1 && task.getPriorityScore() > node.right.task.getPriorityScore()) {
            return rotateLeft(node);
        }

        // Left Right Case
        if (balance > 1 && task.getPriorityScore() > node.left.task.getPriorityScore()) {
            node.left = rotateLeft(node.left);
            return rotateRight(node);
        }

        // Right Left Case
        if (balance < -1 && task.getPriorityScore() < node.right.task.getPriorityScore()) {
            node.right = rotateRight(node.right);
            return rotateLeft(node);
        }

        return node;
    }

    // ─── Delete Logic ─────────────────────────────────────

    private AVLNode deleteNode(AVLNode node, String taskName) {
        if (node == null) return null;

        // Find the node to delete
        if (taskName.equalsIgnoreCase(node.task.getName())) {

            // Case 1: leaf node
            if (node.left == null && node.right == null) return null;

            // Case 2: one child
            if (node.left == null) return node.right;
            if (node.right == null) return node.left;

            // Case 3: two children — replace with in-order successor
            AVLNode successor = getMinNode(node.right);
            node.task         = successor.task;
            node.right        = deleteNode(node.right, successor.task.getName());

        } else {
            // Search both sides
            node.left  = deleteNode(node.left, taskName);
            node.right = deleteNode(node.right, taskName);
        }

        // Rebalance after deletion
        updateHeight(node);
        int balance = getBalance(node);

        // Left Left
        if (balance > 1 && getBalance(node.left) >= 0)
            return rotateRight(node);

        // Left Right
        if (balance > 1 && getBalance(node.left) < 0) {
            node.left = rotateLeft(node.left);
            return rotateRight(node);
        }

        // Right Right
        if (balance < -1 && getBalance(node.right) <= 0)
            return rotateLeft(node);

        // Right Left
        if (balance < -1 && getBalance(node.right) > 0) {
            node.right = rotateRight(node.right);
            return rotateLeft(node);
        }

        return node;
    }

    // Get leftmost (minimum priority) node
    private AVLNode getMinNode(AVLNode node) {
        while (node.left != null) node = node.left;
        return node;
    }

    // ─── Traversal ────────────────────────────────────────

    // Reverse in-order = highest priority first
    private void reverseInOrder(AVLNode node, List<Task> result) {
        if (node == null) return;
        reverseInOrder(node.right, result);
        result.add(node.task);
        reverseInOrder(node.left, result);
    }
    }
