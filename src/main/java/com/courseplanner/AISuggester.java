package com.courseplanner;

import java.util.*;


public class AISuggester {
    private CourseBST courseTree;
    private PrerequisiteGraph graph;
    private List<String> completedCourses;
    private HashMap<String, Double> grades;
    private List<String> interests;

    
    public AISuggester(CourseBST courseTree, PrerequisiteGraph graph) {
        this.courseTree = courseTree;
        this.graph = graph;
        this.completedCourses = new ArrayList<>();
        this.grades = new HashMap<>();
        this.interests = new ArrayList<>();
    }

    
    public void setCompletedCourses(List<String> completedCourses) {
        this.completedCourses = new ArrayList<>(completedCourses);
    }


    public void setGrades(HashMap<String, Double> grades) {
        this.grades = new HashMap<>(grades);
    }

    /**
     * Set student interests
     */
    public void setInterests(List<String> interests) {
        this.interests = new ArrayList<>(interests);
    }

   
    public List<Course> suggestElectives(int count) {
        // Get all courses that can be taken now
        List<Course> availableCourses = getAvailableCourses();

        if (availableCourses.isEmpty()) {
            return new ArrayList<>();
        }

        // Use PriorityQueue to sort by score
        PriorityQueue<ScoredCourse> priorityQueue = new PriorityQueue<>(
                (a, b) -> Double.compare(b.getScore(), a.getScore())
        );

        // Calculate score for each available course
        for (Course course : availableCourses) {
            double score = calculateScore(course);
            priorityQueue.offer(new ScoredCourse(course, score));
        }

        // Get top N suggestions
        List<Course> suggestions = new ArrayList<>();
        for (int i = 0; i < count && !priorityQueue.isEmpty(); i++) {
            suggestions.add(priorityQueue.poll().getCourse());
        }

        return suggestions;
    }

    /**
     * Get all courses that can be taken now
     */
    private List<Course> getAvailableCourses() {
        List<Course> available = new ArrayList<>();
        List<Course> allCourses = courseTree.inOrderTraversal();

        for (Course course : allCourses) {
            if (!course.isCompleted() && canTakeCourse(course.getCode())) {
                available.add(course);
            }
        }
        return available;
    }

    /**
     * Check if course can be taken
     */
    private boolean canTakeCourse(String courseCode) {
        List<String> prerequisites = graph.getPrerequisites(courseCode);
        for (String prereq : prerequisites) {
            if (!completedCourses.contains(prereq)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Calculate AI score for a course (0-100)
     * @param course Course to score
     * @return Score from 0 to 100
     */
    private double calculateScore(Course course) {
        double score = 0.0;

        // 1. Performance in prerequisites (40% weight)
        double prereqScore = calculatePrerequisiteScore(course);
        score += prereqScore * 0.4;

        // 2. Interest matching (30% weight)
        double interestScore = calculateInterestScore(course);
        score += interestScore * 0.3;

        // 3. Course difficulty adjustment (20% weight)
        double difficultyScore = calculateDifficultyScore(course);
        score += difficultyScore * 0.2;

        // 4. Popularity/importance (10% weight)
        double popularityScore = calculatePopularityScore(course);
        score += popularityScore * 0.1;

        // Ensure score is between 0 and 100
        return Math.max(0, Math.min(100, score));
    }

    /**
     * Calculate score based on performance in prerequisites
     */
    private double calculatePrerequisiteScore(Course course) {
        List<String> prerequisites = graph.getPrerequisites(course.getCode());
        if (prerequisites.isEmpty()) {
            return 75.0; // Default score for courses with no prerequisites
        }

        double totalGrade = 0;
        int count = 0;

        for (String prereq : prerequisites) {
            if (grades.containsKey(prereq)) {
                totalGrade += grades.get(prereq);
                count++;
            }
        }

        if (count > 0) {
            return totalGrade / count;
        } else {
            return 50.0; // No grade data available
        }
    }

    /**
     * Calculate score based on interest matching
     */
    private double calculateInterestScore(Course course) {
        if (interests.isEmpty()) {
            return 50.0; // Default if no interests specified
        }

        String courseName = course.getName().toLowerCase();
        String courseCode = course.getCode().toLowerCase();

        for (String interest : interests) {
            String interestLower = interest.toLowerCase();

            // Check if interest appears in course name or code
            if (courseName.contains(interestLower) ||
                    courseCode.contains(interestLower)) {
                return 100.0; // Perfect match
            }

            // Check for common keywords
            if (interestLower.contains("program") &&
                    (courseName.contains("program") || courseCode.startsWith("cs"))) {
                return 90.0;
            }
            if (interestLower.contains("web") &&
                    (courseName.contains("web") || courseName.contains("internet"))) {
                return 90.0;
            }
            if (interestLower.contains("data") &&
                    (courseName.contains("data") || courseName.contains("database"))) {
                return 90.0;
            }
        }

        return 30.0; // No interest match
    }

    /**
     * Calculate score based on course difficulty
     * Lower credits = easier (assumption)
     */
    private double calculateDifficultyScore(Course course) {
        int credits = course.getCredits();

        // Scale: 1-2 credits = easy (100), 5+ credits = hard (50)
        if (credits <= 2) return 100.0;
        if (credits == 3) return 80.0;
        if (credits == 4) return 60.0;
        return 50.0; // 5+ credits
    }

    /**
     * Calculate score based on course popularity/importance
     */
    private double calculatePopularityScore(Course course) {
        // Courses with more dependents are more important
        List<String> dependents = graph.getDependentCourses(course.getCode());
        int dependentCount = dependents.size();

        // Scale: 0 dependents = 50, 5+ dependents = 100
        if (dependentCount >= 5) return 100.0;
        if (dependentCount >= 3) return 80.0;
        if (dependentCount >= 1) return 70.0;
        return 50.0;
    }

    /**
     * Get explanation for why a course is suggested
     * @param course Suggested course
     * @return Explanation string
     */
    public String getSuggestionExplanation(Course course) {
        List<String> reasons = new ArrayList<>();

        // Check prerequisite performance
        List<String> prerequisites = graph.getPrerequisites(course.getCode());
        double avgGrade = calculatePrerequisiteScore(course);

        if (avgGrade >= 80) {
            reasons.add("Excellent performance in prerequisites (" + String.format("%.1f", avgGrade) + "%)");
        } else if (avgGrade >= 60) {
            reasons.add("Good performance in prerequisites (" + String.format("%.1f", avgGrade) + "%)");
        }

        // Check interest match
        double interestScore = calculateInterestScore(course);
        if (interestScore >= 90) {
            reasons.add("Strongly matches your interests");
        } else if (interestScore >= 70) {
            reasons.add("Related to your interests");
        }

        // Check difficulty
        double difficultyScore = calculateDifficultyScore(course);
        if (difficultyScore >= 90) {
            reasons.add("Manageable difficulty level");
        }

        // Check popularity
        double popularityScore = calculatePopularityScore(course);
        if (popularityScore >= 80) {
            reasons.add("Important foundational course");
        }

        if (reasons.isEmpty()) {
            reasons.add("Good fit for your academic progression");
        }

        return String.join(" • ", reasons);
    }

    /**
     * Inner class for storing courses with scores
     */
    private class ScoredCourse {
        private Course course;
        private double score;

        public ScoredCourse(Course course, double score) {
            this.course = course;
            this.score = score;
        }

        public Course getCourse() {
            return course;
        }

        public double getScore() {
            return score;
        }
    }

}



