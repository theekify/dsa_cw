package com.courseplanner;

/**
 * Course class represents a single academic course with all its properties.
 * This is a POJO (Plain Old Java Object) that stores course information.
 * Each course has a unique code, name, credit value, and completion status.
 */
public class Course {
    // Private fields - encapsulation
    private String code;        // Course code like "CS101"
    private String name;        // Course name like "Programming Fundamentals"
    private int credits;        // Credit hours (3, 4, etc.)
    private boolean completed;  // Whether student has completed this course
    private double grade;       // Grade obtained (0-100)//

    /**
     * Constructor to create a new Course object
     * @param code Unique course identifier
     * @param name Full course name
     * @param credits Number of credit hours
     */
    public Course(String code, String name, int credits) {
        this.code = code;
        this.name = name;
        this.credits = credits;
        this.completed = false;  // Default: not completed
        this.grade = 0.0;        // Default: no grade
    }

    // Getter and Setter methods (Encapsulation principle)

    /**
     * @return Course code
     */
    public String getCode() {
        return code;
    }

    /**
     * @return Course name
     */
    public String getName() {
        return name;
    }

    /**
     * @return Credit hours
     */
    public int getCredits() {
        return credits;
    }



    /**
     * String representation for display
     * @return Formatted course information
     */
    @Override
    public String toString() {
        String status = completed ? "✓" : "○";
        return String.format("%s %s: %s (%d credits) %s",
                status, code, name, credits,
                completed ? String.format("[Grade: %.1f%%]", grade) : "");
    }
}