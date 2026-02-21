package com.courseplanner;


public class CourseNode {
    
    Course course;

    // BST pointers
    CourseNode left;   
    CourseNode right;  

   
    int height;

    
    public CourseNode(Course course) {
        this.course = course;
        this.left = null;
        this.right = null;
        this.height = 1;  
    }

    
    public Course getCourse() {
        return course;
    }

    public CourseNode getLeft() {
        return left;
    }

    public void setLeft(CourseNode left) {
        this.left = left;
    }

    public CourseNode getRight() {
        return right;
    }

    public void setRight(CourseNode right) {
        this.right = right;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

}
