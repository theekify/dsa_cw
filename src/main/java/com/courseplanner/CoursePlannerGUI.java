package com.courseplanner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
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
import javax.swing.table.JTableHeader;

public class CoursePlannerGUI {
    private CoursePlannerApp plannerApp;
    private JFrame mainFrame;
    
    // Modern color palette
    private static final Color BG_PRIMARY = new Color(255, 255, 255);
    private static final Color BG_SECONDARY = new Color(249, 250, 251);
    private static final Color ACCENT_PRIMARY = new Color(79, 70, 229); // Indigo
    private static final Color ACCENT_HOVER = new Color(67, 56, 202); // Darker indigo
    private static final Color ACCENT_PRIMARY = new Color(79, 70, 229);
    private static final Color ACCENT_HOVER = new Color(67, 56, 202);
    private static final Color TEXT_PRIMARY = new Color(17, 24, 39);
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128);
    private static final Color BORDER_COLOR = new Color(229, 231, 235);
    private static final Color SUCCESS = new Color(16, 185, 129);
    private static final Color WARNING = new Color(245, 158, 11);
    
    // Components
    private JTextArea outputArea;
    private JTable courseTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> courseComboBox;
    private JLabel statusLabel;
    
    public CoursePlannerGUI() {
        plannerApp = new CoursePlannerApp();
        createGUI();
        
        // Show welcome dialog after GUI is created
        SwingUtilities.invokeLater(() -> showWelcomeDialog());
    }
    
    private void createGUI() {
        // Set system look and feel as base, then customize
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        mainFrame = new JFrame("Course Planner");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1200, 800);
        mainFrame.setLayout(new BorderLayout());
        mainFrame.getContentPane().setBackground(BG_PRIMARY);
        
        // Top Navigation Bar
        JPanel navBar = createNavBar();
        mainFrame.add(navBar, BorderLayout.NORTH);
        
        // Main Content with Sidebar
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(BG_PRIMARY);
        
        // Sidebar with main actions
        JPanel sidebar = createSidebar();
        mainContent.add(sidebar, BorderLayout.WEST);
        
        // Center Panel with Tabs
        JTabbedPane centerTabbedPane = createTabbedPane();
        mainContent.add(centerTabbedPane, BorderLayout.CENTER);
        
        mainFrame.add(mainContent, BorderLayout.CENTER);
        
        // Modern Status Bar
        JPanel statusBar = createStatusBar();
        mainFrame.add(statusBar, BorderLayout.SOUTH);
        
        // Show initial setup dialog
        SwingUtilities.invokeLater(() -> showInitialSetupDialog());
        
        // Load initial data
        refreshAll();
        
        mainFrame.setVisible(true);
    }
    
    private void showWelcomeDialog() {
        JPanel panel = new JPanel(new BorderLayout(10, 20));
        panel.setBackground(BG_PRIMARY);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        // Welcome message
        JLabel welcomeLabel = new JLabel("🎓 Welcome to Course Planner!");
        welcomeLabel.setFont(new Font("Inter", Font.BOLD, 24));
        welcomeLabel.setForeground(ACCENT_PRIMARY);
        welcomeLabel.setHorizontalAlignment(JLabel.CENTER);
        
        JLabel subLabel = new JLabel("Let's set up your academic profile");
        subLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        subLabel.setForeground(TEXT_SECONDARY);
        subLabel.setHorizontalAlignment(JLabel.CENTER);
        
        JPanel messagePanel = new JPanel(new GridLayout(2, 1));
        messagePanel.setBackground(BG_PRIMARY);
        messagePanel.add(welcomeLabel);
        messagePanel.add(subLabel);
        
        // Options
        JPanel optionsPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        optionsPanel.setBackground(BG_PRIMARY);
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        JButton setupButton = createModernButton("📝 Mark Completed Courses");
        setupButton.setFont(new Font("Inter", Font.BOLD, 14));
        setupButton.addActionListener(e -> {
            JOptionPane.getRootFrame().dispose();
            showCompleteSetupDialog();
        });
        
        JButton skipButton = createModernButton("⏭️ Skip for Now");
        skipButton.setFont(new Font("Inter", Font.PLAIN, 14));
        skipButton.setBackground(TEXT_SECONDARY);
        skipButton.addActionListener(e -> {
            JOptionPane.getRootFrame().dispose();
            refreshAll();
        });
        
        optionsPanel.add(setupButton);
        optionsPanel.add(skipButton);
        
        panel.add(messagePanel, BorderLayout.NORTH);
        panel.add(optionsPanel, BorderLayout.CENTER);
        
        JOptionPane.showOptionDialog(mainFrame, panel, "Welcome", 
            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, 
            null, new Object[]{}, null);
    }
    
    private void showCompleteSetupDialog() {
        JDialog dialog = new JDialog(mainFrame, "Course Setup", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(BG_PRIMARY);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 20));
        mainPanel.setBackground(BG_PRIMARY);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("Mark Your Completed Courses");
        titleLabel.setFont(new Font("Inter", Font.BOLD, 18));
        titleLabel.setForeground(ACCENT_PRIMARY);
        
        // Get all courses
        List<Course> allCourses = plannerApp.courseTree.inOrderTraversal();
        
        // Create scrollable panel for courses
        JPanel coursesPanel = new JPanel(new GridLayout(allCourses.size() + 3, 3, 10, 10));
        coursesPanel.setBackground(BG_PRIMARY);
        
        // Headers
        coursesPanel.add(new JLabel("Course"));
        coursesPanel.add(new JLabel("Completed"));
        coursesPanel.add(new JLabel("Grade"));
        
        JCheckBox[] checkBoxes = new JCheckBox[allCourses.size()];
        JTextField[] gradeFields = new JTextField[allCourses.size()];
        
        // Add all courses
        for (int i = 0; i < allCourses.size(); i++) {
            Course course = allCourses.get(i);
            
            JLabel courseLabel = new JLabel(course.getCode() + " - " + course.getName());
            courseLabel.setFont(new Font("Inter", Font.PLAIN, 12));
            coursesPanel.add(courseLabel);
            
            JCheckBox checkBox = new JCheckBox();
            checkBox.setBackground(BG_PRIMARY);
            gradeFields[i] = new JTextField(5);
            gradeFields[i].setEnabled(false);
            
            int index = i;
            checkBox.addActionListener(e -> {
                gradeFields[index].setEnabled(checkBox.isSelected());
                if (!checkBox.isSelected()) {
                    gradeFields[index].setText("");
                }
            });
            
            checkBoxes[i] = checkBox;
            coursesPanel.add(checkBox);
            coursesPanel.add(gradeFields[i]);
        }
        
        // Interests section
        coursesPanel.add(new JLabel(""));
        coursesPanel.add(new JLabel(""));
        coursesPanel.add(new JLabel(""));
        
        coursesPanel.add(new JLabel("Academic Interests:"));
        JTextField interestsField = new JTextField(20);
        interestsField.setToolTipText("e.g., Programming, AI, Web Development, Data Science");
        coursesPanel.add(interestsField);
        coursesPanel.add(new JLabel(""));
        
        JScrollPane scrollPane = new JScrollPane(coursesPanel);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(BG_PRIMARY);
        
        JButton saveButton = createModernButton("Save Setup");
        saveButton.addActionListener(e -> {
            // Process completed courses
            for (int i = 0; i < allCourses.size(); i++) {
                if (checkBoxes[i].isSelected()) {
                    String gradeText = gradeFields[i].getText().trim();
                    if (!gradeText.isEmpty()) {
                        try {
                            double grade = Double.parseDouble(gradeText);
                            if (grade >= 0 && grade <= 100) {
                                Course course = allCourses.get(i);
                                course.setCompleted(true);
                                course.setGrade(grade);
                                plannerApp.completedCourses.add(course.getCode());
                                plannerApp.grades.put(course.getCode(), grade);
                            }
                        } catch (NumberFormatException ex) {
                            // Skip invalid grades
                        }
                    }
                }
            }
            
            // Process interests
            String interests = interestsField.getText().trim();
            if (!interests.isEmpty()) {
                String[] interestArray = interests.split(",");
                for (String interest : interestArray) {
                    plannerApp.interests.add(interest.trim());
                }
            }
            
            // Update components
            plannerApp.planGenerator.setCompletedCourses(plannerApp.completedCourses);
            plannerApp.aiSuggester.setCompletedCourses(plannerApp.completedCourses);
            plannerApp.aiSuggester.setGrades(plannerApp.grades);
            plannerApp.aiSuggester.setInterests(plannerApp.interests);
            
            refreshAll();
            dialog.dispose();
            
            JOptionPane.showMessageDialog(mainFrame, 
                "✓ Setup complete!\n\n" +
                "• " + plannerApp.completedCourses.size() + " courses marked completed\n" +
                "• " + plannerApp.interests.size() + " interests saved", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
        });
        
        JButton cancelButton = createModernButton("Cancel");
        cancelButton.setBackground(TEXT_SECONDARY);
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setSize(700, 600);
        dialog.setLocationRelativeTo(mainFrame);
        dialog.setVisible(true);
    }
    
    private JPanel createNavBar() {
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(ACCENT_PRIMARY);
        navBar.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        
        JLabel titleLabel = new JLabel("📚 Course Planner");
        titleLabel.setFont(new Font("Inter", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel versionLabel = new JLabel("v2.0 • Modern UI");
        versionLabel.setFont(new Font("Inter", Font.PLAIN, 12));
        versionLabel.setForeground(new Color(255, 255, 255, 200));
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setBackground(ACCENT_PRIMARY);
        rightPanel.add(versionLabel);
        
        navBar.add(titleLabel, BorderLayout.WEST);
        navBar.add(rightPanel, BorderLayout.EAST);
        
        return navBar;
    }
    
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new GridLayout(14, 1, 0, 8));
        sidebar.setBackground(BG_SECONDARY);
        sidebar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR),
            BorderFactory.createEmptyBorder(24, 16, 24, 16)
        ));
        sidebar.setPreferredSize(new Dimension(200, 0));
        
        String[][] menuItems = {
            {"📋", "View All Courses"},
            {"✅", "Check Prerequisites"},
            {"📅", "Generate Study Plan"},
            {"🤖", "Get AI Suggestions"},
            {"💾", "Export Plan"},
            {"🔄", "Detect Cycles"},
            {"📝", "Manage Completed"},
            {"➕", "Manage Courses"},
            {"👤", "View Profile"},
            {"💾", "Save Progress"},
            {"📊", "View Statistics"},
            {"❌", "Exit"}
        };
        
        for (String[] item : menuItems) {
            JButton btn = createModernButton(item[0] + "  " + item[1]);
            btn.addActionListener(new ButtonClickListener());
            sidebar.add(btn);
        }
        
        return sidebar;
    }
    
    private JTabbedPane createTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Inter", Font.PLAIN, 13));
        tabbedPane.setBackground(BG_PRIMARY);
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        
        JPanel coursesTab = createCoursesTab();
        tabbedPane.addTab("📋 Courses", coursesTab);
        
        JPanel outputTab = createOutputTab();
        tabbedPane.addTab("📝 Output", outputTab);
        
        JPanel quickTab = createQuickActionsTab();
        tabbedPane.addTab("⚡ Quick Actions", quickTab);
        
        return tabbedPane;
    }
    
    private JPanel createCoursesTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_PRIMARY);
        
        String[] columnNames = {"Code", "Course Name", "Credits", "Completed", "Grade", "Prerequisites"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        courseTable = new JTable(tableModel);
        courseTable.setFont(new Font("Inter", Font.PLAIN, 13));
        courseTable.setRowHeight(36);
        courseTable.setBackground(BG_PRIMARY);
        courseTable.setGridColor(BORDER_COLOR);
        courseTable.setSelectionBackground(new Color(79, 70, 229, 30));
        courseTable.setSelectionForeground(TEXT_PRIMARY);
        courseTable.setShowVerticalLines(false);
        
        JTableHeader header = courseTable.getTableHeader();
        header.setFont(new Font("Inter", Font.BOLD, 12));
        header.setBackground(BG_SECONDARY);
        header.setForeground(TEXT_SECONDARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        
        JScrollPane scrollPane = new JScrollPane(courseTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BG_PRIMARY);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createOutputTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(BG_PRIMARY);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        
        outputArea = new JTextArea();
        outputArea.setFont(new Font("JetBrains Mono", Font.PLAIN, 13));
        outputArea.setEditable(false);
        outputArea.setBackground(BG_SECONDARY);
        outputArea.setForeground(TEXT_PRIMARY);
        outputArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BG_SECONDARY);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createQuickActionsTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_PRIMARY);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        
        JPanel content = new JPanel(new GridLayout(6, 2, 12, 12));
        content.setBackground(BG_PRIMARY);
        content.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(24, 24, 24, 24)
        ));
        
        JLabel selectLabel = new JLabel("Select Course");
        selectLabel.setFont(new Font("Inter", Font.BOLD, 13));
        selectLabel.setForeground(TEXT_PRIMARY);
        content.add(selectLabel);
        
        courseComboBox = new JComboBox<>();
        courseComboBox.setFont(new Font("Inter", Font.PLAIN, 13));
        courseComboBox.setBackground(BG_PRIMARY);
        courseComboBox.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        content.add(courseComboBox);
        
        JButton checkBtn = createModernButton("✓ Check Prerequisites");
        checkBtn.addActionListener(e -> checkSelectedCourse());
        content.add(checkBtn);
        
        JButton markBtn = createModernButton("★ Mark Completed");
        markBtn.addActionListener(e -> markCourseCompleted());
        content.add(markBtn);
        
        JButton detailsBtn = createModernButton("ℹ View Details");
        detailsBtn.addActionListener(e -> viewCourseDetails());
        content.add(detailsBtn);
        
        content.add(new JLabel(""));
        content.add(new JLabel(""));
        
        JButton refreshBtn = createModernButton("↻ Refresh Data");
        refreshBtn.addActionListener(e -> refreshAll());
        content.add(refreshBtn);
        
        panel.add(content);
        
        return panel;
    }
    
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBackground(BG_SECONDARY);
        statusBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        
        statusLabel = new JLabel("● System Ready");
        statusLabel.setFont(new Font("Inter", Font.PLAIN, 12));
        statusLabel.setForeground(SUCCESS);
        
        statusBar.add(statusLabel);
        
        return statusBar;
    }
    
    private JButton createModernButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2.setColor(ACCENT_HOVER);
                } else if (getModel().isRollover()) {
                    g2.setColor(ACCENT_PRIMARY.brighter());
                } else {
                    g2.setColor(ACCENT_PRIMARY);
                }
                
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                
                super.paintComponent(g);
            }
        };
        
        button.setFont(new Font("Inter", Font.PLAIN, 12));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    private class ButtonClickListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = ((JButton)e.getSource()).getText();
            statusLabel.setText("● Processing...");
            statusLabel.setForeground(WARNING);
            
            String action = command.replaceAll("[^\\p{L}\\p{N}\\s]", "").trim();
            
            switch (action) {
                case "View All Courses":
                    viewAllCourses();
                    break;
                case "Check Prerequisites":
                    checkPrerequisitesDialog();
                    break;
                case "Generate Study Plan":
                    generateStudyPlan();
                    break;
                case "Get AI Suggestions":
                    getAISuggestions();
                    break;
                case "Export Plan":
                    exportPlanDialog();
                    break;
                case "Detect Cycles":
                    detectCycles();
                    break;
                case "Manage Completed":
                    manageCompletedDialog();
                    break;
                case "Manage Courses":
                    manageCoursesDialog();
                    break;
                case "View Profile":
                    viewStudentProfile();
                    break;
                case "Save Progress":
                    saveProgress();
                    break;
                case "View Statistics":
                    showStatistics();
                    break;
                case "Exit":
                    System.exit(0);
                    break;
            }
            
            statusLabel.setText("● System Ready");
            statusLabel.setForeground(SUCCESS);
        }
    }
    
    // All your existing functionality methods remain exactly the same
    private void refreshAll() {
        courseComboBox.removeAllItems();
        List<Course> courses = plannerApp.courseTree.inOrderTraversal();
        for (Course course : courses) {
            courseComboBox.addItem(course.getCode() + " - " + course.getName());
        }
        refreshCourseTable();
    }
    
    private void refreshCourseTable() {
        tableModel.setRowCount(0);
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
            
            Course course = plannerApp.courseTree.search(courseCode);
            if (course == null) {
                sb.append("❌ Course not found: ").append(courseCode);
                outputArea.setText(sb.toString());
                return;
            }
            
            sb.append("Course: ").append(course.getName()).append("\n");
            sb.append("Credits: ").append(course.getCredits()).append("\n\n");
            
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
                            
                            if (!plannerApp.completedCourses.contains(courseCode)) {
                                plannerApp.completedCourses.add(courseCode);
                            }
                            plannerApp.grades.put(courseCode, grade);
                            
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
                            
                            if (!plannerApp.completedCourses.contains(courseCode)) {
                                plannerApp.completedCourses.add(courseCode);
                            }
                            plannerApp.grades.put(courseCode, grade);
                            
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
            new CoursePlannerGUI();
        });
    }
}
