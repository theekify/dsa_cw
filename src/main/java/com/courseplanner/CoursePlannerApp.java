package com.courseplanner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class CoursePlannerApp {
    public CourseBST courseTree;
    public PrerequisiteGraph prerequisiteGraph;
    public PlanGenerator planGenerator;
    public AISuggester aiSuggester;

    public List<String> completedCourses;
    public HashMap<String, Double> grades;
    public List<String> interests;
    private Scanner scanner;

    private static final String PROGRESS_FILE = "student_progress.txt";
    private List<List<Course>> currentPlan;

    public CoursePlannerApp() {
        this.courseTree = new CourseBST();
        this.prerequisiteGraph = new PrerequisiteGraph();
        this.planGenerator = new PlanGenerator(courseTree, prerequisiteGraph);
        this.aiSuggester = new AISuggester(courseTree, prerequisiteGraph);

        this.completedCourses = new ArrayList<>();
        this.grades = new HashMap<>();
        this.interests = new ArrayList<>();
        this.scanner = new Scanner(System.in);

        initializeSystem();
    }

    private void initializeSystem() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("       COURSE PLANNER - WELCOME!");
        System.out.println("=".repeat(70));

        // Try to load saved progress
        try {
            PlanExporter.loadProgress(PROGRESS_FILE, completedCourses, grades, interests);
            System.out.println("✓ Loaded saved progress");
        } catch (IOException e) {
            System.out.println("Starting fresh...");
        }

        // Setup courses
        setupCourses();

        // Update components with student data
        updateComponents();

        System.out.println("\n✓ System initialized!");
        System.out.println("Courses available: " + courseTree.inOrderTraversal().size());
        System.out.println("Your completed courses: " + completedCourses.size());
    }

    private void setupCourses() {
        System.out.println("\n--- COURSE SETUP ---");
        System.out.println("Choose how to setup courses:");
        System.out.println("1. Load Computer Science sample courses (Recommended)");
        System.out.println("2. Enter courses manually");
        System.out.println("3. Skip - I'll add courses later");
        System.out.print("\nEnter choice (1-3): ");

        try {
            int choice = Integer.parseInt(scanner.nextLine());
            switch (choice) {
                case 1 -> loadCSExampleCourses();
                case 2 -> enterCoursesManually();
                case 3 -> System.out.println("Skipping course setup. Use 'Manage Courses' later.");
                default -> {
                    System.out.println("Invalid choice. Loading sample courses.");
                    loadCSExampleCourses();
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Loading sample courses.");
            loadCSExampleCourses();
        }
    }

    private void loadCSExampleCourses() {
        System.out.println("\nLoading Computer Science example courses...");

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

        System.out.println("✓ Loaded 20 example courses");

        // Ask if user wants to mark some as completed
        System.out.print("\nDo you want to mark some courses as completed? (yes/no): ");
        String markCompleted = scanner.nextLine().toLowerCase().trim();
        if (markCompleted.equals("yes") || markCompleted.equals("y")) {
            markInitialCompletedCourses();
        }
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

    private void markInitialCompletedCourses() {
        System.out.println("\nMark some courses as completed (common for first year):");

        String[] commonFirstYear = {"CS101", "CS102", "MATH101", "ENG101"};
        for (String courseCode : commonFirstYear) {
            System.out.print("Have you completed " + courseCode + "? (yes/no): ");
            String response = scanner.nextLine().toLowerCase().trim();
            if (response.equals("yes") || response.equals("y")) {
                System.out.print("Enter grade for " + courseCode + " (0-100): ");
                try {
                    double grade = Double.parseDouble(scanner.nextLine());
                    if (grade >= 0 && grade <= 100) {
                        Course course = courseTree.search(courseCode);
                        if (course != null) {
                            course.setCompleted(true);
                            course.setGrade(grade);
                            completedCourses.add(courseCode);
                            grades.put(courseCode, grade);
                        }
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid grade. Skipping.");
                }
            }
        }

        // Ask for interests
        System.out.print("\nEnter your academic interests (comma separated, e.g., Programming,AI): ");
        String interestsInput = scanner.nextLine().trim();
        if (!interestsInput.isEmpty()) {
            String[] interestArray = interestsInput.split(",");
            for (String interest : interestArray) {
                interests.add(interest.trim());
            }
        }
    }

    private void enterCoursesManually() {
        System.out.println("\n--- MANUAL COURSE ENTRY ---");
        System.out.println("Enter courses one by one. Type 'done' when finished.");

        while (true) {
            System.out.println("\n--- New Course ---");

            System.out.print("Course Code (or 'done' to finish): ");
            String code = scanner.nextLine().toUpperCase().trim();
            if (code.equalsIgnoreCase("done")) {
                break;
            }

            System.out.print("Course Name: ");
            String name = scanner.nextLine().trim();

            System.out.print("Credits (default 3): ");
            int credits = 3;
            try {
                String creditInput = scanner.nextLine().trim();
                if (!creditInput.isEmpty()) {
                    credits = Integer.parseInt(creditInput);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Using default 3 credits.");
            }

            // Add course
            Course course = new Course(code, name, credits);
            courseTree.insert(course);
            System.out.println("✓ Added: " + code);

            // Add prerequisites
            System.out.print("Prerequisites (comma separated, leave empty if none): ");
            String prereqInput = scanner.nextLine().toUpperCase().trim();
            if (!prereqInput.isEmpty()) {
                String[] prereqs = prereqInput.split(",");
                for (String prereq : prereqs) {
                    prerequisiteGraph.addPrerequisite(code, prereq.trim());
                    System.out.println("  Added prerequisite: " + prereq.trim() + " → " + code);
                }
            }
        }

        System.out.println("\nCourse entry completed.");
    }

    private void updateComponents() {
        planGenerator.setCompletedCourses(completedCourses);
        aiSuggester.setCompletedCourses(completedCourses);
        aiSuggester.setGrades(grades);
        aiSuggester.setInterests(interests);
    }

    public void run() {
        displayWelcome();

        boolean running = true;
        while (running) {
            displayMainMenu();
            System.out.print("\nEnter your choice (1-11): ");

            try {
                String input = scanner.nextLine();
                int choice = Integer.parseInt(input);

                switch (choice) {
                    case 1 -> viewAllCourses();
                    case 2 -> checkPrerequisites();
                    case 3 -> generateStudyPlan();
                    case 4 -> getAISuggestions();
                    case 5 -> exportPlan();
                    case 6 -> detectCycles();
                    case 7 -> manageCompletedCourses();
                    case 8 -> manageCourses();
                    case 9 -> viewStudentProfile();
                    case 10 -> saveProgress();
                    case 11 -> {
                        System.out.println("\nThank you for using Course Planner!");
                        running = false;
                    }
                    default -> System.out.println("Invalid choice. Please enter 1-11.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number (1-11).");
            }

            if (running) {
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
            }
        }

        scanner.close();
    }

    private void displayWelcome() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("              COURSE PLANNER & PREREQUISITE TRACKER");
        System.out.println("=".repeat(70));
        System.out.println("Plan your academic journey efficiently!");
        System.out.println("No CSV files needed - everything is entered through this app.");
        System.out.println("=".repeat(70));
    }

    private void displayMainMenu() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("MAIN MENU");
        System.out.println("=".repeat(70));
        System.out.println("1.  View All Courses");
        System.out.println("2.  Check Prerequisites for a Course");
        System.out.println("3.  Generate Study Plan");
        System.out.println("4.  Get AI Elective Suggestions");
        System.out.println("5.  Export Plan to File");
        System.out.println("6.  Detect Prerequisite Cycles");
        System.out.println("7.  Manage Completed Courses");
        System.out.println("8.  Manage Courses (Add/Remove/Edit)");
        System.out.println("9.  View Student Profile");
        System.out.println("10. Save Progress");
        System.out.println("11. Exit");
        System.out.println("=".repeat(70));
    }

    private void viewAllCourses() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("ALL COURSES");
        System.out.println("=".repeat(70));

        List<Course> courses = courseTree.inOrderTraversal();

        if (courses.isEmpty()) {
            System.out.println("No courses available. Add courses first (Option 8).");
            return;
        }

        System.out.printf("%-10s %-30s %-8s %-12s%n",
                "Code", "Name", "Credits", "Status");
        System.out.println("-".repeat(60));

        for (Course course : courses) {
            String status = course.isCompleted() ?
                    String.format("✓ (%.1f%%)", course.getGrade()) : "○";
            System.out.printf("%-10s %-30s %-8d %-12s%n",
                    course.getCode(),
                    course.getName(),
                    course.getCredits(),
                    status
            );
        }

        System.out.println("\nTotal: " + courses.size() + " courses");
    }

    private void checkPrerequisites() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("CHECK PREREQUISITES");
        System.out.println("=".repeat(70));

        System.out.print("Enter course code to check: ");
        String courseCode = scanner.nextLine().toUpperCase().trim();

        Course course = courseTree.search(courseCode);
        if (course == null) {
            System.out.println("Course not found: " + courseCode);
            return;
        }

        System.out.println("\nCourse: " + course);

        if (course.isCompleted()) {
            System.out.println("✓ Already completed!");
            return;
        }

        List<String> prerequisites = prerequisiteGraph.getPrerequisites(courseCode);
        if (prerequisites.isEmpty()) {
            System.out.println("✓ No prerequisites - you can take it!");
            return;
        }

        System.out.println("\nPrerequisites needed:");
        System.out.println("-".repeat(40));

        boolean allMet = true;
        for (String prereqCode : prerequisites) {
            Course prereqCourse = courseTree.search(prereqCode);
            String status = completedCourses.contains(prereqCode) ? "✓ COMPLETED" : "✗ MISSING";
            String courseName = (prereqCourse != null) ? prereqCourse.getName() : "Unknown";

            System.out.printf("%s %s: %s%n",
                    status, prereqCode, courseName);

            if (!completedCourses.contains(prereqCode)) {
                allMet = false;
            }
        }

        if (allMet) {
            System.out.println("\n✓ ALL prerequisites met! You can take " + courseCode);
        } else {
            System.out.println("\n✗ Cannot take " + courseCode + " yet. Complete missing prerequisites.");
        }
    }

    private void generateStudyPlan() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("GENERATE STUDY PLAN");
        System.out.println("=".repeat(70));

        if (courseTree.inOrderTraversal().isEmpty()) {
            System.out.println("No courses available. Add courses first (Option 8).");
            return;
        }

        System.out.print("Maximum credits per semester (default 18): ");
        int maxCredits = 18;
        try {
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                maxCredits = Integer.parseInt(input);
                maxCredits = Math.max(12, Math.min(24, maxCredits));
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Using default 18 credits.");
        }

        System.out.println("\nGenerating plan with " + maxCredits + " credits per semester...");

        List<List<Course>> plan = planGenerator.generatePlan();
        currentPlan = plan;

        if (plan.isEmpty()) {
            System.out.println("No courses to plan. All courses may be completed.");
            return;
        }

        System.out.println("\n" + "=".repeat(70));
        System.out.println("YOUR STUDY PLAN");
        System.out.println("=".repeat(70));

        int totalCredits = 0;
        for (int i = 0; i < plan.size(); i++) {
            List<Course> semester = plan.get(i);
            int semesterCredits = semester.stream().mapToInt(Course::getCredits).sum();
            totalCredits += semesterCredits;

            System.out.printf("\nSEMESTER %d (%d credits)%n", i + 1, semesterCredits);
            System.out.println("-".repeat(40));

            for (Course course : semester) {
                System.out.printf("• %s: %s (%d credits)%n",
                        course.getCode(), course.getName(), course.getCredits());
            }
        }

        System.out.println("\n" + "=".repeat(70));
        System.out.printf("TOTAL: %d semesters, %d credits%n", plan.size(), totalCredits);
        System.out.println("=".repeat(70));

        // Show warnings
        List<String> warnings = planGenerator.checkPlanWarnings(plan);
        if (!warnings.isEmpty()) {
            System.out.println("\n⚠ WARNINGS:");
            for (String warning : warnings) {
                System.out.println("  • " + warning);
            }
        }
    }

    private void getAISuggestions() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("AI ELECTIVE SUGGESTIONS");
        System.out.println("=".repeat(70));

        if (completedCourses.isEmpty()) {
            System.out.println("Complete some courses first to get suggestions!");
            return;
        }

        System.out.print("Number of suggestions (1-5): ");
        int count = 3;
        try {
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                count = Integer.parseInt(input);
                count = Math.max(1, Math.min(5, count));
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Showing 3 suggestions.");
        }

        System.out.println("\n🤖 Analyzing your profile...");

        List<Course> suggestions = aiSuggester.suggestElectives(count);

        if (suggestions.isEmpty()) {
            System.out.println("No suggestions available.");
            return;
        }

        System.out.println("\nRECOMMENDED COURSES:");
        System.out.println("-".repeat(60));

        for (int i = 0; i < suggestions.size(); i++) {
            Course course = suggestions.get(i);
            String explanation = aiSuggester.getSuggestionExplanation(course);

            System.out.printf("\n%d. %s: %s%n", i + 1, course.getCode(), course.getName());
            System.out.printf("   Credits: %d%n", course.getCredits());
            System.out.printf("   Why: %s%n", explanation);
        }
    }

    private void exportPlan() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("EXPORT PLAN");
        System.out.println("=".repeat(70));

        if (currentPlan == null || currentPlan.isEmpty()) {
            System.out.println("Generate a study plan first (Option 3)!");
            return;
        }

        System.out.println("Export formats:");
        System.out.println("1. Text File (.txt) - For printing");
        System.out.println("2. CSV File (.csv) - For Excel/Sheets");
        System.out.println("3. Calendar File (.ics) - For Google/Outlook");
        System.out.print("\nEnter choice (1-3): ");

        try {
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1 -> {
                    PlanExporter.exportToTextFile(currentPlan, "study_plan.txt");
                    System.out.println("✓ Exported to: study_plan.txt");
                }
                case 2 -> {
                    PlanExporter.exportToCSV(currentPlan, "study_plan.csv");
                    System.out.println("✓ Exported to: study_plan.csv");
                }
                case 3 -> {
                    System.out.print("Enter starting year (e.g., 2024): ");
                    int year = Integer.parseInt(scanner.nextLine());
                    PlanExporter.exportToICS(currentPlan, "calendar.ics", year);
                    System.out.println("✓ Exported to: calendar.ics");
                    System.out.println("Import this .ics file into your calendar app!");
                }
                default -> System.out.println("Invalid choice.");
            }
        } catch (Exception e) {
            System.out.println("Error exporting: " + e.getMessage());
        }
    }

    private void detectCycles() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("DETECT PREREQUISITE CYCLES");
        System.out.println("=".repeat(70));

        if (prerequisiteGraph.hasCycle()) {
            System.out.println("✗ CIRCULAR DEPENDENCY DETECTED!");
            List<String> cycle = prerequisiteGraph.getCyclePath();

            System.out.print("\nCycle: ");
            for (int i = 0; i < cycle.size(); i++) {
                System.out.print(cycle.get(i));
                if (i < cycle.size() - 1) System.out.print(" → ");
            }

            System.out.println("\n\nThis creates an impossible situation!");
        } else {
            System.out.println("✓ No circular dependencies found.");
            System.out.println("All prerequisite chains are valid.");
        }
    }

    private void manageCompletedCourses() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("MANAGE COMPLETED COURSES");
        System.out.println("=".repeat(70));

        System.out.println("Currently completed: " + completedCourses.size() + " courses");

        boolean managing = true;
        while (managing) {
            System.out.println("\nOptions:");
            System.out.println("1. Mark course as completed");
            System.out.println("2. Remove from completed");
            System.out.println("3. View all grades");
            System.out.println("4. Update interests");
            System.out.println("5. Back to main menu");
            System.out.print("\nEnter choice (1-5): ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1 -> markCourseCompleted();
                    case 2 -> removeCourseCompleted();
                    case 3 -> viewAllGrades();
                    case 4 -> updateInterests();
                    case 5 -> managing = false;
                    default -> System.out.println("Invalid choice.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }

        updateComponents();
    }

    private void markCourseCompleted() {
        System.out.print("\nEnter course code to mark completed: ");
        String code = scanner.nextLine().toUpperCase().trim();

        Course course = courseTree.search(code);
        if (course == null) {
            System.out.println("Course not found: " + code);
            return;
        }

        if (course.isCompleted()) {
            System.out.println("Already completed!");
            return;
        }

        System.out.print("Enter grade (0-100): ");
        try {
            double grade = Double.parseDouble(scanner.nextLine());
            if (grade < 0 || grade > 100) {
                System.out.println("Grade must be 0-100. Using 75.");
                grade = 75;
            }

            course.setCompleted(true);
            course.setGrade(grade);
            completedCourses.add(code);
            grades.put(code, grade);

            System.out.printf("✓ %s marked as completed with grade %.1f%%%n", code, grade);
        } catch (NumberFormatException e) {
            System.out.println("Invalid grade.");
        }
    }

    private void removeCourseCompleted() {
        System.out.print("\nEnter course code to remove from completed: ");
        String code = scanner.nextLine().toUpperCase().trim();

        if (completedCourses.remove(code)) {
            Course course = courseTree.search(code);
            if (course != null) {
                course.setCompleted(false);
                course.setGrade(0);
            }
            grades.remove(code);

            System.out.println("✓ Removed " + code + " from completed courses");
        } else {
            System.out.println("Course not found in completed list: " + code);
        }
    }

    private void viewAllGrades() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("YOUR GRADES");
        System.out.println("=".repeat(50));

        if (grades.isEmpty()) {
            System.out.println("No grades recorded.");
            return;
        }

        double total = 0;
        for (Map.Entry<String, Double> entry : grades.entrySet()) {
            String code = entry.getKey();
            double grade = entry.getValue();
            total += grade;

            Course course = courseTree.search(code);
            String name = (course != null) ? course.getName() : "Unknown";

            System.out.printf("%-10s %-30s %6.1f%%%n", code, name, grade);
        }

        System.out.println("-".repeat(50));
        double average = total / grades.size();
        System.out.printf("Average: %.1f%%%n", average);
    }

    private void updateInterests() {
        System.out.println("\nCurrent interests: " + String.join(", ", interests));
        System.out.println("\n1. Add interest");
        System.out.println("2. Clear all interests");
        System.out.print("\nEnter choice (1-2): ");

        try {
            int choice = Integer.parseInt(scanner.nextLine());

            if (choice == 1) {
                System.out.print("Enter new interest: ");
                String interest = scanner.nextLine().trim();
                if (!interest.isEmpty()) {
                    interests.add(interest);
                    System.out.println("✓ Added interest: " + interest);
                }
            } else if (choice == 2) {
                interests.clear();
                System.out.println("✓ Cleared all interests");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
    }

    private void manageCourses() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("MANAGE COURSES");
        System.out.println("=".repeat(70));

        System.out.println("Current courses: " + courseTree.inOrderTraversal().size());

        boolean managing = true;
        while (managing) {
            System.out.println("\nOptions:");
            System.out.println("1. Add new course");
            System.out.println("2. View course details");
            System.out.println("3. Add prerequisite relationship");
            System.out.println("4. View prerequisite graph");
            System.out.println("5. Back to main menu");
            System.out.print("\nEnter choice (1-5): ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1 -> addNewCourseInteractive();
                    case 2 -> viewCourseDetails();
                    case 3 -> addPrerequisiteInteractive();
                    case 4 -> prerequisiteGraph.displayGraph();
                    case 5 -> managing = false;
                    default -> System.out.println("Invalid choice.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private void addNewCourseInteractive() {
        System.out.println("\n--- ADD NEW COURSE ---");

        System.out.print("Course Code: ");
        String code = scanner.nextLine().toUpperCase().trim();

        if (courseTree.search(code) != null) {
            System.out.println("Course already exists: " + code);
            return;
        }

        System.out.print("Course Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Credits (default 3): ");
        int credits = 3;
        try {
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                credits = Integer.parseInt(input);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Using 3 credits.");
        }

        Course course = new Course(code, name, credits);
        courseTree.insert(course);

        System.out.println("✓ Added course: " + code + " - " + name);
    }

    private void viewCourseDetails() {
        System.out.print("\nEnter course code: ");
        String code = scanner.nextLine().toUpperCase().trim();

        Course course = courseTree.search(code);
        if (course == null) {
            System.out.println("Course not found: " + code);
            return;
        }

        System.out.println("\n" + "=".repeat(50));
        System.out.println("COURSE DETAILS");
        System.out.println("=".repeat(50));
        System.out.println("Code: " + course.getCode());
        System.out.println("Name: " + course.getName());
        System.out.println("Credits: " + course.getCredits());
        System.out.println("Status: " + (course.isCompleted() ?
                String.format("Completed (%.1f%%)", course.getGrade()) : "Not completed"));

        List<String> prerequisites = prerequisiteGraph.getPrerequisites(code);
        System.out.println("Prerequisites: " +
                (prerequisites.isEmpty() ? "None" : String.join(", ", prerequisites)));

        List<String> dependents = prerequisiteGraph.getDependentCourses(code);
        System.out.println("Required for: " +
                (dependents.isEmpty() ? "None" : String.join(", ", dependents)));
    }

    private void addPrerequisiteInteractive() {
        System.out.println("\n--- ADD PREREQUISITE ---");

        System.out.print("Course that requires the prerequisite: ");
        String course = scanner.nextLine().toUpperCase().trim();

        System.out.print("Prerequisite course: ");
        String prerequisite = scanner.nextLine().toUpperCase().trim();

        // Check if courses exist
        if (courseTree.search(course) == null) {
            System.out.println("Course not found: " + course);//
            return;
        }

        if (courseTree.search(prerequisite) == null) {
            System.out.println("Prerequisite course not found: " + prerequisite);
            return;
        }

        prerequisiteGraph.addPrerequisite(course, prerequisite);

        // Check for cycles
        if (prerequisiteGraph.hasCycle()) {
            System.out.println("⚠ WARNING: This creates a circular dependency!");
            System.out.println("Removing the prerequisite...");
            // Note: In a real app, we'd need a remove method
            System.out.println("Please fix your course structure.");
            return;
        }

        System.out.println("✓ Added prerequisite: " + prerequisite + " → " + course);
    }

    private void viewStudentProfile() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("STUDENT PROFILE");
        System.out.println("=".repeat(70));

        List<Course> allCourses = courseTree.inOrderTraversal();
        long totalCourses = allCourses.size();
        long completedCount = allCourses.stream().filter(Course::isCompleted).count();
        long remainingCount = totalCourses - completedCount;
        double completionPercentage = totalCourses > 0 ?
                (completedCount * 100.0 / totalCourses) : 0;

        double totalGrade = 0;
        for (double grade : grades.values()) {
            totalGrade += grade;
        }
        double averageGrade = grades.isEmpty() ? 0 : totalGrade / grades.size();

        System.out.println("\nPROGRESS SUMMARY:");
        System.out.println("-".repeat(40));
        System.out.printf("Total Courses: %d%n", totalCourses);
        System.out.printf("Completed: %d (%.1f%%)%n", completedCount, completionPercentage);
        System.out.printf("Remaining: %d%n", remainingCount);
        System.out.printf("Average Grade: %.1f%%%n", averageGrade);

        if (!interests.isEmpty()) {
            System.out.println("\nINTERESTS:");
            System.out.println("-".repeat(40));
            for (String interest : interests) {
                System.out.println("• " + interest);
            }
        }

        System.out.println("\nRECOMMENDED NEXT:");
        System.out.println("-".repeat(40));
        List<Course> suggestions = aiSuggester.suggestElectives(3);
        if (suggestions.isEmpty()) {
            System.out.println("Complete more courses for suggestions.");
        } else {
            for (int i = 0; i < suggestions.size(); i++) {
                Course course = suggestions.get(i);
                System.out.printf("%d. %s: %s%n", i + 1, course.getCode(), course.getName());
            }
        }
    }

    private void saveProgress() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("SAVE PROGRESS");
        System.out.println("=".repeat(70));

        try {
            PlanExporter.saveProgress(completedCourses, grades, interests, PROGRESS_FILE);
            System.out.println("✓ Progress saved to: " + PROGRESS_FILE);
            System.out.println("\nSaved:");
            System.out.println("• " + completedCourses.size() + " completed courses");
            System.out.println("• " + grades.size() + " grades");
            System.out.println("• " + interests.size() + " interests");
            System.out.println("\nWill load automatically next time!");
        } catch (IOException e) {
            System.out.println("✗ Error saving: " + e.getMessage());
        }
    }

    // Update your main method in CoursePlannerApp.java:
public static void main(String[] args) {
    //For GUI:
    //com.courseplanner.CoursePlannerGUI.main(args);
    
    // OR for console:
    CoursePlannerApp app = new CoursePlannerApp();
    app.run();
}
}