package com.smarttask.avl;
import com.smarttask.model.Task;
public class AVLNode {
    Task task;       // data stored in this node
    AVLNode left;    // left child
    AVLNode right;   // right child
    int height;      // height of this node


    public AVLNode(Task task) {
        this.task   = task;
        this.height = 1;  // new node starts at height 1
        this.left   = null;
        this.right  = null;
    }
}
