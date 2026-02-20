package com.courseplanner;

import java.util.*;

/**
 * PrerequisiteGraph manages course prerequisite relationships using adjacency lists.
 * This graph implementation supports:
 * - Adding prerequisite relationships
 * - Cycle detection to prevent impossible course sequences
 * - Topological sorting for course ordering
 * - Finding dependent courses
 */
public class PrerequisiteGraph {
    // Adjacency list: course -> list of prerequisites
    private HashMap<String, List<String>> adjList;

    // Reverse adjacency list: prerequisite -> list of courses that require it
    private HashMap<String, List<String>> reverseAdjList;

    /**
     * Constructor initializes empty graph
     */
    public PrerequisiteGraph() {
        this.adjList = new HashMap<>();
        this.reverseAdjList = new HashMap<>();
    }

    /**
     * Add a prerequisite relationship
     * @param course Course that requires the prerequisite
     * @param prerequisite Required course
     */
    public void addPrerequisite(String course, String prerequisite) {
        // Add to forward adjacency list
        adjList.computeIfAbsent(course, k -> new ArrayList<>()).add(prerequisite);

        // Add to reverse adjacency list
        reverseAdjList.computeIfAbsent(prerequisite, k -> new ArrayList<>()).add(course);

        // Ensure both courses exist in both maps
        adjList.putIfAbsent(prerequisite, new ArrayList<>());
        reverseAdjList.putIfAbsent(course, new ArrayList<>());
    }

    /**
     * Get all prerequisites for a course
     * @param course Course code
     * @return List of prerequisite course codes
     */
    public List<String> getPrerequisites(String course) {
        return adjList.getOrDefault(course, new ArrayList<>());
    }

    /**
     * Get all courses that require this course as prerequisite
     * @param prerequisite Course code
     * @return List of dependent course codes
     */
    public List<String> getDependentCourses(String prerequisite) {
        return reverseAdjList.getOrDefault(prerequisite, new ArrayList<>());
    }

    /**
     * Check if course has any prerequisites
     * @param course Course code
     * @return true if course has prerequisites
     */
    public boolean hasPrerequisites(String course) {
        List<String> prereqs = adjList.get(course);
        return prereqs != null && !prereqs.isEmpty();
    }

    /**
     * Detect cycles in prerequisite graph using DFS
     * @return true if cycle exists
     */
    public boolean hasCycle() {
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();

        for (String course : adjList.keySet()) {
            if (!visited.contains(course)) {
                if (hasCycleDFS(course, visited, recursionStack)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * DFS helper for cycle detection
     */
    private boolean hasCycleDFS(String course, Set<String> visited, Set<String> recursionStack) {
        visited.add(course);
        recursionStack.add(course);

        List<String> prerequisites = adjList.getOrDefault(course, new ArrayList<>());
        for (String prereq : prerequisites) {
            if (!visited.contains(prereq)) {
                if (hasCycleDFS(prereq, visited, recursionStack)) {
                    return true;
                }
            } else if (recursionStack.contains(prereq)) {
                // Cycle detected: course -> ... -> prereq -> course
                return true;
            }
        }

        recursionStack.remove(course);
        return false;
    }

    /**
     * Get cycle path if exists
     * @return List showing cycle path
     */
    public List<String> getCyclePath() {
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();
        List<String> path = new ArrayList<>();

        for (String course : adjList.keySet()) {
            if (!visited.contains(course)) {
                if (getCyclePathDFS(course, visited, recursionStack, path)) {
                    return path;
                }
            }
        }
        return new ArrayList<>();
    }

    private boolean getCyclePathDFS(String course, Set<String> visited, Set<String> recursionStack, List<String> path) {
        visited.add(course);
        recursionStack.add(course);
        path.add(course);

        List<String> prerequisites = adjList.getOrDefault(course, new ArrayList<>());
        for (String prereq : prerequisites) {
            if (!visited.contains(prereq)) {
                if (getCyclePathDFS(prereq, visited, recursionStack, path)) {
                    return true;
                }
            } else if (recursionStack.contains(prereq)) {
                path.add(prereq);  // Complete the cycle
                return true;
            }
        }

        recursionStack.remove(course);
        path.remove(path.size() - 1);
        return false;
    }

    /**
     * Perform topological sort using Kahn's algorithm
     * @return Courses in topological order
     */
    public List<String> topologicalSort() {
        List<String> result = new ArrayList<>();

        // Calculate in-degree for each course
        HashMap<String, Integer> inDegree = new HashMap<>();

        // Initialize all courses
        for (String course : adjList.keySet()) {
            inDegree.putIfAbsent(course, 0);
        }

        // Calculate in-degrees
        for (Map.Entry<String, List<String>> entry : adjList.entrySet()) {
            String course = entry.getKey();
            for (String prereq : entry.getValue()) {
                inDegree.put(prereq, inDegree.getOrDefault(prereq, 0) + 1);
            }
        }

        // Queue for courses with 0 in-degree
        Queue<String> queue = new LinkedList<>();
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        // Process queue
        while (!queue.isEmpty()) {
            String course = queue.poll();
            result.add(course);

            // Reduce in-degree of neighbors
            for (String dependent : adjList.getOrDefault(course, new ArrayList<>())) {
                inDegree.put(dependent, inDegree.get(dependent) - 1);
                if (inDegree.get(dependent) == 0) {
                    queue.add(dependent);
                }
            }
        }

        // Check if all courses were processed (no cycles)
        if (result.size() != inDegree.size()) {
            return new ArrayList<>();  // Cycle exists
        }

        return result;
    }

    /**
     * Display graph structure
     */
    public void displayGraph() {
        System.out.println("\nPrerequisite Graph:");
        for (Map.Entry<String, List<String>> entry : adjList.entrySet()) {
            String course = entry.getKey();
            List<String> prereqs = entry.getValue();
            if (!prereqs.isEmpty()) {
                System.out.println(course + " → " + String.join(", ", prereqs));
            }
        }
    }
}