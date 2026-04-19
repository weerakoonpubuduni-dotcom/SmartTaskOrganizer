package com.smarttask;

import com.smarttask.model.Task;

import java.time.LocalDate;

public class Main {
     public  static  void main(String[] args){
         Task t1 = new Task("Submit Assignment ", LocalDate.now().plusDays(2),5);
         Task t2 = new Task("Read Chapter 4 ", LocalDate.now().plusDays(7),3);
         Task t3 = new Task("Team Meeting Prep ", LocalDate.now().plusDays(1),4);
         Task t4 = new Task("Fix Project Bug ", LocalDate.now().plusDays(0),5);
         Task t5 = new Task("Update Resume ", LocalDate.now().plusDays(14),2);

         System.out.println("=============Task List===============");
         System.out.println(t1);
         System.out.println(t2);
         System.out.println(t3);
         System.out.println(t4);
         System.out.println(t5);
     }
}
