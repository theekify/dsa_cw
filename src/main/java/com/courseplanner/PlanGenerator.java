package com.courseplanner;

import java.util.*;


public class PlanGenerator {
    private CourseBST courseTree;
    private PrerequisiteGraph graph;
    private List<String> completedCourses;
    private static final int MAX_CREDITS_PER_SEMESTER = 18;
    private static final int MIN_CREDITS_PER_SEMESTER = 12;

    /**
     * Constructor
     * @param courseTree BST containing all courses
     * @param graph Prerequisite graph
     */
    public PlanGenerator(CourseBST courseTree, PrerequisiteGraph graph) {
        this.courseTree = courseTree;
        this.graph = graph;
        this.completedCourses = new ArrayList<>();
    }

    /**
     * Set completed courses
     * @param completedCourses List of completed course codes
     */
    public void setCompletedCourses(List<String> completedCourses) {
        this.completedCourses = new ArrayList<>(completedCourses);
    }

    /**
     * Check if a course can be taken based on completed prerequisites
     * @param courseCode Course to check
     * @return true if all prerequisites are completed
     */
    public boolean canTakeCourse(String courseCode) {
        Course course = courseTree.search(courseCode);
        if (course == null || course.isCompleted()) {
            return false;
        }

        List<String> prerequisites = graph.getPrerequisites(courseCode);
        for (String prereq : prerequisites) {
            if (!completedCourses.contains(prereq)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get missing prerequisites for a course
     * @param courseCode Course to check
     * @return List of missing prerequisite codes
     */
    public List<String> getMissingPrerequisites(String courseCode) {
        List<String> missing = new ArrayList<>();
        List<String> prerequisites = graph.getPrerequisites(courseCode);

        for (String prereq : prerequisites) {
            if (!completedCourses.contains(prereq)) {
                missing.add(prereq);
            }
        }
        return missing;
    }

    /**
     * Generate semester study plan
     * @return List of semesters, each containing list of courses
     */
    public List<List<Course>> generatePlan() {
        List<List<Course>> plan = new ArrayList<>();

        // Get all courses that aren't completed
        List<Course> allCourses = courseTree.inOrderTraversal();
        List<Course> remainingCourses = new ArrayList<>();

        for (Course course : allCourses) {
            if (!course.isCompleted()) {
                remainingCourses.add(course);
            }
        }

        // If no courses remaining, return empty plan
        if (remainingCourses.isEmpty()) {
            return plan;
        }

        // Sort by number of prerequisites (courses with fewer prerequisites first)
        remainingCourses.sort((c1, c2) -> {
            int prereqCount1 = graph.getPrerequisites(c1.getCode()).size();
            int prereqCount2 = graph.getPrerequisites(c2.getCode()).size();
            return Integer.compare(prereqCount1, prereqCount2);
        });

        int semester = 1;
        while (!remainingCourses.isEmpty()) {
            List<Course> currentSemester = new ArrayList<>();
            int currentCredits = 0;
            List<Course> toRemove = new ArrayList<>();

            // Try to add courses to current semester
            for (Course course : remainingCourses) {
                if (canTakeCourse(course.getCode())) {
                    if (currentCredits + course.getCredits() <= MAX_CREDITS_PER_SEMESTER) {
                        currentSemester.add(course);
                        currentCredits += course.getCredits();
                        toRemove.add(course);
                    }
                }
            }

            // Remove courses we've scheduled
            remainingCourses.removeAll(toRemove);

            // Add semester to plan if it has courses
            if (!currentSemester.isEmpty()) {
                plan.add(new ArrayList<>(currentSemester));
            } else if (!remainingCourses.isEmpty()) {
                // Can't schedule any more courses (deadlock due to prerequisites)
                System.out.println("Warning: Cannot schedule all courses due to prerequisite constraints.");
                break;
            }

            semester++;

            // Safety check to prevent infinite loop
            if (semester > 20) {
                System.out.println("Warning: Too many semesters generated. Check for issues.");
                break;
            }
        }

        return plan;
    }

    /**
     * Generate plan using topological sort (alternative method)
     * @return Semester plan
     */
    public List<List<Course>> generatePlanTopological() {
        List<List<Course>> plan = new ArrayList<>();

        // Get topological order
        List<String> courseOrder = graph.topologicalSort();
        if (courseOrder.isEmpty()) {
            System.out.println("Warning: Circular dependencies detected. Cannot generate plan.");
            return plan;
        }

        // Filter out completed courses
        List<String> remainingCourses = new ArrayList<>();
        for (String courseCode : courseOrder) {
            Course course = courseTree.search(courseCode);
            if (course != null && !course.isCompleted()) {
                remainingCourses.add(courseCode);
            }
        }

        // Allocate to semesters
        List<Course> currentSemester = new ArrayList<>();
        int currentCredits = 0;

        for (String courseCode : remainingCourses) {
            Course course = courseTree.search(courseCode);
            if (course == null) continue;

            // Check if prerequisites are satisfied (in completed courses or current semester)
            boolean prerequisitesSatisfied = true;
            List<String> prerequisites = graph.getPrerequisites(courseCode);
            for (String prereq : prerequisites) {
                if (!completedCourses.contains(prereq) &&
                        !currentSemesterContains(currentSemester, prereq)) {
                    prerequisitesSatisfied = false;
                    break;
                }
            }

            if (prerequisitesSatisfied) {
                if (currentCredits + course.getCredits() <= MAX_CREDITS_PER_SEMESTER) {
                    currentSemester.add(course);
                    currentCredits += course.getCredits();
                } else {
                    // Start new semester
                    plan.add(new ArrayList<>(currentSemester));
                    currentSemester = new ArrayList<>();
                    currentSemester.add(course);
                    currentCredits = course.getCredits();
                }
            }
        }

        // Add last semester if not empty
        if (!currentSemester.isEmpty()) {
            plan.add(currentSemester);
        }

        return plan;
    }

    /**
     * Check if current semester contains a course
     */
    private boolean currentSemesterContains(List<Course> semester, String courseCode) {
        for (Course course : semester) {
            if (course.getCode().equals(courseCode)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check for warnings in generated plan
     * @param plan Generated study plan
     * @return List of warning messages
     */
    public List<String> checkPlanWarnings(List<List<Course>> plan) {
        List<String> warnings = new ArrayList<>();

        for (int i = 0; i < plan.size(); i++) {
            List<Course> semester = plan.get(i);
            int totalCredits = semester.stream().mapToInt(Course::getCredits).sum();

            if (totalCredits > MAX_CREDITS_PER_SEMESTER) {
                warnings.add(String.format(
                        "Semester %d: Overload (%d/%d credits)",
                        i + 1, totalCredits, MAX_CREDITS_PER_SEMESTER
                ));
            } else if (totalCredits < MIN_CREDITS_PER_SEMESTER) {
                warnings.add(String.format(
                        "Semester %d: Light load (%d/%d credits recommended)",
                        i + 1, totalCredits, MIN_CREDITS_PER_SEMESTER
                ));
            }

            // Check for prerequisite violations within semester
            for (Course course : semester) {
                List<String> missing = getMissingPrerequisites(course.getCode());
                if (!missing.isEmpty()) {
                    warnings.add(String.format(
                            "Semester %d: %s missing prerequisites: %s",
                            i + 1, course.getCode(), String.join(", ", missing)
                    ));
                }
            }
        }

        return warnings;
    }

    /**
     * Calculate total credits in plan
     * @param plan Study plan
     * @return Total credit hours
     */
    public int calculateTotalCredits(List<List<Course>> plan) {
        return plan.stream()
                .flatMap(List::stream)
                .mapToInt(Course::getCredits)
                .sum();
    }

}
