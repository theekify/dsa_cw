package com.courseplanner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

public class CoursePlannerGUI {
    private CoursePlannerApp plannerApp;
    private JFrame mainFrame;
    
    // Components
    private JTextArea outputArea;
    private JTable courseTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> courseComboBox;
    
    public CoursePlannerGUI() {
        plannerApp = new CoursePlannerApp();
        createGUI();
    }
    
    private void createGUI() {
        mainFrame = new JFrame("Course Planner & Prerequisite Tracker");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1000, 700);
        mainFrame.setLayout(new BorderLayout());
        
        // Top Panel - Title
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(41, 128, 185));
        JLabel titleLabel = new JLabel("🎓 COURSE PLANNER & PREREQUISITE TRACKER");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel);
        mainFrame.add(topPanel, BorderLayout.NORTH);
        
        // Left Panel - Buttons
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new GridLayout(12, 1, 5, 5));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        String[] buttons = {
            "📚 View All Courses",
            "✅ Check Prerequisites", 
            "📅 Generate Study Plan",
            "🤖 Get AI Suggestions",
            "💾 Export Plan",
            "🔄 Detect Cycles",
            "📝 Manage Completed",
            "➕ Manage Courses",
            "👤 View Profile",
            "💾 Save Progress",
            "📊 View Statistics",
            "❌ Exit"
        };
        
        for (String buttonText : buttons) {
            JButton button = new JButton(buttonText);
            button.addActionListener(new ButtonClickListener());
            leftPanel.add(button);
        }
        
        mainFrame.add(leftPanel, BorderLayout.WEST);
        
        // Center Panel - Output/Table
        JTabbedPane centerTabbedPane = new JTabbedPane();
        
        // Tab 1: Course Table
        JPanel tablePanel = new JPanel(new BorderLayout());
        String[] columnNames = {"Code", "Name", "Credits", "Completed", "Grade", "Prerequisites"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        courseTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(courseTable);
        tablePanel.add(tableScrollPane, BorderLayout.CENTER);
        centerTabbedPane.addTab("📋 Courses", tablePanel);
        
        // Tab 2: Output Console
        JPanel outputPanel = new JPanel(new BorderLayout());
        outputArea = new JTextArea();
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        outputArea.setEditable(false);
        JScrollPane outputScrollPane = new JScrollPane(outputArea);
        outputPanel.add(outputScrollPane, BorderLayout.CENTER);
        centerTabbedPane.addTab("📝 Output", outputPanel);
        
        // Tab 3: Quick Actions
        JPanel quickPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        quickPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Course selector for quick checks
        quickPanel.add(new JLabel("Select Course:"));
        courseComboBox = new JComboBox<>();
        quickPanel.add(courseComboBox);
        
        JButton checkPrereqBtn = new JButton("Check Prerequisites");
        checkPrereqBtn.addActionListener(e -> checkSelectedCourse());
        quickPanel.add(checkPrereqBtn);
        
        JButton markCompletedBtn = new JButton("Mark as Completed");
        markCompletedBtn.addActionListener(e -> markCourseCompleted());
        quickPanel.add(markCompletedBtn);
        
        JButton viewDetailsBtn = new JButton("View Details");
        viewDetailsBtn.addActionListener(e -> viewCourseDetails());
        quickPanel.add(viewDetailsBtn);
        
        quickPanel.add(new JLabel(""));
        quickPanel.add(new JLabel(""));
        
        JButton refreshBtn = new JButton("🔄 Refresh All");
        refreshBtn.addActionListener(e -> refreshAll());
        quickPanel.add(refreshBtn);
        
        centerTabbedPane.addTab("⚡ Quick Actions", quickPanel);
        
        mainFrame.add(centerTabbedPane, BorderLayout.CENTER);
        
        // Bottom Panel - Status
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel statusLabel = new JLabel("Ready");
        bottomPanel.add(statusLabel);
        mainFrame.add(bottomPanel, BorderLayout.SOUTH);
        
        // Load initial data
        refreshAll();
        
        mainFrame.setVisible(true);
    }
    
    private class ButtonClickListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = ((JButton)e.getSource()).getText();
            
            switch (command) {
                case "📚 View All Courses":
                    viewAllCourses();
                    break;
                case "✅ Check Prerequisites":
                    checkPrerequisitesDialog();
                    break;
                case "📅 Generate Study Plan":
                    generateStudyPlan();
                    break;
                case "🤖 Get AI Suggestions":
                    getAISuggestions();
                    break;
                case "💾 Export Plan":
                    exportPlanDialog();
                    break;
                case "🔄 Detect Cycles":
                    detectCycles();
                    break;
                case "📝 Manage Completed":
                    manageCompletedDialog();
                    break;
                case "➕ Manage Courses":
                    manageCoursesDialog();
                    break;
                case "👤 View Profile":
                    viewStudentProfile();
                    break;
                case "💾 Save Progress":
                    saveProgress();
                    break;
                case "📊 View Statistics":
                    showStatistics();
                    break;
                case "❌ Exit":
                    System.exit(0);
                    break;
            }
        }
    }
    
    private void refreshAll() {
        // Refresh course list in combo box
        courseComboBox.removeAllItems();
        List<Course> courses = plannerApp.courseTree.inOrderTraversal();
        for (Course course : courses) {
            courseComboBox.addItem(course.getCode() + " - " + course.getName());
        }
        
        // Refresh table
        refreshCourseTable();
    }
    
    private void refreshCourseTable() {
        tableModel.setRowCount(0); // Clear table
        
        List<Course> courses = plannerApp.courseTree.inOrderTraversal();
        for (Course course : courses) {
            List<String> prereqs = plannerApp.prerequisiteGraph.getPrerequisites(course.getCode());
            String prereqStr = prereqs.isEmpty() ? "None" : String.join(", ", prereqs);
            
            Object[] row = {
                course.getCode(),
                course.getName(),
                course.getCredits(),
                course.isCompleted() ? "✓" : "○",
                course.isCompleted() ? String.format("%.1f%%", course.getGrade()) : "-",
                prereqStr
            };
            tableModel.addRow(row);
        }
    }
    
    private void viewAllCourses() {
        StringBuilder sb = new StringBuilder();
        sb.append("ALL COURSES\n");
        sb.append("=".repeat(50)).append("\n");
        
        List<Course> courses = plannerApp.courseTree.inOrderTraversal();
        for (Course course : courses) {
            sb.append(course.toString()).append("\n");
            
            List<String> prereqs = plannerApp.prerequisiteGraph.getPrerequisites(course.getCode());
            if (!prereqs.isEmpty()) {
                sb.append("  Prerequisites: ").append(String.join(", ", prereqs)).append("\n");
            }
            sb.append("\n");
        }
        
        sb.append("Total: ").append(courses.size()).append(" courses");
        outputArea.setText(sb.toString());
    }
    
    private void checkPrerequisitesDialog() {
        String courseCode = JOptionPane.showInputDialog(mainFrame, 
            "Enter course code:", "Check Prerequisites", JOptionPane.QUESTION_MESSAGE);
        
        if (courseCode != null && !courseCode.trim().isEmpty()) {
            courseCode = courseCode.trim().toUpperCase();
            
            StringBuilder sb = new StringBuilder();
            sb.append("CHECKING PREREQUISITES FOR: ").append(courseCode).append("\n");
            sb.append("=".repeat(50)).append("\n\n");
            
            // Check if course exists
            Course course = plannerApp.courseTree.search(courseCode);
            if (course == null) {
                sb.append("❌ Course not found: ").append(courseCode);
                outputArea.setText(sb.toString());
                return;
            }
            
            sb.append("Course: ").append(course.getName()).append("\n");
            sb.append("Credits: ").append(course.getCredits()).append("\n\n");
            
            // Check prerequisites using PlanGenerator
            List<String> missing = plannerApp.planGenerator.getMissingPrerequisites(courseCode);
            
            if (missing.isEmpty()) {
                sb.append("✅ ALL PREREQUISITES SATISFIED!\n");
                sb.append("You can take this course.");
            } else {
                sb.append("❌ MISSING PREREQUISITES:\n");
                for (String prereq : missing) {
                    Course prereqCourse = plannerApp.courseTree.search(prereq);
                    String prereqName = (prereqCourse != null) ? prereqCourse.getName() : "Unknown";
                    sb.append("  • ").append(prereq).append(": ").append(prereqName).append("\n");
                }
                sb.append("\nComplete these prerequisites first.");
            }
            
            outputArea.setText(sb.toString());
        }
    }
    
    private void generateStudyPlan() {
        String creditsStr = JOptionPane.showInputDialog(mainFrame,
            "Maximum credits per semester (default 18):", "Generate Plan", JOptionPane.QUESTION_MESSAGE);
        
        int maxCredits = 18;
        if (creditsStr != null && !creditsStr.trim().isEmpty()) {
            try {
                maxCredits = Integer.parseInt(creditsStr);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(mainFrame, "Invalid number. Using default 18.");
            }
        }
        
        // Use PlanGenerator to generate actual plan
        List<List<Course>> plan = plannerApp.planGenerator.generatePlan();
        
        StringBuilder sb = new StringBuilder();
        sb.append("STUDY PLAN (Max ").append(maxCredits).append(" credits/semester)\n");
        sb.append("=".repeat(60)).append("\n\n");
        
        if (plan.isEmpty()) {
            sb.append("No courses available for planning.\n");
            sb.append("Add courses first using 'Manage Courses'.");
        } else {
            int totalCredits = 0;
            for (int i = 0; i < plan.size(); i++) {
                List<Course> semester = plan.get(i);
                int semesterCredits = semester.stream().mapToInt(Course::getCredits).sum();
                totalCredits += semesterCredits;
                
                sb.append("SEMESTER ").append(i + 1).append(" (").append(semesterCredits).append(" credits)\n");
                sb.append("-".repeat(40)).append("\n");
                
                for (Course course : semester) {
                    sb.append("• ").append(course.getCode()).append(": ")
                      .append(course.getName()).append(" (").append(course.getCredits()).append(" credits)\n");
                }
                sb.append("\n");
            }
            
            sb.append("=".repeat(60)).append("\n");
            sb.append("TOTAL: ").append(plan.size()).append(" semesters, ")
              .append(totalCredits).append(" credits\n");
        }
        
        outputArea.setText(sb.toString());
    }
    
    private void getAISuggestions() {
        String countStr = JOptionPane.showInputDialog(mainFrame,
            "Number of suggestions (1-5):", "AI Suggestions", JOptionPane.QUESTION_MESSAGE);
        
        int count = 3;
        if (countStr != null && !countStr.trim().isEmpty()) {
            try {
                count = Integer.parseInt(countStr);
                count = Math.max(1, Math.min(5, count));
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(mainFrame, "Invalid number. Showing 3 suggestions.");
            }
        }
        
        // Use AISuggester to get actual suggestions
        List<Course> suggestions = plannerApp.aiSuggester.suggestElectives(count);
        
        StringBuilder sb = new StringBuilder();
        sb.append("🤖 AI COURSE SUGGESTIONS\n");
        sb.append("=".repeat(50)).append("\n\n");
        
        if (suggestions.isEmpty()) {
            sb.append("No suggestions available.\n");
            sb.append("Complete some courses first to get personalized suggestions.");
        } else {
            for (int i = 0; i < suggestions.size(); i++) {
                Course course = suggestions.get(i);
                String explanation = plannerApp.aiSuggester.getSuggestionExplanation(course);
                
                sb.append(i + 1).append(". ").append(course.getCode()).append(": ")
                  .append(course.getName()).append("\n");
                sb.append("   Why: ").append(explanation).append("\n\n");
            }
        }
        
        outputArea.setText(sb.toString());
    }
    
    private void exportPlanDialog() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Study Plan");
        
        FileNameExtensionFilter txtFilter = new FileNameExtensionFilter("Text Files (*.txt)", "txt");
        FileNameExtensionFilter csvFilter = new FileNameExtensionFilter("CSV Files (*.csv)", "csv");
        
        fileChooser.addChoosableFileFilter(txtFilter);
        fileChooser.addChoosableFileFilter(csvFilter);
        fileChooser.setFileFilter(txtFilter);
        
        int userSelection = fileChooser.showSaveDialog(mainFrame);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            
            try {
                // Get current plan
                List<List<Course>> plan = plannerApp.planGenerator.generatePlan();
                
                if (fileChooser.getFileFilter() == txtFilter) {
                    String path = fileToSave.getAbsolutePath();
                    if (!path.toLowerCase().endsWith(".txt")) {
                        path += ".txt";
                    }
                    PlanExporter.exportToTextFile(plan, path);
                } else if (fileChooser.getFileFilter() == csvFilter) {
                    String path = fileToSave.getAbsolutePath();
                    if (!path.toLowerCase().endsWith(".csv")) {
                        path += ".csv";
                    }
                    PlanExporter.exportToCSV(plan, path);
                }
                
                JOptionPane.showMessageDialog(mainFrame,
                    "✓ Plan exported successfully!",
                    "Export Complete",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(mainFrame,
                    "Error exporting: " + e.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void detectCycles() {
        outputArea.setText("🔍 DETECTING PREREQUISITE CYCLES\n");
        outputArea.append("=".repeat(50) + "\n\n");
        
        boolean hasCycle = plannerApp.prerequisiteGraph.hasCycle();
        
        if (!hasCycle) {
            outputArea.append("✅ No circular dependencies found!\n");
            outputArea.append("All prerequisite chains are valid.\n\n");
        } else {
            outputArea.append("❌ CIRCULAR DEPENDENCY DETECTED!\n\n");
            List<String> cycle = plannerApp.prerequisiteGraph.getCyclePath();
            
            outputArea.append("Cycle: ");
            for (int i = 0; i < cycle.size(); i++) {
                outputArea.append(cycle.get(i));
                if (i < cycle.size() - 1) {
                    outputArea.append(" → ");
                }
            }
            outputArea.append("\n\nThis creates an impossible situation!");
        }
    }
    
    private void manageCompletedDialog() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        
        JTextField courseField = new JTextField();
        JTextField gradeField = new JTextField();
        
        panel.add(new JLabel("Course Code:"));
        panel.add(courseField);
        panel.add(new JLabel("Grade (0-100):"));
        panel.add(gradeField);
        panel.add(new JLabel(""));
        panel.add(new JLabel(""));
        
        int result = JOptionPane.showConfirmDialog(mainFrame, panel,
            "Mark Course as Completed", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            String courseCode = courseField.getText().toUpperCase().trim();
            String gradeStr = gradeField.getText().trim();
            
            if (!courseCode.isEmpty() && !gradeStr.isEmpty()) {
                try {
                    double grade = Double.parseDouble(gradeStr);
                    if (grade >= 0 && grade <= 100) {
                        Course course = plannerApp.courseTree.search(courseCode);
                        if (course != null) {
                            course.setCompleted(true);
                            course.setGrade(grade);
                            
                            // Update lists in plannerApp
                            plannerApp.completedCourses.add(courseCode);
                            plannerApp.grades.put(courseCode, grade);
                            
                            // Update components
                            plannerApp.planGenerator.setCompletedCourses(plannerApp.completedCourses);
                            plannerApp.aiSuggester.setCompletedCourses(plannerApp.completedCourses);
                            plannerApp.aiSuggester.setGrades(plannerApp.grades);
                            
                            outputArea.setText("✓ Marked " + courseCode + " as completed with grade " + grade + "%\n");
                            refreshAll();
                        } else {
                            JOptionPane.showMessageDialog(mainFrame, "Course not found!");
                        }
                    } else {
                        JOptionPane.showMessageDialog(mainFrame, "Grade must be between 0-100");
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(mainFrame, "Invalid grade format");
                }
            }
        }
    }
    
    private void manageCoursesDialog() {
        String[] options = {"Add New Course", "Add Prerequisite", "Cancel"};
        int choice = JOptionPane.showOptionDialog(mainFrame,
            "Course Management Options:",
            "Manage Courses",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            options,
            options[0]);
        
        if (choice == 0) {
            // Add new course
            JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
            JTextField codeField = new JTextField();
            JTextField nameField = new JTextField();
            JTextField creditsField = new JTextField("3");
            
            panel.add(new JLabel("Course Code:"));
            panel.add(codeField);
            panel.add(new JLabel("Course Name:"));
            panel.add(nameField);
            panel.add(new JLabel("Credits:"));
            panel.add(creditsField);
            panel.add(new JLabel(""));
            panel.add(new JLabel(""));
            
            int result = JOptionPane.showConfirmDialog(mainFrame, panel,
                "Add New Course", JOptionPane.OK_CANCEL_OPTION);
            
            if (result == JOptionPane.OK_OPTION) {
                String code = codeField.getText().toUpperCase().trim();
                String name = nameField.getText().trim();
                
                try {
                    int credits = Integer.parseInt(creditsField.getText().trim());
                    
                    Course course = new Course(code, name, credits);
                    plannerApp.courseTree.insert(course);
                    
                    outputArea.setText("✓ Added new course: " + code + " - " + name + "\n");
                    refreshAll();
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(mainFrame, "Invalid credits value");
                }
            }
        } else if (choice == 1) {
            // Add prerequisite
            JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
            JTextField courseField = new JTextField();
            JTextField prereqField = new JTextField();
            
            panel.add(new JLabel("Course Code:"));
            panel.add(courseField);
            panel.add(new JLabel("Prerequisite:"));
            panel.add(prereqField);
            panel.add(new JLabel(""));
            panel.add(new JLabel(""));
            
            int result = JOptionPane.showConfirmDialog(mainFrame, panel,
                "Add Prerequisite", JOptionPane.OK_CANCEL_OPTION);
            
            if (result == JOptionPane.OK_OPTION) {
                String course = courseField.getText().toUpperCase().trim();
                String prereq = prereqField.getText().toUpperCase().trim();
                
                if (!course.isEmpty() && !prereq.isEmpty()) {
                    plannerApp.prerequisiteGraph.addPrerequisite(course, prereq);
                    
                    if (!plannerApp.prerequisiteGraph.hasCycle()) {
                        outputArea.setText("✓ Added prerequisite: " + prereq + " → " + course + "\n");
                    } else {
                        JOptionPane.showMessageDialog(mainFrame, 
                            "⚠ Warning: This creates a cycle!", "Cycle Detected", JOptionPane.WARNING_MESSAGE);
                    }
                    refreshAll();
                }
            }
        }
    }
    
    private void viewStudentProfile() {
        StringBuilder sb = new StringBuilder();
        sb.append("👤 STUDENT PROFILE\n");
        sb.append("=".repeat(50)).append("\n\n");
        
        List<Course> allCourses = plannerApp.courseTree.inOrderTraversal();
        long completed = allCourses.stream().filter(Course::isCompleted).count();
        
        double avgGrade = plannerApp.grades.values().stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
        
        sb.append("PROGRESS SUMMARY:\n");
        sb.append("-".repeat(30)).append("\n");
        sb.append("Total Courses: ").append(allCourses.size()).append("\n");
        sb.append("Completed: ").append(completed).append("\n");
        sb.append("Completion: ").append(String.format("%.1f%%", 
            allCourses.isEmpty() ? 0 : (completed * 100.0 / allCourses.size()))).append("\n");
        sb.append("Average Grade: ").append(String.format("%.1f%%", avgGrade)).append("\n\n");
        
        if (!plannerApp.interests.isEmpty()) {
            sb.append("INTERESTS:\n");
            sb.append("-".repeat(30)).append("\n");
            for (String interest : plannerApp.interests) {
                sb.append("• ").append(interest).append("\n");
            }
            sb.append("\n");
        }
        
        sb.append("RECOMMENDED NEXT:\n");
        sb.append("-".repeat(30)).append("\n");
        List<Course> suggestions = plannerApp.aiSuggester.suggestElectives(3);
        if (suggestions.isEmpty()) {
            sb.append("Complete more courses for recommendations.\n");
        } else {
            for (int i = 0; i < suggestions.size(); i++) {
                Course c = suggestions.get(i);
                sb.append(i + 1).append(". ").append(c.getCode()).append(": ")
                  .append(c.getName()).append("\n");
            }
        }
        
        outputArea.setText(sb.toString());
    }
    
    private void saveProgress() {
        try {
            PlanExporter.saveProgress(
                plannerApp.completedCourses,
                plannerApp.grades,
                plannerApp.interests,
                "student_progress.txt"
            );
            
            JOptionPane.showMessageDialog(mainFrame,
                "✓ Progress saved successfully!\n\n" +
                "• " + plannerApp.completedCourses.size() + " completed courses\n" +
                "• " + plannerApp.grades.size() + " grades recorded\n" +
                "• " + plannerApp.interests.size() + " interests",
                "Save Complete",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(mainFrame,
                "Error saving: " + e.getMessage(),
                "Save Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("📊 SYSTEM STATISTICS\n");
        sb.append("=".repeat(50)).append("\n\n");
        
        List<Course> courses = plannerApp.courseTree.inOrderTraversal();
        long completed = courses.stream().filter(Course::isCompleted).count();
        long withPrereqs = courses.stream()
            .filter(c -> !plannerApp.prerequisiteGraph.getPrerequisites(c.getCode()).isEmpty())
            .count();
        
        double avgGrade = plannerApp.grades.values().stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
        
        sb.append("Course Statistics:\n");
        sb.append("-".repeat(30)).append("\n");
        sb.append("Total Courses: ").append(courses.size()).append("\n");
        sb.append("Completed: ").append(completed).append("\n");
        sb.append("Average Grade: ").append(String.format("%.1f%%", avgGrade)).append("\n");
        sb.append("With Prerequisites: ").append(withPrereqs).append("\n");
        sb.append("Without Prerequisites: ").append(courses.size() - withPrereqs).append("\n\n");
        
        sb.append("Graph Statistics:\n");
        sb.append("-".repeat(30)).append("\n");
        sb.append("Has Cycles: ").append(plannerApp.prerequisiteGraph.hasCycle() ? "Yes" : "No").append("\n");
        
        outputArea.setText(sb.toString());
    }
    
    private void checkSelectedCourse() {
        String selected = (String) courseComboBox.getSelectedItem();
        if (selected != null) {
            String courseCode = selected.split(" - ")[0];
            
            StringBuilder sb = new StringBuilder();
            sb.append("CHECKING: ").append(courseCode).append("\n\n");
            
            Course course = plannerApp.courseTree.search(courseCode);
            if (course == null) {
                sb.append("Course not found.");
                outputArea.setText(sb.toString());
                return;
            }
            
            sb.append("Course: ").append(course.getName()).append("\n");
            sb.append("Credits: ").append(course.getCredits()).append("\n");
            sb.append("Status: ").append(course.isCompleted() ? "Completed ✓" : "Not completed ○").append("\n\n");
            
            sb.append("PREREQUISITES:\n");
            List<String> prereqs = plannerApp.prerequisiteGraph.getPrerequisites(courseCode);
            
            if (prereqs.isEmpty()) {
                sb.append("None\n");
            } else {
                for (String prereq : prereqs) {
                    Course prereqCourse = plannerApp.courseTree.search(prereq);
                    String status = plannerApp.completedCourses.contains(prereq) ? "✓" : "✗";
                    sb.append("  ").append(status).append(" ").append(prereq);
                    if (prereqCourse != null) {
                        sb.append(": ").append(prereqCourse.getName());
                    }
                    sb.append("\n");
                }
            }
            
            outputArea.setText(sb.toString());
        }
    }
    
    private void markCourseCompleted() {
        String selected = (String) courseComboBox.getSelectedItem();
        if (selected != null) {
            String courseCode = selected.split(" - ")[0];
            
            String gradeStr = JOptionPane.showInputDialog(mainFrame,
                "Enter grade for " + courseCode + " (0-100):", "Mark Completed", JOptionPane.QUESTION_MESSAGE);
            
            if (gradeStr != null && !gradeStr.trim().isEmpty()) {
                try {
                    double grade = Double.parseDouble(gradeStr);
                    if (grade >= 0 && grade <= 100) {
                        Course course = plannerApp.courseTree.search(courseCode);
                        if (course != null) {
                            course.setCompleted(true);
                            course.setGrade(grade);
                            
                            // Update lists
                            if (!plannerApp.completedCourses.contains(courseCode)) {
                                plannerApp.completedCourses.add(courseCode);
                            }
                            plannerApp.grades.put(courseCode, grade);
                            
                            // Update components
                            plannerApp.planGenerator.setCompletedCourses(plannerApp.completedCourses);
                            plannerApp.aiSuggester.setCompletedCourses(plannerApp.completedCourses);
                            plannerApp.aiSuggester.setGrades(plannerApp.grades);
                            
                            outputArea.setText("✓ Marked " + courseCode + " as completed with grade " + grade + "%\n");
                            refreshAll();
                        }
                    } else {
                        JOptionPane.showMessageDialog(mainFrame, "Grade must be between 0-100");
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(mainFrame, "Invalid grade");
                }
            }
        }
    }
    
    private void viewCourseDetails() {
        String selected = (String) courseComboBox.getSelectedItem();
        if (selected != null) {
            String courseCode = selected.split(" - ")[0];
            String courseName = selected.split(" - ")[1];
            
            StringBuilder sb = new StringBuilder();
            sb.append("📄 COURSE DETAILS\n");
            sb.append("=".repeat(50)).append("\n\n");
            
            Course course = plannerApp.courseTree.search(courseCode);
            if (course == null) {
                sb.append("Course not found.");
                outputArea.setText(sb.toString());
                return;
            }
            
            sb.append("Code: ").append(course.getCode()).append("\n");
            sb.append("Name: ").append(course.getName()).append("\n");
            sb.append("Credits: ").append(course.getCredits()).append("\n");
            sb.append("Status: ").append(course.isCompleted() ? 
                "Completed ✓ (Grade: " + String.format("%.1f%%", course.getGrade()) + ")" : 
                "Not completed ○").append("\n\n");
            
            List<String> prereqs = plannerApp.prerequisiteGraph.getPrerequisites(courseCode);
            sb.append("PREREQUISITES:\n");
            sb.append("-".repeat(20)).append("\n");
            if (prereqs.isEmpty()) {
                sb.append("None\n");
            } else {
                for (String prereq : prereqs) {
                    sb.append("• ").append(prereq).append("\n");
                }
            }
            sb.append("\n");
            
            List<String> dependents = plannerApp.prerequisiteGraph.getDependentCourses(courseCode);
            sb.append("REQUIRED FOR:\n");
            sb.append("-".repeat(20)).append("\n");
            if (dependents.isEmpty()) {
                sb.append("None\n");
            } else {
                for (String dependent : dependents) {
                    sb.append("• ").append(dependent).append("\n");
                }
            }
            
            outputArea.setText(sb.toString());
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new CoursePlannerGUI();
        });
    }
}
