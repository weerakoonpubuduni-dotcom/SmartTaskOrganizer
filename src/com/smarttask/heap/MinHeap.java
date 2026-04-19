package com.smarttask.heap;

import com.smarttask.model.Task;
import java.util.ArrayList;
import java.util.List;

public class MinHeap {

    // Internal Storage
    private List<Task> heap;
    private int alertThresholdDays;  // days within which to alert

    public MinHeap(int alertThresholdDays) {
        this.heap                = new ArrayList<>();
        this.alertThresholdDays  = alertThresholdDays;
    }

    // Core Operations

    // Insert a task and bubble up
    public void insert(Task task) {
        heap.add(task);
        bubbleUp(heap.size() - 1);
    }

    // Remove and return the task with soonest deadline
    public Task extractMin() {
        if (isEmpty()) return null;

        Task min = heap.get(0);

        // Move last element to root and bubble down
        Task last = heap.remove(heap.size() - 1);
        if (!isEmpty()) {
            heap.set(0, last);
            bubbleDown(0);
        }

        return min;
    }

    // Peek at soonest deadline without removing
    public Task peekMin() {
        return isEmpty() ? null : heap.get(0);
    }

    // Remove a specific task by name
    public void remove(String taskName) {
        int index = -1;
        for (int i = 0; i < heap.size(); i++) {
            if (heap.get(i).getName().equalsIgnoreCase(taskName)) {
                index = i;
                break;
            }
        }
        if (index == -1) return;  // not found

        // Replace with last element and reheapify
        Task last = heap.remove(heap.size() - 1);
        if (index < heap.size()) {
            heap.set(index, last);
            bubbleUp(index);
            bubbleDown(index);
        }
    }

    // Get all tasks with deadlines within threshold
    public List<Task> getDeadlineAlerts() {
        List<Task> alerts = new ArrayList<>();
        for (Task task : heap) {
            if (task.isDeadlineApproching(alertThresholdDays)) {
                alerts.add(task);
            }
        }
        // Sort alerts by days until deadline
        alerts.sort((a, b) ->
                Long.compare(a.getDaysUntilDeadline(), b.getDaysUntilDeadline()));
        return alerts;
    }

    // Get all tasks sorted by deadline
    public List<Task> getAllSortedByDeadline() {
        List<Task> sorted = new ArrayList<>(heap);
        sorted.sort((a, b) -> a.getDeadline().compareTo(b.getDeadline()));
        return sorted;
    }

    public boolean isEmpty()  { return heap.isEmpty(); }
    public int size()         { return heap.size(); }

    // Heap Navigation

    private int parent(int i)    { return (i - 1) / 2; }
    private int leftChild(int i) { return 2 * i + 1; }
    private int rightChild(int i){ return 2 * i + 2; }

    private void swap(int i, int j) {
        Task temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }

    // Bubble Up
    // After insert — move new node up until heap order restored
    // Min-Heap: parent deadline must be <= child deadline
    private void bubbleUp(int index) {
        while (index > 0) {
            int parentIdx = parent(index);
            boolean parentIsLater = heap.get(parentIdx)
                    .getDeadline()
                    .isAfter(heap.get(index).getDeadline());

            if (parentIsLater) {
                swap(parentIdx, index);
                index = parentIdx;
            } else {
                break;
            }
        }
    }

    // Bubble Down
    // After extractMin — move root down until heap order restored
    private void bubbleDown(int index) {
        int size = heap.size();

        while (true) {
            int left    = leftChild(index);
            int right   = rightChild(index);
            int smallest = index;

            // Find child with earliest deadline
            if (left < size && heap.get(left)
                    .getDeadline()
                    .isBefore(heap.get(smallest).getDeadline())) {
                smallest = left;
            }
            if (right < size && heap.get(right)
                    .getDeadline()
                    .isBefore(heap.get(smallest).getDeadline())) {
                smallest = right;
            }

            if (smallest != index) {
                swap(index, smallest);
                index = smallest;
            } else {
                break;
            }
        }
    }
}