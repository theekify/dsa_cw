package com.courseplanner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CoursePlannerApp {
    public CourseBST courseTree;
    public PrerequisiteGraph prerequisiteGraph;
    public PlanGenerator planGenerator;
    public AISuggester aiSuggester;

    public List<String> completedCourses;
    public HashMap<String, Double> grades;
    public List<String> interests;

    private static final String PROGRESS_FILE = "student_progress.txt";

    public CoursePlannerApp() {
        this.courseTree = new CourseBST();
        this.prerequisiteGraph = new PrerequisiteGraph();
        this.planGenerator = new PlanGenerator(courseTree, prerequisiteGraph);
        this.aiSuggester = new AISuggester(courseTree, prerequisiteGraph);

        this.completedCourses = new ArrayList<>();
        this.grades = new HashMap<>();
        this.interests = new ArrayList<>();

        // Load saved progress silently (no terminal output)
        try {
            PlanExporter.loadProgress(PROGRESS_FILE, completedCourses, grades, interests);
        } catch (IOException e) {
            // Silently fail - no saved progress yet
        }

        // Initialize with sample courses (no terminal prompts)
        initializeWithSampleCourses();
        
        // Update components with loaded data
        updateComponents();
    }

    private void initializeWithSampleCourses() {
        // Add all CS courses with prerequisites
        addCourse("CS101", "Programming Fundamentals", 3, "");
        addCourse("CS102", "Object-Oriented Programming", 3, "CS101");
        addCourse("CS201", "Data Structures", 3, "CS102");
        addCourse("CS202", "Discrete Mathematics", 3, "MATH101");
        addCourse("CS301", "Algorithms", 3, "CS201,CS202");
        addCourse("CS302", "Database Systems", 3, "CS201");
        addCourse("CS303", "Computer Networks", 3, "CS201");
        addCourse("CS401", "Software Engineering", 3, "CS301,CS302");
        addCourse("CS402", "Web Development", 3, "CS102");
        addCourse("CS403", "Mobile App Development", 3, "CS102");
        addCourse("CS404", "Artificial Intelligence", 3, "CS301");
        addCourse("CS405", "Machine Learning", 3, "CS404,MATH301");

        // Math courses
        addCourse("MATH101", "Calculus I", 4, "");
        addCourse("MATH201", "Calculus II", 4, "MATH101");
        addCourse("MATH301", "Linear Algebra", 3, "MATH201");

        // General courses
        addCourse("ENG101", "English Composition", 3, "");
        addCourse("ENG201", "Technical Writing", 3, "ENG101");
        addCourse("SCI101", "General Science", 4, "");
        addCourse("PHY101", "Physics I", 4, "MATH101");
        addCourse("HUM101", "Humanities", 3, "");
    }

    private void addCourse(String code, String name, int credits, String prerequisites) {
        Course course = new Course(code, name, credits);
        courseTree.insert(course);

        if (!prerequisites.isEmpty()) {
            String[] prereqArray = prerequisites.split(",");
            for (String prereq : prereqArray) {
                prerequisiteGraph.addPrerequisite(code, prereq.trim());
            }
        }
    }

    private void updateComponents() {
        planGenerator.setCompletedCourses(completedCourses);
        aiSuggester.setCompletedCourses(completedCourses);
        aiSuggester.setGrades(grades);
        aiSuggester.setInterests(interests);
    }

    // Keep the run() method for terminal mode if needed
    public void run() {
        // This is for terminal mode - we won't use it in GUI
    }
}
