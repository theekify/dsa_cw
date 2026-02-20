package com.courseplanner;

/**
 * CourseNode represents a node in the Binary Search Tree (BST).
 * Each node contains a Course object and pointers to left/right children.
 * This enables the BST to organize courses alphabetically by course code.
 */
public class CourseNode {
    // Course stored in this node
    Course course;

    // BST pointers
    CourseNode left;   // Left child (courses with smaller codes)
    CourseNode right;  // Right child (courses with larger codes)

    // Height for AVL balancing
    int height;

    /**
     * Constructor creates a new node with given course
     * @param course Course to store in this node
     */
    public CourseNode(Course course) {
        this.course = course;
        this.left = null;
        this.right = null;
        this.height = 1;  // New node has height 1
    }

    // Getter and Setter methods

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