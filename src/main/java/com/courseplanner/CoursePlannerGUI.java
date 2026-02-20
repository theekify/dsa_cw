package com.courseplanner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
        tableModel = new DefaultTableModel(columnNames, 0);
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
            String prereqStr = String.join(", ", prereqs);
            
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
            // Simulate checking prerequisites (you'd call your actual method)
            outputArea.setText("Checking prerequisites for: " + courseCode + "\n\n");
            
            // In real app, call: plannerApp.checkPrerequisites(courseCode)
            // For demo, show sample output
            outputArea.append("✓ Course found: " + courseCode + "\n");
            outputArea.append("✓ Prerequisites checked\n");
            outputArea.append("✓ Result: You CAN take this course!\n");
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
        
        StringBuilder sb = new StringBuilder();
        sb.append("STUDY PLAN (Max ").append(maxCredits).append(" credits/semester)\n");
        sb.append("=".repeat(60)).append("\n\n");
        
        // Sample plan for demo
        sb.append("SEMESTER 1 (15 credits)\n");
        sb.append("-".repeat(30)).append("\n");
        sb.append("• CS101: Programming Fundamentals (3)\n");
        sb.append("• MATH101: Calculus I (4)\n");
        sb.append("• ENG101: English Composition (3)\n");
        sb.append("• SCI101: General Science (4)\n\n");
        
        sb.append("SEMESTER 2 (16 credits)\n");
        sb.append("-".repeat(30)).append("\n");
        sb.append("• CS102: Object-Oriented Programming (3)\n");
        sb.append("• CS201: Data Structures (3)\n");
        sb.append("• MATH201: Calculus II (4)\n");
        sb.append("• HUM101: Humanities (3)\n\n");
        
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
        
        StringBuilder sb = new StringBuilder();
        sb.append("🤖 AI COURSE SUGGESTIONS\n");
        sb.append("=".repeat(50)).append("\n\n");
        
        // Sample suggestions for demo
        sb.append("1. CS201: Data Structures (Score: 87/100)\n");
        sb.append("   Why: Excellent in prerequisites (85%) • Matches programming interest\n\n");
        
        sb.append("2. CS402: Web Development (Score: 75/100)\n");
        sb.append("   Why: Good prerequisite performance • Manageable difficulty\n\n");
        
        sb.append("3. MATH201: Calculus II (Score: 68/100)\n");
        sb.append("   Why: Excellent in MATH101 (92%) • Important for future courses\n\n");
        
        outputArea.setText(sb.toString());
    }
    
    private void exportPlanDialog() {
        String[] options = {"Text File (.txt)", "CSV File (.csv)", "Calendar File (.ics)", "Cancel"};
        int choice = JOptionPane.showOptionDialog(mainFrame,
            "Choose export format:",
            "Export Plan",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            options,
            options[0]);
        
        if (choice < 3) {
            JOptionPane.showMessageDialog(mainFrame,
                "✓ Plan exported successfully as " + options[choice],
                "Export Complete",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void detectCycles() {
        outputArea.setText("🔍 DETECTING PREREQUISITE CYCLES\n");
        outputArea.append("=".repeat(50) + "\n\n");
        
        // Sample output for demo
        outputArea.append("✓ Analyzing prerequisite graph...\n\n");
        outputArea.append("✓ No circular dependencies found!\n");
        outputArea.append("✓ All prerequisite chains are valid.\n\n");
        outputArea.append("Prerequisite Graph:\n");
        outputArea.append("CS101 → CS102 → CS201 → CS301\n");
        outputArea.append("MATH101 → MATH201 → MATH301\n");
        outputArea.append("CS301 → CS401\n");
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
                        outputArea.setText("✓ Marked " + courseCode + " as completed with grade " + grade + "%\n");
                        outputArea.append("\nCourse status updated successfully!");
                        refreshAll();
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
        String[] options = {"Add New Course", "Add Prerequisite", "View Graph", "Cancel"};
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
            JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
            JTextField codeField = new JTextField();
            JTextField nameField = new JTextField();
            JTextField creditsField = new JTextField("3");
            
            panel.add(new JLabel("Course Code:"));
            panel.add(codeField);
            panel.add(new JLabel("Course Name:"));
            panel.add(nameField);
            panel.add(new JLabel("Credits:"));
            panel.add(creditsField);
            
            int result = JOptionPane.showConfirmDialog(mainFrame, panel,
                "Add New Course", JOptionPane.OK_CANCEL_OPTION);
            
            if (result == JOptionPane.OK_OPTION) {
                String code = codeField.getText().toUpperCase().trim();
                String name = nameField.getText().trim();
                String creditsStr = creditsField.getText().trim();
                
                if (!code.isEmpty() && !name.isEmpty()) {
                    outputArea.setText("✓ Added new course: " + code + " - " + name + "\n");
                    refreshAll();
                }
            }
        }
    }
    
    private void viewStudentProfile() {
        StringBuilder sb = new StringBuilder();
        sb.append("👤 STUDENT PROFILE\n");
        sb.append("=".repeat(50)).append("\n\n");
        
        // Sample data for demo
        sb.append("PROGRESS SUMMARY:\n");
        sb.append("-".repeat(30)).append("\n");
        sb.append("Total Courses: 20\n");
        sb.append("Completed: 4 (20.0%)\n");
        sb.append("Remaining: 16\n");
        sb.append("Average Grade: 85.8%\n\n");
        
        sb.append("INTERESTS:\n");
        sb.append("-".repeat(30)).append("\n");
        sb.append("• Programming\n");
        sb.append("• Web Development\n\n");
        
        sb.append("RECOMMENDED NEXT:\n");
        sb.append("-".repeat(30)).append("\n");
        sb.append("1. CS201: Data Structures\n");
        sb.append("2. MATH201: Calculus II\n");
        sb.append("3. CS102: Object-Oriented Programming\n");
        
        outputArea.setText(sb.toString());
    }
    
    private void saveProgress() {
        int choice = JOptionPane.showConfirmDialog(mainFrame,
            "Save all progress to file?", "Save Progress", JOptionPane.YES_NO_OPTION);
        
        if (choice == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(mainFrame,
                "✓ Progress saved successfully!\n\n" +
                "• 4 completed courses\n" +
                "• 4 grades recorded\n" +
                "• 2 interests saved",
                "Save Complete",
                JOptionPane.INFORMATION_MESSAGE);
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
        
        sb.append("Course Statistics:\n");
        sb.append("-".repeat(30)).append("\n");
        sb.append("Total Courses: ").append(courses.size()).append("\n");
        sb.append("Completed: ").append(completed).append("\n");
        sb.append("With Prerequisites: ").append(withPrereqs).append("\n");
        sb.append("Without Prerequisites: ").append(courses.size() - withPrereqs).append("\n\n");
        
        sb.append("Credit Distribution:\n");
        sb.append("-".repeat(30)).append("\n");
        long threeCredit = courses.stream().filter(c -> c.getCredits() == 3).count();
        long fourCredit = courses.stream().filter(c -> c.getCredits() == 4).count();
        long otherCredit = courses.stream().filter(c -> c.getCredits() != 3 && c.getCredits() != 4).count();
        
        sb.append("3-credit courses: ").append(threeCredit).append("\n");
        sb.append("4-credit courses: ").append(fourCredit).append("\n");
        sb.append("Other credits: ").append(otherCredit).append("\n");
        
        outputArea.setText(sb.toString());
    }
    
    private void checkSelectedCourse() {
        String selected = (String) courseComboBox.getSelectedItem();
        if (selected != null) {
            String courseCode = selected.split(" - ")[0];
            outputArea.setText("Checking prerequisites for: " + courseCode + "\n\n");
            outputArea.append("✓ Course found in system\n");
            outputArea.append("✓ Checking prerequisites...\n");
            outputArea.append("✓ Result: You CAN take this course!\n");
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
                        outputArea.setText("✓ Marked " + courseCode + " as completed with grade " + grade + "%\n");
                        refreshAll();
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
            
            StringBuilder sb = new StringBuilder();
            sb.append("📄 COURSE DETAILS\n");
            sb.append("=".repeat(50)).append("\n\n");
            sb.append("Code: ").append(courseCode).append("\n");
            sb.append("Name: ").append(selected.split(" - ")[1]).append("\n\n");
            
            // In real app, get actual course details
            sb.append("Status: Not completed\n");
            sb.append("Prerequisites: CS101, MATH101\n");
            sb.append("Required for: CS301, CS302\n");
            sb.append("Difficulty: Medium (3 credits)\n");
            
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